package cl.duoc.biblioteca;

import cl.duoc.biblioteca.controller.PrestamoController;
import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.dao.LibroDAO;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Libro;
import cl.duoc.biblioteca.util.AppException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Prueba manual de concurrencia (ejecútala como aplicación normal desde IntelliJ).
 * Lanza varios hilos que piden el MISMO libro a la vez y comprueba que el stock nunca queda negativo
 * y que solo se aprueban tantos préstamos como stock había.
 *
 * OJO: inserta préstamos reales. Para repetir la prueba, vuelve a ejecutar el script de poblado.
 */
public class PruebaConcurrencia {

    private static final int HILOS = 10;

    public static void main(String[] args) throws Exception {
        LibroDAO libroDAO = new LibroDAO();
        List<Estudiante> estudiantes = new EstudianteDAO().listar();
        Libro libro = libroDAO.listar().stream().filter(Libro::hayStock).findFirst().orElse(null);
        if (libro == null || estudiantes.size() < HILOS) {
            System.out.println("Se necesita al menos un libro con stock y " + HILOS
                    + " estudiantes registrados (el script de poblado incluye ambos).");
            return;
        }
        int stockInicial = libro.getStock();
        System.out.println("Libro: " + libro.getTitulo() + " | stock inicial: " + stockInicial
                + " | hilos simultáneos: " + HILOS);

        AtomicInteger aprobados = new AtomicInteger();
        AtomicInteger rechazados = new AtomicInteger();
        List<Thread> hilos = new ArrayList<>();
        for (int i = 0; i < HILOS; i++) {
            Estudiante est = estudiantes.get(i); // cada hilo usa un estudiante distinto
            Thread t = new Thread(() -> {
                try {
                    PrestamoController.registrarPrestamo(est.getId(), libro.getId());
                    aprobados.incrementAndGet();
                } catch (AppException e) {
                    rechazados.incrementAndGet();
                    System.out.println("  Rechazado (" + est.getNombre() + "): " + e.getMessage());
                }
            }, "prueba-" + i);
            hilos.add(t);
        }
        hilos.forEach(Thread::start);
        for (Thread t : hilos) {
            t.join();
        }

        int stockFinal = libroDAO.buscarPorId(libro.getId()).getStock();
        System.out.println("Aprobados: " + aprobados + " | Rechazados: " + rechazados + " | stock final: " + stockFinal);
        boolean ok = stockFinal >= 0 && aprobados.get() == Math.min(HILOS, stockInicial)
                && stockFinal == stockInicial - aprobados.get();
        System.out.println(ok ? "RESULTADO: OK, el stock se mantuvo consistente."
                : "RESULTADO: FALLO, revisar la sincronización.");
    }
}
