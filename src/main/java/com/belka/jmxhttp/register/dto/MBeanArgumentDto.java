package com.belka.jmxhttp.register.dto;

import lombok.*;

@Setter
@Getter
public class MBeanArgumentDto {
    private String type;
    private String value;
}