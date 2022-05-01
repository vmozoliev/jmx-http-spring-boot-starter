package com.belka.jmxhttp.register.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class MBeanParameter {
    private String name;
    private String type;
    private String descriptor;
    private String description;
}
