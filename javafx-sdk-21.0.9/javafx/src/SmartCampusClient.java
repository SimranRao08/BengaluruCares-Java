import java.io.*;
import java.net.*;
import java.rmi.registry.*;
import java.util.Scanner;

public class SmartCampusClient {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try {
            // --- SECTION 1: InetAddress ---
            System.out.println("--- 1. System Identification ---");
            InetAddress local = InetAddress.getLocalHost();
            System.out.println("My Host: " + local.getHostName() + " | IP: " + local.getHostAddress());
            
            System.out.print("Enter target host (e.g., localhost): ");
            String target = sc.nextLine();
            System.out.println("Resolved " + target + " to: " + InetAddress.getByName(target).getHostAddress());

            // --- SECTION 2: URLConnection ---
            System.out.println("\n--- 2. Local Resource Access ---");
            // Note: Create a 'student_info.html' in this folder first!
            File file = new File("student_info.html");
            if(file.exists()) {
                URLConnection conn = file.toURI().toURL().openConnection();
                System.out.println("Type: " + conn.getContentType() + " | Length: " + conn.getContentLength());
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                System.out.println("First line of file: " + br.readLine());
                br.close();
            } else {
                System.out.println("student_info.html not found. Skipping read.");
            }

            // --- SECTION 3: TCP Communication ---
            System.out.println("\n--- 3. TCP Student-Server Sync ---");
            try (Socket socket = new Socket("localhost", 5000);
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                 DataInputStream in = new DataInputStream(socket.getInputStream())) {
                
                out.writeUTF("Get Attendance");
                System.out.println("Server Response: " + in.readUTF());
            }

            // --- SECTION 4: RMI Implementation ---
            System.out.println("\n--- 4. RMI Grade Calculation ---");
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            GradeInterface stub = (GradeInterface) registry.lookup("GradeService");
            
            System.out.print("Enter your marks: ");
            int marks = sc.nextInt();
            System.out.println("RMI Server says: " + stub.getGrade(marks));

        } catch (Exception e) {
            System.err.println("Client Error: " + e.getMessage());
        }
    }
}