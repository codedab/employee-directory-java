package com.app.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateEmployeeRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String email;

    private String department;
    private String role;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
