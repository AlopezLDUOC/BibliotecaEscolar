package cl.duoc.biblioteca.dao;

import java.sql.SQLException;
import java.util.List;

/** Contrato CRUD que implementa cada DAO (interfaz + polimorfismo). */
public interface GenericDAO<T> {
    void insertar(T entidad) throws SQLException;
    void actualizar(T entidad) throws SQLException;
    void eliminar(int id) throws SQLException;
    T buscarPorId(int id) throws SQLException;
    List<T> listar() throws SQLException;
}
