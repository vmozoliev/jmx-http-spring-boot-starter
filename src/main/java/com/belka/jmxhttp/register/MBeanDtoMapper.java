package com.belka.jmxhttp.register;

import com.belka.jmxhttp.register.dto.MBeanDto;
import com.belka.jmxhttp.register.dto.MBeanOperationDto;
import com.belka.jmxhttp.register.dto.MBeanParameter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.management.modelmbean.ModelMBeanInfo;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MBeanDtoMapper {
    public static List<MBeanDto> getDto(Map<String, ModelMBeanInfo> map) {
        return map.entrySet().stream()
                .map(entry -> getDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public static MBeanDto getDto(String beanName, ModelMBeanInfo info) {
        return MBeanDto.builder()
                .beanName(beanName)
                .classname(info.getClassName())
                .description(info.getDescription())
                .operations(Arrays.stream(info.getOperations())
                        .map(operation -> MBeanOperationDto.builder()
                                .name(operation.getName())
                                .description(operation.getDescription())
                                .returnType(operation.getReturnType())
                                .parameters(Arrays.stream(operation.getSignature())
                                        .map(parameter-> MBeanParameter.builder()
                                                .name(parameter.getName())
                                                .type(parameter.getType())
                                                .descriptor(parameter.getDescriptor().toString())
                                                .build())
                                        .collect(Collectors.toList()))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
