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
            // 1. Block empty submissions right away
            if (nameInput.getText().trim().isEmpty() ||
                    emailInput.getText().trim().isEmpty() ||
                    passInput.getText().isEmpty()) {

                messageLabel.setText("Please fill out all fields!");
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

    private Tab createStudentTab() {
        Tab tab = new Tab("Student Portal");
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.getChildren().add(new Label("Student features will go here."));
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

        // Dropdown to switch views
        ComboBox<String> viewSelector = new ComboBox<>();
        viewSelector.getItems().addAll("Books Availability", "Study Rooms Availability");
        viewSelector.setValue("Books Availability"); // Default starting view

        // The list that will update based on the choice
        ListView<String> displayList = new ListView<>();
        displayList.getItems().add("Available Books will appear here..."); // Placeholder

        // Toggle logic
        viewSelector.setOnAction(e -> {
            displayList.getItems().clear();
            if (viewSelector.getValue().equals("Books Availability")) {
                // TODO: system.getAvailableBooks() goes here later
                displayList.getItems().add("Available Books will appear here...");
            } else {
                // TODO: system.getRoomStatus() goes here later
                displayList.getItems().addAll("Room 1: Open", "Room 2: Booked", "Room 3: Open");
            }
        });

        layout.getChildren().addAll(new Label("Select what to view:"), viewSelector, displayList);
        tab.setContent(layout);
        return tab;
    }

    public static void main(String[] args) {
        launch(args);
    }
}