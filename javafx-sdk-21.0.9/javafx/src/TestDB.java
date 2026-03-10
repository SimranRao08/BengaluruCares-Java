import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDB {
    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/4bcajava";  // your DB name
        String user = "root";    // your username
        String password = "Simran@123"; // your password

        try {
            Connection conn = DriverManager.getConnection(url, user, password);

            if (conn != null) {
                System.out.println("Database Connected Successfully!");
            }

            conn.close();

        } catch (SQLException e) {
            System.out.println("Connection Failed!");
            e.printStackTrace();
        }
    }
}