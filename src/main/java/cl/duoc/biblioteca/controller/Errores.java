package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.util.AppException;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

final class Errores {

    private Errores() { }

    static AppException desde(SQLException e, String accion) {
        if (e instanceof SQLIntegrityConstraintViolationException) {
            String msg = String.valueOf(e.getMessage());
            if (msg.contains("Duplicate")) {
                return new AppException("Ya existe un registro con esos datos (RUT, correo, ISBN o nombre repetido).", e);
            }
            return new AppException("No se puede " + accion + " porque tiene registros asociados "
                    + "(por ejemplo, préstamos o libros).", e);
        }
        return new AppException("Error de base de datos al " + accion + ": " + e.getMessage(), e);
    }
}
