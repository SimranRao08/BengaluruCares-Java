import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;

public class StudentSystem extends Application {

    // Student Model
    public static class Student {
        private int id;
        private String name;
        private String department;
        private double percentage;

        public Student(int id, String name, String department, double percentage) {
            this.id = id;
            this.name = name;
            this.department = department;
            this.percentage = percentage;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDepartment() {
            return department;
        }

        public double getPercentage() {
            return percentage;
        }
    }

    // Database Credentials
    String url = "jdbc:mysql://localhost:3306/student_db";
    String user = "root";
    String password = "Simran@123";

    private TableView<Student> table = new TableView<>();
    private TextField txtName = new TextField(), txtDept = new TextField(), txtPerc = new TextField();
    private ObservableList<Student> studentList = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        setupTable();
        refreshData();

        Button btnAdd = new Button("Insert");
        Button btnUpdate = new Button("Update");
        Button btnDelete = new Button("Delete");
        Button btnHighPerf = new Button("High Performers");
        Button btnStats = new Button("Avg Stats");

        // INSERT (CRUD)
        btnAdd.setOnAction(e -> {
            try {
                Connection conn = DriverManager.getConnection(url, user, password);
                String sql = "INSERT INTO students (name, department, percentage) VALUES (?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, txtName.getText());
                pstmt.setString(2, txtDept.getText());
                pstmt.setDouble(3, Double.parseDouble(txtPerc.getText()));
                pstmt.executeUpdate();
                conn.close();
                refreshData();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // UPDATE (CRUD)
        btnUpdate.setOnAction(e -> {
            Student selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    Connection conn = DriverManager.getConnection(url, user, password);
                    String sql = "UPDATE students SET name=?, department=?, percentage=? WHERE id=?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, txtName.getText());
                    pstmt.setString(2, txtDept.getText());
                    pstmt.setDouble(3, Double.parseDouble(txtPerc.getText()));
                    pstmt.setInt(4, selected.getId());
                    pstmt.executeUpdate();
                    conn.close();
                    refreshData();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // DELETE (CRUD)
        btnDelete.setOnAction(e -> {
            Student selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    Connection conn = DriverManager.getConnection(url, user, password);
                    String sql = "DELETE FROM students WHERE id=?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, selected.getId());
                    pstmt.executeUpdate();
                    conn.close();
                    refreshData();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Task a: Lambda (Filter/Sort)
        btnHighPerf.setOnAction(e -> {
            List<Student> filtered = studentList.stream()
                    .filter(s -> s.getPercentage() > 70)
                    .sorted((s1, s2) -> Double.compare(s2.getPercentage(), s1.getPercentage()))
                    .collect(Collectors.toList());
            table.setItems(FXCollections.observableArrayList(filtered));
        });

        // Task b: Streams (Avg)
        btnStats.setOnAction(e -> {
            double avg = studentList.stream().mapToDouble(Student::getPercentage).average().orElse(0);
            System.out.println("Class Average: " + avg);
            // Message Box Codde
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Average Percentage");
            alert.setHeaderText(null);
            alert.setContentText("Class Average Percentage: " + avg);
            alert.showAndWait();
            
        });

        // Fill form on click
        table.setOnMouseClicked(e -> {
            Student s = table.getSelectionModel().getSelectedItem();
            if (s != null) {
                txtName.setText(s.getName());
                txtDept.setText(s.getDepartment());
                txtPerc.setText(String.valueOf(s.getPercentage()));
            }
        });

        // Layout
        HBox inputFields = new HBox(10, new Label("Name:"), txtName, new Label("Dept:"), txtDept, new Label("%:"),
                txtPerc);
        HBox controls = new HBox(10, btnAdd, btnUpdate, btnDelete, btnHighPerf, btnStats);
        VBox root = new VBox(15, inputFields, controls, table);
        root.setPadding(new Insets(15));

        stage.setScene(new Scene(root, 750, 450));
        stage.setTitle("Academic System - Lab Exam");
        stage.show();
    }

    private void refreshData() {
        studentList.clear();
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM students");
            while (rs.next()) {
                studentList.add(new Student(rs.getInt("id"), rs.getString("name"),
                        rs.getString("department"), rs.getDouble("percentage")));
            }
            table.setItems(studentList);
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setupTable() {
        TableColumn<Student, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Student, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Student, Double> percCol = new TableColumn<>("Percentage");
        percCol.setCellValueFactory(new PropertyValueFactory<>("percentage"));
        table.getColumns().addAll(idCol, nameCol, percCol);
    }

    public static void main(String[] args) {
        launch(args);
    }
}