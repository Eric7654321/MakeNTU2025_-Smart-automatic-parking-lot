package com.makentu2025.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 後端統一返回結果
 * @param <T>
 */
@Data
public class Result<T> implements Serializable {

    public static final int SUCCESS = 1;
    public static final int FAIL = 0;

    private Integer code; //1為成功，0為失敗
    private String msg; //錯誤資訊
    private T data; //資料

    /**
     * 呼叫端一律問這個，不要自己比對 code。
     * 裸寫 code == 0 兩種意思都讀得通，寫反了編譯器也不會說話。
     */
    public boolean succeeded() {
        return code != null && code == SUCCESS;
    }

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = SUCCESS;
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = SUCCESS;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<T>();
        result.msg = msg;
        result.code = FAIL;
        return result;
    }

}
