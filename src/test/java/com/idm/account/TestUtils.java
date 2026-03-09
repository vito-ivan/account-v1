package com.idm.account;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Test helper utilities. */
public final class TestUtils {
    private static final ObjectMapper OM = new ObjectMapper();

    private TestUtils() { }

    public static String toJson(Object value) {
        try {
            return OM.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String readResource(String path) {
        try (var is = TestUtils.class.getResourceAsStream(path)) {
            if (is == null) throw new IllegalArgumentException("Resource not found: " + path);
            return new String(is.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
