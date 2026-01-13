/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package bookwise.UI.Panels;

/**
 *
 * @author wsr
 */
public class BorrowBookPanel extends javax.swing.JPanel {

    /**
     * Creates new form BorrowBookPanel
     */
    public BorrowBookPanel() {
        initComponents();
        setupListeners();
    }

    private void setupListeners() {
        // Align ID spinner text to the left to match other fields
        javax.swing.JSpinner.DefaultEditor editor = (javax.swing.JSpinner.DefaultEditor) jSpinner1.getEditor();
        editor.getTextField().setHorizontalAlignment(javax.swing.JTextField.LEFT);
        
        // Make read-only fields
        jTextField1.setEditable(false); // Name
        jTextField1.setFocusable(false);
        jTextField4.setEditable(false); // Mobile
        jTextField4.setFocusable(false);
        jTextField5.setEditable(false); // Address
        jTextField5.setFocusable(false);
        
        // Make Book info read-only (except ISBN)
        jTextField6.setEditable(false); // Title
        jTextField6.setFocusable(false);
        jTextField8.setEditable(false); // Author
        jTextField8.setFocusable(false);
        jTextField9.setEditable(false); // Category
        jTextField9.setFocusable(false);

        // User Fetch Listeners
        jButton1.addActionListener(e -> {
            if (jButton1.getText().equals("Confirm")) {
                if (fetchUser()) {
                    jButton1.setText("Edit");
                    toggleUserFields(false);
                } else {
                     javax.swing.JOptionPane.showMessageDialog(this, "User not found.", "Not Found", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                jButton1.setText("Confirm");
                toggleUserFields(true);
                clearUserFields();
            }
        });
        
        // Fetch on Enter (Search only)
        jTextField2.addActionListener(e -> { if (jButton1.getText().equals("Confirm")) fetchUser(); }); // NIC
        jTextField3.addActionListener(e -> { if (jButton1.getText().equals("Confirm")) fetchUser(); }); // Email
        
        // Fetch on Focus Lost
        java.awt.event.FocusAdapter fetchFocusListener = new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (jButton1.getText().equals("Confirm")) fetchUser();
            }
        };
        jTextField2.addFocusListener(fetchFocusListener);
        jTextField3.addFocusListener(fetchFocusListener);
        
        // Book Fetch Listeners
        jButton2.addActionListener(e -> {
            if (jButton2.getText().equals("Confirm")) {
                if (fetchBook()) {
                    jButton2.setText("Edit");
                    toggleBookFields(false);
                }
            } else {
                jButton2.setText("Confirm");
                toggleBookFields(true);
                clearBookFields();
            }
        });
        jTextField7.addActionListener(e -> { if (jButton2.getText().equals("Confirm")) fetchBook(); }); // Fetch on Enter in ISBN field
        
        // BORROW BUTTON (Proceed)
        jButton3.setText("Borrow");
        jButton3.addActionListener(e -> {
             int userId = (Integer) jSpinner1.getValue();
             String isbn = jTextField7.getText().trim();
             
             // 1. Basic Validation
             if (userId <= 0) {
                 javax.swing.JOptionPane.showMessageDialog(this, "Please confirm a valid user first.", "Invalid User", javax.swing.JOptionPane.WARNING_MESSAGE);
                 return;
             }
             if (isbn.isEmpty()) {
                  javax.swing.JOptionPane.showMessageDialog(this, "Please confirm a valid book first.", "Invalid Book", javax.swing.JOptionPane.WARNING_MESSAGE);
                  return;
             }
             
             // 2. Refresh/Verify User and Book Existence (Optional but safe)
             // ... assuming Confirm steps verified them.
             
             // 3. User MAX_BOOKS Rule Check
             bookwise.DataAccess.Book[] borrowedBooks = bookwise.DataAccess.BookTransaction.getUnreturnedBooksByUser(userId);
             int currentCount = borrowedBooks.length;
             int maxBooks = bookwise.DataAccess.CommonData.Rules.MAX_BOOKS_PER_USER;
             
             if (currentCount >= maxBooks) {
                 javax.swing.JOptionPane.showMessageDialog(this, 
                     "This user has reached the borrowing limit of " + maxBooks + " books.\n" +
                     "Current borrowed: " + currentCount, 
                     "Limit Reached", 
                     javax.swing.JOptionPane.ERROR_MESSAGE);
                 return;
             }
             
             // 4. Proceed to Borrow
             // We need Book ID. fetchBook found it but BorrowPanel doesn't store Book ID in a field?
             // Re-fetch book to get ID.
             bookwise.DataAccess.Book book = bookwise.DataAccess.Book.get(isbn);
             if (book == null) {
                  javax.swing.JOptionPane.showMessageDialog(this, "Book not found in database.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                  return;
             }
             
             if (book.getAvailableBooks() <= 0) {
                  javax.swing.JOptionPane.showMessageDialog(this, "This book is currently out of stock.", "Out of Stock", javax.swing.JOptionPane.WARNING_MESSAGE);
                  return;
             }
             
             boolean success = bookwise.DataAccess.BookTransaction.create(userId, book.getId());
             if (success) {
                 javax.swing.JOptionPane.showMessageDialog(this, "Book borrowed successfully!", "Success", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                 // Reset fields for next transaction
                 // Reset Book fields but keep User? Usually librarians process multiple books for one user.
                 // But logic says Confirm/Edit. 
                 // If we keep User confirmed, we just reset Book.
                 jButton2.setText("Confirm");
                 toggleBookFields(true);
                 clearBookFields();
             } else {
                 javax.swing.JOptionPane.showMessageDialog(this, "Failed to process transaction.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
             }
        });
        
    }

    private boolean fetchUser() {
        try {
            int userId = (Integer) jSpinner1.getValue();
            String nic = jTextField2.getText().trim();
            String email = jTextField3.getText().trim();

            if (userId <= 0 && nic.isEmpty() && email.isEmpty()) {
                // Don't show popup on focus lost if empty, just return
                return false;
            }

            bookwise.DataAccess.User user = bookwise.DataAccess.User.getUserByUniqueIdentifier(userId, nic, email);
            if (user != null) {
                // Populate fields, including setting the ID if found via other means
                jSpinner1.setValue(user.getId());
                jTextField1.setText(user.getFirstName() + " " + user.getLastName());
                jTextField2.setText(user.getNic());
                jTextField3.setText(user.getEmail());
                jTextField4.setText(user.getPhone());
                jTextField5.setText(user.getAddress());
                return true;
            } else {
                // Only show alert if explicit action (button/enter) or meaningful input? 
                 // Note: We might want to suppressing noise for passive focus lost, but usually button click needs feedback.
                 // For now returning false lets the caller decide or just fail silently for passive events.
                 // But for the button click, we explicitly check returning 'false' and show message there.
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void clearUserFields() {
        jTextField1.setText("");
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField4.setText("");
        jTextField5.setText("");
        jSpinner1.setValue(0);
    }

    private boolean fetchBook() {
        String isbn = jTextField7.getText().trim();
        if (isbn.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Please enter an ISBN number.", "Missing Input", javax.swing.JOptionPane.WARNING_MESSAGE);
            return false;
        }

        bookwise.DataAccess.Book book = bookwise.DataAccess.Book.get(isbn);
        if (book != null) {
            jTextField6.setText(book.getTitle());
            jTextField8.setText(book.getAuthor());
            jTextField9.setText(book.getCategory());
            return true;
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Book not found.", "Not Found", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            clearBookFields();
            return false;
        }
    }

    private void clearBookFields() {
        jTextField6.setText("");
        jTextField8.setText("");
        jTextField9.setText("");
        jTextField7.setText("");
    }
    
    private void toggleUserFields(boolean enable) {
        toggleSpinner(jSpinner1, enable);
        jTextField2.setEditable(enable);
        jTextField2.setFocusable(enable);
        jTextField3.setEditable(enable);
        jTextField3.setFocusable(enable);
    }
    
    private void toggleSpinner(javax.swing.JSpinner spinner, boolean enable) {
        if (spinner.getEditor() instanceof javax.swing.JSpinner.DefaultEditor) {
             javax.swing.JSpinner.DefaultEditor editor = (javax.swing.JSpinner.DefaultEditor) spinner.getEditor();
             editor.getTextField().setEditable(enable);
             editor.getTextField().setFocusable(enable);
        }
        for (java.awt.Component c : spinner.getComponents()) {
            if (c != spinner.getEditor()) {
                c.setEnabled(enable);
            }
        }
    }
    
    private void toggleBookFields(boolean enable) {
        jTextField7.setEditable(enable);
        jTextField7.setFocusable(enable);
    }


    public void setBook(String isbn) {
        if (isbn != null && !isbn.isEmpty()) {
            jTextField7.setText(isbn);
            fetchBook();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jTextField2 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jTextField7 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jTextField8 = new javax.swing.JTextField();
        jTextField9 = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();

        setBackground(new java.awt.Color(248, 248, 255));
        setFont(new java.awt.Font("Segoe UI", 0, 9)); // NOI18N
        setMaximumSize(new java.awt.Dimension(0, 0));
        setPreferredSize(new java.awt.Dimension(929, 541));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "User Information", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 24), new java.awt.Color(30, 41, 59))); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(400, 400));

        jTextField1.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jTextField1.setMaximumSize(new java.awt.Dimension(0, 0));
        jTextField1.setMinimumSize(new java.awt.Dimension(0, 0));
        jTextField1.setPreferredSize(new java.awt.Dimension(213, 29));

        jButton1.setBackground(new java.awt.Color(37, 56, 140));
        jButton1.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Confirm");
        jButton1.setFocusPainted(false);
        jButton1.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jButton1.setMaximumSize(new java.awt.Dimension(0, 0));
        jButton1.setMinimumSize(new java.awt.Dimension(0, 0));
        jButton1.setPreferredSize(new java.awt.Dimension(111, 41));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel4.setText("NIC  No");

        jSpinner1.setMaximumSize(new java.awt.Dimension(0, 0));
        jSpinner1.setMinimumSize(new java.awt.Dimension(0, 0));
        jSpinner1.setPreferredSize(new java.awt.Dimension(213, 29));

        jTextField2.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jTextField2.setMaximumSize(new java.awt.Dimension(0, 0));
        jTextField2.setMinimumSize(new java.awt.Dimension(0, 0));
        jTextField2.setPreferredSize(new java.awt.Dimension(213, 29));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel2.setText("Name");

        jTextField5.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jTextField5.setMaximumSize(new java.awt.Dimension(0, 0));
        jTextField5.setMinimumSize(new java.awt.Dimension(0, 0));
        jTextField5.setPreferredSize(new java.awt.Dimension(213, 29));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel3.setText("ID  No");

        jTextField4.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jTextField4.setMaximumSize(new java.awt.Dimension(0, 0));
        jTextField4.setMinimumSize(new java.awt.Dimension(0, 0));
        jTextField4.setPreferredSize(new java.awt.Dimension(213, 29));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel5.setText("Email");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel6.setText("Mobile No");

        jTextField3.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jTextField3.setMaximumSize(new java.awt.Dimension(0, 0));
        jTextField3.setMinimumSize(new java.awt.Dimension(0, 0));
        jTextField3.setPreferredSize(new java.awt.Dimension(213, 29));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel7.setText("Address");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 64, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jSpinner1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTextField3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTextField4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(20, 20, 20))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Book  Information", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 24), new java.awt.Color(30, 41, 59))); // NOI18N
        jPanel2.setPreferredSize(new java.awt.Dimension(400, 400));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel1.setText("Title");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel9.setText("Author");

        jButton2.setBackground(new java.awt.Color(37, 56, 140));
        jButton2.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("Confirm");
        jButton2.setFocusPainted(false);
        jButton2.setPreferredSize(new java.awt.Dimension(111, 41));

        jTextField7.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jTextField7.setPreferredSize(new java.awt.Dimension(213, 29));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel10.setText("Category");

        jTextField6.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jTextField6.setPreferredSize(new java.awt.Dimension(213, 29));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel8.setText("ISBM  No");

        jTextField8.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jTextField8.setPreferredSize(new java.awt.Dimension(213, 29));
        jTextField8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField8ActionPerformed(evt);
            }
        });

        jTextField9.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jTextField9.setPreferredSize(new java.awt.Dimension(213, 29));
        jTextField9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField9ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 72, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jTextField7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jTextField6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jTextField9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(20, 20, 20))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabel11.setFont(new java.awt.Font("Segoe UI Semibold", 1, 24)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(30, 41, 59));
        jLabel11.setText("Borrow a Book");
        jLabel11.setPreferredSize(new java.awt.Dimension(195, 32));

        jButton3.setBackground(new java.awt.Color(37, 56, 140));
        jButton3.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setText("Proceed");
        jButton3.setFocusPainted(false);
        jButton3.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jButton3.setMaximumSize(new java.awt.Dimension(0, 0));
        jButton3.setMinimumSize(new java.awt.Dimension(0, 0));
        jButton3.setPreferredSize(new java.awt.Dimension(111, 41));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(70, 70, 70))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(70, 70, 70))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(52, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField9ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField9ActionPerformed

    private void jTextField8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField8ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    // End of variables declaration//GEN-END:variables
}
