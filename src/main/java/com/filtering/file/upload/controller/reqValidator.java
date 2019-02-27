package com.filtering.file.upload.controller;

import com.filtering.file.upload.exception.DataNullException;
import org.springframework.stereotype.Component;

@Component
public class reqValidator {

    /**
     * request null check
     * @param request
     */
    public String validateRequest(String request) {

        if(!request.isEmpty()) return request;
        else
            throw new DataNullException();
    }

}
