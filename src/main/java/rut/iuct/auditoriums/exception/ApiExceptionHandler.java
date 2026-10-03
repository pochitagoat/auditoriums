package rut.iuct.auditoriums.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import rut.iuct.auditoriums.storage.StorageException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import tools.jackson.databind.exc.MismatchedInputException;
import rut.iuct.auditoriums.dto.ApiError;
import rut.iuct.auditoriums.service.AuditSupport;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private final Clock clock;

    public ApiExceptionHandler(Clock clock) { this.clock = clock; }

    private ResponseEntity<ApiError> error(HttpStatus status, String message, Map<String, String> fields, HttpServletRequest request) {
        ApiError result = new ApiError();
        result.setTimestamp(LocalDateTime.now(clock));
        result.setStatus(status.value());
        result.setError(status.getReasonPhrase());
        result.setMessage(message);
        result.setPath(request.getRequestURI());
        result.setFieldErrors(fields);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(result);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> business(ApiException exception, HttpServletRequest request) {
        return error(exception.getStatus(), exception.getMessage(), exception.getFieldErrors(), request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> tooLarge(MaxUploadSizeExceededException exception, HttpServletRequest request) {
        return error(HttpStatus.PAYLOAD_TOO_LARGE, "Размер фотографии не должен превышать 10 000 000 байт", Map.of("file", "Файл слишком большой"), request);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiError> missingPart(MissingServletRequestPartException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Не указана обязательная часть запроса", Map.of(exception.getRequestPartName(), "Укажите значение"), request);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ApiError> storage(StorageException exception, HttpServletRequest request) {
        logger.error("Ошибка файлового хранилища", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось сохранить файл. Повторите попытку позднее", Map.of("file", "Ошибка файлового хранилища"), request);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> binding(BindException exception, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getFieldErrors().forEach(e -> fields.putIfAbsent(AuditSupport.fieldName(e.getField()),
                e.isBindingFailure() ? "Недопустимое значение" : e.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "Проверьте заполнение формы", fields, request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiError> methodValidation(HandlerMethodValidationException exception, HttpServletRequest request) {
        if (exception.isForReturnValue()) return unexpected(exception, request);
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getParameterValidationResults().forEach(result -> result.getResolvableErrors().forEach(e -> {
            String name = e instanceof FieldError field ? field.getField() : result.getMethodParameter().getParameterName();
            fields.putIfAbsent(AuditSupport.fieldName(name == null ? "request" : name), e.getDefaultMessage());
        }));
        return error(HttpStatus.BAD_REQUEST, "Проверьте параметры запроса", fields, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> constraint(ConstraintViolationException exception, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(v -> fields.putIfAbsent(AuditSupport.fieldName(v.getPropertyPath().toString()), v.getMessage()));
        return error(HttpStatus.BAD_REQUEST, "Проверьте заполнение формы", fields, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> missing(MissingServletRequestParameterException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Не указан обязательный параметр", Map.of(exception.getParameterName(), "Укажите значение"), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> mismatch(MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Недопустимое значение параметра", Map.of(exception.getName(), "Проверьте значение"), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> unreadable(HttpMessageNotReadableException exception, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof UnrecognizedPropertyException unknown) {
                fields.put(unknown.getPropertyName(), "Это поле не разрешено в данном запросе");
                break;
            }
            if (cause instanceof MismatchedInputException mismatch && !mismatch.getPath().isEmpty()) {
                String name = mismatch.getPath().getLast().getPropertyName();
                if (name != null) fields.put(name, "Недопустимое значение");
                break;
            }
        }
        return error(HttpStatus.BAD_REQUEST, "Проверьте формат JSON и значения полей", fields, request);
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<ApiError> stale(Exception exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "Запись уже изменена. Обновите данные и повторите операцию",
                Map.of("version", "Использована устаревшая версия записи"), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integrity(DataIntegrityViolationException exception, HttpServletRequest request) {
        String internalMessage = exception.getMostSpecificCause().getMessage();
        if (internalMessage != null && internalMessage.contains("ux_workstation_pc_name_nonblank")) {
            return error(HttpStatus.CONFLICT, "Инвентарный номер уже используется", Map.of("pcName", "Укажите другой инвентарный номер"), request);
        }
        return error(HttpStatus.CONFLICT, "Операция конфликтует со связанными записями. Обновите данные и проверьте связи", Map.of(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Object missingRoute(NoResourceFoundException exception, HttpServletRequest request) {
        if (!request.getRequestURI().startsWith("/api/")) {
            var view = new org.springframework.web.servlet.ModelAndView("error");
            view.setStatus(HttpStatus.NOT_FOUND);
            view.addObject("status", 404);
            view.addObject("message", "Страница не найдена. Проверьте адрес");
            return view;
        }
        return error(HttpStatus.NOT_FOUND, "Ресурс не найден", Map.of(), request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> method(HttpRequestMethodNotSupportedException exception, HttpServletRequest request) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "Этот HTTP-метод не поддерживается", Map.of(), request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> media(HttpMediaTypeNotSupportedException exception, HttpServletRequest request) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Неподдерживаемый формат запроса", Map.of(), request);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiError> accept(HttpMediaTypeNotAcceptableException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_ACCEPTABLE, "Ответ предоставляется в формате JSON", Map.of(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception exception, HttpServletRequest request) {
        logger.error("Ошибка обработки {}", request.getRequestURI(), exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось выполнить операцию. Повторите попытку позднее", Map.of(), request);
    }
}
