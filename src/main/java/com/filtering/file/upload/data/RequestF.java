package com.filtering.file.upload.data;


import javax.validation.constraints.NotNull;

/**
 * 금칙어 request model
 */
public class RequestF {

    @NotNull
    private String requestStr;

    public String getRequestStr() {
        return requestStr;
    }

    public void setRequestStr(String requestStr) {
        this.requestStr = requestStr;
    }
}
