package com.example.mbtitest.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Controller
public class MainController {
    
    // application.properties에서 AI_KEY 값을 주입받음
    @Value("${AI_KEY}")
    private String aiKey;
    
    /**
     * 메인 페이지
     */
    @GetMapping("/")
    public String main() {
        return "main";
    }
    
    /**
     * 테스트 페이지
     */
    @GetMapping("/test")
    public String test() {
        return "test";
    }
    
    /**
     * AI 분석 요청 처리 (서버에서 직접 API 호출)
     * 클라이언트는 답변만 전송, API 키는 서버에서만 사용
     */
    @PostMapping("/api/analyze")
    @ResponseBody
    public Map<String, Object> analyzePersonality(@RequestBody Map<String, Object> requestData) {
        try {
            // 클라이언트에서 받은 프롬프트
            String prompt = (String) requestData.get("prompt");
            
            // Gemini API 호출을 위한 요청 데이터 구성
            Map<String, Object> geminiRequest = new HashMap<>();
            
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            List<Map<String, Object>> parts = new ArrayList<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);
            parts.add(part);
            content.put("parts", parts);
            contents.add(content);
            geminiRequest.put("contents", contents);
            
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 8000);
            geminiRequest.put("generationConfig", generationConfig);
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(geminiRequest, headers);
            
            // Gemini API 호출
            RestTemplate restTemplate = new RestTemplate();
            String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + aiKey;
            
            ResponseEntity<Map> response = restTemplate.exchange(
                geminiUrl,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            // 성공 응답 반환
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response.getBody());
            return result;
            
        } catch (Exception e) {
            // 에러 응답 반환
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    /**
     * 헬스체크 엔드포인트 (서버 상태 확인용)
     */
    @GetMapping("/health")
    @ResponseBody
    public Map<String, Object> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", java.time.Instant.now().toString());
        status.put("service", "MBTI Test API");
        
        // API 키 설정 상태 확인 (키 값은 노출하지 않음)
        boolean apiKeyConfigured = aiKey != null && !aiKey.equals("NOT_SET") && !aiKey.isEmpty();
        status.put("apiKeyConfigured", apiKeyConfigured);
        
        return status;
    }
    
    /**
     * API 연결 테스트 (서버에서 처리)
     */
    @PostMapping("/api/test")
    @ResponseBody
    public Map<String, Object> testAPI() {
        try {
            // 간단한 테스트 프롬프트
            Map<String, Object> geminiRequest = new HashMap<>();
            
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            List<Map<String, Object>> parts = new ArrayList<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", "한 단어로만 답해주세요: OK");
            parts.add(part);
            content.put("parts", parts);
            contents.add(content);
            geminiRequest.put("contents", contents);
            
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.1);
            generationConfig.put("maxOutputTokens", 1000);
            geminiRequest.put("generationConfig", generationConfig);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(geminiRequest, headers);
            
            RestTemplate restTemplate = new RestTemplate();
            String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + aiKey;
            
            ResponseEntity<Map> response = restTemplate.exchange(
                geminiUrl,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response.getBody());
            return result;
            
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
}
