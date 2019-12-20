package com.demo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Slf4j
public class AnyExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result exceptionError(Exception e){
        // TODO: 2019/12/19 进入一个异常的页面
        System.out.println("进入了异常处理");
        System.out.println("异常信息是::::::"+e.getMessage());
        return Result.fail(ResultCode.OTHER_ERROR);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Result validException(MethodArgumentNotValidException e){
        FieldError fieldError = e.getBindingResult().getFieldError();
        System.out.println("进入了非空异常处理");
        log.info("信息::{}",fieldError.getField());
        return Result.fail(false,fieldError.getDefaultMessage());
    }



}
