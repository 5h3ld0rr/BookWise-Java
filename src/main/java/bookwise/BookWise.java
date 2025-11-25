/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package bookwise;

import bookwise.DataAccess.DB;
import bookwise.UI.SignInForm;
import javax.swing.JOptionPane;

/**
 *
 * @author chira
 */
public class BookWise {

    public static void main(String[] args) {
        
        // Initialize database connection
        System.out.println("===========================================");
        System.out.println("BookWise Library Management System v1.0");
        System.out.println("===========================================");
        System.out.println("Initializing database connection...");

        if (DB.getConnection() != null) {
            System.out.println("✓ Database connection established successfully!");
            System.out.println("✓ Connected to: " + Config.Database.NAME);
            System.out.println("===========================================");

            // Launch the application
            
            java.awt.EventQueue.invokeLater(() -> {
                SignInForm form = new SignInForm();
                form.setLocationRelativeTo(null);
                form.setVisible(true);
            });
        } else {
            System.err.println("✗ Failed to establish database connection!");
            System.err.println("Please check your database configuration.");
            System.err.println("===========================================");

            // Show error dialog
            JOptionPane.showMessageDialog(null,
                    "Failed to connect to the database.\n" +
                            "Please check your network connection and database settings.\n\n" +
                            "Server: " + Config.Database.HOST + "\n" +
                            "Database: " + Config.Database.NAME,
                    "Database Connection Error",
                    JOptionPane.ERROR_MESSAGE);

            System.exit(1);
        }
        
    }
}
