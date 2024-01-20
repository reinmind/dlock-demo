package org.zx.util;

import com.google.gson.Gson;

public class GsonUtil {
    private static Gson gson = new Gson();

    public static String toString(Object o){
        return gson.toJson(o);
    }

    public static <T> T fromJSON(String json,Class<T> clazz){
        return gson.fromJson(json,clazz);
    }
}
