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

        // We will build the Sign-Up screen next!
        signupBtn.setOnAction(e -> {
            showSignUpScreen(stage);
        });

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

        registerBtn.setOnAction(e -> {
            // 0. Block empty submissions right away
            if (nameInput.getText().trim().isEmpty() ||
                    emailInput.getText().trim().isEmpty() ||
                    passInput.getText().isEmpty()) {
                messageLabel.setText("Please fill out all fields!");
                messageLabel.setTextFill(Color.RED);
                return;
            }

            // 1. Block passwords that are too short
            if (passInput.getText().length() < 6) {
                messageLabel.setText("Password must be at least 6 characters!");
                messageLabel.setTextFill(Color.RED);
                return;
            }

            // 2. Check if passwords match
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
            root.getTabs().addAll(createStudentTab(), createAvailabilityTab());
        } else if (currentUser instanceof Librarian) {
            root.getTabs().addAll(createLibrarianTab(), createAvailabilityTab());
        }

        // Put the top bar above the tabs
        mainLayout.setTop(topBar);
        mainLayout.setCenter(root);

        stage.setScene(new Scene(mainLayout, 800, 600));
        stage.centerOnScreen();
    }

    // ---   THE MAIN TABS   ---
    // --- THE  STUDENT PORTAL ---
    private Tab createStudentTab() {
        Tab tab = new Tab("Student Portal");
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        // Teammate's Pending Search UI
        HBox searchBar = new HBox(10);
        TextField searchInput = new TextField();
        searchInput.setPromptText("Search Title/Author");
        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.getItems().addAll("All", "Fiction", "Education", "Computer Science");
        filterBox.setValue("All");
        Button searchBtn = new Button("Search");
        searchBar.getChildren().addAll(searchInput, filterBox, searchBtn);
        ListView<String> searchResultsList = new ListView<>();
        searchResultsList.setPrefHeight(150); // Keeps it from taking up the whole screen

        searchBtn.setOnAction(e -> {
            searchResultsList.getItems().clear(); // Clear the old search results

            String keyword = searchInput.getText().trim();
            String category = filterBox.getValue();

            // Start with the entire library catalog
            java.util.List<com.library.models.Book> foundBooks = system.getCatalog();

            // 1. Filter by Category (if they didn't select "All")
            if (category != null && !category.equals("All")) {
                foundBooks = searchEngine.filterByCategory(foundBooks, category);
            }

            // 2. Search by Keyword (Check both Title AND Author)
            if (!keyword.isEmpty()) {
                java.util.List<com.library.models.Book> titleMatches = searchEngine.searchByTitle(foundBooks, keyword);
                java.util.List<com.library.models.Book> authorMatches = searchEngine.searchByAuthor(foundBooks, keyword);

                // Merge the two lists so we don't miss anything (avoiding duplicates)
                foundBooks = new java.util.ArrayList<>(titleMatches);
                for (com.library.models.Book b : authorMatches) {
                    if (!foundBooks.contains(b)) {
                        foundBooks.add(b);
                    }
                }
            }

            // 3. Flex Suhail's Sorting Method: Put available books at the top!
            foundBooks = searchEngine.prioritizeAvailableBooks(foundBooks);

            // 4. Display the results in the ListView
            if (foundBooks.isEmpty()) {
                searchResultsList.getItems().add("No books found matching your search.");
            } else {
                for (com.library.models.Book b : foundBooks) {
                    String status = b.getAvailableCopies() > 0 ? "(Available: " + b.getAvailableCopies() + ")" : "(Waitlist Only)";
                    searchResultsList.getItems().add("ID: " + b.getBookId() + " | " + b.getTitle() + " by " + b.getAuthor() + " " + status);
                }
            }
        });

        // Action UI
        // --- My Borrowed Books UI ---
        Label myBooksLabel = new Label("My Borrowed Books");
        ListView<String> borrowedList = new ListView<>();
        borrowedList.setPrefHeight(150);
        Label messageLabel = new Label();

        Runnable loadMyBooks = () -> {
            borrowedList.getItems().clear();
            com.library.models.Student student = (com.library.models.Student) currentUser;
            for (com.library.models.Book b : student.getBorrowedBooks()) {
                borrowedList.getItems().add("ID: " + b.getBookId() + " | " + b.getTitle());
            }
            if (borrowedList.getItems().isEmpty()) {
                borrowedList.getItems().add("You have no borrowed books.");
            }
        };

        // Auto-refresh the list whenever they click the Student Tab
        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) loadMyBooks.run();
        });
        loadMyBooks.run();

        Button returnBtn = new Button("Return Selected Book");
        HBox returnAction = new HBox(10, returnBtn);
        returnAction.setAlignment(Pos.CENTER_LEFT);

        returnBtn.setOnAction(e -> {
            String selected = borrowedList.getSelectionModel().getSelectedItem();
            if (selected == null || !selected.startsWith("ID: ")) {
                messageLabel.setText("Please select a valid book to return!");
                messageLabel.setTextFill(Color.RED);
                return;
            }
            try {
                String bookId = selected.substring(4, selected.indexOf(" |"));
                system.returnBook(currentUser.getUserId(), bookId);
                system.saveSystemData();
                loadMyBooks.run(); // Instantly refresh their personal list
                messageLabel.setText("Successfully returned " + bookId);
                messageLabel.setTextFill(Color.GREEN);
                showNotification("System checked for waitlist transfers.", (Stage) tab.getTabPane().getScene().getWindow());
            } catch (Exception ex) {
                messageLabel.setText(ex.getMessage());
                messageLabel.setTextFill(Color.RED);
            }
        });
        // --- Study Room Actions ---
        ComboBox<Integer> roomCombo = new ComboBox<>();
        roomCombo.getItems().addAll(1, 2, 3, 4, 5);
        roomCombo.setPromptText("Room #");

        Button bookRoomBtn = new Button("Book Room");
        HBox roomActions = new HBox(10, new Label("Book Study Room:"), roomCombo, bookRoomBtn);
        roomActions.setAlignment(Pos.CENTER_LEFT);

        bookRoomBtn.setOnAction(e -> {
            if (roomCombo.getValue() == null) {
                messageLabel.setText("Please select a room number!");
                messageLabel.setTextFill(Color.RED);
                return;
            }
            try {
                system.bookStudyRoom(currentUser.getUserId(), roomCombo.getValue());
                system.saveSystemData();
                messageLabel.setText("Successfully booked Room " + roomCombo.getValue());
                messageLabel.setTextFill(Color.GREEN);
            } catch (Exception ex) {
                messageLabel.setText(ex.getMessage());
                messageLabel.setTextFill(Color.RED);
            }
        });

        // Notification Settings Toggle
        CheckBox notifToggle = new CheckBox("Enable Waitlist & System Notifications");
        notifToggle.setSelected(currentUser.isReceiveNotifications()); // Set to current status

        notifToggle.setOnAction(e -> {
            boolean isEnabled = notifToggle.isSelected();

            // 1. Temporarily trick the system into allowing our popup through
            currentUser.setReceiveNotifications(true);

            // 2. Show the correct message
            if (isEnabled) {
                showNotification("Notifications enabled!", (Stage) tab.getTabPane().getScene().getWindow());
            } else {
                showNotification("Notifications disabled!", (Stage) tab.getTabPane().getScene().getWindow());
            }

            // 3. Set the actual preference the user chose and save it to the text file
            currentUser.setReceiveNotifications(isEnabled);
            system.saveSystemData();
        });
        layout.getChildren().addAll(
                new Label("Search Catalog"), searchBar,searchResultsList,
                new Separator(),
                myBooksLabel, borrowedList, returnAction,
                new Separator(),
                roomActions,
                messageLabel,
                new Separator(),
                notifToggle
        );

        tab.setContent(layout);
        return tab;
    }

    private Tab createLibrarianTab() {
        Tab tab = new Tab("Librarian Dashboard");
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.getChildren().add(new Label("Librarian features will go here."));
        tab.setContent(layout);
        return tab;
    }

    private Tab createAvailabilityTab() {
        Tab tab = new Tab("Availability");
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        ComboBox<String> viewSelector = new ComboBox<>();
        viewSelector.getItems().addAll("Books", "Study Rooms");
        viewSelector.setValue("Books");

        ListView<String> displayList = new ListView<>();
        Button refreshBtn = new Button("Refresh Data");

        // The logic to pull live data from your system
        Runnable loadData = () -> {
            displayList.getItems().clear();

            if (viewSelector.getValue().equals("Books")) {
                // Assuming your system has a getCatalog() method
                for ( Book b : system.getCatalog()) {
                    if (b.getAvailableCopies() > 0) {
                        displayList.getItems().add(
                                "ID: " + b.getBookId() + " | " + b.getTitle() + " (" + b.getAvailableCopies() + " available)"
                        );
                    } else {
                        displayList.getItems().add(
                                "ID: " + b.getBookId() + " | " + b.getTitle() + " (OUT OF STOCK - Reserve Only)"
                        );
                    }
                }
                if (displayList.getItems().isEmpty()) {
                    displayList.getItems().add("No books currently available.");
                }
            } else {
                // Assuming your system has a getRooms() method
                for (StudyRoom r : system.getRooms()) {
                    String status = r.isBooked() ? "Booked" : "Open";
                    displayList.getItems().add(
                            "Room " + r.getRoomNumber() + " - Status: " + status
                    );
                }
            }
        };

        // Trigger data load when dropdown changes or refresh is clicked
        viewSelector.setOnAction(e -> loadData.run());
        refreshBtn.setOnAction(e -> loadData.run());

        // Auto-refresh every time the user clicks this tab
        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) {
                loadData.run();
            }
        });

        // --- Clickable Actions for Students ---
        Label messageLabel = new Label();
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_LEFT);

        if (currentUser instanceof Student) {
            Button borrowBtn = new Button("Borrow ");
            Button reserveBtn = new Button("Reserve ");

            borrowBtn.setOnAction(e -> {
                String selected = displayList.getSelectionModel().getSelectedItem();
                if (selected == null || !selected.startsWith("ID: ")) {
                    messageLabel.setText("Please select a book from the list!");
                    messageLabel.setTextFill(Color.RED);
                    return;
                }
                try {
                    // Extract the ID from our string format "ID: B01 | Title..."
                    String bookId = selected.substring(4, selected.indexOf(" |"));
                    system.borrowBook(currentUser.getUserId(), bookId);
                    system.saveSystemData();
                    loadData.run(); // Instantly refresh the list!
                    messageLabel.setText("Successfully borrowed " + bookId);
                    messageLabel.setTextFill(Color.GREEN);
                } catch (Exception ex) {
                    messageLabel.setText(ex.getMessage());
                    messageLabel.setTextFill(Color.RED);
                }
            });

            reserveBtn.setOnAction(e -> {
                String selected = displayList.getSelectionModel().getSelectedItem();
                if (selected == null || !selected.startsWith("ID: ")) {
                    messageLabel.setText("Please select a book from the list!");
                    messageLabel.setTextFill(Color.RED);
                    return;
                }
                try {
                    String bookId = selected.substring(4, selected.indexOf(" |"));
                    system.reserveBook(currentUser.getUserId(), bookId);
                    system.saveSystemData();
                    loadData.run();
                    messageLabel.setText("Successfully reserved " + bookId);
                    messageLabel.setTextFill(Color.GREEN);
                } catch (Exception ex) {
                    messageLabel.setText(ex.getMessage());
                    messageLabel.setTextFill(Color.RED);
                }
            });

            actionBox.getChildren().addAll(borrowBtn, reserveBtn);
        }

        // Load it once right at the start
        loadData.run();

        HBox controls = new HBox(10, new Label("Select what to view:"), viewSelector, refreshBtn);
        controls.setAlignment(Pos.CENTER_LEFT);

        layout.getChildren().addAll(controls, displayList, actionBox, messageLabel);
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