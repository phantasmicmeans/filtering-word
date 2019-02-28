package com.filtering.file.upload.controller;

import com.filtering.file.upload.exception.DataNullException;
import org.springframework.stereotype.Component;

@Component
public class reqValidator {

    private String[] word = {"bword","wword"};
    /**
     * request null check
     * @param request
     */
    public String validateRequest(String request) {

        if(!request.isEmpty()) return request;
        else
            throw new DataNullException();
    }

    public String validateType(String wordType) {
        this.validateRequest(wordType);

        if(wordType.equals(word[0]))
            return word[0];
        else
            return word[1];
    }

}
