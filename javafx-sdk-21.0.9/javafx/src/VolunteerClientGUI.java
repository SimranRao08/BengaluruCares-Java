import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;

public class VolunteerClientGUI extends Application {

    private static final String SERVER_HOST = "localhost";
    private static final int TCP_PORT = 5000;
    private static final int MULTICAST_PORT = 4446;
    private static final String MULTICAST_GROUP = "230.0.0.1";

    private Stage primaryStage;
    private TextArea eventLogArea; 
    private TextField eventTitleInput;
    private TextField locationInput;
    
    private Socket tcpSocket;
    private PrintWriter out;
    private String orgName;
    private volatile boolean isRunning = true;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("Bangalore VMS - Volunteer Hub");
        showLoginScreen();
    }

    private void showLoginScreen() {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f0f2f5;");

        Label titleLabel = new Label("Volunteer Login");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#d97706")); 

        TextField nameField = new TextField();
        nameField.setPromptText("Organization / Volunteer Name");
        nameField.setMaxWidth(250);

        Button joinButton = new Button("Enter Dashboard");
        joinButton.setStyle("-fx-background-color: #d97706; -fx-text-fill: white; -fx-font-weight: bold;");
        joinButton.setPrefWidth(250);

        joinButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) connectToServer(name);
        });

        root.getChildren().addAll(titleLabel, nameField, joinButton);
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showDashboard() {
        BorderPane root = new BorderPane();

        HBox header = new HBox(10);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #fff7ed; -fx-border-color: #fdba74; -fx-border-width: 0 0 2 0;");
        Label brand = new Label("Bangalore VMS Live Feed");
        brand.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        header.getChildren().add(brand);
        root.setTop(header);

        eventLogArea = new TextArea();
        eventLogArea.setEditable(false);
        eventLogArea.setWrapText(true);
        eventLogArea.setFont(Font.font("Monospaced", 14));
        root.setCenter(eventLogArea);

        VBox bottomPanel = new VBox(10);
        bottomPanel.setPadding(new Insets(15));
        bottomPanel.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 1 0 0 0;");

        eventTitleInput = new TextField();
        eventTitleInput.setPromptText("Event Title (e.g. Cubbon Park Cleanup)");
        
        locationInput = new TextField();
        locationInput.setPromptText("Location (e.g. Shivajinagar)");

        Button postButton = new Button("Post Event");
        postButton.setStyle("-fx-background-color: #d97706; -fx-text-fill: white; -fx-font-weight: bold;");
        postButton.setMaxWidth(Double.MAX_VALUE);

        postButton.setOnAction(e -> sendEvent());

        bottomPanel.getChildren().addAll(new Label("New Opportunity:"), eventTitleInput, locationInput, postButton);
        root.setBottom(bottomPanel);

        Scene scene = new Scene(root, 500, 600);
        primaryStage.setScene(scene);
    }


    private void connectToServer(String name) {
        this.orgName = name;
        try {
            tcpSocket = new Socket(SERVER_HOST, TCP_PORT);
            out = new PrintWriter(tcpSocket.getOutputStream(), true);
            out.println(orgName); 

            Thread listenerThread = new Thread(this::listenForMulticastMessages);
            listenerThread.setDaemon(true);
            listenerThread.start();

            showDashboard();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not connect to VMS Server!");
            alert.showAndWait();
        }
    }

    private void sendEvent() {
        String title = eventTitleInput.getText().trim();
        String loc = locationInput.getText().trim();

        if (!title.isEmpty() && !loc.isEmpty() && out != null) {
            out.println("ADD::" + title + "::" + loc);
            
            eventTitleInput.clear();
            locationInput.clear();
        }
    }

    private void listenForMulticastMessages() {
        try (MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
            multicastSocket.joinGroup(group);

            byte[] buffer = new byte[1024];

            while (isRunning) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);

                String receivedMsg = new String(packet.getData(), 0, packet.getLength());
                
                Platform.runLater(() -> eventLogArea.appendText(receivedMsg + "\n\n"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        isRunning = false;
        if (out != null) out.println("exit");
        if (tcpSocket != null) tcpSocket.close();
    }
}