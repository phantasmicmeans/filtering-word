package com.filtering.file.upload.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FileEmptyException extends RuntimeException {

    private static final String message = "파일이 비어있습니다.";

    public FileEmptyException() { super(message); }

}
