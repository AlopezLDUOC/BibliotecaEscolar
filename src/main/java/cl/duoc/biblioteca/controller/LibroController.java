package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.LibroDAO;
import cl.duoc.biblioteca.model.Libro;
import cl.duoc.biblioteca.util.AppException;
import cl.duoc.biblioteca.util.Validador;

import java.sql.SQLException;
import java.util.List;

public class LibroController {
    private final LibroDAO dao = new LibroDAO();

    public List<Libro> listar() throws AppException {
        try {
            return dao.listar();
        } catch (SQLException e) {
            throw Errores.desde(e, "listar los libros");
        }
    }

    public List<Libro> buscar(String texto) throws AppException {
        if (Validador.vacio(texto)) {
            return listar();
        }
        try {
            return dao.buscar(texto.trim());
        } catch (SQLException e) {
            throw Errores.desde(e, "buscar libros");
        }
    }

    /** Inserta si id == 0; actualiza en caso contrario. */
    public void guardar(Libro l) throws AppException {
        if (Validador.vacio(l.getTitulo()) || Validador.vacio(l.getAutor())
                || Validador.vacio(l.getIsbn()) || Validador.vacio(l.getEditorial())) {
            throw new AppException("Título, autor, ISBN y editorial son obligatorios.");
        }
        if (l.getStock() < 0) {
            throw new AppException("El stock no puede ser negativo.");
        }
        if (l.getIdCategoria() <= 0) {
            throw new AppException("Selecciona una categoría.");
        }
        try {
            if (l.getId() == 0) {
                dao.insertar(l);
            } else {
                dao.actualizar(l);
            }
        } catch (SQLException e) {
            throw Errores.desde(e, "guardar el libro");
        }
    }

    public void eliminar(int id) throws AppException {
        try {
            dao.eliminar(id);
        } catch (SQLException e) {
            throw Errores.desde(e, "eliminar el libro");
        }
    }
}
