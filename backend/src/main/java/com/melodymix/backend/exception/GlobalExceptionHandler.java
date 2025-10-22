package com.melodymix.backend.exception; // [3]

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * @RestControllerAdvice: 这是一个组合注解，结合了 @ControllerAdvice 和 @ResponseBody。
 * 它允许我们集中处理所有 @RestController 中抛出的异常。 [1]
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理由 @Valid 注解触发的验证失败异常 [2]
     * @param ex 捕获到的异常对象，其中包含了所有验证失败的详细信息。
     * @return 返回一个包含错误信息的 Map, Spring 会自动将其序列化为 JSON。
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST) // [2]
    @ExceptionHandler(MethodArgumentNotValidException.class) // [2]
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    /**
     * 新增：处理资源未找到的异常 (例如，通过ID查找用户或歌曲失败)
     * 当Service层代码抛出 EntityNotFoundException 时，这个方法会被调用。
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFound(EntityNotFoundException ex) {
        return new ResponseEntity<>(
                Map.of("error", ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * 新增：处理访问被拒绝的异常 (例如，用户试图修改不属于自己的播放列表)
     * 当Service层代码抛出 AccessDeniedException 时，这个方法会被调用。
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        return new ResponseEntity<>(
                Map.of("error", ex.getMessage()),
                HttpStatus.FORBIDDEN
        );
    }
}
