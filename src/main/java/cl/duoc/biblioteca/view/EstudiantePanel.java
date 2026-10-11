package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.EstudianteController;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.util.AppException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/** CRUD de estudiantes, con opción de crear su usuario de acceso al registrarlos. */
public class EstudiantePanel extends JPanel implements Refrescable {

    private final EstudianteController controller = new EstudianteController();
    private final DefaultTableModel modelo = Ui.modelo("ID", "Nombre", "RUT", "Curso", "Correo");
    private final JTable tabla = Ui.tabla(modelo);

    private final JTextField txtNombre = new JTextField();
    private final JTextField txtRut = new JTextField();
    private final JTextField txtCurso = new JTextField();
    private final JTextField txtCorreo = new JTextField();
    private final JCheckBox chkUsuario = new JCheckBox("Crear usuario de acceso (rol estudiante)");
    private final JPasswordField txtClave = new JPasswordField();

    private List<Estudiante> estudiantes = new ArrayList<>();
    private int idSeleccionado = 0;

    public EstudiantePanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Ui.anchos(tabla, 40, 200, 110, 110, 200);

        JPanel campos = new JPanel(new GridLayout(0, 4, 8, 6));
        campos.add(Ui.etiqueta("Nombre:"));
        campos.add(txtNombre);
        campos.add(Ui.etiqueta("RUT:"));
        campos.add(txtRut);
        campos.add(Ui.etiqueta("Curso:"));
        campos.add(txtCurso);
        campos.add(Ui.etiqueta("Correo:"));
        campos.add(txtCorreo);
        campos.add(chkUsuario);
        campos.add(Ui.etiqueta("   Contraseña del usuario:"));
        campos.add(txtClave);
        campos.add(Ui.etiqueta(""));
        txtClave.setEnabled(false);
        chkUsuario.addActionListener(e -> txtClave.setEnabled(chkUsuario.isSelected()));

        JButton btnGuardar = new JButton("Guardar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnNuevo = new JButton("Nuevo / Limpiar");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnGuardar);
        botones.add(btnEliminar);
        botones.add(btnNuevo);

        JPanel form = new JPanel(new BorderLayout(0, 6));
        form.setBorder(BorderFactory.createTitledBorder("Datos del estudiante (selecciona una fila para editar)"));
        form.add(campos, BorderLayout.CENTER);
        form.add(botones, BorderLayout.SOUTH);

        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(form, BorderLayout.SOUTH);

        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        btnNuevo.addActionListener(e -> limpiar());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (!e.getValueIsAdjusting() && fila >= 0) {
                Estudiante est = estudiantes.get(fila);
                idSeleccionado = est.getId();
                txtNombre.setText(est.getNombre());
                txtRut.setText(est.getRut());
                txtCurso.setText(est.getCurso());
                txtCorreo.setText(est.getCorreo());
                chkUsuario.setSelected(false);
                chkUsuario.setEnabled(false); // el usuario solo se crea al registrar
                txtClave.setEnabled(false);
            }
        });
    }

    @Override
    public void refrescar() {
        try {
            estudiantes = controller.listar();
            modelo.setRowCount(0);
            for (Estudiante e : estudiantes) {
                modelo.addRow(new Object[]{e.getId(), e.getNombre(), e.getRut(), e.getCurso(), e.getCorreo()});
            }
        } catch (AppException e) {
            Ui.error(this, e.getMessage());
        }
    }

    private void guardar() {
        Estudiante est = new Estudiante(idSeleccionado, txtNombre.getText(), txtRut.getText(),
                txtCurso.getText(), txtCorreo.getText());
        String clave = chkUsuario.isSelected() ? new String(txtClave.getPassword()) : null;
        if (chkUsuario.isSelected() && (clave == null || clave.isBlank())) {
            Ui.error(this, "Ingresa una contraseña para el usuario de acceso.");
            return;
        }
        try {
            controller.guardar(est, clave);
            limpiar();
            refrescar();
        } catch (AppException e) {
            Ui.error(this, e.getMessage());
        }
    }

    private void eliminar() {
        if (idSeleccionado == 0) {
            Ui.info(this, "Selecciona un estudiante de la tabla.");
            return;
        }
        if (!Ui.confirmar(this, "¿Eliminar al estudiante seleccionado?")) {
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
        txtRut.setText("");
        txtCurso.setText("");
        txtCorreo.setText("");
        txtClave.setText("");
        chkUsuario.setSelected(false);
        chkUsuario.setEnabled(true);
        txtClave.setEnabled(false);
        tabla.clearSelection();
    }
}
