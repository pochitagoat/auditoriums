package rut.iuct.auditoriums.controller.mvc;

import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import rut.iuct.auditoriums.exception.ApiException;

@ControllerAdvice(basePackages = "rut.iuct.auditoriums.controller.mvc")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MvcExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ModelAndView business(ApiException exception) { return view(exception.getStatus(), exception.getMessage()); }

    @ExceptionHandler({BindException.class, org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    public ModelAndView invalid(Exception exception) { return view(HttpStatus.BAD_REQUEST, "Проверьте параметры поиска или адрес страницы"); }

    @ExceptionHandler(Exception.class)
    public ModelAndView unexpected(Exception exception) {
        LoggerFactory.getLogger(MvcExceptionHandler.class).error("Ошибка открытия страницы", exception);
        return view(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось открыть страницу. Повторите попытку позднее");
    }

    private ModelAndView view(HttpStatus status, String message) {
        ModelAndView result = new ModelAndView("error");
        result.setStatus(status);
        result.addObject("status", status.value());
        result.addObject("message", message);
        return result;
    }
}
