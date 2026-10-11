package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.EstudianteController;
import cl.duoc.biblioteca.controller.ReporteController;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.util.AppException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

/** Reportes: libros más prestados, historial por estudiante y libros actualmente en préstamo. */
public class ReportePanel extends JPanel implements Refrescable {

    private static final String R_MAS_PRESTADOS = "Libros más prestados";
    private static final String R_HISTORIAL = "Historial por estudiante";
    private static final String R_EN_PRESTAMO = "Libros actualmente en préstamo";

    private final ReporteController controller = new ReporteController();
    private final EstudianteController estudianteController = new EstudianteController();

    private final JComboBox<String> cbReporte = new JComboBox<>(new String[]{R_MAS_PRESTADOS, R_HISTORIAL, R_EN_PRESTAMO});
    private final JComboBox<Estudiante> cbEstudiante = new JComboBox<>();
    private final JLabel lblEstudiante = Ui.etiqueta("Estudiante:");
    private final JLabel lblResumen = new JLabel(" ");
    private final JTable tabla = Ui.tabla(Ui.modelo(ReporteController.COLS_MAS_PRESTADOS));

    public ReportePanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel opciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        opciones.add(Ui.etiqueta("Reporte:"));
        opciones.add(cbReporte);
        opciones.add(lblEstudiante);
        opciones.add(cbEstudiante);
        JButton btnGenerar = new JButton("Generar");
        opciones.add(btnGenerar);

        add(opciones, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(lblResumen, BorderLayout.SOUTH);

        cbReporte.addActionListener(e -> actualizarVisibilidad());
        btnGenerar.addActionListener(e -> generar());
        actualizarVisibilidad();
    }

    @Override
    public void refrescar() {
        try {
            cbEstudiante.removeAllItems();
            for (Estudiante e : estudianteController.listar()) {
                cbEstudiante.addItem(e);
            }
        } catch (AppException e) {
            Ui.error(this, e.getMessage());
        }
    }

    private void actualizarVisibilidad() {
        boolean historial = R_HISTORIAL.equals(cbReporte.getSelectedItem());
        lblEstudiante.setVisible(historial);
        cbEstudiante.setVisible(historial);
    }

    private void generar() {
        try {
            String reporte = (String) cbReporte.getSelectedItem();
            String[] columnas;
            List<Object[]> filas;
            if (R_HISTORIAL.equals(reporte)) {
                Estudiante est = (Estudiante) cbEstudiante.getSelectedItem();
                if (est == null) {
                    Ui.error(this, "Selecciona un estudiante.");
                    return;
                }
                columnas = ReporteController.COLS_HISTORIAL;
                filas = controller.historialPorEstudiante(est.getId());
            } else if (R_EN_PRESTAMO.equals(reporte)) {
                columnas = ReporteController.COLS_EN_PRESTAMO;
                filas = controller.librosEnPrestamo();
            } else {
                columnas = ReporteController.COLS_MAS_PRESTADOS;
                filas = controller.librosMasPrestados();
            }
            DefaultTableModel modelo = Ui.modelo(columnas);
            for (Object[] fila : filas) {
                modelo.addRow(fila);
            }
            tabla.setModel(modelo);
            if (columnas.length > 0 && "Estado".equals(columnas[columnas.length - 1])) {
                Ui.pintarAtrasados(tabla, columnas.length - 1);
            }
            lblResumen.setText(reporte + ": " + filas.size() + " registro(s).");
        } catch (AppException e) {
            Ui.error(this, e.getMessage());
        }
    }
}
