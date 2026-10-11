package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.EstudianteController;
import cl.duoc.biblioteca.controller.LibroController;
import cl.duoc.biblioteca.controller.PrestamoController;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Libro;
import cl.duoc.biblioteca.util.AppException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/**
 * Préstamos y devoluciones.
 * Bibliotecario: elige al estudiante. Estudiante: el panel queda fijo a su propia ficha.
 * El registro y la devolución corren en un hilo aparte; aquí solo se actualiza la interfaz
 *  cuando el hilo termina.
 */
public class PrestamoPanel extends JPanel implements Refrescable {

    private final PrestamoController controller = new PrestamoController();
    private final LibroController libroController = new LibroController();
    private final EstudianteController estudianteController = new EstudianteController();

    private final Estudiante estudianteFijo; // null si es bibliotecario
    private final JComboBox<Estudiante> cbEstudiante = new JComboBox<>();
    private final JComboBox<Libro> cbLibro = new JComboBox<>();
    private final JButton btnPrestar = new JButton("Registrar préstamo");
    private final JButton btnDevolver = new JButton("Registrar devolución del préstamo seleccionado");
    private final JProgressBar progreso = new JProgressBar();
    private final JLabel lblEstado = new JLabel(" ");

    private final DefaultTableModel modelo = Ui.modelo("N° préstamo", "Libro", "Estudiante", "Fecha préstamo", "Vence", "Estado");
    private final JTable tabla = Ui.tabla(modelo);

    public PrestamoPanel(Estudiante estudianteFijo) {
        this.estudianteFijo = estudianteFijo;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Registrar préstamo ---
        JPanel nuevo = new JPanel(new GridLayout(0, 1));
        nuevo.setBorder(BorderFactory.createTitledBorder("Nuevo préstamo (plazo: " + PrestamoController.DIAS_PRESTAMO + " días)"));
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        fila1.add(Ui.etiqueta("Estudiante:"));
        fila1.add(cbEstudiante);
        fila1.add(Ui.etiqueta("Libro:"));
        fila1.add(cbLibro);
        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        fila2.add(btnPrestar);
        progreso.setIndeterminate(true);
        progreso.setVisible(false);
        fila2.add(progreso);
        nuevo.add(fila1);
        nuevo.add(fila2);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(nuevo, BorderLayout.CENTER);
        norte.add(lblEstado, BorderLayout.SOUTH);

        // --- Pendientes de devolución ---
        JPanel pendientes = new JPanel(new BorderLayout(0, 6));
        pendientes.setBorder(BorderFactory.createTitledBorder(
                estudianteFijo == null ? "Préstamos pendientes de devolución" : "Mis préstamos pendientes"));
        Ui.anchos(tabla, 90, 260, 180, 110, 110, 90);
        Ui.pintarAtrasados(tabla, 5);
        pendientes.add(new JScrollPane(tabla), BorderLayout.CENTER);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(btnDevolver);
        pendientes.add(botones, BorderLayout.SOUTH);

        add(norte, BorderLayout.NORTH);
        add(pendientes, BorderLayout.CENTER);

        btnPrestar.addActionListener(e -> prestar());
        btnDevolver.addActionListener(e -> devolver());

        if (estudianteFijo != null) {
            cbEstudiante.addItem(estudianteFijo);
            cbEstudiante.setEnabled(false);
        }
    }

    @Override
    public void refrescar() {
        if (!btnPrestar.isEnabled()) {
            return; // hay una operación en curso: se recarga sola cuando termina
        }
        try {
            if (estudianteFijo == null) {
                Estudiante previo = (Estudiante) cbEstudiante.getSelectedItem();
                cbEstudiante.removeAllItems();
                for (Estudiante e : estudianteController.listar()) {
                    cbEstudiante.addItem(e);
                    if (previo != null && previo.getId() == e.getId()) {
                        cbEstudiante.setSelectedItem(e);
                    }
                }
            }
            cbLibro.removeAllItems();
            for (Libro l : libroController.listar()) {
                if (l.hayStock()) { // solo se ofrecen libros con stock
                    cbLibro.addItem(l);
                }
            }
            Integer filtro = estudianteFijo != null ? estudianteFijo.getId() : null;
            modelo.setRowCount(0);
            for (Object[] fila : controller.listarActivos(filtro)) {
                modelo.addRow(fila);
            }
            if (estudianteFijo == null && cbEstudiante.getItemCount() == 0) {
                lblEstado.setText("No hay estudiantes registrados.");
            }
        } catch (AppException e) {
            Ui.error(this, e.getMessage());
        }
    }

    private void prestar() {
        Estudiante est = (Estudiante) cbEstudiante.getSelectedItem();
        Libro libro = (Libro) cbLibro.getSelectedItem();
        if (est == null) {
            Ui.error(this, estudianteFijo == null
                    ? "Selecciona un estudiante."
                    : "Tu usuario no está vinculado a un estudiante registrado (mismo RUT). Consulta al bibliotecario.");
            return;
        }
        if (libro == null) {
            Ui.error(this, "Selecciona un libro con stock disponible.");
            return;
        }
        ocupado(true, "Registrando préstamo en segundo plano...");
        // El trabajo pesado corre en otro hilo; la interfaz sigue respondiendo.
        controller.registrarPrestamoAsync(est.getId(), libro.getId(),
                prestamo -> SwingUtilities.invokeLater(() -> {
                    ocupado(false, "Préstamo registrado: vence el " + prestamo.getFechaDevolucion() + ".");
                    refrescar();
                }),
                mensaje -> SwingUtilities.invokeLater(() -> {
                    ocupado(false, " ");
                    Ui.error(this, mensaje);
                    refrescar();
                }));
    }

    private void devolver() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Ui.info(this, "Selecciona un préstamo de la tabla.");
            return;
        }
        int idPrestamo = (Integer) modelo.getValueAt(fila, 0);
        ocupado(true, "Registrando devolución en segundo plano...");
        controller.devolverAsync(idPrestamo,
                diasAtraso -> SwingUtilities.invokeLater(() -> {
                    ocupado(false, " ");
                    if (diasAtraso > 0) {
                        Ui.info(this, "Devolución registrada con ATRASO de " + diasAtraso
                                + " día(s). Se dejó constancia en logs/atrasos.log.");
                    } else {
                        Ui.info(this, "Devolución registrada correctamente.");
                    }
                    refrescar();
                }),
                mensaje -> SwingUtilities.invokeLater(() -> {
                    ocupado(false, " ");
                    Ui.error(this, mensaje);
                    refrescar();
                }));
    }

    /** Bloquea/desbloquea los botones y muestra la barra mientras corre el hilo. */
    private void ocupado(boolean trabajando, String mensaje) {
        btnPrestar.setEnabled(!trabajando);
        btnDevolver.setEnabled(!trabajando);
        progreso.setVisible(trabajando);
        lblEstado.setText(mensaje);
    }
}
