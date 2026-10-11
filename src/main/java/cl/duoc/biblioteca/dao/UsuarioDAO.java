package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.model.Usuario;
import cl.duoc.biblioteca.util.DatabaseConnection;
import cl.duoc.biblioteca.util.Esquema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO implements GenericDAO<Usuario> {

    private static final String SELECT = "SELECT id, nombre, rut, correo, " + Esquema.COL_CONTRASENA
            + " AS contrasena, rol FROM usuarios ";

    private Connection con() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(rs.getInt("id"), rs.getString("nombre"), rs.getString("rut"),
                rs.getString("correo"), rs.getString("contrasena"), rs.getString("rol"));
    }

    /** Busca un usuario por RUT o correo y contraseña. Devuelve null si las credenciales no existen. */
    public Usuario autenticar(String identificador, String contrasena) throws SQLException {
        String sql = SELECT + "WHERE (rut = ? OR correo = ?) AND " + Esquema.COL_CONTRASENA + " = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, identificador);
            ps.setString(2, identificador);
            ps.setString(3, contrasena);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Usuario buscarPorRut(String rut) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement(SELECT + "WHERE rut = ?")) {
            ps.setString(1, rut);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public void insertar(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, rut, correo, " + Esquema.COL_CONTRASENA + ", rol) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getRut());
            ps.setString(3, u.getCorreo());
            ps.setString(4, u.getContrasena());
            ps.setString(5, u.getRol());
            ps.executeUpdate();
        }
    }

    @Override
    public void actualizar(Usuario u) throws SQLException {
        String sql = "UPDATE usuarios SET nombre = ?, rut = ?, correo = ?, " + Esquema.COL_CONTRASENA
                + " = ?, rol = ? WHERE id = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getRut());
            ps.setString(3, u.getCorreo());
            ps.setString(4, u.getContrasena());
            ps.setString(5, u.getRol());
            ps.setInt(6, u.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Usuario buscarPorId(int id) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement(SELECT + "WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Usuario> listar() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement(SELECT + "ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }
}
