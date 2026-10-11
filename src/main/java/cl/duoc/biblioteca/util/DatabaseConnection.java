package cl.duoc.biblioteca.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Patrón SINGLETON: existe una única instancia que administra la conexión a MySQL.
 * Toda la aplicación obtiene la conexión con DatabaseConnection.getInstance().getConnection().
 */
public final class DatabaseConnection {

    private static final String URL_DEFECTO =
            "jdbc:mysql://localhost:3306/biblioteca?useSSL=false&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Santiago&characterEncoding=UTF-8";

    // volatile + doble verificación: la instancia se crea una sola vez, incluso con varios hilos
    private static volatile DatabaseConnection instancia;

    private final String url;
    private final String usuario;
    private final String clave;
    private Connection conexion;

    // Constructor privado: nadie más puede hacer "new DatabaseConnection()"
    private DatabaseConnection() {
        Properties props = new Properties();
        try (InputStream in = DatabaseConnection.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            System.err.println("No se pudo leer db.properties, se usan valores por defecto: " + e.getMessage());
        }
        this.url = props.getProperty("db.url", URL_DEFECTO);
        this.usuario = props.getProperty("db.user", "root");
        this.clave = props.getProperty("db.password", "");
    }

    /** Devuelve la única instancia (la crea la primera vez). */
    public static DatabaseConnection getInstance() {
        if (instancia == null) {
            synchronized (DatabaseConnection.class) {
                if (instancia == null) {
                    instancia = new DatabaseConnection();
                }
            }
        }
        return instancia;
    }

    /** Entrega la conexión; si se cerró o cayó, la restablece con DriverManager.getConnection. */
    public synchronized Connection getConnection() throws SQLException {
        if (conexion == null || conexion.isClosed() || !conexion.isValid(2)) {
            conexion = DriverManager.getConnection(url, usuario, clave);
        }
        return conexion;
    }

    public synchronized void cerrar() {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            } finally {
                conexion = null;
            }
        }
    }
}
