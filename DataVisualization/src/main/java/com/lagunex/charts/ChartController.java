/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lagunex.charts;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 *
 * @author carloshq
 */
public interface ChartController {
    public void update(List<Map<String, Object>> data, LocalDateTime start, LocalDateTime end); 
}
