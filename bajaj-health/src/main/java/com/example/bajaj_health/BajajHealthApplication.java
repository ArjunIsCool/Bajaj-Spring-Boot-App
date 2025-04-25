package com.example.bajaj_health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@SpringBootApplication
public class BajajHealthApplication {

    public static void main(String[] args) {
        SpringApplication.run(BajajHealthApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ApplicationRunner runner(RestTemplate restTemplate) {
        return args -> {
            String regNo = "REG12347";
            String name = "John Doe";
            String email = "john@example.com";

            // 1. Call generateWebhook
            String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook";
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", name);
            requestBody.put("regNo", regNo);
            requestBody.put("email", email);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<GenerateWebhookResponse> response = restTemplate.exchange(
                    generateUrl, HttpMethod.POST, entity, GenerateWebhookResponse.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                System.err.println("Failed to get webhook data. Exiting.");
                return;
            }

            String webhookUrl = response.getBody().getWebhook();
            String accessToken = response.getBody().getAccessToken();
            DataWrapper data = response.getBody().getData();

            Map<String, Object> result = new HashMap<>();
            result.put("regNo", regNo);

            // Decide which question to solve
            if (data.getN() != null && data.getFindId() != null) {
                // Question 2: Nth-Level Followers
                List<Integer> outcome = findNthLevelFollowers(data.getUsers(), data.getFindId(), data.getN());
                result.put("outcome", outcome);
            } else {
                // Question 1: Mutual Followers
                List<List<Integer>> outcome = findMutualFollowers(data.getUsers());
                result.put("outcome", outcome);
            }

            // POST to webhook with retry
            postWithRetry(restTemplate, webhookUrl, accessToken, result, 4);
        };
    }

    private static List<List<Integer>> findMutualFollowers(List<User> users) {
        Map<Integer, Set<Integer>> followsMap = new HashMap<>();
        for (User user : users) {
            followsMap.put(user.getId(), new HashSet<>(user.getFollows()));
        }
        Set<List<Integer>> resultSet = new HashSet<>();
        for (User user : users) {
            int id = user.getId();
            for (int followedId : user.getFollows()) {
                if (followsMap.containsKey(followedId) && followsMap.get(followedId).contains(id)) {
                    int min = Math.min(id, followedId);
                    int max = Math.max(id, followedId);
                    resultSet.add(Arrays.asList(min, max));
                }
            }
        }
        List<List<Integer>> result = new ArrayList<>(resultSet);
        result.sort(Comparator.comparingInt((List<Integer> l) -> l.get(0)).thenComparingInt(l -> l.get(1)));
        return result;
    }

    private static List<Integer> findNthLevelFollowers(List<User> users, int findId, int n) {
        Map<Integer, List<Integer>> graph = new HashMap<>();
        for (User user : users) {
            graph.put(user.getId(), user.getFollows());
        }

        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(findId);
        visited.add(findId);

        int level = 0;
        while (!queue.isEmpty() && level < n) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                int curr = queue.poll();
                for (int next : graph.getOrDefault(curr, Collections.emptyList())) {
                    if (!visited.contains(next)) {
                        queue.add(next);
                        visited.add(next);
                    }
                }
            }
            level++;
        }

        List<Integer> result = new ArrayList<>(queue);
        Collections.sort(result);
        return result;
    }

    private static void postWithRetry(RestTemplate restTemplate, String webhookUrl, String accessToken,
                                      Map<String, Object> result, int maxAttempts) throws InterruptedException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(result, headers);

        int attempts = 0;
        while (attempts < maxAttempts) {
            try {
                ResponseEntity<String> resp = restTemplate.postForEntity(webhookUrl, entity, String.class);
                if (resp.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Successfully posted result to webhook.");
                    return;
                } else {
                    System.err.println("Failed to post to webhook, status: " + resp.getStatusCode());
                }
            } catch (Exception e) {
                System.err.println("Attempt " + (attempts + 1) + " failed: " + e.getMessage());
            }
            attempts++;
            Thread.sleep(1000);
        }
        System.err.println("Failed to post to webhook after " + maxAttempts + " attempts.");
    }
}
