package com.lagunex.charts;

import java.time.LocalDateTime;
import java.util.ResourceBundle;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.text.Text;

/**
 * Class that displays a context menu that opens a popup window with tweets
 * when clicked
 * 
 * @author Carlos A. Henríquez Q. <carlos.henriquez@lagunex.com>
 */
public class TweetMenu {

    private final ResourceBundle bundle;
    private ContextMenu menu;
    MenuItem viewTweet;
    
    TweetMenu(ResourceBundle bundle) {
        this.bundle = bundle;
        if (menu == null) {
            menu = new ContextMenu();
            viewTweet = new MenuItem(bundle.getString("menu.viewTweets"));
            menu.getItems().add(viewTweet);
        }
    }

    /**
     * shows the context menu and configure the popup window to show
     * tweets that satisfy the input query parameters
     * 
     * @param target UI element that ask for the context menu. It contains the
     *               text to perform the query
     * @param start time range for the query. Inclusive.
     * @param end time range for the query. Exclusive.
     */
    void loadMenu(Text target, LocalDateTime start, LocalDateTime end) {
        String query = target.getText();  
        if (isTime(query) || isSentiment(query)) {
            viewTweet.setText(bundle.getString("menu.viewTweets")+query);
            viewTweet.setOnAction(action -> {
                TweetPopup pop = new TweetPopup();
                pop.showTweetsWith(query,start,end);
            });
            menu.show(target, Side.BOTTOM, 0, 0);
        }
    }

    private boolean isTime(String text) {
        return text.contains("2015");
    }

    private boolean isSentiment(String text) {
        return !text.matches(".*\\d.*") && !text.contains("others");
    }
}
