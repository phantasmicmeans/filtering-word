package com.filtering.file.upload.exception;

import org.omg.SendingContext.RunTime;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * filtering에 적용될 Exception
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DataNullException extends RuntimeException {

    private static final String message = "입력이 비어있습니다.";

    public DataNullException(){
        super(message); // 임시 코드
    }

}
