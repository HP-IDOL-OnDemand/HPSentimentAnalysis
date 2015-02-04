/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lagunex.app;

import com.lagunex.charts.ChartResult;
import com.lagunex.vertica.Vertica;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author carloshq
 */
public class MainController implements Initializable {
    
    @FXML private TextField dateStartHour;
    @FXML private TextField dateStartMinute;
    
    @FXML private TextField dateEndHour;
    @FXML private TextField dateEndMinute;

    @FXML private Button updateCharts;

    @FXML private TabPane chartTabs;

    @FXML private Label status;
    @FXML private ProgressBar progressBar;

    private LocalDateTime begin;
    private LocalDateTime end;
    
    private ResourceBundle bundle;

    private final Vertica vertica = Vertica.getInstance();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bundle = resources;
        initTimes();
        updateCharts();
    }

    private void initTimes() {
        long now = startProgress();
        Map<String, LocalDateTime> times = vertica.getDateRange();
        updateQueryStatus(stopProgress(now));
        
        begin = times.get("begin").withSecond(0);
        end = times.get("end").withSecond(0);

        dateStartHour.setText(begin.format(DateTimeFormatter.ofPattern("HH")));
        dateStartMinute.setText(begin.format(DateTimeFormatter.ofPattern("mm")));

        dateEndHour.setText(end.format(DateTimeFormatter.ofPattern("HH")));
        dateEndMinute.setText(end.format(DateTimeFormatter.ofPattern("mm")));
    }

    private long startProgress() {
        progressBar.setProgress(-1);
        status.setText(bundle.getString("status.loading"));
        return System.currentTimeMillis();
    }

    private long stopProgress(long start) {
        progressBar.setProgress(1);
        return System.currentTimeMillis()-start;
    }
    
    private void updateQueryStatus(long milliseconds) {
        progressBar.setProgress(1);
        status.setStyle("-fx-text-fill: black");
        status.setText(bundle.getString("status.time")+milliseconds);
    }
    
    @FXML
    protected void validateInputLength(KeyEvent event) {
        TextField source = (TextField) event.getSource();
        char digit = event.getCharacter().length() > 0 ? event.getCharacter().charAt(0) : 0;
        if (source.getText().length() >= 2 || digit < '0' || digit > '9') {
            event.consume();
        }
    }

    @FXML
    protected void updateCharts(ActionEvent event) {
        updateCharts();
    }

    private void updateCharts() {
        try {
            LocalDateTime queryBegin = LocalDateTime.of(2015,02,02,
                    Integer.parseInt(dateStartHour.getText()),Integer.parseInt(dateStartMinute.getText()));
            LocalDateTime queryEnd = LocalDateTime.of(2015,02,02,
                    Integer.parseInt(dateEndHour.getText()),Integer.parseInt(dateEndMinute.getText()));

            long now = startProgress();
            
            Task<List<ChartResult>> result = queryVertica(queryBegin, queryEnd);
            result.setOnSucceeded(worker->{
                updateUI(result.getValue());
                updateQueryStatus(stopProgress(now));
            });
            result.setOnFailed(worker -> {
                errorStatus(worker.getSource().getException().getLocalizedMessage());
            });
            
            new Thread(result).start();
        } catch (Exception e) {
            errorStatus(e.getLocalizedMessage());
        } 
    }

    private void updateUI(List<ChartResult> charts) {
        ObservableList<Tab> tabs = chartTabs.getTabs();
        for (int i=0; i<charts.size(); i++) {
            tabs.get(i).setContent(charts.get(i).getContent());
        }
    }

    private Task<List<ChartResult>> queryVertica(LocalDateTime begin, LocalDateTime end) throws Exception {
        return new Task<List<ChartResult>>(){
            @Override
            protected List<ChartResult> call() throws Exception {
                return Arrays.asList(
                    new ChartResult(ChartResult.Type.Pie, vertica.getAggregateTotal(begin, end), begin, end),
                    new ChartResult(ChartResult.Type.Line, vertica.getAggregateHistogram(begin, end), begin, end),
                    new ChartResult(ChartResult.Type.Pie, vertica.getSentimentTotal(begin, end), begin, end),
                    new ChartResult(ChartResult.Type.Line, vertica.getSentimentHistogram(begin, end), begin, end)
                );
            }
        }; 
    }

    private void errorStatus(String message) {
        status.setStyle("-fx-text-fill: red");
        status.setText(message);
    }
}
