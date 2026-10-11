package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.CategoriaController;
import cl.duoc.biblioteca.controller.LibroController;
import cl.duoc.biblioteca.model.Categoria;
import cl.duoc.biblioteca.model.Libro;
import cl.duoc.biblioteca.util.AppException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestión de libros. Con soloLectura = true (rol estudiante) solo muestra el catálogo y permite buscar.
 */
public class LibroPanel extends JPanel implements Refrescable {

    private final LibroController controller = new LibroController();
    private final CategoriaController categoriaController = new CategoriaController();
    private final boolean soloLectura;

    private final DefaultTableModel modelo = Ui.modelo("ID", "Título", "Autor", "ISBN", "Editorial", "Categoría", "Stock");
    private final JTable tabla = Ui.tabla(modelo);
    private final JTextField txtBuscar = new JTextField(22);

    private final JTextField txtTitulo = new JTextField();
    private final JTextField txtAutor = new JTextField();
    private final JTextField txtIsbn = new JTextField();
    private final JTextField txtEditorial = new JTextField();
    private final JSpinner spStock = new JSpinner(new SpinnerNumberModel(1, 0, 9999, 1));
    private final JComboBox<Categoria> cbCategoria = new JComboBox<>();

    private List<Libro> libros = new ArrayList<>(); // mismo orden que las filas de la tabla
    private int idSeleccionado = 0;

    public LibroPanel(boolean soloLectura) {
        this.soloLectura = soloLectura;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Ui.anchos(tabla, 40, 260, 190, 120, 120, 100, 50);

        // Búsqueda
        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        busqueda.add(Ui.etiqueta("Buscar (título, autor o ISBN):"));
        busqueda.add(txtBuscar);
        JButton btnBuscar = new JButton("Buscar");
        JButton btnTodos = new JButton("Mostrar todos");
        busqueda.add(btnBuscar);
        busqueda.add(btnTodos);
        add(busqueda, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> buscar());
        txtBuscar.addActionListener(e -> buscar());
        btnTodos.addActionListener(e -> {
            txtBuscar.setText("");
            buscar();
        });

        if (!soloLectura) {
            add(crearFormulario(), BorderLayout.SOUTH);
            tabla.getSelectionModel().addListSelectionListener(e -> {
                int fila = tabla.getSelectedRow();
                if (!e.getValueIsAdjusting() && fila >= 0) {
                    llenarFormulario(libros.get(fila));
                }
            });
        }
    }

    private JPanel crearFormulario() {
        JPanel campos = new JPanel(new GridLayout(0, 4, 8, 6));
        campos.add(Ui.etiqueta("Título:"));
        campos.add(txtTitulo);
        campos.add(Ui.etiqueta("Autor:"));
        campos.add(txtAutor);
        campos.add(Ui.etiqueta("ISBN:"));
        campos.add(txtIsbn);
        campos.add(Ui.etiqueta("Editorial:"));
        campos.add(txtEditorial);
        campos.add(Ui.etiqueta("Categoría:"));
        campos.add(cbCategoria);
        campos.add(Ui.etiqueta("Stock:"));
        campos.add(spStock);

        JButton btnGuardar = new JButton("Guardar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnNuevo = new JButton("Nuevo / Limpiar");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnGuardar);
        botones.add(btnEliminar);
        botones.add(btnNuevo);

        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnNuevo.addActionListener(e -> limpiar());

        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Datos del libro (selecciona una fila para editar)"));
        panel.add(campos, BorderLayout.CENTER);
        panel.add(botones, BorderLayout.SOUTH);
        return panel;
    }

    @Override
    public void refrescar() {
        if (!soloLectura) {
            try {
                cbCategoria.removeAllItems();
                for (Categoria c : categoriaController.listar()) {
                    cbCategoria.addItem(c);
                }
            } catch (AppException e) {
                Ui.error(this, e.getMessage());
            }
        }
        buscar();
    }

    private void buscar() {
        try {
            mostrar(controller.buscar(txtBuscar.getText()));
        } catch (AppException e) {
            Ui.error(this, e.getMessage());
        }
    }

    private void mostrar(List<Libro> lista) {
        libros = lista;
        modelo.setRowCount(0);
        for (Libro l : lista) {
            modelo.addRow(new Object[]{l.getId(), l.getTitulo(), l.getAutor(), l.getIsbn(),
                    l.getEditorial(), l.getCategoriaNombre(), l.getStock()});
        }
    }

    private void llenarFormulario(Libro l) {
        idSeleccionado = l.getId();
        txtTitulo.setText(l.getTitulo());
        txtAutor.setText(l.getAutor());
        txtIsbn.setText(l.getIsbn());
        txtEditorial.setText(l.getEditorial());
        spStock.setValue(l.getStock());
        for (int i = 0; i < cbCategoria.getItemCount(); i++) {
            if (cbCategoria.getItemAt(i).getId() == l.getIdCategoria()) {
                cbCategoria.setSelectedIndex(i);
                break;
            }
        }
    }

    private void guardar() {
        Categoria cat = (Categoria) cbCategoria.getSelectedItem();
        Libro l = new Libro(idSeleccionado, txtTitulo.getText(), txtAutor.getText(), txtIsbn.getText(),
                txtEditorial.getText(), (Integer) spStock.getValue(), cat != null ? cat.getId() : 0);
        try {
            controller.guardar(l);
            limpiar();
            buscar();
        } catch (AppException e) {
            Ui.error(this, e.getMessage());
        }
    }

    private void eliminar() {
        if (idSeleccionado == 0) {
            Ui.info(this, "Selecciona un libro de la tabla.");
            return;
        }
        if (!Ui.confirmar(this, "¿Eliminar el libro seleccionado?")) {
            return;
        }
        try {
            controller.eliminar(idSeleccionado);
            limpiar();
            buscar();
        } catch (AppException e) {
            Ui.error(this, e.getMessage());
        }
    }

    private void limpiar() {
        idSeleccionado = 0;
        txtTitulo.setText("");
        txtAutor.setText("");
        txtIsbn.setText("");
        txtEditorial.setText("");
        spStock.setValue(1);
        tabla.clearSelection();
    }
}
