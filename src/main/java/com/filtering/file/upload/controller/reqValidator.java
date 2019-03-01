package com.filtering.file.upload.controller;

import com.filtering.file.upload.exception.DataInvalidException;
import com.filtering.file.upload.exception.DataNullException;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class reqValidator {

    private String[] words = {"bword","wword"};
    /**
     * request null check
     * @param request
     */
    public String validateRequest(String request) {
        if(!request.isEmpty()) return request;
        else {
            System.out.println("validation in");
            throw new DataNullException();
        }
    }

    /**
     * type-check : type = wword/bword
     * @param wordType
     * @return
     */
    public String validateType(String wordType) {
        this.validateRequest(wordType);
        if(Arrays.stream(words)
                 .anyMatch(word -> word.equals(wordType)))
            return wordType;
        else
            throw new DataInvalidException();
    }

}
