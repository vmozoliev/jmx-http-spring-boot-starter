package com.belka.jmxhttp.register.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class MBeanDto {
    private String beanName;
    private String classname;
    private String description;
    private List<MBeanOperationDto> operations;
}
