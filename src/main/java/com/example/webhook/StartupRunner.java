package com.example.webhook;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class StartupRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();

        // 1. Generate webhook
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        String regNo = "22ucs013"; // <-- apna regNo daalein
        String name = "akshita";  // <-- apna naam daalein
        String email = "22ucs013@lnmiit.ac.in"; // <-- apna email daalein

        String requestBody = String.format("{\"name\":\"%s\",\"regNo\":\"%s\",\"email\":\"%s\"}", name, regNo, email);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        // Print the full response for debugging
        System.out.println("API Response: " + response.getBody());

        JsonNode json = mapper.readTree(response.getBody());
        String webhookUrl = json.get("webhookUrl") != null ? json.get("webhookUrl").asText() : null;
        String accessToken = json.get("accessToken") != null ? json.get("accessToken").asText() : null;
        if (webhookUrl == null || accessToken == null) {
            System.err.println("ERROR: webhookUrl or accessToken not found in response!");
            return;
        }

        // 2. Question selection (as per regNo)
        String regDigits = regNo.replaceAll("\\D", "");
        int lastTwoDigits = Integer.parseInt(regDigits.substring(regDigits.length() - 2));
        String questionUrl = (lastTwoDigits % 2 == 0)
            ? "https://drive.google.com/file/d/143MR5cLFrlNEuHzzWJ5RHnEWxijjuM9X/view?usp=sharing"
            : "https://drive.google.com/file/d/1IeSI6I6KoSqAFFRihIT9tEDICtoz-G/view?usp=sharing";
        System.out.println("Question URL: " + questionUrl);

        // 3. Solve the SQL problem (example query for demonstration)
        // Example: Find employees with salary greater than average salary
        String finalQuery = "SELECT * FROM employees WHERE salary > (SELECT AVG(salary) FROM employees);";

        // 4. Submit solution
        String submitUrl = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        String submitBody = String.format("{\"finalQuery\":\"%s\"}", finalQuery.replace("\"", "\\\""));
        entity = new HttpEntity<>(submitBody, headers);

        ResponseEntity<String> submitResponse = restTemplate.postForEntity(submitUrl, entity, String.class);
        System.out.println("Submission Response: " + submitResponse.getBody());
    }
} 