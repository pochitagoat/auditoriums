package rut.iuct.auditoriums.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationRequest {
    @NotBlank(message = "Укажите фамилию") @Size(max = 100, message = "Фамилия не длиннее 100 символов")
    private String lastName;
    @NotBlank(message = "Укажите имя") @Size(max = 100, message = "Имя не длиннее 100 символов")
    private String firstName;
    @NotBlank(message = "Укажите отчество") @Size(max = 100, message = "Отчество не длиннее 100 символов")
    private String middleName;
    @NotBlank(message = "Укажите должность") @Size(max = 255, message = "Должность не длиннее 255 символов")
    private String position;
    @NotBlank(message = "Укажите пароль") @Size(min = 8, max = 72, message = "Пароль должен содержать от 8 до 72 символов")
    private String password;
    public String getLastName() { return lastName; } public void setLastName(String value) { lastName = value; }
    public String getFirstName() { return firstName; } public void setFirstName(String value) { firstName = value; }
    public String getMiddleName() { return middleName; } public void setMiddleName(String value) { middleName = value; }
    public String getFullName() { return String.join(" ", TextValues.required(lastName), TextValues.required(firstName), TextValues.required(middleName)); }
    public String getPosition() { return position; } public void setPosition(String value) { position = value; }
    public String getPassword() { return password; } public void setPassword(String value) { password = value; }
}
