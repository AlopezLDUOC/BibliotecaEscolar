package cl.duoc.biblioteca.model;

import java.time.LocalDate;

/**
 * Un préstamo. Con las columnas del enunciado, fecha_devolucion se usa como
 * FECHA DE VENCIMIENTO (fecha_prestamo + 7 días) y "devuelto" indica si ya se devolvió el libro.
 */
public class Prestamo {
    private int id;
    private int idEstudiante;
    private int idLibro;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;
    private boolean devuelto;

    public Prestamo(int id, int idEstudiante, int idLibro, LocalDate fechaPrestamo,
                    LocalDate fechaDevolucion, boolean devuelto) {
        this.id = id;
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.devuelto = devuelto;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdEstudiante() { return idEstudiante; }
    public void setIdEstudiante(int idEstudiante) { this.idEstudiante = idEstudiante; }
    public int getIdLibro() { return idLibro; }
    public void setIdLibro(int idLibro) { this.idLibro = idLibro; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDate fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }
    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDate fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }
    public boolean isDevuelto() { return devuelto; }
    public void setDevuelto(boolean devuelto) { this.devuelto = devuelto; }

    /** Un préstamo está atrasado si no se devolvió y ya pasó la fecha de vencimiento. */
    public boolean estaAtrasado() {
        return !devuelto && LocalDate.now().isAfter(fechaDevolucion);
    }
}
