package cl.duoc.biblioteca;

import cl.duoc.biblioteca.util.DatabaseConnection;
import cl.duoc.biblioteca.view.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Punto de entrada de la aplicación. */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // si falla se usa el aspecto por defecto de Swing
        }
        // Cierra la conexión (Singleton) al terminar el programa
        Runtime.getRuntime().addShutdownHook(new Thread(() -> DatabaseConnection.getInstance().cerrar()));
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
