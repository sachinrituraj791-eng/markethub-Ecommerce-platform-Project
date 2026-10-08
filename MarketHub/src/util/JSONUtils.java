package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * JSONUtils
 * Lightweight, zero-external-dependency JSON utility for Java Servlets.
 * Converts request payloads into Maps and formats standardized API responses.
 * Follows evaluation format:
 * {
 *   "success": true,
 *   "message": "...",
 *   "data": { ... }
 * }
 */
public class JSONUtils {

    private JSONUtils() {}

    public static String readBody(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * Basic JSON payload key-value parser for simple JSON objects without external libraries.
     */
    public static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) return map;
        String trimmed = json.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        String[] pairs = trimmed.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = cleanToken(keyValue[0]);
                String value = cleanToken(keyValue[1]);
                map.put(key, value);
            }
        }
        return map;
    }

    private static String cleanToken(String token) {
        String clean = token.trim();
        if (clean.startsWith("\"") && clean.endsWith("\"") && clean.length() >= 2) {
            clean = clean.substring(1, clean.length() - 1);
        }
        return clean;
    }

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static String successResponse(String message, String dataJson) {
        return "{\"success\":true,\"message\":\"" + escape(message) + "\",\"data\":" +
                (dataJson == null || dataJson.isEmpty() ? "{}" : dataJson) + "}";
    }

    public static String errorResponse(String message) {
        return "{\"success\":false,\"message\":\"" + escape(message) + "\",\"data\":null}";
    }

    public static String paginatedResponse(String dataJson, int page, int limit, int total, int totalPages) {
        return "{\"success\":true,\"data\":" + dataJson + ",\"pagination\":{" +
                "\"page\":" + page + "," +
                "\"limit\":" + limit + "," +
                "\"total\":" + total + "," +
                "\"totalPages\":" + totalPages +
                "}}";
    }
}
