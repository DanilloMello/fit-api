package com.connecthealth.shared.dto;

import java.util.Map;

/**
 * Standard API response wrapper for consistent response structure across the application.
 *
 * @param <T> the type of the response data
 */
public class ApiResponse<T> {

    private T data;
    private Map<String, Object> meta;

    public ApiResponse() {
    }

    public ApiResponse(T data, Map<String, Object> meta) {
        this.data = data;
        this.meta = meta;
    }

    /**
     * Creates a successful API response wrapping the given data.
     *
     * @param data the response payload
     * @param <T>  the type of the data
     * @return a new ApiResponse containing the data
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, null);
    }

    /**
     * Creates a successful API response with data and metadata.
     *
     * @param data the response payload
     * @param meta additional metadata
     * @param <T>  the type of the data
     * @return a new ApiResponse containing the data and metadata
     */
    public static <T> ApiResponse<T> success(T data, Map<String, Object> meta) {
        return new ApiResponse<>(data, meta);
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }
}
