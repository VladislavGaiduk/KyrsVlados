package com.server.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.LocalDateTime;

public class Serializer {
    public static String toJson(Object obj) {
        try {
            Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
            return gson.toJson(obj);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to serialize object to JSON: " + e.getMessage(), e);
        }
    }
}
