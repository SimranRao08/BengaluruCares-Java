import java.awt.TextField;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
// Fix 1: Ensure you are using javafx.scene.control.TableColumn, NOT javax.swing
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;

public class EmployeeSystems extends Application{

    public static class Employee{
        private int id;
        private string name;
        private string designation;
        private string department;
        private double salary;

    public Employee(int id, string name, string desgination, string department, double salary){
        this.id= id;
        this.name = name;
        this.designation = designation;
        this.department = department;
        this.salary = salary;
    }
    public int getId(){ return id; }
    public string getName(){return name;}
    public String getDesignation() { return designation; }
    public String getDepartment() { return department; }
    public double getSalary() { return salary; }
    
    }
    String url= "jdbc:mysql://localhost:3306/company_db";
    String user= "root";
    String password= "Simran@123";

    private TableView<Employee>table= new TableView<>();
    TextField txtName= new TextField(), txtDesig = new TextField(), 
             txtDept = new TextField(), txtSalary = new TextField();
    private ObservableList<Employee>emplist= FXCollections.observableArrayList();
    @Override

    public void start(Stage stage);
    setUpTable();
    refreshData();

    Button btnAdd= new Button("Add");
    Button btnHighesButton = new Button("High Earners (>50k)");
    Button btnStats = new Button("Total Payroll");
    Button btnClear = new Button("Reset Table");

    btnAdd.setOnAction(e->{
        try(Connection conn= DriverManager.getConnection(url, user, password)){
            String sql= "INSERT INTO employees (name, designation, department, salary) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt= conn.prepareStatement(sql);
            pstmt.setString(1, txtName.getText());
             pstmt.setString(2, txtDesig.getText());
                pstmt.setString(3, txtDept.getText());
            pstmt.setDouble(4, Double.parseDouble(txtSalary.getText()));

            pstmt.executeUpdate();
            refreshData();
            clearFields();
        }catch(Exception ex){ex.printStackTrace();}
    });

    

}