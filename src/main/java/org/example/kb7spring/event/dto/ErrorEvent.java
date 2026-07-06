package org.example.kb7spring.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorEvent {
    private String source;
    private String exceptionType;
    private String message;
    private String stackTrace;
    private String occurredAt;

    public static ErrorEvent of(String source, Throwable e) {
        StringBuilder stackTrace = new StringBuilder();
        StackTraceElement[] elements = e.getStackTrace();
        int limit = Math.min(elements.length, 10);
        for (int i = 0; i < limit; i++) {
            stackTrace.append(elements[i].toString()).append("\n");
        }

        return new ErrorEvent(
                source,
                e.getClass().getName(),
                e.getMessage(),
                stackTrace.toString(),
                LocalDateTime.now().toString()
        );
    }
}
