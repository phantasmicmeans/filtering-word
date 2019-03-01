package com.filtering.file.upload.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DataInvalidException extends RuntimeException{

    private static final String message = "입력을 확인하세요.";

    public DataInvalidException(){ super(message); }
}
