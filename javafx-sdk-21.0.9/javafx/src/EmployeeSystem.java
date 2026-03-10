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

public class EmployeeSystem extends Application {

    // --- Task: Employee Model ---
    public static class Employee {
        private int id;
        private String name;
        private String designation;
        private String department;
        private double salary;

        public Employee(int id, String name, String designation, String department, double salary) {
            this.id = id;
            this.name = name;
            this.designation = designation;
            this.department = department;
            this.salary = salary;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getDesignation() { return designation; }
        public String getDepartment() { return department; }
        public double getSalary() { return salary; }
    }

    // Database Credentials
    String url = "jdbc:mysql://localhost:3306/company_db";
    String user = "root";
    String password = "Simran@123"; 

    private TableView<Employee> table = new TableView<>();
    private TextField txtName = new TextField(), txtDesig = new TextField(), 
                      txtDept = new TextField(), txtSalary = new TextField();
    private ObservableList<Employee> empList = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        setupTable();
        refreshData();

        Button btnAdd = new Button("Add Employee");
        Button btnHighEarners = new Button("High Earners (>50k)");
        Button btnStats = new Button("Total Payroll");
        Button btnClear = new Button("Reset Table");

        // --- JDBC: INSERT ---
        btnAdd.setOnAction(e -> {
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                String sql = "INSERT INTO employees (name, designation, department, salary) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, txtName.getText());
                pstmt.setString(2, txtDesig.getText());
                pstmt.setString(3, txtDept.getText());
                pstmt.setDouble(4, Double.parseDouble(txtSalary.getText()));
                pstmt.executeUpdate();
                refreshData();
                clearFields();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        // --- Task a: Lambda (Filter & Sort) ---
        btnHighEarners.setOnAction(e -> {
            List<Employee> filtered = empList.stream()
                .filter(emp -> emp.getSalary() > 50000)
                .sorted((e1, e2) -> Double.compare(e2.getSalary(), e1.getSalary())) // Descending sort lambda
                .collect(Collectors.toList());
            table.setItems(FXCollections.observableArrayList(filtered));
        });

        // --- Task b: Streams (Total Payroll & Count) ---
        btnStats.setOnAction(e -> {
            double total = empList.stream().mapToDouble(Employee::getSalary).sum();
            long count = empList.stream().filter(emp -> emp.getSalary() > 50000).count();
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Payroll Analytics");
            alert.setHeaderText(null);
            alert.setContentText("Total Monthly Payroll: " + total + "\nHigh Earners Count: " + count);
            alert.showAndWait();
        });

        btnClear.setOnAction(e -> refreshData());

        // Layout UI
        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(10);
        form.add(new Label("Name:"), 0, 0); form.add(txtName, 1, 0);
        form.add(new Label("Designation:"), 2, 0); form.add(txtDesig, 3, 0);
        form.add(new Label("Dept:"), 0, 1); form.add(txtDept, 1, 1);
        form.add(new Label("Salary:"), 2, 1); form.add(txtSalary, 3, 1);

        HBox controls = new HBox(10, btnAdd, btnHighEarners, btnStats, btnClear);
        VBox root = new VBox(20, form, controls, table);
       // root.setPadding(new Insets(20));

        stage.setScene(new Scene(root, 800, 500));
        stage.setTitle("Employee Payroll System - Lab Exam");
        stage.show();
    }

    private void refreshData() {
        empList.clear();
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM employees");
            while (rs.next()) {
                empList.add(new Employee(rs.getInt("id"), rs.getString("name"),
                        rs.getString("designation"), rs.getString("department"), rs.getDouble("salary")));
            }
            table.setItems(empList);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void setupTable() {
        TableColumn<Employee, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Employee, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Employee, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        TableColumn<Employee, Double> salCol = new TableColumn<>("Salary");
        salCol.setCellValueFactory(new PropertyValueFactory<>("salary"));

        table.getColumns().addAll(idCol, nameCol, deptCol, salCol);
    }

    private void clearFields() {
        txtName.clear(); txtDesig.clear(); txtDept.clear(); txtSalary.clear();
    }

    public static void main(String[] args) { launch(args); }
}