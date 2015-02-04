/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lagunex.charts;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

/**
 *
 * @author carloshq
 */
public class ChartResult {

    public enum Type {
        Line("line.fxml"),
        Pie("pie.fxml");

        String fxml;
        
        Type(String fxml) {
            this.fxml = fxml;
        }
    }
    
    private final Node node;
    
    public ChartResult(Type type, List<Map<String, Object>> data, LocalDateTime start, LocalDateTime end)
            throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource(type.fxml), ResourceBundle.getBundle("com.lagunex.charts.charts")
        );
        node = loader.load();
        ((ChartController)loader.getController()).update(data, start, end);
    }

    public Node getContent() {
       return node; 
    }
}
