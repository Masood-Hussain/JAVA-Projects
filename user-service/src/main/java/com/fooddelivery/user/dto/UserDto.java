package com.fooddelivery.user.dto;

import com.fooddelivery.user.entity.User;
import java.time.LocalDateTime;

public class UserDto {
    
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private User.Role role;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    public UserDto() {}

    public UserDto(Long id, String username, String email, String fullName, 
                   String phone, String address, User.Role role, Boolean enabled,
                   LocalDateTime createdAt, LocalDateTime lastLogin) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.role = role;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public User.Role getRole() { return role; }
    public void setRole(User.Role role) { this.role = role; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
}
