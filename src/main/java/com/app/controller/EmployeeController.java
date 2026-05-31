package com.app.controller;

import com.app.dto.CreateEmployeeRequest;
import com.app.dto.EmployeeResponse;
import com.app.dto.UpdateEmployeeRequest;
import com.app.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listEmployees() {
        return ResponseEntity.ok(Map.of("employees", employeeService.findAll()));
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.findById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @RequestBody UpdateEmployeeRequest req) {
        return ResponseEntity.ok(employeeService.update(id, req));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateEmployee(@PathVariable Long id) {
        EmployeeResponse resp = employeeService.deactivate(id);
        return ResponseEntity.ok(Map.of("id", resp.getId(), "active", resp.isActive()));
    }

    @GetMapping("/{id}/salary")
    public ResponseEntity<Map<String, Object>> getSalary(@PathVariable Long id) {
        // Existence check (throws 404 if missing); salary data is placeholder
        employeeService.findById(id);
        return ResponseEntity.ok(Map.of("id", id, "salary", "CONFIDENTIAL"));
    }
}
