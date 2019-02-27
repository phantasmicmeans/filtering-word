package com.filtering.file.upload.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseF<T> {

    @JsonProperty("matched")
    private boolean matched;

    @JsonProperty("converted")
    private T converted_data;

    public ResponseF(boolean matched, T converted_data) {
        this.matched = matched;
        this.converted_data = converted_data;
    }

    public T getConverted_data() {
        return converted_data;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public void setConverted_data(T converted_data) {
        this.converted_data = converted_data;
    }
}
