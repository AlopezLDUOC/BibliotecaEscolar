package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.model.Prestamo;
import cl.duoc.biblioteca.util.DatabaseConnection;
import cl.duoc.biblioteca.util.Esquema;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAO implements GenericDAO<Prestamo> {

    private static final String SELECT = "SELECT id, id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto "
            + "FROM prestamos ";
    private static final String TITULO = "l." + Esquema.COL_TITULO;

    private Connection con() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Prestamo mapear(ResultSet rs) throws SQLException {
        return new Prestamo(rs.getInt("id"), rs.getInt("id_estudiante"), rs.getInt("id_libro"),
                rs.getDate("fecha_prestamo").toLocalDate(), rs.getDate("fecha_devolucion").toLocalDate(),
                rs.getBoolean("devuelto"));
    }

    /** Inserta usando una conexión recibida (para participar en una transacción). */
    public void insertar(Connection con, Prestamo p) throws SQLException {
        String sql = "INSERT INTO prestamos (id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getIdEstudiante());
            ps.setInt(2, p.getIdLibro());
            ps.setDate(3, Date.valueOf(p.getFechaPrestamo()));
            ps.setDate(4, Date.valueOf(p.getFechaDevolucion()));
            ps.setBoolean(5, p.isDevuelto());
            ps.executeUpdate();
        }
    }

    @Override
    public void insertar(Prestamo p) throws SQLException {
        insertar(con(), p);
    }

    @Override
    public void actualizar(Prestamo p) throws SQLException {
        String sql = "UPDATE prestamos SET id_estudiante = ?, id_libro = ?, fecha_prestamo = ?, "
                + "fecha_devolucion = ?, devuelto = ? WHERE id = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, p.getIdEstudiante());
            ps.setInt(2, p.getIdLibro());
            ps.setDate(3, Date.valueOf(p.getFechaPrestamo()));
            ps.setDate(4, Date.valueOf(p.getFechaDevolucion()));
            ps.setBoolean(5, p.isDevuelto());
            ps.setInt(6, p.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("DELETE FROM prestamos WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Prestamo buscarPorId(int id) throws SQLException {
        return buscarPorId(con(), id);
    }

    public Prestamo buscarPorId(Connection con, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT + "WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Prestamo> listar() throws SQLException {
        List<Prestamo> lista = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement(SELECT + "ORDER BY id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** Marca el préstamo como devuelto solo si seguía pendiente. Devuelve false si ya estaba devuelto. */
    public boolean marcarDevuelto(Connection con, int idPrestamo) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE prestamos SET devuelto = TRUE WHERE id = ? AND devuelto = FALSE")) {
            ps.setInt(1, idPrestamo);
            return ps.executeUpdate() == 1;
        }
    }

    /** ¿El estudiante ya tiene este libro en préstamo sin devolver? */
    public boolean existePrestamoActivo(Connection con, int idEstudiante, int idLibro) throws SQLException {
        String sql = "SELECT COUNT(*) FROM prestamos WHERE id_estudiante = ? AND id_libro = ? AND devuelto = FALSE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            ps.setInt(2, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    // ---------- Consultas para las tablas de la interfaz y los reportes (filas = Object[]) ----------

    /**
     * Préstamos pendientes de devolución. Columnas: id, libro, estudiante, fecha préstamo, vence, estado.
     * Si idEstudiante no es null, solo los de ese estudiante.
     */
    public List<Object[]> listarActivos(Integer idEstudiante) throws SQLException {
        String sql = "SELECT p.id, " + TITULO + " AS titulo, e.nombre, p.fecha_prestamo, p.fecha_devolucion "
                + "FROM prestamos p JOIN libros l ON l.id = p.id_libro JOIN estudiantes e ON e.id = p.id_estudiante "
                + "WHERE p.devuelto = FALSE " + (idEstudiante != null ? "AND p.id_estudiante = ? " : "")
                + "ORDER BY p.fecha_devolucion";
        List<Object[]> filas = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            if (idEstudiante != null) {
                ps.setInt(1, idEstudiante);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate vence = rs.getDate("fecha_devolucion").toLocalDate();
                    String estado = LocalDate.now().isAfter(vence) ? "ATRASADO" : "En plazo";
                    filas.add(new Object[]{rs.getInt("id"), rs.getString("titulo"), rs.getString("nombre"),
                            rs.getDate("fecha_prestamo").toLocalDate(), vence, estado});
                }
            }
        }
        return filas;
    }

    /** Libros más prestados. Columnas: título, autor, veces prestado. */
    public List<Object[]> librosMasPrestados() throws SQLException {
        String sql = "SELECT " + TITULO + " AS titulo, l.autor, COUNT(p.id) AS total "
                + "FROM prestamos p JOIN libros l ON l.id = p.id_libro "
                + "GROUP BY l.id, " + TITULO + ", l.autor ORDER BY total DESC, titulo";
        List<Object[]> filas = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                filas.add(new Object[]{rs.getString("titulo"), rs.getString("autor"), rs.getInt("total")});
            }
        }
        return filas;
    }

    /** Historial de un estudiante. Columnas: id, libro, fecha préstamo, vence, estado. */
    public List<Object[]> historialPorEstudiante(int idEstudiante) throws SQLException {
        String sql = "SELECT p.id, " + TITULO + " AS titulo, p.fecha_prestamo, p.fecha_devolucion, p.devuelto "
                + "FROM prestamos p JOIN libros l ON l.id = p.id_libro "
                + "WHERE p.id_estudiante = ? ORDER BY p.fecha_prestamo DESC, p.id DESC";
        List<Object[]> filas = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate vence = rs.getDate("fecha_devolucion").toLocalDate();
                    String estado = rs.getBoolean("devuelto") ? "Devuelto"
                            : (LocalDate.now().isAfter(vence) ? "ATRASADO" : "En préstamo");
                    filas.add(new Object[]{rs.getInt("id"), rs.getString("titulo"),
                            rs.getDate("fecha_prestamo").toLocalDate(), vence, estado});
                }
            }
        }
        return filas;
    }
}
