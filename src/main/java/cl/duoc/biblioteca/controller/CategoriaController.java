package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.CategoriaDAO;
import cl.duoc.biblioteca.model.Categoria;
import cl.duoc.biblioteca.util.AppException;
import cl.duoc.biblioteca.util.Validador;

import java.sql.SQLException;
import java.util.List;

public class CategoriaController {
    private final CategoriaDAO dao = new CategoriaDAO();

    public List<Categoria> listar() throws AppException {
        try {
            return dao.listar();
        } catch (SQLException e) {
            throw Errores.desde(e, "listar las categorías");
        }
    }

    /** Inserta si id == 0; actualiza en caso contrario. */
    public void guardar(Categoria c) throws AppException {
        if (Validador.vacio(c.getNombre())) {
            throw new AppException("El nombre de la categoría es obligatorio.");
        }
        c.setNombre(c.getNombre().trim());
        try {
            if (c.getId() == 0) {
                dao.insertar(c);
            } else {
                dao.actualizar(c);
            }
        } catch (SQLException e) {
            throw Errores.desde(e, "guardar la categoría");
        }
    }

    public void eliminar(int id) throws AppException {
        try {
            dao.eliminar(id);
        } catch (SQLException e) {
            throw Errores.desde(e, "eliminar la categoría");
        }
    }
}
