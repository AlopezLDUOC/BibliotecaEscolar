package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.model.Libro;
import cl.duoc.biblioteca.util.DatabaseConnection;
import cl.duoc.biblioteca.util.Esquema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LibroDAO implements GenericDAO<Libro> {

    private static final String SELECT_BASE =
            "SELECT l.id, l." + Esquema.COL_TITULO + " AS titulo, l.autor, l.isbn, l.editorial, l.stock, "
            + "l.id_categoria, c.nombre AS categoria "
            + "FROM libros l LEFT JOIN categorias c ON c.id = l.id_categoria ";

    private Connection con() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Libro mapear(ResultSet rs) throws SQLException {
        Libro l = new Libro(rs.getInt("id"), rs.getString("titulo"), rs.getString("autor"),
                rs.getString("isbn"), rs.getString("editorial"), rs.getInt("stock"), rs.getInt("id_categoria"));
        l.setCategoriaNombre(rs.getString("categoria"));
        return l;
    }

    @Override
    public void insertar(Libro l) throws SQLException {
        String sql = "INSERT INTO libros (" + Esquema.COL_TITULO + ", autor, isbn, editorial, stock, id_categoria) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, l.getTitulo());
            ps.setString(2, l.getAutor());
            ps.setString(3, l.getIsbn());
            ps.setString(4, l.getEditorial());
            ps.setInt(5, l.getStock());
            ps.setInt(6, l.getIdCategoria());
            ps.executeUpdate();
        }
    }

    @Override
    public void actualizar(Libro l) throws SQLException {
        String sql = "UPDATE libros SET " + Esquema.COL_TITULO + " = ?, autor = ?, isbn = ?, editorial = ?, "
                + "stock = ?, id_categoria = ? WHERE id = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, l.getTitulo());
            ps.setString(2, l.getAutor());
            ps.setString(3, l.getIsbn());
            ps.setString(4, l.getEditorial());
            ps.setInt(5, l.getStock());
            ps.setInt(6, l.getIdCategoria());
            ps.setInt(7, l.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void eliminar(int id) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement("DELETE FROM libros WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Libro buscarPorId(int id) throws SQLException {
        try (PreparedStatement ps = con().prepareStatement(SELECT_BASE + "WHERE l.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public List<Libro> listar() throws SQLException {
        List<Libro> lista = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement(SELECT_BASE + "ORDER BY l." + Esquema.COL_TITULO);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /** Busca por título, autor o ISBN. */
    public List<Libro> buscar(String texto) throws SQLException {
        List<Libro> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE l." + Esquema.COL_TITULO + " LIKE ? OR l.autor LIKE ? OR l.isbn LIKE ? "
                + "ORDER BY l." + Esquema.COL_TITULO;
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            String patron = "%" + texto + "%";
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Descuenta 1 de stock SOLO si queda stock disponible (la condición stock > 0 va en el propio UPDATE,
     * así que la base de datos tampoco permite stock negativo). Recibe la conexión para poder
     * usarse dentro de una transacción. Devuelve false si no había stock.
     */
    public boolean descontarStock(Connection con, int idLibro) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE libros SET stock = stock - 1 WHERE id = ? AND stock > 0")) {
            ps.setInt(1, idLibro);
            return ps.executeUpdate() == 1;
        }
    }

    public void aumentarStock(Connection con, int idLibro) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE libros SET stock = stock + 1 WHERE id = ?")) {
            ps.setInt(1, idLibro);
            ps.executeUpdate();
        }
    }
}
