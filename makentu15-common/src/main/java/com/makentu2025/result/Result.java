package com.makentu2025.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 後端統一返回結果
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {

    private Integer code; //1為成功，0為失敗
    private String msg; //錯誤資訊
    private T data; //資料

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = 1;
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 1;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = 0;
        return result;
    }

}
