package com.demo.exception;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result implements Serializable {
    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private boolean success;
    private Object data;

    public Result(ResultCode resultCode, boolean success, Object data) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.success = success;
        this.data = data;
    }

    public Result(ResultCode resultCode, boolean success) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.success = success;
    }

    public Result(int code,boolean success,String message) {
        this.code = code;
        this.message = message;
        this.success = success;
    }

    public Result(int code,boolean success,String message,Object data) {
        this.code = code;
        this.message = message;
        this.success = success;
        this.data = data;
    }

    public static Result success(){
        return new Result(200,true,"成功");
    }

    public static Result success(Object data){
        return new Result(200,true,"成功",data);
    }

    public static Result fail(ResultCode resultCode){
        return new Result(resultCode,false);
    }

    public static Result fail(boolean success,String message){
        return new Result(900,false,message);
    }

}
