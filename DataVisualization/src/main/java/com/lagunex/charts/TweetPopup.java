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
 * This class defines a popup window that show tweets from a given query.
 * It interacts directly with the Vertica service, just like com.lagunex.app.MainController
 * 
 * @author Carlos A. Henríquez Q. <carlos.henriquez@lagunex.com>
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

    /**
     * Queries Vertica for tweets given some parameters
     * @param text could be a string representing a datetime or a topic
     * @param start time range for the query. Inclusive.
     * @param end time range for the query. Exclusive.
     */
    void showTweetsWith(String text, LocalDateTime start, LocalDateTime end) {
        stage.setTitle(text);
        if (text.contains("2015")) {
            showTweetsWithTime(text);
        } else if (text.equals("positive") || text.equals("negative") || text.equals("neutral")) {
            showTweetsWithAggregate(text, start, end);
        } else {
            showTweetsWithTopic(text, start, end);
        }
    }

    private void showTweetsWithTime(String text) {
        Task<List<Map<String,Object>>> task = new Task(){
            @Override
            protected List<Map<String,Object>> call() throws Exception {
                return vertica.getTweetsWithTime(text);
            }
        };        
        new Thread(task).start();
        updateListOnSuccess(task);
    }

    /**
     * @param task elements of List<Map<String,Object>> must be {"time": datetime, "message": string} 
     */
    private void updateListOnSuccess(Task<List<Map<String,Object>>> task) {
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
        updateListOnSuccess(task);
    }
    
    private void showTweetsWithTopic(String text, LocalDateTime start, LocalDateTime end) {
        Task<List<Map<String,Object>>> task = new Task(){
            @Override
            protected List<Map<String,Object>> call() throws Exception {
                return vertica.getTweetsWithTopic(text, start, end);
            }
        };        
        new Thread(task).start();
        updateListOnSuccess(task);
    }
}
