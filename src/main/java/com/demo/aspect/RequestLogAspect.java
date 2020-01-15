package com.demo.aspect;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springblade.core.tool.jackson.JsonUtil;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.core.tool.utils.WebUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.SynthesizingMethodParameter;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author songxy
 * @date 2020/1/2
 */
@Slf4j
@Aspect
@Configuration
public class RequestLogAspect {


    @Around("@within(org.springframework.stereotype.Controller)" +
            " || @within(org.springframework.web.bind.annotation.RestController)")
    public Object around(ProceedingJoinPoint point) throws Throwable {

        //获取类名
        String className = point.getTarget().getClass().getName();
        //获取方法
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();
        //请求参数
        Object[] args = point.getArgs();


        final HashMap<String, Object> map = Maps.newHashMap();
        //处理请求参数
        for (int i = 0; i < args.length; i++) {
            //获取方法参数
            MethodParameter methodParameter = getMethodParameter(method, i);
            //如果是PathVariable 参数,跳过不计
            PathVariable parameterAnnotation = methodParameter.getParameterAnnotation(PathVariable.class);
            if (parameterAnnotation != null) {
                continue;
            }

            RequestBody requestBody = methodParameter.getParameterAnnotation(RequestBody.class);
            Object value = args[i];
            Class<?> type = value.getClass();
            BeanInfo beanInfo = Introspector.getBeanInfo(type);
            if (requestBody != null && value != null) {
//                Splitter splitter = Splitter.onPattern("(\\()([0-9a-zA-Z\\.\\/\\=])*(\\))");
//                List<String> list = splitter.splitToList(value.toString());
                Map<String, Object> stringObjectMap = BeanUtil.toMap(value);
                log.info("map:" + stringObjectMap);
                map.putAll(stringObjectMap);
                continue;
            }
            // 处理 List
            if (value instanceof List) {
                value = ((List) value).get(0);
            }
            // 处理 参数
            if (value instanceof HttpServletRequest) {
                map.putAll(((HttpServletRequest) value).getParameterMap());
            } else if (value instanceof WebRequest) {
                map.putAll(((WebRequest) value).getParameterMap());
            } else if (value instanceof MultipartFile) {
                MultipartFile multipartFile = (MultipartFile) value;
                String name = multipartFile.getName();
                String fileName = multipartFile.getOriginalFilename();
                map.put(name, fileName);
            } else if (value instanceof HttpServletResponse) {
            } else if (value instanceof InputStream) {
            } else if (value instanceof InputStreamSource) {
            } else {
                // 参数名
                RequestParam requestParam = methodParameter.getParameterAnnotation(RequestParam.class);
                String paramName;
                if (requestParam != null && StringUtil.isNotBlank(requestParam.value())) {
                    paramName = requestParam.value();
                } else {
                    paramName = methodParameter.getParameterName();
                }
                map.put(paramName, value);
            }

        }

        HttpServletRequest request = WebUtil.getRequest();
        String requestURI = Objects.requireNonNull(request).getRequestURI();
        String requestMethod = request.getMethod();


        StringBuilder beforeReqLog = new StringBuilder(300);
        //日志参数
        List<Object> beforeReqArgs = new ArrayList<>();
        beforeReqLog.append("\n ==========request===========\n");

        beforeReqLog.append("===> {}: {}  ");
        beforeReqArgs.add(requestMethod);
        beforeReqArgs.add(requestURI);

        if (map == null){
            beforeReqLog.append("\n");
        }else{
            beforeReqLog.append("  Parameters:{}\n");
            beforeReqArgs.add(JsonUtil.toJson(map));
        }

        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()){
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            beforeReqLog.append("===Headers=== {}:{}\n");
            beforeReqArgs.add(headerName);
            beforeReqArgs.add(headerValue);
        }
        beforeReqLog.append("========Request End======\n");
        long startTime = System.nanoTime();
        log.info(beforeReqLog.toString(), beforeReqArgs.toArray());

        //aop后日志
        StringBuilder afterReqLog = new StringBuilder(300);
        List<Object> afterReqArgs = new ArrayList<>();
        afterReqLog.append("\n\n==========Response start=============\n");

        try {
            Object result = null;
            result = point.proceed();
            afterReqLog.append("====Result:{}\n");
            afterReqArgs.add(JsonUtil.toJson(result));
            return result;
        }finally {
            long l = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            afterReqLog.append("<==={}:{}({}ms)\n");
            afterReqArgs.add(requestMethod);
            afterReqArgs.add(requestURI);
            afterReqArgs.add(l);
            afterReqLog.append("============Response End============");
            log.info(afterReqLog.toString(),afterReqArgs.toArray());
        }

//        //开始时间
//        LocalDateTime now = LocalDateTime.now();
//        long curTime = System.currentTimeMillis();
//
//
//        //获取方法参数
//        MethodParameter methodParameter = getMethodParameter(method, 0);
//        Optional<String> parameterName = Optional.ofNullable(methodParameter.getParameterName());
//        Optional<String> name = Optional.ofNullable(methodParameter.getParameter().getName());
//
//        log.info("从methodParameter中取出的参数名:::::" + parameterName.orElse("null"));
//        log.info("从methodParameter中取出的参数:::::" + name.orElse("null"));
//
//        for (Object arg : args) {
//            log.info("args:::::::" + arg);
//        }
//
//        RequestParam requestParam = methodParameter.getParameterAnnotation(RequestParam.class);
//        PathVariable pathVariable = methodParameter.getParameterAnnotation(PathVariable.class);
//        RequestBody requestBody = methodParameter.getParameterAnnotation(RequestBody.class);
//
//
////            HttpServletRequest request = getRequest();
////            String contextPath = request.getContextPath();
////            String requestURI = request.getRequestURI();
//

//        log.info("测试::::::::::::" + className + "@@@@" + method);
//        log.info("测试req::::::::::::" + requestURI);
//        log.info("测试获取参数::::::::::::" + requestParam);
//        log.info("测试获取参数::::::::::::" + pathVariable);
//        log.info("测试获取参数::::::::::::" + requestBody);

    }

    private HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        return requestAttributes == null ? null : ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    private MethodParameter getMethodParameter(Method method, int parameterIndex) {
        MethodParameter methodParameter = new SynthesizingMethodParameter(method, parameterIndex);
        methodParameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
        return methodParameter;
    }
}
