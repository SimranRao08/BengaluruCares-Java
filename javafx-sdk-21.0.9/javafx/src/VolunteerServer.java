import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

public class VolunteerServer {
    private static final int TCP_PORT = 5000;
    private static final int MULTICAST_PORT = 4446;
    private static final String MULTICAST_GROUP = "230.0.0.1";

    private static final ExecutorService clientPool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   BANGALORE VMS - LIVE EVENT SERVER      ");
        System.out.println("==========================================");

        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
            System.out.println(" TCP Server Listening on Port " + TCP_PORT);
            System.out.println(" Multicast Broadcaster Ready on " + MULTICAST_GROUP + ":" + MULTICAST_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcastToMulticastGroup(String message) {
        try (DatagramSocket udpSocket = new DatagramSocket()) {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            byte[] buffer = message.getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, MULTICAST_PORT);
            udpSocket.send(packet);

            System.out.println("[BROADCAST] " + message);

        } catch (IOException e) {
            System.err.println("Error broadcasting: " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String orgName = in.readLine();
                String joinMsg = " " + orgName + " joined the Bangalore VMS network.";
                VolunteerServer.broadcastToMulticastGroup(joinMsg);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.equalsIgnoreCase("exit")) break;

                    if (inputLine.startsWith("ADD::")) {
                        String[] parts = inputLine.split("::");
                        if (parts.length == 3) {
                            String title = parts[1];
                            String loc = parts[2];
                            
                            String time = new SimpleDateFormat("HH:mm").format(new Date());
                            String formattedMsg = String.format("[%s] %s posted: %s @ %s", 
                                    time, orgName, title, loc);

                            VolunteerServer.broadcastToMulticastGroup(formattedMsg);
                        }
                    }
                }

            } catch (IOException e) {
                // Connection lost
            } finally {
                try { socket.close(); } catch (IOException e) {}
            }
        }
    }
}