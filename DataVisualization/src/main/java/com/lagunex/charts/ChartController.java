package com.lagunex.charts;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interface that defines the behaviou of updating a UI given some data.
 * 
 * Useful for polymorphism in ChartResult
 * 
 * @author Carlos A. Henríquez Q. <carlos.henriquez@lagunex.com>
 */
public interface ChartController {
    
    /**
     * Updates the content of this chart with data and stores the related start
     * and end times for further queries
     * 
     * @param data result from vertica to visualize as a graph
     * @param start inclusive
     * @param end exclusive
     */
    public void update(List<Map<String, Object>> data, LocalDateTime start, LocalDateTime end); 
}
