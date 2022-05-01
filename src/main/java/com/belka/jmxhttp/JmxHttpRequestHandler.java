package com.belka.jmxhttp;

import com.belka.jmxhttp.exception.MBeanNotFoundException;
import com.belka.jmxhttp.register.MBeanDtoMapper;
import com.belka.jmxhttp.register.MBeanInfoRegister;
import com.belka.jmxhttp.register.dto.MBeanArgumentDto;
import com.belka.jmxhttp.register.dto.MBeanDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.servlet.HandlerMapping;

import javax.management.modelmbean.ModelMBeanInfo;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Slf4j
public class JmxHttpRequestHandler implements HttpRequestHandler {

    private final MBeanMethodInvoker mBeanMethodInvoker;
    private final MBeanInfoRegister mBeanInfoRegister;
    private final ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private final ThreadLocal<HttpServletRequest> requestThreadLocal = new ThreadLocal<>();
    private final ThreadLocal<HttpServletResponse> responseThreadLocal = new ThreadLocal<>();


    public JmxHttpRequestHandler(MBeanMethodInvoker mBeanMethodInvoker, MBeanInfoRegister mBeanInfoRegister) {
        this.mBeanMethodInvoker = mBeanMethodInvoker;
        this.mBeanInfoRegister = mBeanInfoRegister;
    }

    @Override
    public void handleRequest(HttpServletRequest request,
                              HttpServletResponse response) throws IOException {
        try {
            requestThreadLocal.set(request);
            responseThreadLocal.set(response);

            String path = extractPath();
            processRequest(path, request.getMethod());
        } catch (Exception e) {
            log.error("", e);
            doResponseWithStatus(500, e);
        } finally {
            requestThreadLocal.remove();
            responseThreadLocal.remove();
        }
    }

    private void processRequest(String path, String method) throws IOException {
        if ("GET".equals(method)) {
            if ("".equals(path)) {
                doList();
                return;
            }
            if (splitPath(path, 1) == null) {
                doResponseWithStatus(400);
                return;
            }
            doBeanOperationList(path);
        }
        if ("POST".equals(method)) {
            executeMBeanMethod(path);
            return;
        }
        doResponseWithStatus(404);
    }

    private void doBeanOperationList(String beanName) throws IOException {
        ModelMBeanInfo info = mBeanInfoRegister.getModelMBeanInfo(beanName);
        if (info == null) {
            doResponseWithStatus(404);
            return;
        }
        doResponse(MBeanDtoMapper.getDto(beanName, info));
    }

    private void doList() throws IOException {
        List<MBeanDto> mBeanDtoList = MBeanDtoMapper.getDto(mBeanInfoRegister.getMBeans());
        doResponse(mBeanDtoList);
    }

    private void executeMBeanMethod(String beanNameMethod) throws IOException {
        String[] paths = splitPath(beanNameMethod, 2);
        if (paths == null) {
            doResponseWithStatus(400);
            return;
        }
        String beanName = paths[0];
        String methodName = paths[1];

        log.info("Executing {}.{}", beanName, methodName);

        Object result;
        try {
            MBeanArgumentDto[] arguments = mapper.readValue(requestThreadLocal.get().getReader(), MBeanArgumentDto[].class);

            result = mBeanMethodInvoker.invoke(beanName, methodName, arguments);
        } catch (MBeanNotFoundException | NoSuchMethodException e) {
            doResponseWithStatus(404, e);
            return;
        } catch (IllegalArgumentException e) {
            doResponseWithStatus(400, e);
            return;
        }
        doResponse(result);
    }

    private String extractPath() {
        return (String) requestThreadLocal.get().getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    }

    private String[] splitPath(String path, int expected) {
        String[] paths = path.split("/");
        if (paths.length != expected) {
            return null;
        }
        return paths;
    }

    private void doResponse(Object dto) throws IOException {
        responseThreadLocal.get().setCharacterEncoding("UTF-8");
        responseThreadLocal.get().setContentType("application/json");
        mapper.writeValue(responseThreadLocal.get().getWriter(), dto);
    }

    private void doResponseWithStatus(int status) throws IOException {
        responseThreadLocal.get().sendError(status);
    }

    private void doResponseWithStatus(int status, Exception ex) throws IOException {
        responseThreadLocal.get().setStatus(status);
        ex.printStackTrace(responseThreadLocal.get().getWriter());
    }
}