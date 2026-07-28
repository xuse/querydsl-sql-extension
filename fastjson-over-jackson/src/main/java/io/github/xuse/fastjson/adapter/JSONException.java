package io.github.xuse.fastjson.adapter;

/**
 * JSON 操作异常（兼容 fastjson 的 com.alibaba.fastjson.JSONException）。
 */
public class JSONException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public JSONException(String message) {
        super(message);
    }

    public JSONException(String message, Throwable cause) {
        super(message, cause);
    }
}
