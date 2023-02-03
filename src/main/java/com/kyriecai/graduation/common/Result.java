package com.kyriecai.graduation.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 接口统一返回包装类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private String code;    //返回状态标识
    private String msg;     //返回信息，如错误信息
    private Object data;    //返回数据

    public static Result success() {
        return new Result(Constants.CODE_200, "", null);
    }

    public static Result success(Object data) {
        return new Result(Constants.CODE_200, "", data);
    }

    public static Result error(String code, String msg) {
        //具体错误信息返回
        return new Result(code, msg, null);
    }

    public static Result error() {  //默认错误信息返回
        return new Result(Constants.CODE_500, "系统错误", null);
    }
}
