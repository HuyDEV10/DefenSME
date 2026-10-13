package com.defensme.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.MediaType;

public class ProblemResponseWriter {

    private final ObjectMapper objectMapper;
    private final Clock clock;

    public ProblemResponseWriter(ObjectMapper objectMapper, Clock clock) {
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public void write(
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            String code,
            String title,
            String detail
    ) throws IOException {
        String slug = code.toLowerCase().replace('_', '-');
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "https://defensme.local/problems/" + slug);
        body.put("title", title);
        body.put("status", status);
        body.put("code", code);
        body.put("detail", detail);
        body.put("instance", request.getRequestURI());
        body.put("correlationId", UUID.randomUUID().toString());
        body.put("timestamp", clock.instant().toString());

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
