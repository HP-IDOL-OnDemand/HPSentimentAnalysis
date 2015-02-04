package com.lagunex.charts;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ChartController {
    public void update(List<Map<String, Object>> data, LocalDateTime start, LocalDateTime end); 
}
