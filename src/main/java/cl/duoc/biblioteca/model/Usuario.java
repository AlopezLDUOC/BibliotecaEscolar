package cl.duoc.biblioteca.model;

/** Usuario con acceso al sistema. El rol define qué módulos puede usar. */
public class Usuario extends Persona {
    public static final String ROL_BIBLIOTECARIO = "bibliotecario";
    public static final String ROL_ESTUDIANTE = "estudiante";

    private String contrasena;
    private String rol;

    public Usuario(int id, String nombre, String rut, String correo, String contrasena, String rol) {
        super(id, nombre, rut, correo);
        this.contrasena = contrasena;
        this.rol = rol;
    }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean esBibliotecario() {
        return ROL_BIBLIOTECARIO.equalsIgnoreCase(rol);
    }

    @Override
    public String getTipo() {
        return "Usuario (" + rol + ")";
    }
}
