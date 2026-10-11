package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.dao.UsuarioDAO;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Usuario;
import cl.duoc.biblioteca.util.AppException;
import cl.duoc.biblioteca.util.Validador;

import java.sql.SQLException;
import java.util.List;

public class EstudianteController {
    private final EstudianteDAO dao = new EstudianteDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public List<Estudiante> listar() throws AppException {
        try {
            return dao.listar();
        } catch (SQLException e) {
            throw Errores.desde(e, "listar los estudiantes");
        }
    }

    /**
     * Inserta si id == 0; actualiza en caso contrario.
     * Si se entrega una contraseña al registrar, también se crea el usuario de acceso (rol ESTUDIANTE)
     * con el mismo RUT, para que el estudiante pueda iniciar sesión.
     */
    public void guardar(Estudiante e, String contrasenaUsuario) throws AppException {
        if (Validador.vacio(e.getNombre()) || Validador.vacio(e.getCurso())) {
            throw new AppException("Nombre y curso son obligatorios.");
        }
        if (!Validador.rutValido(e.getRut())) {
            throw new AppException("El RUT no es válido (ejemplo: 12.345.678-5).");
        }
        if (!Validador.correoValido(e.getCorreo())) {
            throw new AppException("El correo no es válido.");
        }
        e.setNombre(e.getNombre().trim());
        e.setRut(e.getRut().trim());
        e.setCorreo(e.getCorreo().trim());
        try {
            if (e.getId() == 0) {
                boolean crearUsuario = !Validador.vacio(contrasenaUsuario);
                if (crearUsuario && usuarioDAO.buscarPorRut(e.getRut()) != null) {
                    throw new AppException("Ya existe un usuario con ese RUT.");
                }
                dao.insertar(e);
                if (crearUsuario) {
                    usuarioDAO.insertar(new Usuario(0, e.getNombre(), e.getRut(), e.getCorreo(),
                            contrasenaUsuario, Usuario.ROL_ESTUDIANTE));
                }
            } else {
                dao.actualizar(e);
            }
        } catch (SQLException ex) {
            throw Errores.desde(ex, "guardar el estudiante");
        }
    }

    public void eliminar(int id) throws AppException {
        try {
            dao.eliminar(id);
        } catch (SQLException e) {
            throw Errores.desde(e, "eliminar el estudiante");
        }
    }
}
