/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lagunex.charts;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.text.Text;

/**
 *
 * @author carloshq
 */
public class HistogramController implements Initializable, ChartController {
    @FXML protected LineChart<String, Number> chart; 
    @FXML protected TableView<Map<String,Object>> table;
    private ResourceBundle bundle;
    private LocalDateTime start, end;
    private TweetMenu tweetMenu = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bundle = resources;
    }
    
    @Override
    public void update(List<Map<String, Object>> data, LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
        
        if (data.size() > 0 && data.get(0).get("label") != null) {
            updateWithThreeColumns(data);
        } else {
            updateWithTwoColumns(data);
        }
    }
    
    private void updateWithThreeColumns(List<Map<String, Object>> data) {
        updateThreeColumnsTable(data);
        updateThreeColumnsChart(data); 
    }
    
    private void updateThreeColumnsTable(List<Map<String, Object>> data) {
        initThreeColumnsTable();
        updateTableData(data);
    } 

    private void initThreeColumnsTable() {
        initTwoColumnsTable();
        table.getColumns().get(1).setText(bundle.getString("chart.total"));
        
        TableColumn<Map<String,Object>,String> label = new TableColumn<>(bundle.getString("chart.sentiment"));
        label.setCellValueFactory(
            cellDataFeature -> new ReadOnlyObjectWrapper<>(
                cellDataFeature.getValue().get("label").toString())
        );
        table.getColumns().add(0, label);
    }   
 
    private void initTwoColumnsTable() {
        TableColumn<Map<String,Object>,String> time = new TableColumn<>(bundle.getString("chart.time"));
        time.setCellValueFactory(
            cellDataFeature -> new ReadOnlyObjectWrapper<>(
                cellDataFeature.getValue().get("time").toString())
        );
               
        TableColumn<Map<String,Object>,Double> total = new TableColumn<>(bundle.getString("chart.avg"));
        total.setCellValueFactory(
            cellDataFeature -> new ReadOnlyObjectWrapper<>(
                new Double(cellDataFeature.getValue().get("total").toString()))
        ); 
        table.getColumns().addAll(time, total);
    }
    
    private void updateTableData(List<Map<String, Object>> data) {
        table.setItems(FXCollections.observableList(data));
    }
    
    private void updateThreeColumnsChart(List<Map<String, Object>> data) {
        initXAxis();
        chart.getYAxis().setLabel(bundle.getString("chart.three.yaxis"));
        updateThreeColumnsChartData(data);
    }
   
    private void initXAxis() {
        chart.getXAxis().setLabel(bundle.getString("chart.xaxis"));
    } 
    
    private void updateThreeColumnsChartData(List<Map<String, Object>> data) {
        Map<String,XYChart.Series> series = new HashMap<>();
        
        data.stream().forEachOrdered(sample -> {
            String label = sample.get("label").toString();
            XYChart.Series serie;
            if (series.containsKey(label)) {
                serie = series.get(label);
            } else {
                serie = new XYChart.Series<>();
                serie.setName(label);
                series.put(label, serie);
            }
            serie.getData().add(new XYChart.Data<>(sample.get("time").toString(),sample.get("total")));
        });
        
        series.values().stream().forEach((serie) -> {
            chart.getData().add(serie);
        });
    }
    
    private void updateWithTwoColumns(List<Map<String, Object>> data) {
        updateTwoColumnsTable(data);
        updateTwoColumnsChart(data); 
    }

    private void updateTwoColumnsTable(List<Map<String, Object>> data) {
        initTwoColumnsTable();
        updateTableData(data);
    }  

    private void updateTwoColumnsChart(List<Map<String, Object>> data) {
        initXAxis();
        chart.getYAxis().setLabel(bundle.getString("chart.two.yaxis"));
        updateTwoColumnsChartData(data);
    }

    private void updateTwoColumnsChartData(List<Map<String, Object>> data) {
        XYChart.Series series = new XYChart.Series(
            bundle.getString("chart.aggregate"),
            FXCollections.observableArrayList(
                data.stream()
                .map(sample -> new XYChart.Data<>(sample.get("time").toString(),sample.get("total")))
                .collect(Collectors.toList())
            )
        );
        chart.getData().add(series);
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
