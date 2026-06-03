import com.library.models.*;
import com.library.services.LibrarySystem;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class LibraryGUI extends Application {

    // Boot up the core backend (automatically loads my text files)
    private LibrarySystem system = new LibrarySystem();
    private User currentUser = null; // Keeps track of who is logged in
    private BookSearchEngine searchEngine = new BookSearchEngine();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Smart Library System");
        showLoginScreen(primaryStage);
        primaryStage.show();
    }

    //Login Screen
    private void showLoginScreen(Stage stage) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);

        Label title = new Label("Library Login");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField emailInput = new TextField();
        emailInput.setPromptText("Email (e.g. musa@uni.edu)");
        emailInput.setMaxWidth(250);

        PasswordField passInput = new PasswordField();
        passInput.setPromptText("Password");
        passInput.setMaxWidth(250);

        Button loginBtn = new Button("Login");
        Button signupBtn = new Button("Need an account? Sign Up");
        Label messageLabel = new Label();

        // The Login Logic
        loginBtn.setOnAction(e -> {
            String email = emailInput.getText();
            String pass = passInput.getText();
            boolean found = false;

            // Check Students
            for (User u : system.getRegisteredStudents()) {
                if (u.login(email, pass)) {
                    currentUser = u;
                    found = true;
                    break;
                }
            }
            // Check Staff if not a student
            if (!found) {
                for (User u : system.getRegisteredStaff()) {
                    if (u.login(email, pass)) {
                        currentUser = u;
                        found = true;
                        break;
                    }
                }
            }

            if (found) {
                showMainDashboard(stage);
            } else {
                messageLabel.setText("Invalid email or password!");
                messageLabel.setTextFill(Color.RED);
            }
        });

        signupBtn.setOnAction(e -> showSignUpScreen(stage));

        layout.getChildren().addAll(title, emailInput, passInput, loginBtn, signupBtn, messageLabel);
        stage.setScene(new Scene(layout, 400, 350));
    }

    //Sign Up screen
    private void showSignUpScreen(Stage stage) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);

        Label title = new Label("Create an Account");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Student", "Librarian");
        roleBox.setValue("Student"); // Default

        TextField nameInput = new TextField();
        nameInput.setPromptText("Full Name");
        nameInput.setMaxWidth(250);
        TextField emailInput = new TextField();
        emailInput.setPromptText("Email");
        emailInput.setMaxWidth(250);

        PasswordField passInput = new PasswordField();
        passInput.setPromptText("Password");
        passInput.setMaxWidth(250);
        PasswordField confirmPassInput = new PasswordField();
        confirmPassInput.setPromptText("Confirm Password");
        confirmPassInput.setMaxWidth(250);

        // Major Dropdown for Students
        ComboBox<String> majorCombo = new ComboBox<>();
        majorCombo.getItems().addAll("Computer Engineering", "Software Engineering", "Biomedical Engineering", "Cybersecurity", "Architecture", "Bachelor of Science in Clowning");
        majorCombo.setPromptText("Select Major");
        majorCombo.setMaxWidth(250);

        // Staff Number Input for Librarians
        TextField staffNumInput = new TextField();
        staffNumInput.setPromptText("Staff Number");
        staffNumInput.setMaxWidth(250);
        staffNumInput.setVisible(false);
        staffNumInput.setManaged(false);

        // StackPane to hold both fields in the exact same spot and toggle between them
        StackPane extraFieldPane = new StackPane();
        extraFieldPane.getChildren().addAll(majorCombo, staffNumInput);

        // Toggle logic based on role selection
        roleBox.setOnAction(e -> {
            boolean isStudent = roleBox.getValue().equals("Student");
            majorCombo.setVisible(isStudent);
            majorCombo.setManaged(isStudent);
            staffNumInput.setVisible(!isStudent);
            staffNumInput.setManaged(!isStudent);
        });

        Button registerBtn = new Button("Register");
        Button backBtn = new Button("Back to Login");
        Label messageLabel = new Label();

        // --- FThe Registration Logic  ---
        registerBtn.setOnAction(e -> {
            // 0. Block empty submissions right away
            if (nameInput.getText().trim().isEmpty() ||
                    emailInput.getText().trim().isEmpty() ||
                    passInput.getText().isEmpty()) {
                messageLabel.setText("Please fill out all fields!");
                messageLabel.setTextFill(Color.RED);
                return;
            }

            // 1. Check email for UNIQUENESS
            String checkEmail = emailInput.getText().trim();
            boolean emailExists = false;
            // Check if any student has this email
            for (User u : system.getRegisteredStudents()) {
                if (u.getEmail().equalsIgnoreCase(checkEmail)) {
                    emailExists = true;
                    break;
                }
            }
            // Check if any librarian has this email
            if (!emailExists) {
                for (User u : system.getRegisteredStaff()) {
                    if (u.getEmail().equalsIgnoreCase(checkEmail)) {
                        emailExists = true;
                        break;
                    }
                }
            }
            if (emailExists) {
                messageLabel.setText("Account with this email already exists! Try to login.");
                messageLabel.setTextFill(Color.RED);
                return; // Stop the registration process!
            }

            // 2. Block passwords that are too short or contain illegal characters
            String passwordText = passInput.getText();
            if (passwordText.trim().length() < 6) {
                messageLabel.setText("Password must be at least 6 characters!");
                messageLabel.setTextFill(Color.RED);
                return;
            }

            if (passwordText.contains("|") || passwordText.contains(",") ||
                    passwordText.contains("\\") || passwordText.contains("/")) {
                messageLabel.setText("Password cannot contain |, commas, or slashes!");
                messageLabel.setTextFill(Color.RED);
                return;
            }

            // 3. Check if passwords match
            if (!passInput.getText().equals(confirmPassInput.getText())) {
                messageLabel.setText("Passwords do not match!");
                messageLabel.setTextFill(Color.RED);
                return;
            }

            try {
                // Auto-generate a random 4-digit ID based on role
                String prefix = roleBox.getValue().equals("Student") ? "S" : "L";
                String generatedId = prefix + (int) (Math.random() * 9000 + 1000);

                if (roleBox.getValue().equals("Student")) {
                    String selectedMajor = majorCombo.getValue();
                    if (selectedMajor == null) {
                        messageLabel.setText("Please select a major!");
                        messageLabel.setTextFill(Color.RED);
                        return;
                    }

                    Student newStudent = new Student(
                            selectedMajor, nameInput.getText(), generatedId, emailInput.getText(), passInput.getText()
                    );
                    system.registerStudent(newStudent);
                } else {
                    if (staffNumInput.getText().trim().isEmpty()) {
                        messageLabel.setText("Please enter a Staff Number!");
                        messageLabel.setTextFill(Color.RED);
                        return;
                    }

                    Librarian newLibrarian = new Librarian(
                            nameInput.getText(), generatedId, emailInput.getText(), passInput.getText(), staffNumInput.getText()
                    );
                    system.registerStaff(newLibrarian);
                }

                system.saveSystemData();
                showLoginScreen(stage);

            } catch (Exception ex) {
                messageLabel.setText("Error creating account!");
                messageLabel.setTextFill(Color.RED);
            }
        });

        backBtn.setOnAction(e -> showLoginScreen(stage));

        layout.getChildren().addAll(title, roleBox, nameInput, emailInput, passInput, confirmPassInput, extraFieldPane, registerBtn, backBtn, messageLabel);
        stage.setScene(new Scene(layout, 400, 550));
    }


    // ---   THE MAIN TABS   ---
    private void showMainDashboard(Stage stage) {
        BorderPane mainLayout = new BorderPane();

        // Top bar with Sign Out button
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10, 20, 10, 20));
        topBar.setAlignment(Pos.CENTER_RIGHT);

        Button signOutBtn = new Button("Sign Out");
        signOutBtn.setStyle("-fx-background-color: #ff4c4c; -fx-text-fill: white;");
        signOutBtn.setOnAction(e -> {
            currentUser = null; // Clear the logged-in user
            showLoginScreen(stage); // Kick back to log in screen
        });

        topBar.getChildren().add(signOutBtn);

        // The tabs we already built
        TabPane root = new TabPane();
        root.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        if (currentUser instanceof Student) {
            root.getTabs().addAll(createStudentTab(), createAvailabilityTab(), createStudyRoomsTab());
        } else if (currentUser instanceof Librarian) {
            root.getTabs().addAll(createLibrarianTab(), createAvailabilityTab());
        }

        // --- CHECK FOR PENDING NOTIFICATIONS ---
        if (currentUser instanceof Student) {
            Student studentUser = (Student) currentUser;
            if (studentUser.hasPendingNotification()) {
                showNotification("Great news! Your reserved book is now available and has been added to your account.", stage);
                studentUser.setHasPendingNotification(false); // Turn it off so it doesn't spam them
                system.saveSystemData();
            }
        }
        // Put the top bar above the tabs
        mainLayout.setTop(topBar);
        mainLayout.setCenter(root);

        stage.setScene(new Scene(mainLayout, 800, 600));
        stage.centerOnScreen();
    }

    // --- THE  STUDENT PORTAL ---
    private Tab createStudentTab() {
        Tab tab = new Tab("Student Portal");
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        // --- STUDENT INFO ---
        Label welcomeLabel = new Label("Welcome, " + currentUser.getName() + "!");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Cast once cleanly since we know they are a Student
        Student studentUser = (Student) currentUser;
        Label infoLabel = new Label("ID: " + studentUser.getUserId() +
                " | Major: " + studentUser.getMajor());

        Label messageLabel = new Label();

        // --- ACCOUNT LISTS & BUTTONS ---
        HBox listsBox = new HBox(20);

        // Borrowed Section
        VBox borrowedBox = new VBox(10);
        Label borrowedLabel = new Label("My Borrowed Books:");
        ListView<String> borrowedList = new ListView<>();
        borrowedList.setPrefHeight(200);
        Button returnBtn = new Button("Return Selected");
        borrowedBox.getChildren().addAll(borrowedLabel, borrowedList, returnBtn);

        // Reserved Section
        VBox reservedBox = new VBox(10);
        Label reservedLabel = new Label("My Reserved Books:");
        ListView<String> reservedList = new ListView<>();
        reservedList.setPrefHeight(200);
        Button cancelBtn = new Button("Cancel Selected");
        reservedBox.getChildren().addAll(reservedLabel, reservedList, cancelBtn);

        listsBox.getChildren().addAll(borrowedBox, reservedBox);

        // --- DATA LOADER ---
        Runnable loadUserData = () -> {
            borrowedList.getItems().clear();
            reservedList.getItems().clear();

            for (Book b : studentUser.getBorrowedBooks()) {
                borrowedList.getItems().add("ID: " + b.getBookId() + " | " + b.getTitle());
            }
            if (borrowedList.getItems().isEmpty()) {
                borrowedList.getItems().add("You have no borrowed books.");
            }

            for (Book b : studentUser.getReservedBooks()) {
                reservedList.getItems().add("ID: " + b.getBookId() + " | " + b.getTitle());
            }
            if (reservedList.getItems().isEmpty()) {
                reservedList.getItems().add("You have no reserved books.");
            }
        };

        // --- BUTTON ACTIONS ---
        returnBtn.setOnAction(e -> {
            String selected = borrowedList.getSelectionModel().getSelectedItem();
            if (selected == null || !selected.startsWith("ID: ")) {
                messageLabel.setText("Please select a valid borrowed book!");
                messageLabel.setTextFill(Color.RED);
                return;
            }
            try {
                String bookId = selected.substring(4, selected.indexOf(" |"));
                system.returnBook(studentUser.getUserId(), bookId);
                system.saveSystemData();
                loadUserData.run();
                messageLabel.setText("Successfully returned " + bookId);
                messageLabel.setTextFill(Color.GREEN);
            } catch (Exception ex) {
                messageLabel.setText(ex.getMessage());
                messageLabel.setTextFill(Color.RED);
            }
        });

        cancelBtn.setOnAction(e -> {
            String selected = reservedList.getSelectionModel().getSelectedItem();
            if (selected == null || !selected.startsWith("ID: ")) {
                messageLabel.setText("Please select a valid reserved book!");
                messageLabel.setTextFill(Color.RED);
                return;
            }
            try {
                String bookId = selected.substring(4, selected.indexOf(" |"));

                // Assuming your cancelReservation method is still in LibrarySystem!
                system.cancelReservation(studentUser.getUserId(), bookId);
                system.saveSystemData();
                loadUserData.run();
                messageLabel.setText("Successfully cancelled reservation for " + bookId);
                messageLabel.setTextFill(Color.GREEN);
            } catch (Exception ex) {
                messageLabel.setText(ex.getMessage());
                messageLabel.setTextFill(Color.RED);
            }
        });

        // --- NOTIFICATION TOGGLE ---
        CheckBox notifToggle = new CheckBox("Enable Waitlist & System Notifications");
        notifToggle.setSelected(studentUser.isReceiveNotifications());

        notifToggle.setOnAction(e -> {
            boolean isEnabled = notifToggle.isSelected();
            studentUser.setReceiveNotifications(true); // Temp trick so popup bypasses check
            if (isEnabled) {
                showNotification("Notifications enabled!", (Stage) tab.getTabPane().getScene().getWindow());
            } else {
                showNotification("Notifications disabled!", (Stage) tab.getTabPane().getScene().getWindow());
            }
            studentUser.setReceiveNotifications(isEnabled);
            system.saveSystemData();
        });

        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) loadUserData.run();
        });
        loadUserData.run();

        // Add everything to the layout
        layout.getChildren().addAll(welcomeLabel, infoLabel, listsBox, messageLabel, new Separator(), notifToggle);
        tab.setContent(layout);

        return tab;
    }

    // --- THE  LIBRARIAN PORTAL ---
    private Tab createLibrarianTab() {
        Tab tab = new Tab("Librarian Dashboard");
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        // --- ADD NEW BOOK SECTION ---
        Label addLabel = new Label("Add New Book to Catalog");
        addLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        // Using GridPane to make the form look clean and organized
        javafx.scene.layout.GridPane addForm = new javafx.scene.layout.GridPane();
        addForm.setHgap(10);
        addForm.setVgap(10);


        TextField titleInput = new TextField();
        titleInput.setPromptText("Title");
        TextField authorInput = new TextField();
        authorInput.setPromptText("Author");

        ComboBox<String> categoryInput = new ComboBox<>();
        categoryInput.getItems().addAll("Fiction", "Education", "Computer Science", "Science", "History");
        categoryInput.setPromptText("Select Category");

        TextField copiesInput = new TextField();
        copiesInput.setPromptText("Copies (e.g. 5)");
        TextField yearInput = new TextField();
        yearInput.setPromptText("Year (e.g. 2024)");

        // Left Column (Removed Book ID)
        addForm.add(new Label("Title:"), 0, 0);
        addForm.add(titleInput, 1, 0);
        addForm.add(new Label("Author:"), 0, 1);
        addForm.add(authorInput, 1, 1);
        addForm.add(new Label("Category:"), 0, 2);
        addForm.add(categoryInput, 1, 2);

        // Right Column (Removed ISBN)
        addForm.add(new Label("Copies:"), 2, 0);
        addForm.add(copiesInput, 3, 0);
        addForm.add(new Label("Year:"), 2, 1);
        addForm.add(yearInput, 3, 1);

        Button addBtn = new Button("Add Book");
        Label messageLabel = new Label();

        addBtn.setOnAction(e -> {
            // Quick check to make sure they didn't leave critical fields blank
            if (titleInput.getText().isEmpty() || categoryInput.getValue() == null) {
                messageLabel.setText("Title, and Category are required!");
                messageLabel.setTextFill(Color.RED);
                return;
            }

            try {
                // Convert text to numbers
                int copies = Integer.parseInt(copiesInput.getText());
                int year = Integer.parseInt(yearInput.getText());

                // ---  YEAR VALIDATION ---
                int currentYear = java.time.Year.now().getValue();
                if (year > currentYear || year < 1000) {
                    messageLabel.setText("Invalid year! Must be between 1000 and " + currentYear + "!");
                    messageLabel.setTextFill(Color.RED);
                    return;
                }

                // --- DUPLICATE CHECK ---
                for (Book b : system.getCatalog()) {
                    if (b.getTitle().equalsIgnoreCase(titleInput.getText().trim()) &&
                            b.getAuthor().equalsIgnoreCase(authorInput.getText().trim())) {

                        messageLabel.setText("This exact book already exists in the catalog!");
                        messageLabel.setTextFill(Color.RED);
                        return;
                    }
                }

                // --- AUTO-GENERATE BOOK ID ---
                int maxId = 0;
                for (Book b : system.getCatalog()) {
                    try {
                        // Extract the number from previous book (e.g "B07")
                        int currentNum = Integer.parseInt(b.getBookId().replace("B", ""));
                        if (currentNum > maxId) {
                            maxId = currentNum;
                        }
                    } catch (Exception ignored) {
                    } // Ignores any weirdly formatted old IDs from previous versions
                }
                // Formats the next number to always have 2 digits (like: B08, B09, B10)
                String autoBookId = String.format("B%02d", maxId + 1);

                // --- AUTO-GENERATE UNIQUE ISBN ---
                String autoIsbn = "ISBN-" + (long) (Math.random() * 9000000000L + 1000000000L);

                // Create the book with mine auto-generated data
                Book newBook = new Book(
                        autoBookId, titleInput.getText(), authorInput.getText(),
                        categoryInput.getValue(), autoIsbn, copies, year
                );

                system.addBookToCatalog(newBook, (Librarian) currentUser);
                system.saveSystemData();

                messageLabel.setText("Successfully added! Assigned ID: " + autoBookId);
                messageLabel.setTextFill(Color.GREEN);

                // Clear the form
                titleInput.clear();
                authorInput.clear();
                copiesInput.clear();
                yearInput.clear();
                categoryInput.setValue(null);
            } catch (NumberFormatException ex) {
                messageLabel.setText("Copies and Year must be actual numbers!");
                messageLabel.setTextFill(Color.RED);
            }
        });

        // --- UPDATE INVENTORY SECTION ---
        Separator sep = new Separator();
        Label updateLabel = new Label("Update Existing Inventory");
        updateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        ListView<String> catalogList = new ListView<>();
        catalogList.setPrefHeight(150);

        // Auto-refresh the list
        Runnable loadCatalog = () -> {
            catalogList.getItems().clear();
            for (Book b : system.getCatalog()) {
                catalogList.getItems().add("ID: " + b.getBookId() + " | " + b.getTitle() + " | ISBN: " + b.getIsbn() + " (" + b.getAvailableCopies() + " available)");
            }
        };

        tab.setOnSelectionChanged(event -> {
            if (tab.isSelected()) loadCatalog.run();
        });
        loadCatalog.run();

        HBox updateBox = new HBox(10);
        updateBox.setAlignment(Pos.CENTER_LEFT);

        TextField qtyInput = new TextField();
        qtyInput.setPromptText("Qty");
        qtyInput.setPrefWidth(60);

        Button addQtyBtn = new Button("Add Copies");
        Button removeQtyBtn = new Button("Remove Copies");
        Button deleteBookBtn = new Button("Delete Book");
        deleteBookBtn.setStyle("-fx-background-color: #ff4c4c; -fx-text-fill: white;"); // Make it red so you don't click it by accident

        updateBox.getChildren().addAll(new Label("Modify Stock:"), qtyInput, addQtyBtn, removeQtyBtn, deleteBookBtn);
        // Logic for adding stock
        addQtyBtn.setOnAction(e -> {
            String selected = catalogList.getSelectionModel().getSelectedItem();
            if (selected == null || qtyInput.getText().isEmpty()) {
                messageLabel.setText("Select a book and enter a quantity!");
                messageLabel.setTextFill(Color.RED);
                return;
            }
            try {
                int qty = Integer.parseInt(qtyInput.getText());
                String bookId = selected.substring(4, selected.indexOf(" |"));

                for (Book b : system.getCatalog()) {
                    if (b.getBookId().equals(bookId)) {
                        b.setAvailableCopies(b.getAvailableCopies() + qty);
                        system.clearReservations(b.getBookId());
                        system.saveSystemData();
                        loadCatalog.run();
                        messageLabel.setText("Added " + qty + " copies to " + b.getTitle());
                        messageLabel.setTextFill(Color.GREEN);
                        qtyInput.clear();
                        break;
                    }
                }
            } catch (NumberFormatException ex) {
                messageLabel.setText("Quantity must be a number!");
                messageLabel.setTextFill(Color.RED);
            } catch (Exception ex) {
                messageLabel.setText(ex.getMessage());
                messageLabel.setTextFill(Color.RED);
            }
        });

        // Logic for removing stock
        removeQtyBtn.setOnAction(e -> {
            String selected = catalogList.getSelectionModel().getSelectedItem();
            if (selected == null || qtyInput.getText().isEmpty()) {
                messageLabel.setText("Select a book and enter a quantity!");
                messageLabel.setTextFill(Color.RED);
                return;
            }
            try {
                int qty = Integer.parseInt(qtyInput.getText());
                String bookId = selected.substring(4, selected.indexOf(" |"));

                for (Book b : system.getCatalog()) {
                    if (b.getBookId().equals(bookId)) {
                        if (b.getAvailableCopies() - qty < 0) {
                            messageLabel.setText("Cannot remove more copies than available!");
                            messageLabel.setTextFill(Color.RED);
                            return;
                        }
                        b.setAvailableCopies(b.getAvailableCopies() - qty);
                        system.saveSystemData();
                        loadCatalog.run();
                        messageLabel.setText("Removed " + qty + " copies from " + b.getTitle());
                        messageLabel.setTextFill(Color.GREEN);
                        qtyInput.clear();
                        break;
                    }
                }
            } catch (NumberFormatException ex) {
                messageLabel.setText("Quantity must be a number!");
                messageLabel.setTextFill(Color.RED);
            } catch (Exception ex) {
                messageLabel.setText(ex.getMessage());
                messageLabel.setTextFill(Color.RED);
            }
        });

        // Logic for annihilating a  book
        deleteBookBtn.setOnAction(e -> {
            String selected = catalogList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                messageLabel.setText("Select a book to delete first!");
                messageLabel.setTextFill(Color.RED);
                return;
            }
            try {
                // Extract the ID from the list string
                String bookId = selected.substring(4, selected.indexOf(" |"));

                // Call the backend method we just wrote
                system.removeBookFromSystem(bookId);

                system.saveSystemData();
                loadCatalog.run(); // Refresh the screen

                messageLabel.setText("Permanently deleted " + bookId + " from the catalog.");
                messageLabel.setTextFill(Color.GREEN);

            } catch (Exception ex) {
                // This will catch the error if a student is currently holding the book!
                messageLabel.setText(ex.getMessage());
                messageLabel.setTextFill(Color.RED);
            }
        });

        layout.getChildren().addAll(addLabel, addForm, addBtn, sep, updateLabel, catalogList, updateBox, messageLabel);
        tab.setContent(layout);
        return tab;
    }

    // --- THE  CATALOG PORTAL ---
    private Tab createAvailabilityTab() {
        Tab tab = new Tab("Availability & Search");
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        // --- SEARCH BAR UI ---
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchInput = new TextField();
        searchInput.setPromptText("Search books...");

        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.getItems().addAll("All", "Fiction", "Education", "Computer Science", "Science", "History");
        filterBox.setValue("All");

        // The Available only books checkbox
        CheckBox availableOnlyBox = new CheckBox("Available Only");
        Button searchBtn = new Button("Search");
        Button clearBtn = new Button("View All");
        searchBox.getChildren().addAll(new Label("Search:"), searchInput, filterBox, availableOnlyBox, searchBtn, clearBtn);

// --- CATALOG LIST ---
        ListView<String> displayList = new ListView<>();
        displayList.setPrefHeight(250);
        Label messageLabel = new Label();

        Runnable loadCatalog = () -> {
            displayList.getItems().clear();

            // Sorts using your teammate's engine
            java.util.List<Book> sortedCatalog = searchEngine.prioritizeAvailableBooks(system.getCatalog());

            // Formats everything cleanly
            for (Book b : sortedCatalog) {
                String status = b.getAvailableCopies() > 0 ? "(Available: " + b.getAvailableCopies() + ")" : "(Waitlist Only)";
                displayList.getItems().add("ID: " + b.getBookId() + " | " + b.getTitle() + " | ISBN: " + b.getIsbn() + " " + status);
            }
        };

        // The critical lines that were missing to make it actually load!
        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) loadCatalog.run();
        });
        loadCatalog.run();

        clearBtn.setOnAction(e -> {
            searchInput.clear();
            loadCatalog.run();
        });

        searchBtn.setOnAction(e -> {
            displayList.getItems().clear();

            String keyword = searchInput.getText().trim();
            String category = filterBox.getValue();

            // Start with the entire library catalog
            java.util.List<Book> foundBooks = system.getCatalog();

            // 1. Filter by Category
            if (category != null && !category.equals("All")) {
                foundBooks = searchEngine.filterByCategory(foundBooks, category);
            }

            // 2. Search by Keyword
            if (!keyword.isEmpty()) {
                java.util.List<Book> titleMatches = searchEngine.searchByTitle(foundBooks, keyword);
                java.util.List<Book> authorMatches = searchEngine.searchByAuthor(foundBooks, keyword);

                foundBooks = new java.util.ArrayList<>(titleMatches);
                for (Book b : authorMatches) {
                    if (!foundBooks.contains(b)) {
                        foundBooks.add(b);
                    }
                }
            }

            // 3. Filter out waitlist books if the box is checked
            if (availableOnlyBox.isSelected()) {
                foundBooks = searchEngine.findAvailableBooks(foundBooks);
            }

            // 4. Sort so available books are at the top
            foundBooks = searchEngine.prioritizeAvailableBooks(foundBooks);

            // 5. Display the results
            if (foundBooks.isEmpty()) {
                displayList.getItems().add("No books found matching your search.");
            } else {
                for (Book b : foundBooks) {
                    String status = b.getAvailableCopies() > 0 ? "(Available: " + b.getAvailableCopies() + ")" : "(Waitlist Only)";
                    displayList.getItems().add("ID: " + b.getBookId() + " | " + b.getTitle() + " by " + b.getAuthor() + " " + status);
                }
            }
        });
        // --- DYNAMIC ACTION MENU (Changes based on User) ---
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_LEFT);

        if (currentUser instanceof Student) {
            Button borrowBtn = new Button("Borrow ");
            Button reserveBtn = new Button("Reserve ");
            Button returnBtn = new Button("Return ");

            // --- BORROW LOGIC ---
            borrowBtn.setOnAction(e -> {
                String selected = displayList.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    messageLabel.setText("Please select a book first!");
                    messageLabel.setTextFill(Color.RED);
                    return;
                }

                try {
                    String bookId = selected.substring(4, selected.indexOf(" |"));

                    system.borrowBook(currentUser.getUserId(), bookId);
                    system.saveSystemData();
                    loadCatalog.run();

                    messageLabel.setText("Successfully borrowed!");
                    messageLabel.setTextFill(Color.GREEN);
                } catch (Exception ex) {
                    // Catches backend errors (like if they hit their borrow limit)
                    messageLabel.setText(ex.getMessage());
                    messageLabel.setTextFill(Color.RED);
                }
            });

            // --- RETURN LOGIC ---
            returnBtn.setOnAction(e -> {
                String selected = displayList.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    messageLabel.setText("Please select a book first!");
                    messageLabel.setTextFill(Color.RED);
                    return;
                }

                try {
                    String bookId = selected.substring(4, selected.indexOf(" |"));

                    system.returnBook(currentUser.getUserId(), bookId);
                    system.saveSystemData();
                    loadCatalog.run();

                    messageLabel.setText("Successfully returned!");
                    messageLabel.setTextFill(Color.GREEN);

                } catch (Exception ex) {
                    messageLabel.setText(ex.getMessage());
                    messageLabel.setTextFill(Color.RED);
                }
            });
            // --- RESERVE LOGIC ---
            reserveBtn.setOnAction(e -> {
                String selected = displayList.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    messageLabel.setText("Please select a book first!");
                    messageLabel.setTextFill(Color.RED);
                    return;
                }

                try {
                    String bookId = selected.substring(4, selected.indexOf(" |"));

                    // Pass the string ID
                    system.reserveBook(currentUser.getUserId(), bookId);
                    system.saveSystemData();
                    loadCatalog.run();

                    messageLabel.setText("Successfully reserved!");
                    messageLabel.setTextFill(Color.GREEN);

                } catch (Exception ex) {
                    // Catches custom errors like reserving an available book
                    messageLabel.setText(ex.getMessage());
                    messageLabel.setTextFill(Color.RED);
                }
            });
            actionBox.getChildren().addAll(borrowBtn, reserveBtn, returnBtn);

        } else if (currentUser instanceof Librarian) {
            TextField qtyInput = new TextField();
            qtyInput.setPromptText("Qty");
            qtyInput.setPrefWidth(60);
            Button addQtyBtn = new Button("Add Copies");
            Button removeQtyBtn = new Button("Remove Copies");

            // --- ADD COPIES LOGIC ---
            addQtyBtn.setOnAction(e -> {
                String selected = displayList.getSelectionModel().getSelectedItem();
                if (selected == null || qtyInput.getText().isEmpty()) {
                    messageLabel.setText("Select a book and enter a quantity!");
                    messageLabel.setTextFill(Color.RED);
                    return;
                }
                try {
                    int qty = Integer.parseInt(qtyInput.getText());
                    String bookId = selected.substring(4, selected.indexOf(" |"));

                    for (Book b : system.getCatalog()) {
                        if (b.getBookId().equals(bookId)) {
                            b.setAvailableCopies(b.getAvailableCopies() + qty);
                            system.clearReservations(b.getBookId());
                            system.saveSystemData();
                            loadCatalog.run();
                            messageLabel.setText("Added " + qty + " copies to " + b.getTitle());
                            messageLabel.setTextFill(Color.GREEN);
                            qtyInput.clear();
                            break;
                        }
                    }
                } catch (NumberFormatException ex) {
                    messageLabel.setText("Quantity must be a number!");
                    messageLabel.setTextFill(Color.RED);
                } catch (Exception ex) {
                    messageLabel.setText(ex.getMessage());
                    messageLabel.setTextFill(Color.RED);
                }
            });

            // --- REMOVE COPIES LOGIC ---
            removeQtyBtn.setOnAction(e -> {
                String selected = displayList.getSelectionModel().getSelectedItem();
                if (selected == null || qtyInput.getText().isEmpty()) {
                    messageLabel.setText("Select a book and enter a quantity!");
                    messageLabel.setTextFill(Color.RED);
                    return;
                }
                try {
                    int qty = Integer.parseInt(qtyInput.getText());
                    String bookId = selected.substring(4, selected.indexOf(" |"));

                    for (Book b : system.getCatalog()) {
                        if (b.getBookId().equals(bookId)) {
                            if (b.getAvailableCopies() - qty < 0) {
                                messageLabel.setText("Cannot remove more copies than available!");
                                messageLabel.setTextFill(Color.RED);
                                return;
                            }
                            b.setAvailableCopies(b.getAvailableCopies() - qty);
                            system.saveSystemData();
                            loadCatalog.run();
                            messageLabel.setText("Removed " + qty + " copies from " + b.getTitle());
                            messageLabel.setTextFill(Color.GREEN);
                            qtyInput.clear();
                            break;
                        }
                    }
                } catch (NumberFormatException ex) {
                    messageLabel.setText("Quantity must be a number!");
                    messageLabel.setTextFill(Color.RED);
                } catch (Exception ex) {
                    messageLabel.setText(ex.getMessage());
                    messageLabel.setTextFill(Color.RED);
                }
            });

            actionBox.getChildren().addAll(new Label("Modify Stock:"), qtyInput, addQtyBtn, removeQtyBtn);
        }

        layout.getChildren().addAll(searchBox, displayList, actionBox, messageLabel);
        tab.setContent(layout);
        return tab;
    }

    // --- THE  STUDY ROOM PORTAL ---
    private Tab createStudyRoomsTab() {
        Tab tab = new Tab("Study Rooms");
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        Label titleLabel = new Label("Reserve a Study Room");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        ListView<String> roomList = new ListView<>();
        roomList.setPrefHeight(200);
        Label messageLabel = new Label();

        Runnable loadRooms = () -> {
            roomList.getItems().clear();
            for (StudyRoom room : system.getRooms()) {
                // If it's booked by the current user, label it "Your Room"
                if (room.isBooked() && currentUser.getUserId().equals(room.getOccupantId())) {
                    roomList.getItems().add("Room " + room.getRoomNumber() + " (Your Room)");
                } else {
                    String status = room.isBooked() ? "(Booked)" : "(Open)";
                    roomList.getItems().add("Room " + room.getRoomNumber() + " " + status);
                }
            }
        };

        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) loadRooms.run();
        });
        loadRooms.run();

        HBox buttonBox = new HBox(10);
        Button bookRoomBtn = new Button("Book Selected Room");
        Button leaveRoomBtn = new Button("Leave My Room");
        buttonBox.getChildren().addAll(bookRoomBtn, leaveRoomBtn);

        bookRoomBtn.setOnAction(e -> {
            String selected = roomList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                messageLabel.setText("Please select a room from the list first!");
                messageLabel.setTextFill(Color.RED);
                return;
            }
            try {
                String roomNumStr = selected.split(" ")[1];
                int roomNum = Integer.parseInt(roomNumStr);

                system.bookStudyRoom(currentUser.getUserId(), roomNum);
                system.saveSystemData();
                loadRooms.run();

                messageLabel.setText("Successfully booked Room " + roomNum + "!");
                messageLabel.setTextFill(Color.GREEN);
            } catch (Exception ex) {
                messageLabel.setText(ex.getMessage());
                messageLabel.setTextFill(Color.RED);
            }
        });

        leaveRoomBtn.setOnAction(e -> {
            try {
                system.leaveStudyRoom(currentUser.getUserId());
                system.saveSystemData();
                loadRooms.run();

                messageLabel.setText("Successfully left your study room.");
                messageLabel.setTextFill(Color.GREEN);
            } catch (Exception ex) {
                messageLabel.setText(ex.getMessage());
                messageLabel.setTextFill(Color.RED);
            }
        });

        layout.getChildren().addAll(titleLabel, roomList, buttonBox, messageLabel);
        tab.setContent(layout);
        return tab;
    }

    // --- THE CUSTOM NOTIFICATION SYSTEM ---
    private void showNotification(String message, Stage stage) {
        // Only show if the user has notifications enabled
        if (currentUser != null && !currentUser.isReceiveNotifications()) {
            return;
        }

        Label toast = new Label(message);
        toast.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-padding: 10px; -fx-background-radius: 5px;");
        Popup popup = new Popup();
        popup.getContent().add(toast);
        popup.setAutoHide(true);

        // Position it at the top right of the window
        popup.show(stage, stage.getX() + stage.getWidth() - 250, stage.getY() + 70);

        // Hide it after 3 seconds
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
        delay.setOnFinished(e -> popup.hide());
        delay.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}