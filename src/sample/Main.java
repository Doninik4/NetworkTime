package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private Thread serverThread;
    private TCPServer server;

    @Override
    public void start(Stage primaryStage) throws Exception{
        LeavePrivileges.leaveAllPrivileges();
        server = new TCPServer();
        serverThread = new Thread(server);
        serverThread.start();
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Network clock");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    @Override
    public void stop(){
        try {
            server.stopServer();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
        // Save file
    }


    public static void main(String[] args) {
        launch(args);
    }
}
