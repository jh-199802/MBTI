package com.example.mbtitest.controller;

import com.example.mbtitest.entity.TestResult;
import com.example.mbtitest.service.TestResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 테스트 결과 API 컨트롤러
 */
@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TestResultController {

    private final TestResultService testResultService;

    /**
     * 테스트 결과 저장
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> saveResult(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String userUuid = (String) request.get("userUuid");
            String aiAnalysis = (String) request.get("aiAnalysis");
            Integer testDuration = (Integer) request.get("testDuration");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> categoryScores = (Map<String, Object>) request.get("categoryScores");
            
            @SuppressWarnings("unchecked")
            List<Integer> answers = (List<Integer>) request.get("answers");

            // 입력값 검증
            if (userUuid == null || categoryScores == null || answers == null) {
                response.put("success", false);
                response.put("error", "필수 정보가 누락되었습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            TestResult result = testResultService.saveTestResult(userUuid, categoryScores, answers, aiAnalysis, testDuration, httpRequest);
            
            response.put("success", true);
            response.put("resultId", result.getResultId());
            response.put("mbtiType", result.getMbtiType());
            response.put("description", result.getMbtiDescription());
            response.put("color", result.getMbtiColor());
            response.put("message", "테스트 결과가 저장되었습니다.");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("테스트 결과 저장 API 오류", e);
            response.put("success", false);
            response.put("error", "결과 저장 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 특정 테스트 결과 조회
     */
    @GetMapping("/{resultId}")
    public ResponseEntity<Map<String, Object>> getResult(@PathVariable Long resultId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<TestResult> resultOpt = testResultService.getTestResult(resultId);
            
            if (resultOpt.isEmpty()) {
                response.put("success", false);
                response.put("error", "결과를 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }

            TestResult result = resultOpt.get();
            
            // 공개된 결과만 반환
            if (!result.isPublic()) {
                response.put("success", false);
                response.put("error", "비공개 결과입니다.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            response.put("success", true);
            response.put("result", buildResultResponse(result));
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("테스트 결과 조회 API 오류: {}", resultId, e);
            response.put("success", false);
            response.put("error", "결과 조회 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 사용자별 테스트 히스토리
     */
    @GetMapping("/user/{userUuid}")
    public ResponseEntity<Map<String, Object>> getUserResults(@PathVariable String userUuid) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<TestResult> results = testResultService.getUserTestHistory(userUuid);
            
            response.put("success", true);
            response.put("results", results.stream()
                .map(this::buildResultResponse)
                .toList());
            response.put("totalCount", results.size());
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("사용자 결과 조회 API 오류: {}", userUuid, e);
            response.put("success", false);
            response.put("error", "결과 조회 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 공개된 결과 목록 (페이징)
     */
    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> getPublicResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Page<TestResult> results = testResultService.getPublicResults(page, size);
            
            response.put("success", true);
            response.put("results", results.getContent().stream()
                .map(this::buildResultResponse)
                .toList());
            response.put("totalElements", results.getTotalElements());
            response.put("totalPages", results.getTotalPages());
            response.put("currentPage", page);
            response.put("size", size);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("공개 결과 조회 API 오류", e);
            response.put("success", false);
            response.put("error", "결과 조회 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 인기 결과 목록
     */
    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getPopularResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Page<TestResult> results = testResultService.getPopularResults(page, size);
            
            response.put("success", true);
            response.put("results", results.getContent().stream()
                .map(this::buildResultResponse)
                .toList());
            response.put("totalElements", results.getTotalElements());
            response.put("totalPages", results.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("인기 결과 조회 API 오류", e);
            response.put("success", false);
            response.put("error", "인기 결과 조회 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * MBTI별 결과 목록
     */
    @GetMapping("/mbti/{mbtiType}")
    public ResponseEntity<Map<String, Object>> getMbtiResults(
            @PathVariable String mbtiType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // MBTI 타입 검증
            if (!isValidMbtiType(mbtiType)) {
                response.put("success", false);
                response.put("error", "올바르지 않은 MBTI 타입입니다.");
                return ResponseEntity.badRequest().body(response);
            }

            Page<TestResult> results = testResultService.getMbtiResults(mbtiType, page, size);
            
            response.put("success", true);
            response.put("mbtiType", mbtiType.toUpperCase());
            response.put("results", results.getContent().stream()
                .map(this::buildResultResponse)
                .toList());
            response.put("totalElements", results.getTotalElements());
            response.put("totalPages", results.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("MBTI별 결과 조회 API 오류: {}", mbtiType, e);
            response.put("success", false);
            response.put("error", "결과 조회 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 댓글이 많은 결과들
     */
    @GetMapping("/most-commented")
    public ResponseEntity<Map<String, Object>> getMostCommentedResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Page<TestResult> results = testResultService.getMostCommentedResults(page, size);
            
            response.put("success", true);
            response.put("results", results.getContent().stream()
                .map(this::buildResultResponse)
                .toList());
            response.put("totalElements", results.getTotalElements());
            response.put("totalPages", results.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("댓글 많은 결과 조회 API 오류", e);
            response.put("success", false);
            response.put("error", "결과 조회 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 바이럴 결과들 (공유가 많은)
     */
    @GetMapping("/viral")
    public ResponseEntity<Map<String, Object>> getViralResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Page<TestResult> results = testResultService.getViralResults(page, size);
            
            response.put("success", true);
            response.put("results", results.getContent().stream()
                .map(this::buildResultResponse)
                .toList());
            response.put("totalElements", results.getTotalElements());
            response.put("totalPages", results.getTotalPages());
            response.put("currentPage", page);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("바이럴 결과 조회 API 오류", e);
            response.put("success", false);
            response.put("error", "바이럴 결과 조회 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 결과 공개/비공개 토글
     */
    @PutMapping("/{resultId}/visibility")
    public ResponseEntity<Map<String, Object>> toggleVisibility(
            @PathVariable Long resultId,
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String userUuid = request.get("userUuid");
            
            if (userUuid == null) {
                response.put("success", false);
                response.put("error", "사용자 정보가 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            boolean success = testResultService.toggleResultVisibility(resultId, userUuid);
            
            if (success) {
                response.put("success", true);
                response.put("message", "공개 설정이 변경되었습니다.");
            } else {
                response.put("success", false);
                response.put("error", "권한이 없거나 결과를 찾을 수 없습니다.");
            }
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("결과 공개설정 변경 API 오류: {}", resultId, e);
            response.put("success", false);
            response.put("error", "설정 변경 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * MBTI 추천 컨텐츠
     */
    @GetMapping("/recommendations/{mbtiType}")
    public ResponseEntity<Map<String, Object>> getMbtiRecommendations(@PathVariable String mbtiType) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (!isValidMbtiType(mbtiType)) {
                response.put("success", false);
                response.put("error", "올바르지 않은 MBTI 타입입니다.");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> recommendations = testResultService.getMbtiRecommendations(mbtiType);
            
            response.put("success", true);
            response.put("mbtiType", mbtiType.toUpperCase());
            response.put("recommendations", recommendations);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("MBTI 추천 컨텐츠 API 오류: {}", mbtiType, e);
            response.put("success", false);
            response.put("error", "추천 컨텐츠 조회 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ======================= Private Helper Methods =======================

    /**
     * TestResult -> 응답용 Map 변환
     */
    private Map<String, Object> buildResultResponse(TestResult result) {
        Map<String, Object> response = new HashMap<>();
        
        response.put("resultId", result.getResultId());
        response.put("mbtiType", result.getMbtiType());
        response.put("description", result.getMbtiDescription());
        response.put("color", result.getMbtiColor());
        response.put("viewCount", result.getViewCount());
        response.put("sharedCount", result.getSharedCount());
        response.put("isPublic", result.isPublic());
        response.put("createdAt", result.getCreatedAt());
        
        // AI 분석 결과가 있으면 포함
        if (result.getAiAnalysis() != null && !result.getAiAnalysis().trim().isEmpty()) {
            response.put("aiAnalysis", result.getAiAnalysis());
        }
        
        // 상세 점수가 있으면 포함
        if (result.getDetailedScores() != null && !result.getDetailedScores().trim().isEmpty()) {
            try {
                // JSON 파싱하여 포함 (실제로는 ObjectMapper 사용)
                response.put("detailedScores", result.getDetailedScores());
            } catch (Exception e) {
                log.warn("상세 점수 파싱 오류: {}", result.getResultId());
            }
        }
        
        return response;
    }

    /**
     * MBTI 타입 유효성 검증
     */
    private boolean isValidMbtiType(String mbtiType) {
        if (mbtiType == null || mbtiType.length() != 4) {
            return false;
        }
        
        String upper = mbtiType.toUpperCase();
        
        // 첫 번째 문자: E 또는 I
        if (upper.charAt(0) != 'E' && upper.charAt(0) != 'I') return false;
        
        // 두 번째 문자: S 또는 N
        if (upper.charAt(1) != 'S' && upper.charAt(1) != 'N') return false;
        
        // 세 번째 문자: T 또는 F
        if (upper.charAt(2) != 'T' && upper.charAt(2) != 'F') return false;
        
        // 네 번째 문자: J 또는 P
        if (upper.charAt(3) != 'J' && upper.charAt(3) != 'P') return false;
        
        return true;
    }
}