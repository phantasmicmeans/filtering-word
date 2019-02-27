package com.filtering.file.upload.redis;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ObjectMapper 기능 확장 클래스
 */
public class NObjectMapper extends ObjectMapper {
    public NObjectMapper(){
        getSerializerProvider().setNullValueSerializer(new NullToEmptyStringSerializer());
    }
}
