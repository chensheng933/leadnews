package com.heima.common.exception;

import com.heima.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
//@ControllerAdvice   //不会返回json
@RestControllerAdvice //会返回json  @ControllerAdvice + @ResponseBody
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     */
    @ExceptionHandler(value = LeadNewsException.class)
    public ResponseResult handleLeadNewsException(LeadNewsException e){
        return ResponseResult.errorResult(e.getStatus(),e.getMessage());
    }

    /**
     * 捕获系统异常
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseResult handleException(Exception e){
        return ResponseResult.errorResult(500,"服务器繁忙，原因："+e.getMessage());
    }
}
