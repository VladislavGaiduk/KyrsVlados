package com.server.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.gui.utils.AlertUtil;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;

public class Deserializer {
    public <T> T extractData(String data, Type type) {
        try {
            Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
            return gson.fromJson(data, type);
        } catch (JsonSyntaxException e) {
            AlertUtil.error("Deserializer error", "Deserialization failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public <T> List<T> extractListData(String data, Class<T> classOfT) {
        Type listType = TypeToken.getParameterized(List.class, classOfT).getType();
        return extractData(data, listType);
    }
}
