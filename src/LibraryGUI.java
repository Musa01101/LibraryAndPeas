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
            messageLabel.setText("Sign Up screen coming next...");
            messageLabel.setTextFill(Color.BLUE);
        });

        layout.getChildren().addAll(title, emailInput, passInput, loginBtn, signupBtn, messageLabel);
        stage.setScene(new Scene(layout, 400, 350));
    }

    private void showMainDashboard(Stage stage) {
        TabPane root = new TabPane();
        root.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        root.getTabs().addAll(
                createStudentTab(),
                createLibrarianTab(),
                createAvailabilityTab()
        );

        stage.setScene(new Scene(root, 800, 600));
        // Center the larger window on the screen
        stage.centerOnScreen();
    }

    // --- YOUR EXISTING TABS STAY EXACTLY THE SAME BELOW ---

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
        layout.getChildren().add(new Label("Availability features will go here."));
        tab.setContent(layout);
        return tab;
    }

    public static void main(String[] args) {
        launch(args);
    }
}