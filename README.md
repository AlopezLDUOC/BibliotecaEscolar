# Sistema de Gestión de Biblioteca Escolar

Aplicación de escritorio en Java para controlar el registro, préstamo y devolución de libros de una biblioteca escolar.

---

### Cómo ejecutarlo

1. Ejecutar en MySQL, en este orden, `sql/Script_crea_tablas_biblioteca.sql` y `sql/Script_poblado_tablas_biblioteca.sql` (crean la base `biblioteca` y cargan datos de ejemplo).
2. Revisar `src/main/resources/db.properties` y ajustar `db.user` y `db.password` a tu servidor MySQL.
3. Abrir la carpeta como proyecto Maven en IntelliJ IDEA y ejecutar `cl.duoc.biblioteca.Main`.
4. Opcional: `mvn package` genera `target/biblioteca.jar`, ejecutable con `java -jar target/biblioteca.jar`.

Usuarios de prueba (contraseña `clave123`): bibliotecario `12345678-9`, estudiante `98765432-1`.

---

### Arquitectura

El proyecto sigue el patrón **MVC** con la siguiente separación de paquetes:

- `model`: `Persona` (abstracta) de la que heredan `Usuario` y `Estudiante`, además de `Libro`, `Categoria` y `Prestamo`.
- `view`: ventanas y paneles Swing (`LoginFrame`, `MainFrame` y un panel por módulo).
- `controller`: validan los datos y hacen de intermediarios entre `view` y `dao`.
- `dao`: cada `*DAO` implementa `GenericDAO<T>` y es el único que habla con la base de datos.
- `util`: `DatabaseConnection` (Singleton), `Validador` y `AppException`.

---

### Patrones y buenas prácticas

- **Singleton:** `DatabaseConnection.getInstance()` entrega la única conexión JDBC de toda la aplicación.
- **DAO:** el SQL vive solo en las clases `*DAO`, siempre con `PreparedStatement`.
- **Herencia y polimorfismo:** `Usuario` y `Estudiante` extienden `Persona` y redefinen `getTipo()`.
- **Interfaz:** `GenericDAO<T>` define el CRUD común y `Refrescable` recarga los paneles al abrirlos.

---

### Concurrencia y consistencia

- `PrestamoController.registrarPrestamoAsync` y `devolverAsync` ejecutan la operación en un `Thread` aparte, de modo que la interfaz no se congela.
- `registrarPrestamo` y `devolver` son `static synchronized`: solo un hilo a la vez modifica el stock.
- Cada operación va en una transacción (`commit` / `rollback`) y el descuento usa `UPDATE ... WHERE stock > 0`, por lo que el stock nunca queda negativo.
- `PruebaConcurrencia` lanza 10 hilos sobre el mismo libro para comprobarlo.

---

### Reglas de negocio

- El plazo de préstamo es de 7 días: `fecha_devolucion` guarda la fecha de vencimiento y `devuelto` indica si el libro ya volvió.
- Al devolver un libro fuera de plazo se informa el atraso y se deja constancia en `logs/atrasos.log`.
- El rol estudiante solo puede consultar el catálogo, pedir libros y devolver los suyos.

---

### Autor 

Alessandro Lopez
