package com.example.mbtitest.service;

import com.example.mbtitest.entity.ShareLog;
import com.example.mbtitest.repository.ShareLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShareService {
    
    private final ShareLogRepository shareLogRepository;
    
    // 지원하는 공유 플랫폼들
    private static final String[] SUPPORTED_PLATFORMS = {
        "kakao", "facebook", "twitter", "instagram", "line", "telegram", "whatsapp", "link"
    };
    
    /**
     * 공유 로그 기록
     */
    public ShareLog recordShare(Long resultId, String platform, HttpServletRequest request) {
        // 플랫폼 검증
        validatePlatform(platform);
        
        String userIp = getClientIpAddress(request);
        
        try {
            ShareLog shareLog = ShareLog.builder()
                .resultId(resultId)
                .sharePlatform(platform.toLowerCase())
                .userIp(userIp)
                .build();
            
            ShareLog savedLog = shareLogRepository.save(shareLog);
            log.info("공유 로그 기록 완료 - ResultID: {}, Platform: {}, IP: {}", 
                resultId, platform, userIp);
            
            return savedLog;
            
        } catch (Exception e) {
            log.error("공유 로그 기록 중 오류 발생", e);
            throw new RuntimeException("공유 기록 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 카카오톡 공유 URL 생성
     */
    public String generateKakaoShareUrl(Long resultId, String mbtiType, String baseUrl) {
        try {
            String resultUrl = baseUrl + "/result/" + resultId;
            String title = "[MBTI 테스트 결과] 나는 " + mbtiType + "!";
            String description = "정확한 MBTI 성격 테스트로 나의 진짜 성격을 알아보세요! 당신도 테스트해보세요 🔥";
            String imageUrl = baseUrl + "/images/mbti-" + mbtiType.toLowerCase() + ".jpg";
            
            // 카카오톡 링크 공유 JavaScript 코드용 데이터 생성
            Map<String, Object> kakaoData = new HashMap<>();
            kakaoData.put("title", title);
            kakaoData.put("description", description);
            kakaoData.put("imageUrl", imageUrl);
            kakaoData.put("webUrl", resultUrl);
            kakaoData.put("mobileWebUrl", resultUrl);
            
            log.debug("카카오톡 공유 URL 생성 - ResultID: {}, MBTI: {}", resultId, mbtiType);
            return resultUrl; // 실제로는 JavaScript에서 처리
            
        } catch (Exception e) {
            log.error("카카오톡 공유 URL 생성 중 오류", e);
            throw new RuntimeException("카카오톡 공유 URL 생성에 실패했습니다.", e);
        }
    }
    
    /**
     * 페이스북 공유 URL 생성
     */
    public String generateFacebookShareUrl(Long resultId, String baseUrl) {
        try {
            String resultUrl = baseUrl + "/result/" + resultId;
            String facebookUrl = "https://www.facebook.com/sharer/sharer.php?u=" + 
                java.net.URLEncoder.encode(resultUrl, "UTF-8");
            
            log.debug("페이스북 공유 URL 생성 - ResultID: {}", resultId);
            return facebookUrl;
            
        } catch (Exception e) {
            log.error("페이스북 공유 URL 생성 중 오류", e);
            throw new RuntimeException("페이스북 공유 URL 생성에 실패했습니다.", e);
        }
    }
    
    /**
     * 트위터 공유 URL 생성
     */
    public String generateTwitterShareUrl(Long resultId, String mbtiType, String baseUrl) {
        try {
            String resultUrl = baseUrl + "/result/" + resultId;
            String text = "나의 MBTI는 " + mbtiType + "! 🔥 정확한 성격 테스트 해보세요 👉";
            String hashtags = "MBTI,성격테스트," + mbtiType;
            
            String twitterUrl = "https://twitter.com/intent/tweet?" +
                "text=" + java.net.URLEncoder.encode(text, "UTF-8") +
                "&url=" + java.net.URLEncoder.encode(resultUrl, "UTF-8") +
                "&hashtags=" + java.net.URLEncoder.encode(hashtags, "UTF-8");
            
            log.debug("트위터 공유 URL 생성 - ResultID: {}, MBTI: {}", resultId, mbtiType);
            return twitterUrl;
            
        } catch (Exception e) {
            log.error("트위터 공유 URL 생성 중 오류", e);
            throw new RuntimeException("트위터 공유 URL 생성에 실패했습니다.", e);
        }
    }
    
    /**
     * 인스타그램 스토리 공유 텍스트 생성
     */
    public String generateInstagramShareText(String mbtiType) {
        String[] templates = {
            "나의 MBTI는 " + mbtiType + "! 🔥\n당신도 테스트해보세요! 💫",
            "MBTI 테스트 결과: " + mbtiType + " ✨\n정말 정확해요! 😍",
            mbtiType + " 성격이 나왔어요! 🌟\n친구들도 해봐요! 🤗"
        };
        
        // 랜덤하게 템플릿 선택
        int randomIndex = (int) (Math.random() * templates.length);
        return templates[randomIndex];
    }
    
    /**
     * 링크 복사용 URL 생성
     */
    public String generateShareableLink(Long resultId, String baseUrl) {
        return baseUrl + "/result/" + resultId + "?shared=true";
    }
    
    /**
     * 공유 통계 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getShareStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 전체 공유 수
            stats.put("totalShares", shareLogRepository.countTotalShares());
            
            // 오늘 공유 수
            stats.put("todayShares", shareLogRepository.countTodayShares());
            
            // 플랫폼별 공유 통계
            List<Object[]> platformStats = shareLogRepository.getShareStatsByPlatform();
            Map<String, Long> platformShareCounts = new HashMap<>();
            for (Object[] row : platformStats) {
                String platform = (String) row[0];
                Long count = ((Number) row[1]).longValue();
                platformShareCounts.put(platform, count);
            }
            stats.put("platformStats", platformShareCounts);
            
            // 가장 인기있는 플랫폼
            List<String> popularPlatforms = shareLogRepository.getMostPopularSharePlatforms();
            stats.put("mostPopularPlatform", popularPlatforms.isEmpty() ? "kakao" : popularPlatforms.get(0));
            
        } catch (Exception e) {
            log.error("공유 통계 조회 중 오류 발생", e);
            stats.put("totalShares", 0L);
            stats.put("todayShares", 0L);
            stats.put("platformStats", new HashMap<>());
            stats.put("mostPopularPlatform", "kakao");
        }
        
        return stats;
    }
    
    /**
     * 일별 공유 통계 (최근 7일)
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDailyShareStats() {
        try {
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            List<Object[]> dailyCounts = shareLogRepository.getDailyShareCounts(weekAgo);
            
            return dailyCounts.stream()
                .map(row -> {
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", row[0].toString());
                    dayData.put("count", ((Number) row[1]).intValue());
                    return dayData;
                })
                .toList();
                
        } catch (Exception e) {
            log.error("일별 공유 통계 조회 중 오류 발생", e);
            return List.of();
        }
    }
    
    /**
     * 가장 많이 공유된 결과들 조회
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMostSharedResults(int limit) {
        try {
            List<Object[]> results = shareLogRepository.getMostSharedResults();
            
            return results.stream()
                .limit(limit)  // limit을 여기서 적용
                .map(row -> {
                    Map<String, Object> resultData = new HashMap<>();
                    resultData.put("resultId", ((Number) row[0]).longValue());
                    resultData.put("shareCount", ((Number) row[1]).intValue());
                    return resultData;
                })
                .toList();
                
        } catch (Exception e) {
            log.error("인기 공유 결과 조회 중 오류 발생", e);
            return List.of();
        }
    }
    
    /**
     * 플랫폼 검증
     */
    private void validatePlatform(String platform) {
        if (platform == null || platform.trim().isEmpty()) {
            throw new RuntimeException("공유 플랫폼을 지정해주세요.");
        }
        
        boolean isSupported = false;
        for (String supportedPlatform : SUPPORTED_PLATFORMS) {
            if (supportedPlatform.equalsIgnoreCase(platform.trim())) {
                isSupported = true;
                break;
            }
        }
        
        if (!isSupported) {
            throw new RuntimeException("지원하지 않는 공유 플랫폼입니다: " + platform);
        }
    }
    
    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };
        
        for (String headerName : headerNames) {
            String ipAddress = request.getHeader(headerName);
            if (ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress)) {
                if (ipAddress.contains(",")) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
                return ipAddress.trim();
            }
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * 공유 가능 여부 확인 (스팸 방지)
     */
    public boolean canShare(String userIp, Long resultId) {
        try {
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
            List<ShareLog> recentShares = shareLogRepository
                .findByUserIpAndCreatedAtAfterOrderByCreatedAtDesc(userIp, fiveMinutesAgo);
            
            // 5분 내에 같은 결과를 3번 이상 공유했다면 제한
            long sameResultShares = recentShares.stream()
                .filter(share -> share.getResultId().equals(resultId))
                .count();
            
            return sameResultShares < 3;
            
        } catch (Exception e) {
            log.error("공유 가능 여부 확인 중 오류", e);
            return true; // 오류 시에는 허용
        }
    }
}
