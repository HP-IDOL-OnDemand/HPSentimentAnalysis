/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lagunex.charts;

import com.lagunex.vertica.Vertica;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

/**
 *
 * @author carloshq
 */
class TweetPopup {
    private final Vertica vertica;
    private final Stage stage;
    private final ListView list;

    TweetPopup() {
        this.vertica = Vertica.getInstance();
        this.stage = new Stage();
        this.list = new ListView();
    }

    void showTweetsWith(String text, LocalDateTime start, LocalDateTime end) {
        stage.setTitle(text);
        if (text.contains("2015")) {
            showTweetsWithTime(text);
        } else if (text.equals("positive") || text.equals("negative") || text.equals("neutral")) {
            showTweetsWithAggregate(text, start, end);
        } else {
            showTweetsWithSentiment(text, start, end);
        }
    }

    void showTweetsWithTime(String text) {
        Task<List<Map<String,Object>>> task = new Task(){
            @Override
            protected List<Map<String,Object>> call() throws Exception {
                return vertica.getTweetsWithTime(text);
            }
        };        
        new Thread(task).start();
        updateListAfter(task);
    }

    private void updateListAfter(Task<List<Map<String,Object>>> task) {
        task.setOnSucceeded(worker -> {
            List<Map<String,Object>> data = task.getValue();
            list.getItems().addAll(data.stream()
                .map(sample -> sample.get("time")+" - "+sample.get("message"))
                .collect(Collectors.toList())
            );
            stage.setScene(new Scene(list, 640, 480));
            stage.show();
        });
    }

    private void showTweetsWithAggregate(String text, LocalDateTime start, LocalDateTime end) {
        Task<List<Map<String,Object>>> task = new Task(){
            @Override
            protected List<Map<String,Object>> call() throws Exception {
                return vertica.getTweetsWithAggregate(text, start, end);
            }
        };        
        new Thread(task).start();
        updateListAfter(task);
    }
    
    private void showTweetsWithSentiment(String text, LocalDateTime start, LocalDateTime end) {
        Task<List<Map<String,Object>>> task = new Task(){
            @Override
            protected List<Map<String,Object>> call() throws Exception {
                return vertica.getTweetsWithSentiment(text, start, end);
            }
        };        
        new Thread(task).start();
        updateListAfter(task);
    }
}
