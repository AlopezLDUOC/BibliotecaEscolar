package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.LoginController;
import cl.duoc.biblioteca.model.Usuario;
import cl.duoc.biblioteca.util.AppException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

/** Pantalla de autenticación. */
public class LoginFrame extends JFrame {

    private final LoginController controller = new LoginController();
    private final JTextField txtUsuario = new JTextField();
    private final JPasswordField txtClave = new JPasswordField();

    public LoginFrame() {
        super("Biblioteca Escolar - Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel titulo = new JLabel("Biblioteca Escolar", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(Ui.etiqueta("RUT o correo:"));
        form.add(txtUsuario);
        form.add(Ui.etiqueta("Contraseña:"));
        form.add(txtClave);

        JButton btnIngresar = new JButton("Ingresar");
        btnIngresar.addActionListener(e -> ingresar());
        getRootPane().setDefaultButton(btnIngresar); // Enter = Ingresar

        JPanel contenido = new JPanel(new BorderLayout(10, 14));
        contenido.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        contenido.add(titulo, BorderLayout.NORTH);
        contenido.add(form, BorderLayout.CENTER);
        contenido.add(btnIngresar, BorderLayout.SOUTH);
        setContentPane(contenido);

        setSize(380, 320);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void ingresar() {
        try {
            Usuario u = controller.autenticar(txtUsuario.getText(), new String(txtClave.getPassword()));
            new MainFrame(u, controller).setVisible(true);
            dispose();
        } catch (AppException ex) {
            Ui.error(this, ex.getMessage());
            txtClave.setText("");
        }
    }
}
