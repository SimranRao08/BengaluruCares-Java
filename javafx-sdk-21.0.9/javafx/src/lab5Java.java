import java.net.*;
import java.util.Date;

public class lab5Java {
    public static void main(String[] args) {
        try {
            URL url = new URL("https://www.google.com/");

            System.out.println("Protocol: " + url.getProtocol());
            System.out.println("Host: " + url.getHost());
            System.out.println("Port: " + url.getPort());
            System.out.println("Default Port: " + url.getDefaultPort());
            System.out.println("File: " + url.getFile());
            System.out.println("Path: " + url.getPath());

            InetAddress address = InetAddress.getByName(url.getHost());
            System.out.println("Host Name: " + address.getHostName());
            System.out.println("IP Address: " + address.getHostAddress());

            URLConnection conn = url.openConnection();
            conn.connect();
 
            System.out.println("Content Type: " + conn.getContentType());
            System.out.println("Content Length: " + conn.getContentLength());
            System.out.println("Last Modified: " + new Date(conn.getLastModified()));

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}