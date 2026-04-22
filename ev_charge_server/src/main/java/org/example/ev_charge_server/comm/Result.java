package org.example.ev_charge_server.comm;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 统一返回结果类
 */
@Accessors(chain = true)//开始链式编程
@Data
public class Result<T> {
    private Integer code;//状态码 404-找不到对象  500-服务器错误  200-🆗  400
    private String message;//附加文本信息
    private boolean success;//是否成功
    private T data;//附加返回数据

    /**
     * 成功返回结果
     * @param <T> 泛型
     * @return
     */
    public static <T> Result<T> ok(T c) {
        return new Result<T>().setSuccess(true).setCode(200).setData(c);
    }
    /**
     * 失败返回结果
     * @param <T> 泛型
     * @return
     */
    public static <T> Result<T> error(String msg) {
        return new Result<T>().setSuccess(false).setCode(500).setMessage(msg);
    }

//    public Result<T> setMsg(String msg) {
//        this.message = msg;
//        return this;
//    }
}
