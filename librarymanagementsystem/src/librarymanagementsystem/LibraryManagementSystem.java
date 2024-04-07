package librarymanagementsystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibraryManagementSystem {
    private JFrame frame;
    private Connection connection;
    private JPanel displayPanel;

    public LibraryManagementSystem() {
        frame = new JFrame();
        frame.setTitle("Library Management System");
        frame.setSize(900, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // Load icon image
        ImageIcon icon = new ImageIcon("icon.png");
        frame.setIconImage(icon.getImage());

        connectToDatabase();

        createMenuBar();
        createMainPanel();
        frame.setVisible(true);
    }

    private void connectToDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            String jdbcUrl = "jdbc:sqlite:library.db";
            connection = DriverManager.getConnection(jdbcUrl);

            createTables();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error connecting to the database: " + e.getMessage());
        }
    }

    private void createTables() {
        String createBookTableQuery = "CREATE TABLE IF NOT EXISTS books (id INTEGER PRIMARY KEY, " +
                "title TEXT, author_full_name TEXT, subject_category TEXT, version_number INTEGER, " +
                "year_of_publish INTEGER)";
        try (PreparedStatement createBookTableStatement = connection.prepareStatement(createBookTableQuery)) {
            createBookTableStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error creating table: " + e.getMessage());
        }
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(160, 160, 160));

        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        fileMenu.setForeground(Color.CYAN);
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        frame.setJMenuBar(menuBar);
    }

    private void createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        displayPanel = new JPanel();
        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 32, 12));

        JButton insertButton = new JButton("Insert Book");
        insertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                insertBook();
            }
        });
        customizeButton(insertButton);

        JButton displayButton = new JButton("Display Books");
        displayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayBooks();
            }
        });
        customizeButton(displayButton);

        JButton modifyButton = new JButton("Modify Book");
        modifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modifyBook();
            }
        });
        customizeButton(modifyButton);

        JButton deleteButton = new JButton("Delete Book");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteBook();
            }
        });
        customizeButton(deleteButton);

        buttonPanel.add(insertButton);
        buttonPanel.add(displayButton);
        buttonPanel.add(modifyButton);
        buttonPanel.add(deleteButton);

        frame.add(buttonPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(displayPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        frame.add(mainPanel, BorderLayout.CENTER);
    }
    
    private void customizeButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(Color.LIGHT_GRAY);
        button.setForeground(Color.BLACK);
}

    private void insertBook() {
        JTextField titleField = new JTextField(20);
        JTextField authorFullNameField = new JTextField(20);
        JTextField subjectCategoryField = new JTextField(20);
        JTextField versionNumberField = new JTextField(5);
        JTextField yearOfPublishField = new JTextField(5);

        JPanel insertPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        insertPanel.add(new JLabel("Title:"));
        insertPanel.add(titleField);
        insertPanel.add(new JLabel("Author's Full Name:"));
        insertPanel.add(authorFullNameField);
        insertPanel.add(new JLabel("Subject Category:"));
        insertPanel.add(subjectCategoryField);
        insertPanel.add(new JLabel("Version Number:"));
        insertPanel.add(versionNumberField);
        insertPanel.add(new JLabel("Year of Publish:"));
        insertPanel.add(yearOfPublishField);

        int result = JOptionPane.showConfirmDialog(null, insertPanel,
                "Insert Book", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText();
            String authorFullName = authorFullNameField.getText();
            String subjectCategory = subjectCategoryField.getText();
            String versionNumberStr = versionNumberField.getText();
            String yearOfPublishStr = yearOfPublishField.getText();

            if (title.isEmpty() || authorFullName.isEmpty() || subjectCategory.isEmpty() ||
                    versionNumberStr.isEmpty() || yearOfPublishStr.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields are required.");
                return;
            }

            try {
                double versionNumber = Double.parseDouble(versionNumberStr);
                int yearOfPublish = Integer.parseInt(yearOfPublishStr);

                String insertBookQuery = "INSERT INTO books (title, author_full_name, subject_category, " +
                        "version_number, year_of_publish) VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement insertBookStatement = connection.prepareStatement(insertBookQuery)) {
                    insertBookStatement.setString(1, title);
                    insertBookStatement.setString(2, authorFullName);
                    insertBookStatement.setString(3, subjectCategory);
                    insertBookStatement.setDouble(4, versionNumber);
                    insertBookStatement.setInt(5, yearOfPublish);

                    insertBookStatement.executeUpdate();
                    JOptionPane.showMessageDialog(frame, "Book inserted successfully!");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Please enter valid numeric values for version number and year of publish.");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error inserting book.");
            }
        }
    }
