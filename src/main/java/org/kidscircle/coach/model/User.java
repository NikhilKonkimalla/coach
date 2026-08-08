package org.kidscircle.coach.model;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String name;
    private String userName;

    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    private String password;

    private String phoneNumber;
    private String preferredCommunication;
    private String graduationYear;

    // Fields added for the Coach MVP
    private String displayName;
    private String timeZone = "America/New_York";
    private String weekStartsOn = "MONDAY";

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (displayName == null && name != null) {
            displayName = name;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getDisplayName() {
        return displayName != null ? displayName : (name != null ? name : email);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long id) { this.userId = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPreferredCommunication() { return preferredCommunication; }
    public void setPreferredCommunication(String preferredCommunication) { this.preferredCommunication = preferredCommunication; }

    public String getGraduationYear() { return graduationYear; }
    public void setGraduationYear(String graduationYear) { this.graduationYear = graduationYear; }

    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }

    public String getWeekStartsOn() { return weekStartsOn; }
    public void setWeekStartsOn(String weekStartsOn) { this.weekStartsOn = weekStartsOn; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}
