import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;

// RMI Interface integrated for simplicity
interface GradeInterface extends Remote {
    String getGrade(int marks) throws RemoteException;
}

public class SmartCampusServer extends UnicastRemoteObject implements GradeInterface {
    
    public SmartCampusServer() throws RemoteException { super(); }

    // RMI Method Implementation
    public String getGrade(int marks) {
        return (marks >= 85) ? "Distinction (A+)" : "First Class (A)";
    }

    public static void main(String[] args) {
        try {
            // 1. Start RMI Registry and Bind Service
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("GradeService", new SmartCampusServer());
            System.out.println("[RMI] Grade Service bound in registry.");

            // 2. Start TCP Server Socket
            ServerSocket serverSocket = new ServerSocket(5000);
            System.out.println("[TCP] Server listening on port 5000...");

            while (true) {
                try (Socket client = serverSocket.accept();
                     DataInputStream in = new DataInputStream(client.getInputStream());
                     DataOutputStream out = new DataOutputStream(client.getOutputStream())) {
                    
                    String request = in.readUTF();
                    System.out.println("Received Request: " + request);
                    
                    if (request.equalsIgnoreCase("Get Attendance")) {
                        out.writeUTF("Confirmed: Your attendance is 88%.");
                    } else {
                        out.writeUTF("Error: Unknown Command.");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Server Exception: " + e.getMessage());
        }
    }
}