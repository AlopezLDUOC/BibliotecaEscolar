package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDAO implements GenericDAO<Estudiante> {

    private static final String SELECT = "SELECT id, nombre, rut, curso, correo FROM estudiantes ";

    private Connection con() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Estudiante mapear(ResultSet rs) throws SQLException {
        return new Estudiante(rs.getInt("id"), rs.getString("nombre"), rs.getString("rut"),
                rs.getString("curso"), rs.getString("correo"));
    }

    @Override
    public void insertar(Estudiante e) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement(
                "INSERT INTO estudiantes (nombre, rut, curso, correo) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getRut());
            ps.setString(3, e.getCurso());
            ps.setString(4, e.getCorreo());
            ps.executeUpdate();
        }
    }

    @Override
    public void actualizar(Estudiante e) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement(
                "UPDATE estudiantes SET nombre = ?, rut = ?, curso = ?, correo = ? WHERE id = ?")) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getRut());
            ps.setString(3, e.getCurso());
            ps.setString(4, e.getCorreo());
            ps.setInt(5, e.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("DELETE FROM estudiantes WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Estudiante buscarPorId(int id) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement(SELECT + "WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Estudiante buscarPorRut(String rut) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement(SELECT + "WHERE rut = ?")) {
            ps.setString(1, rut);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Estudiante> listar() throws SQLException {
        List<Estudiante> lista = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement(SELECT + "ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }
}
