package rut.iuct.auditoriums.model;

import jakarta.persistence.*;

@Entity
@Table(name = "employee_account")
public class EmployeeAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "full_name", nullable = false) private String fullName;
    @Column(nullable = false) private String position;
    @Column(nullable = false, unique = true) private String login;
    @Column(name = "password_hash", nullable = false) private String passwordHash;

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
