package com.filtering.file.upload.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ResponseF<T> {

    private boolean matched;
    private T converted_data;

    @JsonCreator
    public ResponseF(
            @JsonProperty("matched") boolean matched,
            @JsonProperty("converted") T converted_data) {
        this.matched = matched;
        this.converted_data = converted_data;
    }
}
