/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bookwise.DataAccess;

/**
 *
 * @author chira
 */
public class CommonData {
    public static class Rules {
        public static int MAX_BOOKS_PER_USER = 2;
        public static int MAX_DAYS_TO_RETURN = 14;
        public static double FINE_PER_DAY = 50.0;

        public static void load() {
            String sql = "SELECT max_books_per_user, max_days_to_return, fine_per_day FROM rules LIMIT 1";
            try (java.sql.Connection conn = DB.getConnection();
                 java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
                 java.sql.ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    MAX_BOOKS_PER_USER = rs.getInt("max_books_per_user");
                    MAX_DAYS_TO_RETURN = rs.getInt("max_days_to_return");
                    FINE_PER_DAY = rs.getDouble("fine_per_day");
                }
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }

        public static boolean save(int maxBooks, int maxDays, double fine) {
            // Update the single row, assuming id=1 or just update the first row found
            // If table is empty, we should insert. For safety, let's try update first, then insert if 0 rows affect.
            
            // First validation
            if (maxBooks < 0 || maxDays < 0 || fine < 0) return false;

            String updateSql = "UPDATE rules SET max_books_per_user = ?, max_days_to_return = ?, fine_per_day = ?";
            try (java.sql.Connection conn = DB.getConnection();
                 java.sql.PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                 
                pstmt.setInt(1, maxBooks);
                pstmt.setInt(2, maxDays);
                pstmt.setDouble(3, fine);
                
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    // Update local cache
                    MAX_BOOKS_PER_USER = maxBooks;
                    MAX_DAYS_TO_RETURN = maxDays;
                    FINE_PER_DAY = fine;
                    return true;
                } else {
                    // Try insert if update failed (empty table)
                    String insertSql = "INSERT INTO rules (max_books_per_user, max_days_to_return, fine_per_day) VALUES (?, ?, ?)";
                    try (java.sql.PreparedStatement pstmtInsert = conn.prepareStatement(insertSql)) {
                        pstmtInsert.setInt(1, maxBooks);
                        pstmtInsert.setInt(2, maxDays);
                        pstmtInsert.setDouble(3, fine);
                        int insertRows = pstmtInsert.executeUpdate();
                         if (insertRows > 0) {
                            MAX_BOOKS_PER_USER = maxBooks;
                            MAX_DAYS_TO_RETURN = maxDays;
                            FINE_PER_DAY = fine;
                            return true;
                        }
                    }
                }
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
            return false;
        }
    }
}