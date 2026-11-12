package rut.iuct.auditoriums.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rut.iuct.auditoriums.dto.RegistrationRequest;
import rut.iuct.auditoriums.dto.TextValues;
import rut.iuct.auditoriums.exception.ApiException;
import rut.iuct.auditoriums.model.EmployeeAccount;
import rut.iuct.auditoriums.repository.EmployeeAccountRepository;
import java.util.Map;

@Service
public class AccountService {
    private final EmployeeAccountRepository accounts;
    private final PasswordEncoder passwords;
    public AccountService(EmployeeAccountRepository accounts, PasswordEncoder passwords) { this.accounts = accounts; this.passwords = passwords; }
    @Transactional
    public void register(RegistrationRequest request) {
        String fullName = request.getFullName();
        if (accounts.existsByLogin(fullName)) throw new ApiException(org.springframework.http.HttpStatus.CONFLICT, "Аккаунт с таким ФИО уже существует", Map.of("lastName", "Проверьте ФИО"));
        EmployeeAccount account = new EmployeeAccount();
        account.setFullName(fullName); account.setPosition(TextValues.required(request.getPosition()));
        account.setLogin(fullName); account.setPasswordHash(passwords.encode(request.getPassword())); accounts.save(account);
    }
    public String currentFullName() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName()))
            throw ApiException.unauthorized("Войдите или зарегистрируйтесь перед редактированием");
        return accounts.findByLogin(authentication.getName()).orElseThrow(() -> ApiException.unauthorized("Аккаунт не найден")).getFullName();
    }
}
