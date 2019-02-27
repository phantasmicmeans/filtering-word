package com.filtering.file.upload.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RequestWord {

    @JsonProperty("word")
    private List<String> wordList;

    public List<String> getWordList() {
        return wordList;
    }

    public void setWordList(List<String> wordList) {
        this.wordList = wordList;
    }
}
