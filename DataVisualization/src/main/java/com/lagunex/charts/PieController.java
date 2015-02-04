package com.lagunex.charts;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.text.Text;

/**
 * The UI for this controller is defined in the resource file com.lagunex.charts.pie.fxml
 * 
 * It includes a table to show data on the left side and a pie chart on the right to visualize it
 * 
 * @author Carlos A. Henríquez Q. <carlos.henriquez@lagunex.com>
 */
public class PieController implements Initializable, ChartController {
    @FXML protected PieChart chart; 
    @FXML protected TableView<Map<String,Object>> table;
    private ResourceBundle bundle;
    private TweetMenu tweetMenu;
    private LocalDateTime start, end;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bundle = resources;
        initTable();
    }   

    private void initTable() {
        // TableColumn<S,T> receives objects of type T and display objects of type V using a CellValueFactory
        TableColumn<Map<String,Object>,String> label = new TableColumn<>(bundle.getString("chart.sentiment"));
        label.setCellValueFactory(
            cellDataFeature -> new ReadOnlyObjectWrapper<>(
                cellDataFeature.getValue().get("label").toString())
        );
               
        TableColumn<Map<String,Object>,Integer> total = new TableColumn<>(bundle.getString("chart.total"));
        total.setCellValueFactory(
            cellDataFeature -> new ReadOnlyObjectWrapper<>(
                new Integer(cellDataFeature.getValue().get("total").toString()))
        ); 
        table.getColumns().addAll(label, total);
    }

    @Override
    public void update(List<Map<String, Object>> data, LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
        updateTable(data);
        updateChart(data);
    }

    private void updateTable(List<Map<String, Object>> data) {
        table.setItems(FXCollections.observableList(data)); // Collections must be transformed into FXCollections
    }

    private void updateChart(List<Map<String, Object>> data) {
        chart.setData(FXCollections.observableArrayList(
            data.stream()
            .map(sample -> 
                    new PieChart.Data(
                            sample.get("label").toString(),
                            Double.parseDouble(sample.get("total").toString())
                    )
            )
            .collect(Collectors.toList())
        ));
    }

    @FXML
    protected void rightClickTable(ContextMenuEvent event) {
        if (event.getTarget() instanceof Text) {
            if (tweetMenu == null) {
                tweetMenu = new TweetMenu(bundle);
            }
            tweetMenu.loadMenu((Text) event.getTarget(),start,end);
        }
    }
}
