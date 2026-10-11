package cl.duoc.biblioteca.view;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Component;

final class Ui {

    private Ui() { }

    /** Modelo de tabla no editable con las columnas indicadas. */
    static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    static JTable tabla(DefaultTableModel modelo) {
        JTable t = new JTable(modelo);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setRowHeight(24);
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }

    /** Fija el ancho preferido de cada columna (en píxeles), en orden. */
    static void anchos(JTable tabla, int... anchos) {
        for (int i = 0; i < anchos.length && i < tabla.getColumnModel().getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

    /** Pinta en rojo las filas en la cual el estado es ATRASADO. */
    static void pintarAtrasados(JTable tabla, int columnaEstado) {
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel,
                                                           boolean foco, int fila, int col) {
                Component c = super.getTableCellRendererComponent(t, valor, sel, foco, fila, col);
                Object estado = t.getModel().getValueAt(t.convertRowIndexToModel(fila), columnaEstado);
                if (!sel) {
                    c.setForeground("ATRASADO".equals(String.valueOf(estado)) ? new Color(190, 30, 30) : t.getForeground());
                }
                return c;
            }
        });
    }

    static JLabel etiqueta(String texto) {
        return new JLabel(texto);
    }

    static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    static boolean confirmar(Component padre, String mensaje) {
        return JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
