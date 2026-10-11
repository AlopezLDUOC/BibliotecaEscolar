package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.LoginController;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Usuario;
import cl.duoc.biblioteca.util.AppException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import java.awt.Component;

/**
 * Ventana principal. Las pestañas y el menú "Módulos" dependen del rol:
 * si es bibliotecario puede ver todos los módulos.
 */
public class MainFrame extends JFrame {

    private final JTabbedPane pestanas = new JTabbedPane();
    private final JMenu menuModulos = new JMenu("Módulos");

    public MainFrame(Usuario usuario, LoginController loginController) {
        super("Biblioteca Escolar - " + usuario.getNombre() + " (" + usuario.getRol() + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        if (usuario.esBibliotecario()) {
            agregar("Libros", new LibroPanel(false));
            agregar("Categorías", new CategoriaPanel());
            agregar("Estudiantes", new EstudiantePanel());
            agregar("Préstamos", new PrestamoPanel(null));
            agregar("Reportes", new ReportePanel());
        } else {
            Estudiante ficha = null;
            try {
                ficha = loginController.estudianteDe(usuario);
            } catch (AppException e) {
                Ui.error(this, e.getMessage());
            }
            agregar("Catálogo", new LibroPanel(true));
            agregar("Préstamos", new PrestamoPanel(ficha));
        }

        // Al cambiar de pestaña se recargan los datos de ese módulo
        pestanas.addChangeListener(e -> refrescarSeleccionada());

        JMenuBar barra = new JMenuBar();
        JMenu menuArchivo = new JMenu("Archivo");
        JMenuItem cerrarSesion = new JMenuItem("Cerrar sesión");
        cerrarSesion.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        JMenuItem salir = new JMenuItem("Salir");
        salir.addActionListener(e -> System.exit(0));
        menuArchivo.add(cerrarSesion);
        menuArchivo.add(salir);
        barra.add(menuArchivo);
        barra.add(menuModulos);
        setJMenuBar(barra);

        setContentPane(pestanas);
        setSize(1050, 680);
        setLocationRelativeTo(null);
        refrescarSeleccionada();
    }

    private void agregar(String titulo, Component panel) {
        pestanas.addTab(titulo, panel);
        JMenuItem item = new JMenuItem(titulo);
        item.addActionListener(e -> pestanas.setSelectedComponent(panel));
        menuModulos.add(item);
    }

    private void refrescarSeleccionada() {
        Component actual = pestanas.getSelectedComponent();
        if (actual instanceof Refrescable r) {
            r.refrescar();
        }
    }
}
