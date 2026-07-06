package org.example.kb7spring.event.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.kb7spring.event.dto.ErrorEvent;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "error_log")
public class ErrorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String source;

    @Column(name = "exception_type")
    private String exceptionType;

    @Column(length = 1000)
    private String message;

    @Lob
    private String stackTrace;

    @Column(name = "occurred_at", nullable = false)
    private String occurredAt;

    public static ErrorLog from(ErrorEvent event) {
        return new ErrorLog(null, event.getSource(), event.getExceptionType(),
                event.getMessage(), event.getStackTrace(), event.getOccurredAt());
    }
}
