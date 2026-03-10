import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.sql.*;

public class Lab4Java extends Application {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/4bcajava";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Simran@123";

    private Connection conn;
    private Statement scrollableStmt;
    private ResultSet rs;
    
    private TextField idField = new TextField();
    private TextField nameField = new TextField();
    private TextField emailField = new TextField();
    private TextField phoneField = new TextField();
    private ComboBox<String> roleBox = new ComboBox<>();
    private TextField deptIdField = new TextField();
    
    private ImageView photoView = new ImageView();
    private File selectedImageFile; 
    private Label imageStatus = new Label("No Image");
    private Label statusLabel = new Label("System Ready");
    
    private TableView<VolunteerModel> dataTable = new TableView<>();
    private ObservableList<VolunteerModel> masterData = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        initDatabaseConnection();

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f4f6f8;");

        Label title = new Label("Volunteer Management System");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10); formGrid.setVgap(10);
        formGrid.setPadding(new Insets(15));
        formGrid.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);");

        idField.setEditable(false); idField.setPromptText("Auto-ID");
        roleBox.getItems().addAll("Coordinator", "Volunteer", "Medic", "Logistics");
        
        // Photo
        photoView.setFitWidth(100); photoView.setFitHeight(100); photoView.setPreserveRatio(true);
        photoView.setStyle("-fx-border-color: #bdc3c7;");
        Button btnUpload = new Button("Upload Photo");
        btnUpload.setOnAction(e -> handleImageUpload(primaryStage));
        VBox photoBox = new VBox(5, photoView, imageStatus, btnUpload);
        photoBox.setAlignment(Pos.CENTER);

        // Fields
        formGrid.add(new Label("ID:"), 0, 0);       formGrid.add(idField, 1, 0);
        formGrid.add(new Label("Full Name:"), 0, 1); formGrid.add(nameField, 1, 1);
        formGrid.add(new Label("Email:"), 0, 2);    formGrid.add(emailField, 1, 2);
        formGrid.add(new Label("Phone:"), 0, 3);    formGrid.add(phoneField, 1, 3);
        formGrid.add(new Label("Role:"), 0, 4);     formGrid.add(roleBox, 1, 4);
        formGrid.add(new Label("Dept ID:"), 0, 5);  formGrid.add(deptIdField, 1, 5);
        formGrid.add(photoBox, 2, 0, 1, 6);

        // --- BUTTONS ---
        Button btnAdd = new Button("Insert");
        Button btnUpdate = new Button("Update");
        Button btnDelete = new Button("Delete");
        Button btnClear = new Button("Clear Form");
        Button btnJoin = new Button("Show Dept Name");
        Button btnRefresh = new Button("Refresh");

        btnAdd.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        btnUpdate.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
        btnDelete.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
        btnJoin.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;");

        HBox opsBox = new HBox(10, btnAdd, btnUpdate, btnDelete, btnClear, new Separator(), btnJoin, btnRefresh);
        opsBox.setAlignment(Pos.CENTER);

        // --- NAVIGATION ---
        Button btnNext = new Button("Next >");
        Button btnPrev = new Button("< Prev");
        HBox navBox = new HBox(10, btnPrev, btnNext);
        navBox.setAlignment(Pos.CENTER);

        // --- TABLE ---
        setupTableColumns();
        VBox.setVgrow(dataTable, Priority.ALWAYS);

        // --- EVENTS ---
        btnAdd.setOnAction(e -> insertRecord());
        btnUpdate.setOnAction(e -> updateRecord());
        btnDelete.setOnAction(e -> deleteRecord());
        btnClear.setOnAction(e -> clearForm());
        btnRefresh.setOnAction(e -> loadData("STANDARD"));
        btnJoin.setOnAction(e -> loadData("JOIN"));
        
        btnNext.setOnAction(e -> {
            int i = dataTable.getSelectionModel().getSelectedIndex();
            dataTable.getSelectionModel().select(i + 1);
        });
        btnPrev.setOnAction(e -> {
            int i = dataTable.getSelectionModel().getSelectedIndex();
            if(i > 0) dataTable.getSelectionModel().select(i - 1);
        });

        // Link Table Selection to Form
        dataTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateForm(newVal);
            }
        });

        root.getChildren().addAll(title, formGrid, navBox, opsBox, statusLabel, dataTable);

        Scene scene = new Scene(root, 1000, 750); // Made wider to fit new columns
        primaryStage.setTitle("Volunteer Management System");
        primaryStage.setScene(scene);
        primaryStage.show();

        loadData("STANDARD"); // Load initial data
    }

    // --- DB CONNECTION ---
    private void initDatabaseConnection() {
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            scrollableStmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        } catch (SQLException e) {
            setStatus("DB Error: " + e.getMessage(), true);
        }
    }
    private void loadData(String type) {
        masterData.clear();
        String query;
        
        if (type.equals("JOIN")) {
            // Get Name AND ID
            query = "SELECT v.*, d.dept_name FROM volunteers v LEFT JOIN departments d ON v.dept_id = d.dept_id";
        } else {
            query = "SELECT * FROM volunteers";
        }

        try {
            rs = scrollableStmt.executeQuery(query); 
            
            while(rs.next()) {
                String deptName = "---"; 
                if (type.equals("JOIN")) {
                    deptName = rs.getString("dept_name");
                }
                
                masterData.add(new VolunteerModel(
                    rs.getInt("id"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("role"),
                    rs.getInt("dept_id"),
                    deptName
                ));
            }
            dataTable.setItems(masterData);
            if (!masterData.isEmpty()) dataTable.getSelectionModel().selectFirst();
            setStatus(type.equals("JOIN") ? "Joined Data Loaded" : "", false);

        } catch (SQLException e) {
            setStatus("Load Error: " + e.getMessage(), true);
        }
    }

    // --- FORM POPULATION ---
    private void populateForm(VolunteerModel v) {
        idField.setText(String.valueOf(v.getId()));
        nameField.setText(v.getName());
        emailField.setText(v.getEmail());
        phoneField.setText(v.getPhone());
        roleBox.setValue(v.getRole());
        deptIdField.setText(String.valueOf(v.getDeptId()));

        try (PreparedStatement pstmt = conn.prepareStatement("SELECT photo FROM volunteers WHERE id=?")) {
            pstmt.setInt(1, v.getId());
            ResultSet imgRs = pstmt.executeQuery();
            if (imgRs.next()) {
                Blob blob = imgRs.getBlob("photo");
                if (blob != null) {
                    photoView.setImage(new Image(blob.getBinaryStream()));
                    imageStatus.setText("Photo Loaded");
                } else {
                    photoView.setImage(null);
                    imageStatus.setText("No Photo");
                }
            }
        } catch (SQLException e) {
            System.out.println("Image Error: " + e.getMessage());
        }
    }

    // INSERT
    private void insertRecord() {
        try {
            rs.moveToInsertRow();
            rs.updateString("full_name", nameField.getText());
            rs.updateString("email", emailField.getText());
            rs.updateString("phone", phoneField.getText());
            rs.updateString("role", roleBox.getValue());
            rs.updateInt("dept_id", Integer.parseInt(deptIdField.getText()));
            
            if (selectedImageFile != null) {
                rs.updateBinaryStream("photo", new FileInputStream(selectedImageFile), (int)selectedImageFile.length());
            }
            rs.insertRow();
            loadData("STANDARD");
            setStatus("Inserted Successfully", false);
        } catch (Exception e) {
            setStatus("Insert Error: " + e.getMessage(), true);
        }
    }

    //UPDATE
    private void updateRecord() {
        if (idField.getText().isEmpty()) return;
        int targetId = Integer.parseInt(idField.getText());

        try {
            rs.beforeFirst();
            boolean found = false;
            while (rs.next()) {
                if (rs.getInt("id") == targetId) {
                    found = true;
                    break;
                }
            }

            if (found) {
                rs.updateString("full_name", nameField.getText());
                rs.updateString("email", emailField.getText());
                rs.updateString("phone", phoneField.getText());
                rs.updateString("role", roleBox.getValue());
                rs.updateInt("dept_id", Integer.parseInt(deptIdField.getText()));
                if (selectedImageFile != null) {
                    rs.updateBinaryStream("photo", new FileInputStream(selectedImageFile), (int)selectedImageFile.length());
                }
                rs.updateRow();
                loadData("STANDARD");
                setStatus("Record Updated", false);
            }
        } catch (Exception e) {
            setStatus("Update Error: " + e.getMessage(), true);
        }
    }

    // DELETE
    private void deleteRecord() {
        VolunteerModel selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("No record selected!", true);
            return;
        }
        try {
            String sql = "DELETE FROM volunteers WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, selected.getId());
            pstmt.executeUpdate();
            loadData("STANDARD");
            clearForm();
            setStatus("Record Deleted Successfully", false);
        } catch (SQLException e) {
            setStatus("Delete Error: " + e.getMessage(), true);
        }
    }

    // IMAGE UPLOAD
    private void handleImageUpload(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.png"));
        selectedImageFile = fc.showOpenDialog(stage);
        if (selectedImageFile != null) {
            photoView.setImage(new Image(selectedImageFile.toURI().toString()));
            imageStatus.setText("Selected");
        }
    }

    private void clearForm() {
        idField.clear(); nameField.clear(); emailField.clear();
        phoneField.clear(); deptIdField.clear(); roleBox.setValue(null);
        photoView.setImage(null); selectedImageFile = null;
    }

    private void setStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill: " + (error ? "red" : "green") + "; -fx-font-weight: bold;");
    }

 //TABLE
    private void setupTableColumns() {
        TableColumn<VolunteerModel, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(40);
        
        TableColumn<VolunteerModel, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(140);

        TableColumn<VolunteerModel, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(150);

        TableColumn<VolunteerModel, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(100);

        TableColumn<VolunteerModel, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(100);

        TableColumn<VolunteerModel, Integer> deptIdCol = new TableColumn<>("Dept ID");
        deptIdCol.setCellValueFactory(new PropertyValueFactory<>("deptId"));
        deptIdCol.setPrefWidth(70);

        TableColumn<VolunteerModel, String> deptNameCol = new TableColumn<>("Department Name");
        deptNameCol.setCellValueFactory(new PropertyValueFactory<>("deptName"));
        deptNameCol.setPrefWidth(150);

        dataTable.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, roleCol, deptIdCol, deptNameCol);
    }

    // --- DATA MODEL ---
    public static class VolunteerModel {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty email;
        private final SimpleStringProperty phone;
        private final SimpleStringProperty role;
        private final SimpleIntegerProperty deptId;
        private final SimpleStringProperty deptName;

        public VolunteerModel(int id, String name, String email, String phone, String role, int deptId, String deptName) {
            this.id = new SimpleIntegerProperty(id);
            this.name = new SimpleStringProperty(name);
            this.email = new SimpleStringProperty(email);
            this.phone = new SimpleStringProperty(phone);
            this.role = new SimpleStringProperty(role);
            this.deptId = new SimpleIntegerProperty(deptId);
            this.deptName = new SimpleStringProperty(deptName);
        }
        public int getId() { return id.get(); }
        public String getName() { return name.get(); }
        public String getEmail() { return email.get(); }
        public String getPhone() { return phone.get(); }
        public String getRole() { return role.get(); }
        public int getDeptId() { return deptId.get(); }
        public String getDeptName() { return deptName.get(); }
    }
}