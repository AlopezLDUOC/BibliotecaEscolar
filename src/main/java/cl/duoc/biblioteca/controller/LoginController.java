package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.dao.UsuarioDAO;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Usuario;
import cl.duoc.biblioteca.util.AppException;
import cl.duoc.biblioteca.util.Validador;

import java.sql.SQLException;

public class LoginController {
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

    /** Valida las credenciales contra la tabla usuarios. Lanza AppException si no son correctas. */
    public Usuario autenticar(String identificador, String contrasena) throws AppException {
        if (Validador.vacio(identificador) || Validador.vacio(contrasena)) {
            throw new AppException("Ingresa tu RUT o correo y tu contraseña.");
        }
        try {
            Usuario u = usuarioDAO.autenticar(identificador.trim(), contrasena);
            if (u == null) {
                throw new AppException("Credenciales incorrectas.");
            }
            return u;
        } catch (SQLException e) {
            throw new AppException("No se pudo conectar con la base de datos: " + e.getMessage(), e);
        }
    }

    /** Para el rol estudiante: busca su ficha de estudiante a través del RUT (null si no existe). */
    public Estudiante estudianteDe(Usuario usuario) throws AppException {
        try {
            return estudianteDAO.buscarPorRut(usuario.getRut());
        } catch (SQLException e) {
            throw Errores.desde(e, "buscar al estudiante");
        }
    }
}
