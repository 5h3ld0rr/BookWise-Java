/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bookwise.DataAccess;
import java.sql.*;
import javax.swing.JOptionPane;
import org.mindrot.jbcrypt.BCrypt;  // Best BCrypt library for Java

/**
 *
 * @author chira
 */

public class Admin extends User {

    public boolean authenticate() {
        String query = "SELECT id, first_name, last_name, email, nic, role, phone, address, password " +
                       "FROM users " +
                       "WHERE (id = ? OR email = ?) AND role != 'Student'";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Set parameters (prevents SQL injection)
            pstmt.setInt(1, this.getId());
            pstmt.setString(2, this.getEmail());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPasswordFromDB = rs.getString("password");
                    String plainPassword = this.getPassword();  // from your User class

                    // Verify password using BCrypt
                    if (!BCrypt.checkpw(plainPassword, hashedPasswordFromDB)) {
                        return false;
                    }

                    // Load user data into object
                    this.setId(rs.getInt("id"));
                    this.setFirstName(rs.getString("first_name"));
                    this.setLastName(rs.getString("last_name"));
                    this.setEmail(rs.getString("email"));
                    this.setNic(rs.getString("nic"));
                    this.setRole(rs.getString("role"));
                    this.setPhone(rs.getString("phone"));
                    this.setAddress(rs.getString("address"));

                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error: " + e.getMessage());
        }

        return false;
    }
}