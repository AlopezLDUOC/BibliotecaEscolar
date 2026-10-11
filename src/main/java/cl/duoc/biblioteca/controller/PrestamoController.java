package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.LibroDAO;
import cl.duoc.biblioteca.dao.PrestamoDAO;
import cl.duoc.biblioteca.model.Prestamo;
import cl.duoc.biblioteca.util.AppException;
import cl.duoc.biblioteca.util.DatabaseConnection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Préstamos y devoluciones.
 *
 * CONCURRENCIA:
 *  - registrarPrestamoAsync / devolverAsync lanzan un HILO independiente (Thread) para que la interfaz
 *    no se congele mientras se registra el préstamo, se descuenta el stock y se deja el historial.
 *  - registrarPrestamo / devolver son "static synchronized": solo un hilo a la vez puede modificar el
 *    stock (el candado es la clase, así que protege incluso si hay varias instancias del controlador).
 */
public class PrestamoController {

    /** Días de plazo del préstamo (fecha de vencimiento = hoy + 7). */
    public static final int DIAS_PRESTAMO = 7;

    public static final long RETARDO_DEMO_MS = 1000;

    private static final Path LOG_ATRASOS = Path.of("logs", "atrasos.log");

    private final PrestamoDAO prestamoDAO = new PrestamoDAO();

    // ---------------------------- Versión con hilo (la que usa la interfaz) ----------------------------

    /** Registra el préstamo en segundo plano. Los callbacks se ejecutan en el hilo secundario. */
    public void registrarPrestamoAsync(int idEstudiante, int idLibro,
                                       Consumer<Prestamo> alExito, Consumer<String> alError) {
        Thread hilo = new Thread(() -> {
            try {
                Prestamo p = registrarPrestamo(idEstudiante, idLibro);
                alExito.accept(p);
            } catch (AppException e) {
                alError.accept(e.getMessage());
            }
        }, "hilo-prestamo");
        hilo.start();
    }

    /** Registra la devolución en segundo plano. Entrega los días de atraso (0 si llegó a tiempo). */
    public void devolverAsync(int idPrestamo, IntConsumer alExito, Consumer<String> alError) {
        Thread hilo = new Thread(() -> {
            try {
                alExito.accept(devolver(idPrestamo));
            } catch (AppException e) {
                alError.accept(e.getMessage());
            }
        }, "hilo-devolucion");
        hilo.start();
    }

    // ---------------------------- Operaciones críticas (sincronizadas) ----------------------------

    /** Registra un préstamo: valida, descuenta stock e inserta el préstamo*/
    public static synchronized Prestamo registrarPrestamo(int idEstudiante, int idLibro) throws AppException {
        LibroDAO libroDAO = new LibroDAO();
        PrestamoDAO prestamoDAO = new PrestamoDAO();
        Connection con = null;
        boolean autoCommitOriginal = true;
        try {
            con = DatabaseConnection.getInstance().getConnection();
            autoCommitOriginal = con.getAutoCommit();
            con.setAutoCommit(false);

            if (prestamoDAO.existePrestamoActivo(con, idEstudiante, idLibro)) {
                throw new AppException("El estudiante ya tiene este libro en préstamo sin devolver.");
            }
            if (!libroDAO.descontarStock(con, idLibro)) {
                throw new AppException("No hay stock disponible de este libro.");
            }
            pausaDemo();

            LocalDate hoy = LocalDate.now();
            Prestamo p = new Prestamo(0, idEstudiante, idLibro, hoy, hoy.plusDays(DIAS_PRESTAMO), false);
            prestamoDAO.insertar(con, p);
            con.commit();
            return p;
        } catch (AppException e) {
            revertir(con);
            throw e;
        } catch (SQLException e) {
            revertir(con);
            throw Errores.desde(e, "registrar el préstamo");
        } finally {
            restaurarAutoCommit(con, autoCommitOriginal);
        }
    }

    /** Registra la devolución (devuelve el stock). Retorna los días de atraso; si hay atraso deja constancia. */
    public static synchronized int devolver(int idPrestamo) throws AppException {
        LibroDAO libroDAO = new LibroDAO();
        PrestamoDAO prestamoDAO = new PrestamoDAO();
        Connection con = null;
        boolean autoCommitOriginal = true;
        try {
            con = DatabaseConnection.getInstance().getConnection();
            autoCommitOriginal = con.getAutoCommit();
            con.setAutoCommit(false);

            Prestamo p = prestamoDAO.buscarPorId(con, idPrestamo);
            if (p == null) {
                throw new AppException("El préstamo no existe.");
            }
            if (!prestamoDAO.marcarDevuelto(con, idPrestamo)) {
                throw new AppException("Este préstamo ya fue devuelto.");
            }
            libroDAO.aumentarStock(con, p.getIdLibro());
            pausaDemo();
            con.commit();

            long atraso = ChronoUnit.DAYS.between(p.getFechaDevolucion(), LocalDate.now());
            if (atraso > 0) {
                dejarConstancia(p, atraso);
                return (int) atraso;
            }
            return 0;
        } catch (AppException e) {
            revertir(con);
            throw e;
        } catch (SQLException e) {
            revertir(con);
            throw Errores.desde(e, "registrar la devolución");
        } finally {
            restaurarAutoCommit(con, autoCommitOriginal);
        }
    }

    // ----------------------- Consultas -----------------------

    /** Préstamos sin devolver. Si idEstudiante != null, solo los de ese estudiante. */
    public List<Object[]> listarActivos(Integer idEstudiante) throws AppException {
        try {
            return prestamoDAO.listarActivos(idEstudiante);
        } catch (SQLException e) {
            throw Errores.desde(e, "listar los préstamos");
        }
    }

    // ---------------------------- Auxiliares ----------------------------

    private static void pausaDemo() {
        if (RETARDO_DEMO_MS > 0) {
            try {
                Thread.sleep(RETARDO_DEMO_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void revertir(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException ignorada) {
                System.err.println("No se pudo revertir la transacción: " + ignorada.getMessage());
            }
        }
    }

    private static void restaurarAutoCommit(Connection con, boolean valor) {
        if (con != null) {
            try {
                con.setAutoCommit(valor);
            } catch (SQLException ignorada) {
                System.err.println("No se pudo restaurar autoCommit: " + ignorada.getMessage());
            }
        }
    }

    /** Constancia del atraso: una línea en logs/atrasos.log. */
    private static void dejarConstancia(Prestamo p, long dias) {
        String linea = LocalDate.now() + " | préstamo " + p.getId() + " | estudiante " + p.getIdEstudiante()
                + " | libro " + p.getIdLibro() + " | vencía " + p.getFechaDevolucion() + " | atraso " + dias + " día(s)"
                + System.lineSeparator();
        try {
            Files.createDirectories(LOG_ATRASOS.getParent());
            Files.writeString(LOG_ATRASOS, linea, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("No se pudo escribir la constancia de atraso: " + e.getMessage());
        }
    }
}
