package com.heima.common.exception;

import com.heima.common.dtos.AppHttpCodeEnum;
import lombok.Getter;

/**
 * 自定义异常类（业务异常）
 */
@Getter
public class LeadNewsException extends RuntimeException{
    private Integer status;//状态码

    public LeadNewsException(Integer status,String message){
        super(message);
        this.status = status;
    }

    public LeadNewsException(AppHttpCodeEnum codeEnum){
        super(codeEnum.getErrorMessage());
        this.status = codeEnum.getCode();
    }
}
