package com.belka.jmxhttp.register.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
public class MBeanOperationDto {
    private String name;
    private String description;
    private String returnType;
    private List<MBeanParameter> parameters;
}
