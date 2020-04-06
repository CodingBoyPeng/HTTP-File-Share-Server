package com.codeboy.fileclient.tools.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class JsonUtils {
    private static ObjectMapper mapper;

    static {
        // serialize Date to ISO 8601 in UTC
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        
    	mapper = new ObjectMapper()
    			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    			.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        		.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
        		.setDateFormat(dateFormat);
        		
    	// serialize BigDecimal to String
    	mapper.configOverride(BigDecimal.class).setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.STRING));
    }

    public static ObjectMapper getObjectMapper() {
    	return mapper;
    }
    
    @SuppressWarnings("unchecked")
    public static Map<String, Object> jsonToMap(String json) {
        try {
            return mapper.readValue(json, HashMap.class);
        } catch (IOException e) {
            return null;
        }
    }

    public static <T> T jsonToObject(String json, TypeReference<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            return null;
        }
    }
    
    public static <T> T jsonToObject(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * 将java类转换成json字符串
     *
     * @param pojo
     * @return
     */
    public static String pojoToJson(Object pojo) {
        try {
            String str = mapper.writeValueAsString(pojo);
            return str;
        } catch (Exception e) {
        	return null;
        }
    }

    /**
     * 将map类转换成json字符串
     *
     * @param params
     * @return
     */
    public static <T> String mapToJson(Map<String, T> params) {
        return mapToJson(params, true);
    }

    public static <T> String mapToJson(Map<String, T> params, boolean includeNullValue) {
        String result = "{";

        for (String key : params.keySet()) {
            T value = params.get(key);
            if (value != null) {
                String valueJson = objectToJson(value);
                if (value instanceof Number) {
                    result += "\"" + key + "\":" + valueJson + ",";
                } else {
                    result += "\"" + key + "\":\"" + valueJson + "\",";
                }
            } else if (includeNullValue) {
                result += "\"" + key + "\":null,";
            }
        }

        if (result.endsWith(",")) {
            result = result.substring(0, result.length() - 1);
        }

        return result + "}";
    }

    private static <T> String objectToJson(T obj) {
        if (obj.getClass().isArray()) {
            StringBuilder json = new StringBuilder("[");
            int length = Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                Object arrayElement = Array.get(obj, i);
                json.append(objectToJson(arrayElement));

                if (i != length - 1) {
                    json.append(",");
                }
            }

            json.append("]");
            return json.toString();
        } else {
            return obj.toString();
        }
    }
}
