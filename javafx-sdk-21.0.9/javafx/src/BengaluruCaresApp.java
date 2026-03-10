import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class BengaluruCaresApp extends Application {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/4bcajava";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Simran@123"; 

    private int currentSelectedId = -1; 
    private TableView<Volunteer> table = new TableView<>();
    private ObservableList<Volunteer> volunteerData = FXCollections.observableArrayList();

    private TextField nameField = new TextField();
    private TextField emailField = new TextField();
    private TextField phoneField = new TextField();
    private ComboBox<String> roleBox = new ComboBox<>();
    private ToggleGroup genderGroup = new ToggleGroup();
    private RadioButton rbMale = new RadioButton("Male");
    private RadioButton rbFemale = new RadioButton("Female");
    private DatePicker joinDatePicker = new DatePicker();
    private CheckBox weekendCheck = new CheckBox("Available on Weekends?");
    private TextArea addressArea = new TextArea();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        VBox formPanel = createFormPanel();
        setupTable();
        
        loadDataFromDatabase();

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(formPanel, table);
        splitPane.setDividerPositions(0.4); 

        Scene scene = new Scene(splitPane, 1100, 650);
        primaryStage.setTitle("Bengaluru Cares - Volunteer Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createFormPanel() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));
        vbox.setStyle("-fx-background-color: #f0f0f0;");

        Label header = new Label("Registration Form");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        roleBox.getItems().addAll("Coordinator", "Field Volunteer", "Medical Support", "Logistics");
        roleBox.setPromptText("Select Role");
        roleBox.setMaxWidth(Double.MAX_VALUE);
        
        rbMale.setToggleGroup(genderGroup);
        rbFemale.setToggleGroup(genderGroup);
        HBox genderBox = new HBox(10, rbMale, rbFemale);

        addressArea.setPrefRowCount(3);
        addressArea.setWrapText(true);

        Button btnAdd = new Button("ADD");
        Button btnDisplay = new Button("DISPLAY");
        Button btnEdit = new Button("EDIT");
        Button btnDelete = new Button("DELETE");

        btnAdd.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");     // Green
        btnDisplay.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;"); // Blue
        btnEdit.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: black;");    // Yellow
        btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");  // Red
        
        HBox buttonBox = new HBox(10, btnAdd, btnDisplay, btnEdit, btnDelete);

        btnAdd.setOnAction(e -> handleAdd());
        btnDisplay.setOnAction(e -> loadDataFromDatabase());
        btnEdit.setOnAction(e -> handleEdit());
        btnDelete.setOnAction(e -> handleDelete());

        vbox.getChildren().addAll(
            header,
            new Label("Full Name:"), nameField,
            new Label("Email:"), emailField,
            new Label("Phone:"), phoneField,
            new Label("Role:"), roleBox,
            new Label("Gender:"), genderBox,
            new Label("Join Date:"), joinDatePicker,
            weekendCheck,
            new Label("Address:"), addressArea,
            new Separator(),
            buttonBox
        );
        return vbox;
    }

    private void setupTable() {
        TableColumn<Volunteer, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Volunteer, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Volunteer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Volunteer, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        TableColumn<Volunteer, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));

        TableColumn<Volunteer, String> dateCol = new TableColumn<>("Join Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("joinDate"));

        table.getColumns().addAll(idCol, nameCol, emailCol, roleCol, genderCol, dateCol);
        table.setItems(volunteerData);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            }
        });
    }


    // 1. ADD
    private void handleAdd() {
        if (!validateInput()) return;
        
        String query = "INSERT INTO volunteerlab3 (full_name, email, phone, role, gender, join_date, is_weekend_available, address) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        executeSaveOrUpdate(query, false);
    }

    // 2. DISPLAY 
    private void loadDataFromDatabase() {
        volunteerData.clear();
        String query = "SELECT * FROM volunteerlab3";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                volunteerData.add(new Volunteer(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("role"),
                    rs.getString("gender"),
                    rs.getDate("join_date").toString(),
                    rs.getBoolean("is_weekend_available"),
                    rs.getString("address")
                ));
            }
        } catch (SQLException e) {
            showAlert("Database Error", e.getMessage());
        }
    }

    // 3. EDIT
    private void handleEdit() {
        if (currentSelectedId == -1) {
            showAlert("Selection Error", "Please select a volunteer from the table to edit.");
            return;
        }
        if (!validateInput()) return;

        String query = "UPDATE volunteerlab3 SET full_name=?, email=?, phone=?, role=?, gender=?, join_date=?, is_weekend_available=?, address=? WHERE id=?";
        executeSaveOrUpdate(query, true);
    }

    // 4. DELETE
    private void handleDelete() {
        Volunteer selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a row to delete.");
            return;
        }

        String query = "DELETE FROM volunteerlab3 WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setInt(1, selected.getId());
            pstmt.executeUpdate();
            
            loadDataFromDatabase(); // Refresh table
            clearForm();
            showAlert("Success", "Record deleted successfully.");
            
        } catch (SQLException e) {
            showAlert("Delete Error", e.getMessage());
        }
    }

    private void executeSaveOrUpdate(String query, boolean isUpdate) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, emailField.getText());
            pstmt.setString(3, phoneField.getText());
            pstmt.setString(4, roleBox.getValue());
            pstmt.setString(5, rbMale.isSelected() ? "Male" : "Female");
            pstmt.setDate(6, Date.valueOf(joinDatePicker.getValue()));
            pstmt.setBoolean(7, weekendCheck.isSelected());
            pstmt.setString(8, addressArea.getText());

            if (isUpdate) {
                pstmt.setInt(9, currentSelectedId);
            }

            pstmt.executeUpdate();
            loadDataFromDatabase();
            clearForm();
            showAlert("Success", isUpdate ? "Record Updated!" : "Record Added!");

        } catch (SQLException e) {
            showAlert("Database Error", e.getMessage());
        }
    }

    private void populateForm(Volunteer v) {
        currentSelectedId = v.getId();
        nameField.setText(v.getName());
        emailField.setText(v.getEmail());
        phoneField.setText(v.getPhone());
        roleBox.setValue(v.getRole());
        addressArea.setText(v.getAddress());
        
        if ("Male".equalsIgnoreCase(v.getGender())) rbMale.setSelected(true);
        else rbFemale.setSelected(true);
        
        if (v.getJoinDate() != null) joinDatePicker.setValue(LocalDate.parse(v.getJoinDate()));
        weekendCheck.setSelected(v.isWeekendAvailable());
    }

    private void clearForm() {
        currentSelectedId = -1;
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        roleBox.setValue(null);
        genderGroup.selectToggle(null);
        joinDatePicker.setValue(null);
        weekendCheck.setSelected(false);
        addressArea.clear();
    }

    private boolean validateInput() {
        if (nameField.getText().isEmpty() || emailField.getText().isEmpty() || roleBox.getValue() == null || joinDatePicker.getValue() == null) {
            showAlert("Validation Error", "Please fill all required fields.");
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Volunteer {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty email;
        private final SimpleStringProperty phone;
        private final SimpleStringProperty role;
        private final SimpleStringProperty gender;
        private final SimpleStringProperty joinDate;
        private final SimpleBooleanProperty weekendAvailable;
        private final SimpleStringProperty address;

        public Volunteer(int id, String name, String email, String phone, String role, String gender, String joinDate, boolean weekend, String address) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.email = new SimpleStringProperty(email);
            this.phone = new SimpleStringProperty(phone);
            this.role = new SimpleStringProperty(role);
            this.gender = new SimpleStringProperty(gender);
            this.joinDate = new SimpleStringProperty(joinDate);
            this.weekendAvailable = new SimpleBooleanProperty(weekend);
            this.address = new SimpleStringProperty(address);
        }

        public int getId() { return id.get(); }
        public String getName() { return name.get(); }
        public String getEmail() { return email.get(); }
        public String getPhone() { return phone.get(); }
        public String getRole() { return role.get(); }
        public String getGender() { return gender.get(); }
        public String getJoinDate() { return joinDate.get(); }
        public boolean isWeekendAvailable() { return weekendAvailable.get(); }
        public String getAddress() { return address.get(); }
    }
}