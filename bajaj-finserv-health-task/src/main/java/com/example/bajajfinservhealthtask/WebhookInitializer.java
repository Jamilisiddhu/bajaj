package com.example.bajajfinservhealthtask;

import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Component
public class WebhookInitializer implements CommandLineRunner {
    private static final String YOUR_NAME = "Jamili Sai Siddhartha"; 
    private static final String YOUR_REG_NO = "22BDS0260";
    private static final String YOUR_EMAIL = "jamili.sai2022@vitstudent.ac.in";
    private static final String SQL_QUERY = "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT FROM EMPLOYEE e1 JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT AND e1.DOB < e2.DOB GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME ORDER BY e1.EMP_ID DESC;";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Application starting up and sending the initial POST request...");

        // Step 1: Send the initial POST request to generate the webhook
        String generateWebhookUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        HttpHeaders generateHeaders = new HttpHeaders();
        generateHeaders.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> generateRequestBody = new HashMap<>();
        generateRequestBody.put("name", YOUR_NAME);
        generateRequestBody.put("regNo", YOUR_REG_NO);
        generateRequestBody.put("email", YOUR_EMAIL);

        HttpEntity<Map<String, String>> generateRequest = new HttpEntity<>(generateRequestBody, generateHeaders);

        try {
            ResponseEntity<Map> generateResponse = restTemplate.postForEntity(generateWebhookUrl, generateRequest, Map.class);
            Map<String, Object> responseBody = generateResponse.getBody();

            String webhookUrl = (String) responseBody.get("webhookUrl"); //
            String accessToken = (String) responseBody.get("accessToken"); //

            System.out.println("Webhook URL received: " + webhookUrl);
            System.out.println("Access Token received: " + accessToken);

            // Step 2: Submit the SQL solution to the returned webhook URL
            if (webhookUrl != null && accessToken != null) {
                submitSolution(webhookUrl, accessToken);
            } else {
                System.err.println("Failed to retrieve webhook URL or access token.");
            }
        } catch (Exception e) {
            System.err.println("Error sending initial request: " + e.getMessage());
        }
    }

    private void submitSolution(String webhookUrl, String accessToken) {
        System.out.println("Submitting the SQL solution...");

        HttpHeaders submitHeaders = new HttpHeaders();
        submitHeaders.add("Authorization", "Bearer " + accessToken); //
        submitHeaders.setContentType(MediaType.APPLICATION_JSON); //

        Map<String, String> submitRequestBody = new HashMap<>();
        submitRequestBody.put("finalQuery", SQL_QUERY); //

        HttpEntity<Map<String, String>> submitRequest = new HttpEntity<>(submitRequestBody, submitHeaders);

        try {
            ResponseEntity<String> submitResponse = restTemplate.postForEntity(webhookUrl, submitRequest, String.class);

            System.out.println("Submission status: " + submitResponse.getStatusCode());
            System.out.println("Submission response body: " + submitResponse.getBody());
        } catch (Exception e) {
            System.err.println("Error submitting solution: " + e.getMessage());
        }
    }
}