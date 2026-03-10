import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMIClient extends Application {

    private CareServer hubStub;
    private CareClient callback;
    private TextArea updatesArea;
    private String userName;

    private class UpdateHandler extends UnicastRemoteObject implements CareClient {
        protected UpdateHandler() throws RemoteException { super(); }
        
        @Override
        public void notifyUpdate(String message) throws RemoteException {
            Platform.runLater(() -> updatesArea.appendText(message + "\n"));
        }
    }

    @Override
    public void start(Stage primaryStage) {
        updatesArea = new TextArea(); 
        updatesArea.setEditable(false); 
        updatesArea.setWrapText(true);
        
        TextField nameInput = new TextField(); 
        nameInput.setPromptText("Name");
        
        TextField msgInput = new TextField(); 
        msgInput.setPromptText("Update...");
        
        Button btnJoin = new Button("Join Hub");
        Button btnPost = new Button("Post"); 
        btnPost.setDisable(true);

        HBox top = new HBox(10, nameInput, btnJoin);
        HBox bottom = new HBox(10, msgInput, btnPost);
        HBox.setHgrow(msgInput, Priority.ALWAYS);
        
        VBox layout = new VBox(10, top, updatesArea, bottom);
        layout.setPadding(new Insets(15));
        VBox.setVgrow(updatesArea, Priority.ALWAYS);

        layout.setStyle("-fx-background-color: #e8f5e9;"); 
        updatesArea.setStyle("-fx-text-fill: #1b5e20;");
        btnJoin.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;");

        btnJoin.setOnAction(e -> {
            userName = nameInput.getText().trim();
            if (!userName.isEmpty()) {
                try {
                    hubStub = (CareServer) Naming.lookup("rmi://localhost/BengaluruCaresHub");
                    callback = new UpdateHandler();
                    hubStub.joinHub(callback, userName);
                    nameInput.setDisable(true);
                    btnJoin.setDisable(true);
                    btnPost.setDisable(false);
                } catch (Exception ex) {
                    updatesArea.appendText("Error: " + ex.getMessage() + "\n");
                }
            }
        });

        btnPost.setOnAction(e -> {
            String text = msgInput.getText();
            if (!text.isEmpty() && hubStub != null) {
                try {
                    hubStub.postUpdate(userName + ": " + text);
                    msgInput.clear();
                } catch (Exception ex) {
                    updatesArea.appendText("Post Error: " + ex.getMessage() + "\n");
                }
            }
        });

        primaryStage.setOnCloseRequest(event -> {
            try {
                if (hubStub != null && callback != null) {
                    hubStub.leaveHub(callback, userName);
                }
            } catch (Exception ex) { }
            System.exit(0);
        });

        primaryStage.setTitle("Bengaluru Cares");
        primaryStage.setScene(new Scene(layout, 450, 550));
        primaryStage.show();
    }

    public static void main(String[] args) { launch(args); }
}