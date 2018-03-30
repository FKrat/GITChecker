/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gitcheckerapp;
import java.net.URL;
import java.nio.file.Paths;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author FilipKrat
 */
public class GitCheckerApp extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        URL path = Paths.get("./src/main/resources/gitcheckerapp/Home.fxml").toUri().toURL();
        Parent root = FXMLLoader.load(path);
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.setTitle("GitChecker");
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
