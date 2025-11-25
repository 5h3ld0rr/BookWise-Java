/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bookwise.DataAccess;

/**
 *
 * @author chira
 */
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String nic;
    private String role;
    private String phone;
    private String address;
    private String password;           // plain text (only for input)
    private Book[] borrowedBooks;

    // ==================== Getters & Setters ====================
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Book[] getBorrowedBooks() { return borrowedBooks; }
    public void setBorrowedBooks(Book[] borrowedBooks) { this.borrowedBooks = borrowedBooks; }

    // ==================== Instance Methods ====================

    public boolean isRegistered() {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ? OR email = ? OR nic = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, this.id);
            pstmt.setString(2, this.email);
            pstmt.setString(3, this.nic);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean register() {
        String hashedPassword = (password != null && !password.trim().isEmpty())
                ? BCrypt.hashpw(password, BCrypt.gensalt())
                : null;

        String sql = "INSERT INTO users (id, first_name, last_name, email, role, nic, phone, address, password) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.setString(2, firstName);
            pstmt.setString(3, lastName);
            pstmt.setString(4, email);
            pstmt.setString(5, role);
            pstmt.setString(6, nic);
            pstmt.setString(7, phone);
            pstmt.setString(8, address);
            pstmt.setString(9, hashedPassword);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update() {
        String sql;
        if (password != null && !password.trim().isEmpty()) {
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
            sql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, role = ?, nic = ?, phone = ?, address = ?, password = ? WHERE id = ?";
        } else {
            sql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, role = ?, nic = ?, phone = ?, address = ? WHERE id = ?";
        }

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, email);
            pstmt.setString(4, role);
            pstmt.setString(5, nic);
            pstmt.setString(6, phone);
            pstmt.setString(7, address);

            if (password != null && !password.trim().isEmpty()) {
                pstmt.setString(8, BCrypt.hashpw(password, BCrypt.gensalt()));
                pstmt.setInt(9, id);
            } else {
                pstmt.setInt(8, id);
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean remove() {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== Static Methods ====================

    public static User[] getAll(String acceptedRoles) {
        // Build IN clause safely
        String[] roles = acceptedRoles.split(",");
        String placeholders = String.join(",", java.util.Collections.nCopies(roles.length, "?"));
        String sql = "SELECT id, first_name, last_name, email, role, nic, phone, address " +
                     "FROM users WHERE role IN (" + placeholders + ") ORDER BY created_at DESC";

        List<User> list = new ArrayList<>();
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < roles.length; i++) {
                pstmt.setString(i + 1, roles[i].trim());
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list.toArray(new User[0]);
    }

    public static User[] search(String query, String acceptedRoles) {
        String searchTerm = "%" + query + "%";
        String[] roles = acceptedRoles.split(",");
        String placeholders = String.join(",", java.util.Collections.nCopies(roles.length, "?"));
        String sql = "SELECT id, first_name, last_name, email, role, nic, phone, address " +
                     "FROM users WHERE (id LIKE ? OR first_name LIKE ? OR last_name LIKE ? OR email LIKE ? OR nic LIKE ? OR phone LIKE ?) " +
                     "AND role IN (" + placeholders + ") ORDER BY created_at DESC";

        List<User> list = new ArrayList<>();
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 1; i <= 6; i++) {
                pstmt.setString(i, searchTerm);
            }
            for (int i = 0; i < roles.length; i++) {
                pstmt.setString(7 + i, roles[i].trim());
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list.toArray(new User[0]);
    }

    public static User getUserByUniqueIdentifier(int id, String nic, String email) {
        String sql = "SELECT id, first_name, last_name, email, role, nic, phone, address FROM users " +
                     "WHERE id = ? OR nic = ? OR email = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.setString(2, nic);
            pstmt.setString(3, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapRowToUser(rs);
                    user.setBorrowedBooks(BookTransaction.getUnreturnedBooksByUser(user.getId()));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Helper to map ResultSet → User
    private static User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setEmail(rs.getString("email"));
        user.setNic(rs.getString("nic"));
        user.setRole(rs.getString("role"));
        user.setPhone(rs.getString("phone"));
        user.setAddress(rs.getString("address"));
        return user;
    }
}