package rut.iuct.auditoriums.controller.mvc;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import rut.iuct.auditoriums.dto.RegistrationRequest;
import rut.iuct.auditoriums.exception.ApiException;
import rut.iuct.auditoriums.service.AccountService;

@Controller
public class AccountMvcController {
    private final AccountService accounts;
    public AccountMvcController(AccountService accounts) { this.accounts = accounts; }
    @GetMapping("/login") public String login() { return "accounts/login"; }
    @GetMapping("/register") public String register(Model model) { model.addAttribute("registration", new RegistrationRequest()); return "accounts/register"; }
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registration") RegistrationRequest request, BindingResult errors) {
        if (errors.hasErrors()) return "accounts/register";
        try { accounts.register(request); }
        catch (ApiException exception) { errors.rejectValue("lastName", "duplicate", exception.getMessage()); return "accounts/register"; }
        return "redirect:/login?registered";
    }
}
