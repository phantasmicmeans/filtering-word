package com.filtering.file.upload.data;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * 금칙어 request model
 */
@Getter
@Setter
@RequiredArgsConstructor
public class RequestF {
    @NotNull //null만 check, empty는 validator로 진행. 둘다 annotation으로 해결하고 싶다면 @NotEmpty
    @JsonProperty("requestStr")
    private String requestStr;
}
