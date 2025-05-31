import ui.LoginPage;
public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            // Initialize the GUI and set up the main application window
            LoginPage loginPage = new LoginPage();
            loginPage.setVisible(true);
        });
    }
}