/*
 * Copyright ©2022-2022 Howie Young, All rights reserved.
 * Copyright ©2022-2022 杨浩宇，保留所有权利。
 */

package com.github.howieyoung91.farseer.core.annotation;

import com.github.howieyoung91.farseer.core.entity.Document;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * @author Howie Young
 * @version 1.0
 * @since 1.0 [2022/11/17 13:00]
 */
@Component
@Aspect
@Slf4j
public class RecordAspect {
    @Pointcut("@annotation(com.github.howieyoung91.farseer.core.annotation.Log.IndexedDocument)")
    public void logIndexedDocument() {}

    @Pointcut("@annotation(com.github.howieyoung91.farseer.core.annotation.Log.Search)")
    public void logSearch() {}

    @Pointcut("@annotation(com.github.howieyoung91.farseer.core.annotation.Log.Delete)")
    public void logDelete() {}

    @Around("logIndexedDocument()")
    public Object log(ProceedingJoinPoint pjp) throws Throwable {
        Object              result    = pjp.proceed();
        Object              source    = pjp.getArgs()[0];
        Log.IndexedDocument ann       = parseMethod(pjp);
        List<Document>      documents = convert2Documents(source, ann);
        String              operator  = ann.operator();
        log.info("{} {}", operator, documents);
        return result;
    }

    @Around("logSearch()")
    public Object supportLogSearch(ProceedingJoinPoint pjp) throws Throwable {
        Object     result       = pjp.proceed();
        Method     method       = getTargetMethod(pjp);
        Log.Search logSearchAnn = method.getAnnotation(Log.Search.class);
        String     operator     = logSearchAnn.operator();
        log.info("{} {} {}", operator, pjp.getArgs()[0], pjp.getArgs()[1]); // word, page
        return result;
    }

    @Around("logDelete()")
    public Object supportLogDelete(ProceedingJoinPoint pjp) throws Throwable {
        Object     result       = pjp.proceed();
        Method     method       = getTargetMethod(pjp);
        Log.Search logSearchAnn = method.getAnnotation(Log.Search.class);
        String     operator     = logSearchAnn.operator();
        Object     documentId   = pjp.getArgs()[0];
        log.info("{} {} ", operator, documentId);
        return result;
    }

    private static List<Document> convert2Documents(Object source, Log.IndexedDocument ann) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Class<? extends Converter> converterClass = ann.convert();
        Converter                  converter      = converterClass.getDeclaredConstructor().newInstance();
        Object                     documents      = converter.convert(source);
        if (documents instanceof List) {
            return (List<Document>) documents;
        }
        else {
            throw new UnsupportedOperationException();
        }
    }

    private static Log.IndexedDocument parseMethod(ProceedingJoinPoint pjp) {
        Method              method     = getTargetMethod(pjp);
        Log.IndexedDocument annotation = method.getAnnotation(Log.IndexedDocument.class);
        Objects.requireNonNull(annotation,
                "Cannot log indexed documents. Cause: found no @RecordIndexedDocument on method [" + method + "].");
        return annotation;
    }

    private static Method getTargetMethod(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        return signature.getMethod();
    }
}
