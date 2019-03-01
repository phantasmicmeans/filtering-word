package com.filtering.file.upload.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
public class RequestWord {

    @JsonProperty("word")
    private List<String> wordList;
}
