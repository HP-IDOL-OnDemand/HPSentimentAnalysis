package com.lagunex.charts;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

/**
 * Class that loads a chart GUI from a resource file and update its content
 * with the given data.
 * 
 * This class is defined so that you can create different type of charts
 * regardless of its internal implementation
 * 
 * @author Carlos A. Henríquez Q. <carlos.henriquez@lagunex.com>
 */
public class ChartResult {

    /**
     * Indicates the type of chart to load
     */
    public enum Type {
        Line("line.fxml"),
        Pie("pie.fxml");

        private String fxml;
        
        Type(String fxml) {
            this.fxml = fxml;
        }
    }
    
    private final Node node;
    
    /**
     * Load a chart UI from a resource file according to its type and
     * update its content with data, start and end
     * 
     * @param type
     * @param data
     * @param start
     * @param end
     * @throws Exception 
     */
    public ChartResult(Type type, List<Map<String, Object>> data, LocalDateTime start, LocalDateTime end)
            throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource(type.fxml), ResourceBundle.getBundle("com.lagunex.charts.charts")
        );
        node = loader.load();
        ((ChartController)loader.getController()).update(data, start, end);
    }

    /**
     * 
     * @return the UI element to display in the application
     */
    public Node getContent() {
       return node; 
    }
}
