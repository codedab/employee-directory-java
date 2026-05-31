package com.app;

import com.app.dto.EmployeeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class EmployeeDirectoryTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String base() {
        return "http://localhost:" + port;
    }

    @Test
    public void healthEndpointReturns200() {
        ResponseEntity<String> response = restTemplate.getForEntity(base() + "/actuator/health", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    public void listEmployeesReturnsEmployeesKey() {
        ResponseEntity<Map> response = restTemplate.getForEntity(base() + "/api/employees", Map.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsKey("employees");
    }

    @Test
    public void createEmployeeReturnsIdField() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"name":"Amara Okonkwo","email":"amara@test.com","department":"Engineering","role":"staff"}
                """;
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(base() + "/api/employees", entity, Map.class);
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).containsKey("id");
    }

    @Test
    public void getEmployeeByIdReturnsNameField() {
        // Create employee first
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"name":"Test Employee","email":"test.get@test.com","department":"HR","role":"manager"}
                """;
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> created = restTemplate.postForEntity(base() + "/api/employees", entity, Map.class);
        assertThat(created.getStatusCode().value()).isEqualTo(201);

        Integer id = (Integer) created.getBody().get("id");
        ResponseEntity<Map> response = restTemplate.getForEntity(base() + "/api/employees/" + id, Map.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).containsKey("name");
    }

    @Test
    public void patchEmployeeUpdatesDepartment() {
        // Create employee
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String createBody = """
                {"name":"Patch Tester","email":"patch@test.com","department":"OldDept","role":"analyst"}
                """;
        HttpEntity<String> createEntity = new HttpEntity<>(createBody, headers);
        ResponseEntity<Map> created = restTemplate.postForEntity(base() + "/api/employees", createEntity, Map.class);
        Integer id = (Integer) created.getBody().get("id");

        // Patch department
        String patchBody = "{\"department\":\"Data Engineering\"}";
        HttpEntity<String> patchEntity = new HttpEntity<>(patchBody, headers);
        ResponseEntity<Map> patched = restTemplate.exchange(
                base() + "/api/employees/" + id, HttpMethod.PATCH, patchEntity, Map.class);
        assertThat(patched.getStatusCode().value()).isEqualTo(200);

        // Verify with GET
        ResponseEntity<Map> fetched = restTemplate.getForEntity(base() + "/api/employees/" + id, Map.class);
        assertThat(fetched.getStatusCode().value()).isEqualTo(200);
        assertThat(fetched.getBody().get("department")).isEqualTo("Data Engineering");
    }

    @Test
    public void deactivateKeepsRecordRetrievable() {
        // Create employee
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String createBody = """
                {"name":"Deactivate Me","email":"deactivate@test.com","department":"Finance","role":"analyst"}
                """;
        HttpEntity<String> createEntity = new HttpEntity<>(createBody, headers);
        ResponseEntity<Map> created = restTemplate.postForEntity(base() + "/api/employees", createEntity, Map.class);
        Integer id = (Integer) created.getBody().get("id");

        // Deactivate
        ResponseEntity<Map> deactivated = restTemplate.postForEntity(
                base() + "/api/employees/" + id + "/deactivate", null, Map.class);
        assertThat(deactivated.getStatusCode().value()).isEqualTo(200);

        // Record must still be retrievable and active=false
        ResponseEntity<Map> fetched = restTemplate.getForEntity(base() + "/api/employees/" + id, Map.class);
        assertThat(fetched.getStatusCode().value()).isEqualTo(200);
        assertThat(fetched.getBody().get("active")).isEqualTo(false);
    }

    @Test
    public void duplicateEmailReturns409() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"name":"Dup User","email":"dup@test.com","department":"IT","role":"engineer"}
                """;
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> first = restTemplate.postForEntity(base() + "/api/employees", entity, Map.class);
        assertThat(first.getStatusCode().value()).isEqualTo(201);

        ResponseEntity<Map> second = restTemplate.postForEntity(base() + "/api/employees", entity, Map.class);
        assertThat(second.getStatusCode().value()).isEqualTo(409);
    }
}
