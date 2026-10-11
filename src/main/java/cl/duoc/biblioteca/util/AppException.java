package cl.duoc.biblioteca.util;

/**
 * Excepción de negocio: lleva un mensaje pensado para mostrarse al usuario.
 * Los controladores la usan para validaciones y para envolver errores de SQL.
 */
public class AppException extends Exception {
    public AppException(String mensaje) {
        super(mensaje);
    }

    public AppException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
