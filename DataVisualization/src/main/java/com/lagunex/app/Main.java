package com.lagunex.app;

import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX standalone application to visualize data from a Vertica database
 * 
 * @author Carlos A. Henríquez Q. <carlos.henriquez@lagunex.com>
 */
public class Main extends Application {
    
    ResourceBundle i8n;
    
    @Override
    public void start(Stage stage) throws Exception {
        i8n = ResourceBundle.getBundle("com.lagunex.app.main");
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"),i8n);
        
        Scene scene = new Scene(root, 800, 600);
        
        stage.setTitle(i8n.getString("window.title"));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
