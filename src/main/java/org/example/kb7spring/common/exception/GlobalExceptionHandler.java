package org.example.kb7spring.common.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kb7spring.event.dto.ErrorEvent;
import org.example.kb7spring.event.service.ErrorEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Arrays;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ErrorEventPublisher errorEventPublisher;

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handle404(NoHandlerFoundException e) {
        return "/exception/404";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handle500(Exception e, Model model) {
        log.error("==========> 500 에러, {}", e.getMessage());
        e.printStackTrace();

        errorEventPublisher.publish(ErrorEvent.of("GlobalExceptionHandler", e));

        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("stackTrace", Arrays.asList(e.getStackTrace()));

        return "/exception/500";
    }
}
