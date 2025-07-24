package com.example.mbtitest.controller;

import com.example.mbtitest.entity.ShareLog;
import com.example.mbtitest.service.ShareService;
import com.example.mbtitest.service.TestResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/share")
public class ShareController {
    
    private final ShareService shareService;
    private final TestResultService testResultService;
    
    @Value("${server.domain:http://localhost:10000}")
    private String baseUrl;
    
    /**
     * 공유 로그 기록 API
     */
    @PostMapping("/api/log")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> recordShare(@RequestBody Map<String, Object> request,
                                                         HttpServletRequest httpRequest) {
        try {
            Object resultIdObj = request.get("resultId");
            String platform = (String) request.get("platform");
            
            if (resultIdObj == null || platform == null || platform.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "필수 정보가 누락되었습니다."));
            }
            
            Long resultId;
            try {
                if (resultIdObj instanceof Number) {
                    resultId = ((Number) resultIdObj).longValue();
                } else {
                    resultId = Long.parseLong(resultIdObj.toString());
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "올바르지 않은 결과 ID입니다."));
            }
            
            // 테스트 결과 존재 여부 확인
            if (testResultService.getTestResult(resultId).isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "존재하지 않는 테스트 결과입니다."));
            }
            
            // 스팸 방지 체크
            String userIp = getClientIp(httpRequest);
            if (!shareService.canShare(userIp, resultId)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "잠시 후 다시 공유해주세요."));
            }
            
            ShareLog shareLog = shareService.recordShare(resultId, platform, httpRequest);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "공유가 기록되었습니다.",
                "shareId", shareLog.getShareId()
            ));
            
        } catch (RuntimeException e) {
            log.warn("공유 로그 기록 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("공유 로그 기록 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "공유 기록 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 카카오톡 공유 URL 생성 API
     */
    @PostMapping("/api/kakao")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateKakaoShareUrl(@RequestBody Map<String, Object> request,
                                                                   HttpServletRequest httpRequest) {
        try {
            Object resultIdObj = request.get("resultId");
            String mbtiType = (String) request.get("mbtiType");
            
            if (resultIdObj == null || mbtiType == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "필수 정보가 누락되었습니다."));
            }
            
            Long resultId = Long.parseLong(resultIdObj.toString());
            
            // 공유 URL 생성
            String shareUrl = shareService.generateKakaoShareUrl(resultId, mbtiType, baseUrl);
            
            // 카카오톡 공유용 데이터 생성
            String title = "[MBTI 테스트 결과] 나는 " + mbtiType + "!";
            String description = "정확한 MBTI 성격 테스트로 나의 진짜 성격을 알아보세요! 당신도 테스트해보세요 🔥";
            String imageUrl = baseUrl + "/images/mbti-" + mbtiType.toLowerCase() + ".jpg";
            String webUrl = baseUrl + "/result/" + resultId;
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                    "title", title,
                    "description", description,
                    "imageUrl", imageUrl,
                    "webUrl", webUrl,
                    "mobileWebUrl", webUrl
                )
            ));
            
        } catch (Exception e) {
            log.error("카카오톡 공유 URL 생성 중 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "카카오톡 공유 정보 생성에 실패했습니다."));
        }
    }
    
    /**
     * 페이스북 공유 URL 생성 API
     */
    @PostMapping("/api/facebook")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateFacebookShareUrl(@RequestBody Map<String, Object> request) {
        try {
            Object resultIdObj = request.get("resultId");
            
            if (resultIdObj == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "결과 ID가 필요합니다."));
            }
            
            Long resultId = Long.parseLong(resultIdObj.toString());
            String facebookUrl = shareService.generateFacebookShareUrl(resultId, baseUrl);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "url", facebookUrl
            ));
            
        } catch (Exception e) {
            log.error("페이스북 공유 URL 생성 중 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "페이스북 공유 URL 생성에 실패했습니다."));
        }
    }
    
    /**
     * 트위터 공유 URL 생성 API
     */
    @PostMapping("/api/twitter")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateTwitterShareUrl(@RequestBody Map<String, Object> request) {
        try {
            Object resultIdObj = request.get("resultId");
            String mbtiType = (String) request.get("mbtiType");
            
            if (resultIdObj == null || mbtiType == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "필수 정보가 누락되었습니다."));
            }
            
            Long resultId = Long.parseLong(resultIdObj.toString());
            String twitterUrl = shareService.generateTwitterShareUrl(resultId, mbtiType, baseUrl);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "url", twitterUrl
            ));
            
        } catch (Exception e) {
            log.error("트위터 공유 URL 생성 중 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "트위터 공유 URL 생성에 실패했습니다."));
        }
    }
    
    /**
     * 인스타그램 공유 텍스트 생성 API
     */
    @PostMapping("/api/instagram")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateInstagramShareText(@RequestBody Map<String, Object> request) {
        try {
            String mbtiType = (String) request.get("mbtiType");
            
            if (mbtiType == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "MBTI 타입이 필요합니다."));
            }
            
            String shareText = shareService.generateInstagramShareText(mbtiType);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "text", shareText
            ));
            
        } catch (Exception e) {
            log.error("인스타그램 공유 텍스트 생성 중 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "인스타그램 공유 텍스트 생성에 실패했습니다."));
        }
    }
    
    /**
     * 링크 복사용 URL 생성 API
     */
    @PostMapping("/api/link")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateShareableLink(@RequestBody Map<String, Object> request) {
        try {
            Object resultIdObj = request.get("resultId");
            
            if (resultIdObj == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "결과 ID가 필요합니다."));
            }
            
            Long resultId = Long.parseLong(resultIdObj.toString());
            String shareUrl = shareService.generateShareableLink(resultId, baseUrl);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "url", shareUrl
            ));
            
        } catch (Exception e) {
            log.error("공유 링크 생성 중 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "공유 링크 생성에 실패했습니다."));
        }
    }
    
    /**
     * 공유 통계 API
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getShareStatistics() {
        try {
            Map<String, Object> stats = shareService.getShareStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("공유 통계 조회 중 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "공유 통계 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 일별 공유 통계 API
     */
    @GetMapping("/api/daily-stats")
    @ResponseBody
    public ResponseEntity<Object> getDailyShareStats() {
        try {
            return ResponseEntity.ok(shareService.getDailyShareStats());
        } catch (Exception e) {
            log.error("일별 공유 통계 조회 중 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "일별 공유 통계 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 가장 많이 공유된 결과들 API
     */
    @GetMapping("/api/most-shared")
    @ResponseBody
    public ResponseEntity<Object> getMostSharedResults(@RequestParam(defaultValue = "10") int limit) {
        try {
            return ResponseEntity.ok(shareService.getMostSharedResults(limit));
        } catch (Exception e) {
            log.error("인기 공유 결과 조회 중 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "인기 공유 결과 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 클라이언트 IP 추출
     */
    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("X-Real-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }
}
