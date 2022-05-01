package com.belka.jmxhttp;

import com.belka.jmxhttp.exception.MBeanNotFoundException;
import com.belka.jmxhttp.register.MBeanDtoMapper;
import com.belka.jmxhttp.register.MBeanInfoRegister;
import com.belka.jmxhttp.register.dto.MBeanArgumentDto;
import com.belka.jmxhttp.register.dto.MBeanDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.servlet.HandlerMapping;

import javax.management.modelmbean.ModelMBeanInfo;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class JmxRequestHandler implements HttpRequestHandler {

    private Logger log = LoggerFactory.getLogger(JmxRequestHandler.class);

    private final MBeanExecutor mBeanExecutor;
    private final MBeanInfoRegister mBeanInfoRegister;
    private final ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private final ThreadLocal<HttpServletRequest> requestThreadLocal = new ThreadLocal<>();
    private final ThreadLocal<HttpServletResponse> responseThreadLocal = new ThreadLocal<>();


    public JmxRequestHandler(MBeanExecutor mBeanExecutor, MBeanInfoRegister mBeanInfoRegister) {
        this.mBeanExecutor = mBeanExecutor;
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
            if (splitPaths(path, 1) == null) {
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
        List<MBeanDto> mBeanDtoList = MBeanDtoMapper.getDtos(mBeanInfoRegister.getMBeans());
        doResponse(mBeanDtoList);
    }

    private void executeMBeanMethod(String beanNameMethod) throws IOException {
        String[] paths = splitPaths(beanNameMethod, 2);
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

            result = mBeanExecutor.execute(beanName, methodName, arguments);
        } catch (MBeanNotFoundException | NoSuchMethodException e) {
            doResponseWithStatus(404, e);
            return;
        } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            doResponseWithStatus(400, e);
            return;
        }
        doResponse(result);
    }

    private String extractPath() {
        return (String) requestThreadLocal.get().getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
    }

    private String[] splitPaths(String path, int expected) {
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