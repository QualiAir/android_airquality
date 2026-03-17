package com.concordia.qualiair;

import java.util.List;

public class HistoryResponse {

    // This variable name "data" MUST match the key in the Python JSON
    // because history.py backend code returns: {"data": results}
    //Reading is from class Reading, its like a Template <T> in cpp
    private List<Reading> data;

    // Getter lets the HistoryActivity grab the list to update the Chart
    public List<Reading> getData() {
        return data;
    }

    // Setter is used by the Gson library to pour the data in
    public void setData(List<Reading> data) {
        this.data = data;
    }
}