private void displayBooks() {
    displayPanel.removeAll();

    try {
        String selectBooksQuery = "SELECT * FROM books";
        try (PreparedStatement selectBooksStatement = connection.prepareStatement(selectBooksQuery);
             ResultSet resultSet = selectBooksStatement.executeQuery()) {

            while (resultSet.next()) {
                StringBuilder bookInfo = new StringBuilder();
                bookInfo.append("ID: ").append(resultSet.getInt("id")).append("\n")
                        .append("Title: ").append(resultSet.getString("title")).append("\n")
                        .append("Author: ").append(resultSet.getString("author_full_name")).append("\n")
                        .append("Subject: ").append(resultSet.getString("subject_category")).append("\n")
                        .append("Version: ").append(resultSet.getDouble("version_number")).append("\n")
                        .append("Year: ").append(resultSet.getInt("year_of_publish")).append("\n\n");

                JTextArea bookTextArea = new JTextArea(bookInfo.toString());
                bookTextArea.setEditable(false);
                bookTextArea.setOpaque(false);
                bookTextArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
                bookTextArea.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.GRAY, 1),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Add padding
                bookTextArea.setMargin(new Insets(10, 10, 10, 10));

                displayPanel.add(bookTextArea);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(frame, "Error displaying books.");
    }

    frame.revalidate();
    frame.repaint();
}

  
    private void modifyBook() {
    int selectedBookId = getSelectedBookId();
    if (selectedBookId != -1) {
        JTextField newTitleField = new JTextField(20);
        JTextField newAuthorFullNameField = new JTextField(20);
        JTextField newSubjectCategoryField = new JTextField(20);
        JTextField newVersionNumberField = new JTextField(5);
        JTextField newYearOfPublishField = new JTextField(5);

        JPanel modifyPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        modifyPanel.add(new JLabel("New Title:"));
        modifyPanel.add(newTitleField);
        modifyPanel.add(new JLabel("New Author's Full Name:"));
        modifyPanel.add(newAuthorFullNameField);
        modifyPanel.add(new JLabel("New Subject Category:"));
        modifyPanel.add(newSubjectCategoryField);
        modifyPanel.add(new JLabel("New Version Number:"));
        modifyPanel.add(newVersionNumberField);
        modifyPanel.add(new JLabel("New Year of Publish:"));
        modifyPanel.add(newYearOfPublishField);

        int result = JOptionPane.showConfirmDialog(null, modifyPanel,
                "Modify Book", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String newTitle = newTitleField.getText();
            String newAuthorFullName = newAuthorFullNameField.getText();
            String newSubjectCategory = newSubjectCategoryField.getText();
            String newVersionNumberStr = newVersionNumberField.getText();
            String newYearOfPublishStr = newYearOfPublishField.getText();
            
            if (newTitle != null && !newTitle.isEmpty() &&
                    newAuthorFullName != null && !newAuthorFullName.isEmpty() &&
                    newSubjectCategory != null && !newSubjectCategory.isEmpty() &&
                    newVersionNumberStr != null && !newVersionNumberStr.isEmpty() &&
                    newYearOfPublishStr != null && !newYearOfPublishStr.isEmpty()) {

                int newVersionNumber = Integer.parseInt(newVersionNumberStr);
                int newYearOfPublish = Integer.parseInt(newYearOfPublishStr);

                try {
                    String updateBookQuery = "UPDATE books SET title = ?, author_full_name = ?, " +
                            "subject_category = ?, version_number = ?, year_of_publish = ? WHERE id = ?";

                    try (PreparedStatement updateBookStatement = connection.prepareStatement(updateBookQuery)) {
                        updateBookStatement.setString(1, newTitle);
                        updateBookStatement.setString(2, newAuthorFullName);
                        updateBookStatement.setString(3, newSubjectCategory);
                        updateBookStatement.setInt(4, newVersionNumber);
                        updateBookStatement.setInt(5, newYearOfPublish);
                        updateBookStatement.setInt(6, selectedBookId);

                        updateBookStatement.executeUpdate();
                        JOptionPane.showMessageDialog(frame, "Book modified successfully!");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error modifying book.");
                }
        }
    } else {
        JOptionPane.showMessageDialog(frame, "No book selected.");
    }
}
    }

private void deleteBook() {
    int selectedBookId = getSelectedBookId();
    if (selectedBookId != -1) {
        try {
            String deleteBookQuery = "DELETE FROM books WHERE id = ?";

            try (PreparedStatement deleteBookStatement = connection.prepareStatement(deleteBookQuery)) {
                deleteBookStatement.setInt(1, selectedBookId);

                deleteBookStatement.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Book deleted successfully!");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error deleting book.");
        }
    } else {
        JOptionPane.showMessageDialog(frame, "No book selected.");
    }
}


private int getSelectedBookId() {
    String[] options = getBookTitles();

    if (options.length == 0) {
        JOptionPane.showMessageDialog(frame, "No books found.");
        return -1;
    }

    String selectedBookTitle = (String) JOptionPane.showInputDialog(
            frame,
            "Select book to modify/delete:",
            "Select Book",
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);

    try {
        String selectBookIdQuery = "SELECT id FROM books WHERE title = ?";
        try (PreparedStatement selectBookIdStatement = connection.prepareStatement(selectBookIdQuery)) {
            selectBookIdStatement.setString(1, selectedBookTitle);
            ResultSet resultSet = selectBookIdStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return -1;
}

    private String[] getBookTitles() {
    List<String> titleList = new ArrayList<>();

    try {
        String selectTitlesQuery = "SELECT title FROM books";
        try (PreparedStatement selectTitlesStatement = connection.prepareStatement(selectTitlesQuery);
             ResultSet resultSet = selectTitlesStatement.executeQuery()) {

            while (resultSet.next()) {
                titleList.add(resultSet.getString("title"));
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return titleList.toArray(new String[0]);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LibraryManagementSystem();
            }
        });
    }
}
