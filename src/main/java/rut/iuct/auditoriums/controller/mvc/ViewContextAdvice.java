package rut.iuct.auditoriums.controller.mvc;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ViewContextAdvice {
    @ModelAttribute("authenticated")
    public boolean authenticated() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName());
    }
    @ModelAttribute("loginName")
    public String login() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authenticated() ? authentication.getName() : null;
    }
}
