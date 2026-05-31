package com.app.service;

import com.app.domain.Employee;
import com.app.dto.CreateEmployeeRequest;
import com.app.dto.EmployeeResponse;
import com.app.dto.UpdateEmployeeRequest;
import com.app.repository.EmployeeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EmployeeResponse findById(Long id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));
        return toResponse(emp);
    }

    public EmployeeResponse create(CreateEmployeeRequest req) {
        if (employeeRepository.existsByEmailAndActiveTrue(req.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
        Employee emp = new Employee();
        emp.setName(req.getName());
        emp.setEmail(req.getEmail());
        emp.setDepartment(req.getDepartment());
        emp.setRole(req.getRole());
        return toResponse(employeeRepository.save(emp));
    }

    public EmployeeResponse update(Long id, UpdateEmployeeRequest req) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));
        if (req.getName() != null) emp.setName(req.getName());
        if (req.getEmail() != null) emp.setEmail(req.getEmail());
        if (req.getDepartment() != null) emp.setDepartment(req.getDepartment());
        if (req.getRole() != null) emp.setRole(req.getRole());
        emp.setUpdatedAt(LocalDateTime.now());
        return toResponse(employeeRepository.save(emp));
    }

    public EmployeeResponse deactivate(Long id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Employee not found"));
        emp.setActive(false);
        emp.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(emp);
        return toResponse(emp);
    }

    private EmployeeResponse toResponse(Employee emp) {
        return new EmployeeResponse(
                emp.getId(),
                emp.getName(),
                emp.getEmail(),
                emp.getDepartment(),
                emp.getRole(),
                emp.isActive()
        );
    }
}
