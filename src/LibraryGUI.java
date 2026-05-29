import com.library.models.User;
import com.library.services.LibrarySystem;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Popup;

public class LibraryGUI extends Application {

    // Boot up the core backend (automatically loads my text files)
    private LibrarySystem system = new LibrarySystem();
    private User currentUser = null; // Keeps track of who is logged in

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

        // We will build the Sign Up screen next!
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

        TextField nameInput = new TextField(); nameInput.setPromptText("Full Name"); nameInput.setMaxWidth(250);
        TextField emailInput = new TextField(); emailInput.setPromptText("Email"); emailInput.setMaxWidth(250);

        PasswordField passInput = new PasswordField(); passInput.setPromptText("Password"); passInput.setMaxWidth(250);
        PasswordField confirmPassInput = new PasswordField(); confirmPassInput.setPromptText("Confirm Password"); confirmPassInput.setMaxWidth(250);

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
                String generatedId = prefix + (int)(Math.random() * 9000 + 1000);

                if (roleBox.getValue().equals("Student")) {
                    String selectedMajor = majorCombo.getValue();
                    if (selectedMajor == null) {
                        messageLabel.setText("Please select a major!");
                        messageLabel.setTextFill(Color.RED);
                        return;
                    }

                    com.library.models.Student newStudent = new com.library.models.Student(
                            selectedMajor, nameInput.getText(), generatedId, emailInput.getText(), passInput.getText()
                    );
                    system.registerStudent(newStudent);
                } else {
                    if (staffNumInput.getText().trim().isEmpty()) {
                        messageLabel.setText("Please enter a Staff Number!");
                        messageLabel.setTextFill(Color.RED);
                        return;
                    }

                    com.library.models.Librarian newLibrarian = new com.library.models.Librarian(
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
            showLoginScreen(stage); // Kick back to login screen
        });

        topBar.getChildren().add(signOutBtn);

        // The tabs we already built
        TabPane root = new TabPane();
        root.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        if (currentUser instanceof com.library.models.Student) {
            root.getTabs().addAll(createStudentTab(), createAvailabilityTab());
        } else if (currentUser instanceof com.library.models.Librarian) {
            root.getTabs().addAll(createLibrarianTab(), createAvailabilityTab());
        }

        // Put the top bar above the tabs
        mainLayout.setTop(topBar);
        mainLayout.setCenter(root);

        stage.setScene(new Scene(mainLayout, 800, 600));
        stage.centerOnScreen();
    }

    // ---   THE MAIN TABS   ---

    // --- THE WIRED-UP STUDENT PORTAL ---
    private Tab createStudentTab() {
        Tab tab = new Tab("Student Portal");
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));

        // Teammate's Pending Search UI
        HBox searchBar = new HBox(10);
        TextField searchInput = new TextField(); searchInput.setPromptText("Search Title/Author");
        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.getItems().addAll("All", "Fiction", "Education", "Computer Science");
        filterBox.setValue("All");
        Button searchBtn = new Button("Search");
        searchBar.getChildren().addAll(searchInput, filterBox, searchBtn);

        searchBtn.setOnAction(e -> {
            showNotification("Waiting on Search Engine from teammates!", (Stage) tab.getTabPane().getScene().getWindow());
        });

        // Action UI
        TextField bookIdInput = new TextField();
        bookIdInput.setPromptText("Enter Book ID (e.g. B01)");

        Button borrowBtn = new Button("Borrow");
        Button returnBtn = new Button("Return");
        Button reserveBtn = new Button("Reserve");

        HBox actionButtons = new HBox(10, borrowBtn, returnBtn, reserveBtn);

        Label messageLabel = new Label();

        // Borrow Logic
        borrowBtn.setOnAction(e -> {
            try {
                system.borrowBook(currentUser.getUserId(), bookIdInput.getText());
                system.saveSystemData();
                messageLabel.setText("Successfully borrowed " + bookIdInput.getText());
                messageLabel.setTextFill(Color.GREEN);
            } catch (Exception ex) {
                messageLabel.setText(ex.getMessage());
                messageLabel.setTextFill(Color.RED);
            }
        });

        // Return Logic (This is where the magic waitlist transfer happens!)
        returnBtn.setOnAction(e -> {
            try {
                system.returnBook(currentUser.getUserId(), bookIdInput.getText());
                system.saveSystemData();
                messageLabel.setText("Successfully returned " + bookIdInput.getText());
                messageLabel.setTextFill(Color.GREEN);

                // Trigger a notification just in case it auto-transferred
                showNotification("System checked for waitlist transfers.", (Stage) tab.getTabPane().getScene().getWindow());
            } catch (Exception ex) {
                messageLabel.setText(ex.getMessage());
                messageLabel.setTextFill(Color.RED);
            }
        });

        // Reserve Logic
        reserveBtn.setOnAction(e -> {
            try {
                system.reserveBook(currentUser.getUserId(), bookIdInput.getText());
                system.saveSystemData();
                messageLabel.setText("Successfully reserved " + bookIdInput.getText());
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
                new Label("Search Catalog"), searchBar,
                new Separator(),
                new Label("Book Actions (Enter ID)"), bookIdInput, actionButtons, messageLabel,
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
                for (com.library.models.Book b : system.getCatalog()) {
                    if (b.getAvailableCopies() > 0) {
                        displayList.getItems().add(
                                "ID: " + b.getBookId() + " | " + b.getTitle() + " (" + b.getAvailableCopies() + " available)"
                        );
                    }
                }
                if (displayList.getItems().isEmpty()) {
                    displayList.getItems().add("No books currently available.");
                }
            } else {
                // Assuming your system has a getRooms() method
                for (com.library.models.StudyRoom r : system.getRooms()) {
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

        // Load it once right at the start
        loadData.run();

        HBox controls = new HBox(10, new Label("Select what to view:"), viewSelector, refreshBtn);
        controls.setAlignment(Pos.CENTER_LEFT);

        layout.getChildren().addAll(controls, displayList);
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