/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package bookwise.UI.Panels;

/**
 *
 * @author wsr
 */
public class ReturnBookPanel extends javax.swing.JPanel {

    /**
     * Creates new form ReturnBookPanel
     */
    public ReturnBookPanel() {
        initComponents();
        
        // Align ID spinner text to the left
        javax.swing.JSpinner.DefaultEditor editor = (javax.swing.JSpinner.DefaultEditor) jSpinner1.getEditor();
        editor.getTextField().setHorizontalAlignment(javax.swing.JTextField.LEFT);
        setOverdueFieldsVisible(false);
        checkProceedButtonVisibility();
        
        javax.swing.JTextField editorTextField = (javax.swing.JTextField) jComboBox1.getEditor().getEditorComponent();
        editorTextField.setDisabledTextColor(new java.awt.Color(40, 40, 40));
        editorTextField.setBackground(java.awt.Color.WHITE); 
    }

    private boolean fetchUser() {
        try {
            int userId = (Integer) jSpinner1.getValue();
            String nic = jTextField2.getText().trim();
            String email = jTextField3.getText().trim(); 

            if (userId <= 0 && nic.isEmpty() && email.isEmpty()) {
                // Don't show warning on passive focus lost if empty
                return false;
            }

            bookwise.DataAccess.User user = bookwise.DataAccess.User.getUserByUniqueIdentifier(userId, nic, email);
            if (user != null) {
                jSpinner1.setValue(user.getId());
                jTextField1.setText(user.getFirstName() + " " + user.getLastName());
                jTextField2.setText(user.getNic());
                jTextField3.setText(user.getEmail()); 
                jTextField4.setText(user.getPhone()); 
                jTextField5.setText(user.getAddress()); 
                populateBorrowedDropdown(userId);
                return true;
            } else {
               // Optional: feedback
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
         String isbn = (String) jComboBox1.getSelectedItem();
         if (isbn == null || isbn.isEmpty()) {
            return false;
         }

        bookwise.DataAccess.Book book = bookwise.DataAccess.Book.get(isbn);
        if (book != null) {
            jTextField6.setText(book.getTitle());
            jTextField8.setText(book.getCategory()); 
            
            // Populate Borrow Date and Fine
            bookwise.DataAccess.Book[] borrowed = bookwise.DataAccess.BookTransaction.getUnreturnedBooksByUser((Integer) jSpinner1.getValue());
            for (bookwise.DataAccess.Book b : borrowed) {
                if (b.getIsbn().equals(isbn)) {
                     java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                     if (b.getBorrowDate() != null) {
                        java.time.LocalDateTime localDate = b.getBorrowDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
                        jTextField9.setText(formatter.format(localDate));
                     
                        bookwise.DataAccess.BookTransaction.ReturnDetails details = 
                            bookwise.DataAccess.BookTransaction.getReturnDetailsByBorrowDate(localDate);
                     
                        if (details.daysOverdue > 0) {
                            setOverdueFieldsVisible(true);
                            jLabel14.setText(String.valueOf(details.daysOverdue));
                            jLabel15.setText(String.format("%.2f", details.fine));
                        } else {
                            setOverdueFieldsVisible(false);
                        }
                     }
                     break;
                }
            }
            return true;
        } else {
             javax.swing.JOptionPane.showMessageDialog(this, "Book not found.", "Not Found", javax.swing.JOptionPane.INFORMATION_MESSAGE);
             clearBookFields();
             return false;
        }
    }

    private void populateBorrowedDropdown(int userId) {
        // Disable listener temporarily if needed, or just handle logic.
        // removeAllItems triggers events.
        jComboBox1.removeItemListener(jComboBox1.getItemListeners()[0]); // Remove to avoid events
        jComboBox1.removeAllItems();
        
        bookwise.DataAccess.Book[] books = bookwise.DataAccess.BookTransaction.getUnreturnedBooksByUser(userId);
        for (bookwise.DataAccess.Book b : books) {
            jComboBox1.addItem(b.getIsbn());
        }
        jComboBox1.setSelectedIndex(-1);
        
        // Add listener back
        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent e) {
                if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                     fetchBook();
                }
            }
        });
        
        clearBookFields(); // Ensure fields are empty
    }

    private void clearBookFields() {
         jTextField6.setText("");
         jTextField8.setText("");
         jTextField9.setText(""); 
         jLabel14.setText("0"); 
         jLabel15.setText("0.00"); 
         jComboBox1.setSelectedIndex(-1);
         setOverdueFieldsVisible(false);
    }

    private void setOverdueFieldsVisible(boolean visible) {
        if (jLabel11 != null) {
             jLabel11.setVisible(visible);
             jLabel14.setVisible(visible);
             jLabel12.setVisible(visible);
             jLabel15.setVisible(visible);
        }
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
        jComboBox1.setEnabled(enable);
    }
    
    private void checkProceedButtonVisibility() {
        boolean userConfirmed = jButton1.getText().equals("Edit");
        boolean bookConfirmed = jButton2.getText().equals("Edit");
        jButton3.setVisible(userConfirmed && bookConfirmed);
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
        jTextField4 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jTextField6 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jTextField8 = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();

        setBackground(new java.awt.Color(248, 248, 255));
        setPreferredSize(new java.awt.Dimension(1090, 649));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "User Information", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 24))); // NOI18N
        jPanel1.setPreferredSize(new java.awt.Dimension(400, 400));

        jTextField4.setEditable(false);
        jTextField4.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jTextField4.setFocusable(false);
        jTextField4.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jTextField4.setMaximumSize(new java.awt.Dimension(0, 0));
        jTextField4.setMinimumSize(new java.awt.Dimension(0, 0));
        jTextField4.setPreferredSize(new java.awt.Dimension(213, 29));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel4.setText("Email");
        jLabel4.setPreferredSize(new java.awt.Dimension(50, 21));

        jSpinner1.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jSpinner1.setPreferredSize(new java.awt.Dimension(213, 29));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel2.setText("ID No");
        jLabel2.setPreferredSize(new java.awt.Dimension(50, 21));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel1.setText("Name");
        jLabel1.setMaximumSize(new java.awt.Dimension(0, 0));
        jLabel1.setMinimumSize(new java.awt.Dimension(0, 0));
        jLabel1.setPreferredSize(new java.awt.Dimension(50, 21));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel3.setText("NIC No");

        jTextField3.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jTextField3.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jTextField3.setPreferredSize(new java.awt.Dimension(213, 29));

        jButton1.setBackground(new java.awt.Color(37, 56, 140));
        jButton1.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Confirm");
        jButton1.setFocusPainted(false);
        jButton1.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jButton1.setMaximumSize(new java.awt.Dimension(0, 0));
        jButton1.setMinimumSize(new java.awt.Dimension(0, 0));
        jButton1.setPreferredSize(new java.awt.Dimension(111, 41));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel6.setText("Address");
        jLabel6.setPreferredSize(new java.awt.Dimension(50, 21));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel5.setText("Mobile No");
        jLabel5.setPreferredSize(new java.awt.Dimension(50, 21));

        jTextField5.setEditable(false);
        jTextField5.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jTextField5.setFocusable(false);
        jTextField5.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jTextField5.setMaximumSize(new java.awt.Dimension(0, 0));
        jTextField5.setMinimumSize(new java.awt.Dimension(0, 0));
        jTextField5.setPreferredSize(new java.awt.Dimension(213, 35));

        jTextField1.setEditable(false);
        jTextField1.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jTextField1.setForeground(new java.awt.Color(30, 41, 50));
        jTextField1.setFocusable(false);
        jTextField1.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jTextField1.setMaximumSize(new java.awt.Dimension(0, 0));
        jTextField1.setMinimumSize(new java.awt.Dimension(0, 0));
        jTextField1.setPreferredSize(new java.awt.Dimension(213, 29));

        jTextField2.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jTextField2.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jTextField2.setMaximumSize(new java.awt.Dimension(0, 0));
        jTextField2.setMinimumSize(new java.awt.Dimension(0, 0));
        jTextField2.setPreferredSize(new java.awt.Dimension(213, 35));

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
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(50, 50, 50)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                                    .addComponent(jTextField2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jTextField3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jTextField4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jSpinner1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(50, 50, 50)
                                .addComponent(jTextField5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addGap(20, 20, 20))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Book Information", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 24))); // NOI18N
        jPanel2.setPreferredSize(new java.awt.Dimension(400, 400));

        jTextField6.setEditable(false);
        jTextField6.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jTextField6.setFocusable(false);
        jTextField6.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jTextField6.setPreferredSize(new java.awt.Dimension(213, 29));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel12.setText("Fine");

        jTextField9.setEditable(false);
        jTextField9.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jTextField9.setFocusable(false);
        jTextField9.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jTextField9.setPreferredSize(new java.awt.Dimension(213, 29));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel8.setText("ISBM  No");

        jButton2.setBackground(new java.awt.Color(37, 56, 140));
        jButton2.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("Confirm");
        jButton2.setFocusPainted(false);
        jButton2.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jButton2.setMaximumSize(new java.awt.Dimension(0, 0));
        jButton2.setMinimumSize(new java.awt.Dimension(0, 0));
        jButton2.setPreferredSize(new java.awt.Dimension(111, 141));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jTextField8.setEditable(false);
        jTextField8.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jTextField8.setFocusable(false);
        jTextField8.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jTextField8.setPreferredSize(new java.awt.Dimension(213, 29));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel9.setText("Category");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel10.setText("Borrow Date");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel7.setText("Title");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel11.setText("Days Overdue");

        jComboBox1.setBackground(new java.awt.Color(255, 255, 255));
        jComboBox1.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jComboBox1.setForeground(new java.awt.Color(40, 40, 40));

        jLabel14.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 0, 0));
        jLabel14.setText("0");

        jLabel15.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 0, 0));
        jLabel15.setText("0.00");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jTextField8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addGap(24, 24, 24)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jLabel14))
                .addGap(24, 24, 24)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 100, Short.MAX_VALUE)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabel13.setFont(new java.awt.Font("Segoe UI Semibold", 1, 24)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(30, 41, 59));
        jLabel13.setText("Return a Book");

        jButton3.setBackground(new java.awt.Color(30, 56, 140));
        jButton3.setFont(new java.awt.Font("Segoe UI Semibold", 1, 14)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setText("Proceed");
        jButton3.setFocusPainted(false);
        jButton3.setMargin(new java.awt.Insets(3, 3, 3, 3));
        jButton3.setMaximumSize(new java.awt.Dimension(0, 0));
        jButton3.setMinimumSize(new java.awt.Dimension(0, 0));
        jButton3.setPreferredSize(new java.awt.Dimension(111, 141));
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
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(50, 50, 50)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(70, 70, 70))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 510, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 510, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(50, 50, 50))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
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
        checkProceedButtonVisibility();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
       // TODO add your handling code here:
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
        checkProceedButtonVisibility();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
       // TODO add your handling code here:
        int userId = (Integer) jSpinner1.getValue();
        String isbn = (String) jComboBox1.getSelectedItem();

        if (userId <= 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Please confirm a valid user first.", "Invalid User", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (isbn == null || isbn.isEmpty()) {
             javax.swing.JOptionPane.showMessageDialog(this, "Please select a book to return.", "Invalid Book", javax.swing.JOptionPane.WARNING_MESSAGE);
             return;
        }

        bookwise.DataAccess.Book[] borrowedBooks = bookwise.DataAccess.BookTransaction.getUnreturnedBooksByUser(userId);
        bookwise.DataAccess.Book targetBook = null;
        for (bookwise.DataAccess.Book b : borrowedBooks) {
            if (b.getIsbn().equals(isbn)) {
                targetBook = b;
                break;
            }
        }

        if (targetBook == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Selected book transaction not found.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = bookwise.DataAccess.BookTransaction.updateReturn(targetBook.getTransactionId());
        if (success) {
             // Calculate details for email
            java.time.LocalDateTime borrowDate = targetBook.getBorrowDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
            bookwise.DataAccess.BookTransaction.ReturnDetails details = bookwise.DataAccess.BookTransaction.getReturnDetailsByBorrowDate(borrowDate);
            
            String dueDateStr = details.dueDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String userName = jTextField1.getText();
            String userEmail = jTextField3.getText();
            
            // Send Email
            new bookwise.Mails.BookReturnMail(userName, targetBook.getTitle(), details.daysOverdue, dueDateStr).send(userEmail);
            
            javax.swing.JOptionPane.showMessageDialog(this, "Book returned successfully!", "Success", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            
            // Refresh
            populateBorrowedDropdown(userId);
            jButton2.setText("Confirm");
            toggleBookFields(true);
            clearBookFields();
            checkProceedButtonVisibility();
            
        } else {
             javax.swing.JOptionPane.showMessageDialog(this, "Failed to return book.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
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
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    // End of variables declaration//GEN-END:variables
}
