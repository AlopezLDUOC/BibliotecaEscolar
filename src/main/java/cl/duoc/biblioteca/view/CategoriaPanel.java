package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.CategoriaController;
import cl.duoc.biblioteca.model.Categoria;
import cl.duoc.biblioteca.util.AppException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

/** CRUD de categorías. */
public class CategoriaPanel extends JPanel implements Refrescable {

    private final CategoriaController controller = new CategoriaController();
    private final DefaultTableModel modelo = Ui.modelo("ID", "Nombre");
    private final JTable tabla = Ui.tabla(modelo);
    private final JTextField txtNombre = new JTextField(25);
    private List<Categoria> categorias = new ArrayList<>();
    private int idSeleccionado = 0;

    public CategoriaPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Ui.anchos(tabla, 40, 300);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(Ui.etiqueta("Nombre:"));
        form.add(txtNombre);
        JButton btnGuardar = new JButton("Guardar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnNuevo = new JButton("Nuevo / Limpiar");
        form.add(btnGuardar);
        form.add(btnEliminar);
        form.add(btnNuevo);

        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(form, BorderLayout.SOUTH);

        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnNuevo.addActionListener(e -> limpiar());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (!e.getValueIsAdjusting() && fila >= 0) {
                Categoria c = categorias.get(fila);
                idSeleccionado = c.getId();
                txtNombre.setText(c.getNombre());
            }
        });
    }

    @Override
    public void refrescar() {
        try {
            categorias = controller.listar();
            modelo.setRowCount(0);
            for (Categoria c : categorias) {
                modelo.addRow(new Object[]{c.getId(), c.getNombre()});
            }
        } catch (AppException e) {
            Ui.error(this, e.getMessage());
        }
    }

    private void guardar() {
        try {
            controller.guardar(new Categoria(idSeleccionado, txtNombre.getText()));
            limpiar();
            refrescar();
        } catch (AppException e) {
            Ui.error(this, e.getMessage());
        }
    }

    private void eliminar() {
        if (idSeleccionado == 0) {
            Ui.info(this, "Selecciona una categoría de la tabla.");
            return;
        }
        if (!Ui.confirmar(this, "¿Eliminar la categoría seleccionada?")) {
            return;
        }
        try {
            controller.eliminar(idSeleccionado);
            limpiar();
            refrescar();
        } catch (AppException e) {
            Ui.error(this, e.getMessage());
        }
    }

    private void limpiar() {
        idSeleccionado = 0;
        txtNombre.setText("");
        tabla.clearSelection();
    }
}
