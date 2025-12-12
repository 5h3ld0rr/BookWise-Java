/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bookwise.DataAccess;

/**
 *
 * @author chira
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Book {
    private int id;
    private int transactionId;
    private java.util.Date borrowDate;  // or LocalDateTime if you prefer
    private String title;
    private String isbn;
    private String author;
    private String category;
    private int availableBooks;

    // ==================== Getters & Setters ====================
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public java.util.Date getBorrowDate() { return borrowDate; }
    public void setBorrowDate(java.util.Date borrowDate) { this.borrowDate = borrowDate; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getAvailableBooks() { return availableBooks; }
    public void setAvailableBooks(int availableBooks) { this.availableBooks = availableBooks; }

    // ==================== Instance Methods ====================

    public boolean isExisting() {
        String sql = "SELECT COUNT(*) FROM books WHERE isbn_no = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, isbn);
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

    public boolean add() {
        String sql = "INSERT INTO books (title, isbn_no, author, category, available_books) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.setString(2, isbn);
            pstmt.setString(3, author);
            pstmt.setString(4, category);
            pstmt.setInt(5, availableBooks);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update() {
        String sql = "UPDATE books SET title = ?, isbn_no = ?, author = ?, category = ?, available_books = ? WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.setString(2, isbn);
            pstmt.setString(3, author);
            pstmt.setString(4, category);
            pstmt.setInt(5, availableBooks);
            pstmt.setInt(6, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean remove() {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean borrow() {
        String sql = "UPDATE books SET available_books = available_books - 1 WHERE id = ? AND available_books > 0";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean returnBook() {  // renamed from Return() to avoid keyword conflict
        String sql = "UPDATE books SET available_books = available_books + 1 WHERE id = ?";
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

    public static Book get(String isbnNo) {
        String sql = "SELECT id, title, isbn_no, author, category, available_books FROM books WHERE isbn_no = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, isbnNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBook(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Book[] getAll() {
        String sql = "SELECT id, title, isbn_no, author, category, available_books FROM books ORDER BY id DESC";
        return executeQueryAndMap(sql);
    }

    public static Book[] search(String searchQuery) {
        String query = "%" + searchQuery + "%";
        String sql = "SELECT id, title, isbn_no, author, category, available_books FROM books " +
                     "WHERE title LIKE ? OR isbn_no LIKE ? OR author LIKE ? OR category LIKE ? " +
                     "ORDER BY id DESC";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 1; i <= 4; i++) {
                pstmt.setString(i, query);
            }

            List<Book> books = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRowToBook(rs));
                }
            }
            return books.toArray(new Book[0]);

        } catch (SQLException e) {
            e.printStackTrace();
            return new Book[0];
        }
    }

    // Helper: Execute query and return Book[]
    private static Book[] executeQueryAndMap(String sql) {
        List<Book> list = new ArrayList<>();
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToBook(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list.toArray(new Book[0]);
    }

    // Helper: Map ResultSet row → Book object
    private static Book mapRowToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setTitle(rs.getString("title"));
        book.setIsbn(rs.getString("isbn_no"));
        book.setAuthor(rs.getString("author"));
        book.setCategory(rs.getString("category"));
        book.setAvailableBooks(rs.getInt("available_books"));
        return book;
    }

    // ==================== Static Update Method ====================
    public static boolean update(int id, String title, String isbn, String author, String category, int availableBooks) {
        String sql = "UPDATE books SET title = ?, isbn_no = ?, author = ?, category = ?, available_books = ? WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.setString(2, isbn);
            pstmt.setString(3, author);
            pstmt.setString(4, category);
            pstmt.setInt(5, availableBooks);
            pstmt.setInt(6, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}