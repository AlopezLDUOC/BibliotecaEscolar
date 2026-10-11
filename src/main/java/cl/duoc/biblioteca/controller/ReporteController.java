package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.PrestamoDAO;
import cl.duoc.biblioteca.util.AppException;

import java.sql.SQLException;
import java.util.List;

/** Genera los tres reportes pedidos. Cada reporte entrega sus columnas y sus filas. */
public class ReporteController {
    public static final String[] COLS_MAS_PRESTADOS = {"Título", "Autor", "Veces prestado"};
    public static final String[] COLS_HISTORIAL = {"N° préstamo", "Libro", "Fecha préstamo", "Vence", "Estado"};
    public static final String[] COLS_EN_PRESTAMO = {"N° préstamo", "Libro", "Estudiante", "Fecha préstamo", "Vence", "Estado"};

    private final PrestamoDAO dao = new PrestamoDAO();

    public List<Object[]> librosMasPrestados() throws AppException {
        try {
            return dao.librosMasPrestados();
        } catch (SQLException e) {
            throw Errores.desde(e, "generar el reporte");
        }
    }

    public List<Object[]> historialPorEstudiante(int idEstudiante) throws AppException {
        try {
            return dao.historialPorEstudiante(idEstudiante);
        } catch (SQLException e) {
            throw Errores.desde(e, "generar el reporte");
        }
    }

    public List<Object[]> librosEnPrestamo() throws AppException {
        try {
            return dao.listarActivos(null);
        } catch (SQLException e) {
            throw Errores.desde(e, "generar el reporte");
        }
    }
}
