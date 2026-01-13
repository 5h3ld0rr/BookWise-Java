/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bookwise.DataAccess;

/**
 *
 * @author chira
 */
import bookwise.DataAccess.DB;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BookTransaction {

    private static final DateTimeFormatter MYSQL_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // CREATE: Borrow a book
    public static boolean create(int userId, int bookId) {
        String sql = "INSERT INTO book_transactions (user_id, book_id, borrow_date) VALUES (?, ?, ?)";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, bookId);
            pstmt.setString(3, LocalDateTime.now().format(MYSQL_DATETIME));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // UPDATE: Return a book
    public static boolean updateReturn(int transactionId) {
        String sql = "UPDATE book_transactions SET return_date = ? WHERE id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, LocalDateTime.now().format(MYSQL_DATETIME));
            pstmt.setInt(2, transactionId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get due date, overdue days, and fine
    public static class ReturnDetails {
        public final LocalDateTime dueDate;
        public final int daysOverdue;
        public final double fine;

        public ReturnDetails(LocalDateTime dueDate, int daysOverdue, double fine) {
            this.dueDate = dueDate;
            this.daysOverdue = daysOverdue;
            this.fine = fine;
        }
    }

    public static ReturnDetails getReturnDetailsByBorrowDate(LocalDateTime borrowDate) {
        LocalDateTime dueDate = borrowDate.plusDays(CommonData.Rules.MAX_DAYS_TO_RETURN);
        long daysOverdue = java.time.Duration.between(dueDate, LocalDateTime.now()).toDays();
        if (daysOverdue < 0) daysOverdue = 0;

        double fine = daysOverdue > 0 ? daysOverdue * CommonData.Rules.FINE_PER_DAY : 0.0;

        return new ReturnDetails(dueDate, (int) daysOverdue, fine);
    }

    // Get unreturned books for a user (used in User.GetUserByUniqueIdentifier)
    public static Book[] getUnreturnedBooksByUser(int userId) {
        String sql = """
            SELECT bt.id AS transaction_id, bt.book_id, b.title, b.isbn_no, b.author, b.category, 
                   b.available_books, bt.borrow_date
            FROM book_transactions bt
            INNER JOIN books b ON b.id = bt.book_id
            WHERE bt.user_id = ? AND bt.return_date IS NULL
            """;

        List<Book> books = new ArrayList<>();

        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Book book = new Book();
                    book.setId(rs.getInt("book_id"));
                    book.setTitle(rs.getString("title"));
                    book.setIsbn(rs.getString("isbn_no"));
                    book.setAuthor(rs.getString("author"));
                    book.setCategory(rs.getString("category"));
                    book.setAvailableBooks(rs.getInt("available_books"));
                    book.setTransactionId(rs.getInt("transaction_id"));

                    book.setBorrowDate(rs.getTimestamp("borrow_date"));

                    books.add(book);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books.toArray(new Book[0]);
    }

    // === Advanced Filtering & Search (like your C# version) ===
    public static class FilterData {
        public LocalDate borrowStartDate;
        public LocalDate borrowEndDate;
        public LocalDate returnStartDate;
        public LocalDate returnEndDate;
        public String status;     // "Returned", "Unreturned", or null
        public String overdue;    // "Yes", "No", or null

        public FilterData() {}
    }

    public static List<TransactionRow> getAll(FilterData filter) {
        return getTransactions(filter, null);
    }

    public static List<TransactionRow> search(String searchQuery, FilterData filter) {
        return getTransactions(filter, "%" + searchQuery + "%");
    }

    public static class TransactionRow {
        public String isbn;
        public String title;
        public int userId;
        public String userName;
        public LocalDateTime borrowDate;
        public LocalDateTime returnDate;

        // Constructor + getters if needed
        public TransactionRow(String isbn, String title, int userId, String userName,
                              LocalDateTime borrowDate, LocalDateTime returnDate) {
            this.isbn = isbn;
            this.title = title;
            this.userId = userId;
            this.userName = userName;
            this.borrowDate = borrowDate;
            this.returnDate = returnDate;
        }
    }

    private static List<TransactionRow> getTransactions(FilterData filter, String searchQuery) {
        StringBuilder sql = new StringBuilder("""
            SELECT b.isbn_no, b.title, u.id AS user_id, 
                   CONCAT(u.first_name, ' ', u.last_name) AS user_name,
                   bt.borrow_date, bt.return_date
            FROM book_transactions bt
            INNER JOIN users u ON bt.user_id = u.id
            INNER JOIN books b ON bt.book_id = b.id
            """);

        List<Object> params = new ArrayList<>();
        boolean hasWhere = false;

        if (searchQuery != null) {
            sql.append(" WHERE (u.id LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ? OR b.title LIKE ? OR b.isbn_no LIKE ?)");
            for (int i = 0; i < 5; i++) params.add(searchQuery);
            hasWhere = true;
        }

        if (filter != null) {
            String prefix = hasWhere ? " AND" : " WHERE";
            hasWhere = true;

            sql.append(prefix).append(" (bt.borrow_date BETWEEN ? AND ?)");
            params.add(filter.borrowStartDate.atStartOfDay());
            params.add(filter.borrowEndDate.atTime(23, 59, 59));

            sql.append(" AND (bt.return_date IS NULL OR bt.return_date BETWEEN ? AND ?)");
            params.add(filter.returnStartDate.atStartOfDay());
            params.add(filter.returnEndDate.atTime(23, 59, 59));

            if ("Returned".equals(filter.status)) {
                sql.append(" AND bt.return_date IS NOT NULL");
            } else if ("Unreturned".equals(filter.status)) {
                sql.append(" AND bt.return_date IS NULL");
            }

            int maxDays = CommonData.Rules.MAX_DAYS_TO_RETURN;
            if ("Yes".equals(filter.overdue)) {
                sql.append(" AND IF(bt.return_date IS NULL, DATEDIFF(NOW(), bt.borrow_date), DATEDIFF(bt.return_date, bt.borrow_date)) > ").append(maxDays);
            } else if ("No".equals(filter.overdue)) {
                sql.append(" AND IF(bt.return_date IS NULL, DATEDIFF(NOW(), bt.borrow_date), DATEDIFF(bt.return_date, bt.borrow_date)) <= ").append(maxDays);
            }
        }

        sql.append(" ORDER BY COALESCE(bt.return_date, bt.borrow_date) DESC");

        List<TransactionRow> list = new ArrayList<>();
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof LocalDateTime dt) {
                    pstmt.setTimestamp(i + 1, Timestamp.valueOf(dt));
                } else {
                    pstmt.setObject(i + 1, param);
                }
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new TransactionRow(
                        rs.getString("isbn_no"),
                        rs.getString("title"),
                        rs.getInt("user_id"),
                        rs.getString("user_name"),
                        rs.getTimestamp("borrow_date").toLocalDateTime(),
                        rs.getTimestamp("return_date") != null ?
                            rs.getTimestamp("return_date").toLocalDateTime() : null
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static int getBorrowedBooksCount() {
        String sql = "SELECT COUNT(*) FROM book_transactions WHERE return_date IS NULL";
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}