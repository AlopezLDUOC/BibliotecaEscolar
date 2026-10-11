package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.model.Categoria;
import cl.duoc.biblioteca.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO implements GenericDAO<Categoria> {

    private Connection con() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public void insertar(Categoria c) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("INSERT INTO categorias (nombre) VALUES (?)")) {
            ps.setString(1, c.getNombre());
            ps.executeUpdate();
        }
    }

    @Override
    public void actualizar(Categoria c) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("UPDATE categorias SET nombre = ? WHERE id = ?")) {
            ps.setString(1, c.getNombre());
            ps.setInt(2, c.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("DELETE FROM categorias WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Categoria buscarPorId(int id) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("SELECT id, nombre FROM categorias WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Categoria(rs.getInt("id"), rs.getString("nombre")) : null;
            }
        }
    }

    @Override
    public List<Categoria> listar() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement("SELECT id, nombre FROM categorias ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Categoria(rs.getInt("id"), rs.getString("nombre")));
            }
        }
        return lista;
    }
}
