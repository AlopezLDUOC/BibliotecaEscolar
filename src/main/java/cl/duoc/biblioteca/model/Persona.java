package cl.duoc.biblioteca.model;

/**
 * Clase base de las personas del sistema
 * Usuario y Estudiante heredan nombre, rut y correo, y cada uno define su propio getTipo()
 */
public abstract class Persona {
    protected int id;
    protected String nombre;
    protected String rut;
    protected String correo;

    protected Persona(int id, String nombre, String rut, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.rut = rut;
        this.correo = correo;
    }

    /** Cada subclase describe qué tipo de persona es. */
    public abstract String getTipo();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    @Override
    public String toString() {
        return nombre + " (" + rut + ")";
    }
}
