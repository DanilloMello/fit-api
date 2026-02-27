package com.connecthealth.shared.dto;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void success_setsDataAndNullMeta() {
        ApiResponse<String> response = ApiResponse.success("hello");

        assertEquals("hello", response.getData());
        assertNull(response.getMeta());
    }

    @Test
    void successWithMeta_setsDataAndMeta() {
        Map<String, Object> meta = Map.of("total", 10);
        ApiResponse<String> response = ApiResponse.success("hello", meta);

        assertEquals("hello", response.getData());
        assertEquals(10, response.getMeta().get("total"));
    }

    @Test
    void defaultConstructor_nullFields() {
        ApiResponse<String> response = new ApiResponse<>();

        assertNull(response.getData());
        assertNull(response.getMeta());
    }

    @Test
    void setters_updateFields() {
        ApiResponse<String> response = new ApiResponse<>();
        response.setData("value");
        response.setMeta(Map.of("key", "val"));

        assertEquals("value", response.getData());
        assertEquals("val", response.getMeta().get("key"));
    }
}
