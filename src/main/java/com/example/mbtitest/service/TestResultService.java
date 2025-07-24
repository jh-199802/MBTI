package com.example.mbtitest.service;

import com.example.mbtitest.entity.TestResult;
import com.example.mbtitest.repository.TestResultRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TestResultService {
    
    private final TestResultRepository testResultRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 테스트 결과 저장
     */
    public TestResult saveTestResult(String userUuid, Map<String, Object> categoryScores, 
                                   List<Integer> answers, String aiAnalysis, 
                                   Integer testDuration, HttpServletRequest request) {
        try {
            // 사용자 정보 추출
            String userIp = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            // MBTI 타입 계산
            String mbtiType = calculateMbtiType(categoryScores);
            
            // JSON 데이터 변환
            String categoryScoresJson = objectMapper.writeValueAsString(categoryScores);
            String answerDataJson = objectMapper.writeValueAsString(answers);
            String detailedScoresJson = objectMapper.writeValueAsString(generateDetailedScores(categoryScores));
            
            // MBTI 정보 설정
            Map<String, String> mbtiInfo = getMbtiInfo(mbtiType);
            
            TestResult testResult = TestResult.builder()
                .userUuid(userUuid)
                .userIp(userIp)
                .userAgent(userAgent)
                .mbtiType(mbtiType.toUpperCase())
                .mbtiDescription(mbtiInfo.get("description"))
                .mbtiColor(mbtiInfo.get("color"))
                .categoryScores(categoryScoresJson)
                .detailedScores(detailedScoresJson)
                .answerData(answerDataJson)
                .aiAnalysis(aiAnalysis)
                .testDuration(testDuration)
                .viewCount(0)
                .sharedCount(0)
                .isPublicFlag("Y")
                .build();
            
            TestResult savedResult = testResultRepository.save(testResult);
            log.info("테스트 결과 저장 완료 - ID: {}, MBTI: {}, UUID: {}", 
                savedResult.getResultId(), mbtiType, userUuid);
            
            return savedResult;
            
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 중 오류 발생", e);
            throw new RuntimeException("테스트 결과 저장 중 오류가 발생했습니다.", e);
        } catch (Exception e) {
            log.error("테스트 결과 저장 중 오류 발생", e);
            throw new RuntimeException("테스트 결과 저장 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 테스트 결과 조회
     */
    @Transactional(readOnly = true)
    public Optional<TestResult> getTestResult(Long resultId) {
        return testResultRepository.findById(resultId);
    }
    
    /**
     * 최근 테스트 결과들 조회
     */
    @Transactional(readOnly = true)
    public List<TestResult> getRecentTestResults() {
        return testResultRepository.findTop10ByOrderByCreatedAtDesc();
    }
    
    /**
     * 특정 MBTI 타입의 최근 결과들
     */
    @Transactional(readOnly = true)
    public List<TestResult> getRecentTestResultsByMbti(String mbtiType) {
        return testResultRepository.findByMbtiTypeOrderByCreatedAtDesc(mbtiType.toUpperCase());
    }
    
    /**
     * 사용자 테스트 이력 조회
     */
    @Transactional(readOnly = true)
    public List<TestResult> getUserTestHistory(String userUuid) {
        return testResultRepository.findByUserUuidOrderByCreatedAtDesc(userUuid);
    }
    
    /**
     * 공개 결과 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<TestResult> getPublicResults(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return testResultRepository.findByIsPublicFlag("Y", pageable);
    }
    
    /**
     * 인기 결과 조회 (조회수 기준)
     */
    @Transactional(readOnly = true)
    public Page<TestResult> getPopularResults(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.DESC, "viewCount")
                .and(Sort.by(Sort.Direction.DESC, "createdAt")));
        return testResultRepository.findByIsPublicFlag("Y", pageable);
    }
    
    /**
     * 특정 MBTI 타입 결과 조회
     */
    @Transactional(readOnly = true)
    public Page<TestResult> getMbtiResults(String mbtiType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return testResultRepository.findByMbtiTypeAndIsPublicFlag(mbtiType.toUpperCase(), "Y", pageable);
    }
    
    /**
     * 댓글이 많은 결과 조회 (구현 예정)
     */
    @Transactional(readOnly = true)
    public Page<TestResult> getMostCommentedResults(int page, int size) {
        // 현재는 최신순으로 정렬, 추후 댓글 수 기준으로 정렬 예정
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return testResultRepository.findByIsPublicFlag("Y", pageable);
    }
    
    /**
     * 바이럴 결과 조회 (공유수 기준)
     */
    @Transactional(readOnly = true)
    public Page<TestResult> getViralResults(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.DESC, "sharedCount")
                .and(Sort.by(Sort.Direction.DESC, "viewCount"))
                .and(Sort.by(Sort.Direction.DESC, "createdAt")));
        return testResultRepository.findByIsPublicFlag("Y", pageable);
    }
    
    /**
     * 결과 공개/비공개 토글
     */
    public boolean toggleResultVisibility(Long resultId, String userUuid) {
        Optional<TestResult> optionalResult = testResultRepository.findByResultIdAndUserUuid(resultId, userUuid);
        if (optionalResult.isPresent()) {
            TestResult result = optionalResult.get();
            result.setPublic(!result.isPublic());
            testResultRepository.save(result);
            return result.isPublic();
        }
        return false;
    }
    
    /**
     * MBTI 추천 정보 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getMbtiRecommendations(String mbtiType) {
        Map<String, Object> recommendations = new HashMap<>();
        
        // 기본 MBTI 정보
        Map<String, String> mbtiInfo = getMbtiInfo(mbtiType);
        recommendations.put("description", mbtiInfo.get("description"));
        recommendations.put("color", mbtiInfo.get("color"));
        
        // 강점과 약점
        recommendations.put("strengths", getStrengths(mbtiType));
        recommendations.put("weaknesses", getWeaknesses(mbtiType));
        
        // 추천 직업
        recommendations.put("careers", getRecommendedCareers(mbtiType));
        
        // 궁합
        recommendations.put("compatibility", getCompatibility(mbtiType));
        
        // 유명인
        recommendations.put("celebrities", getCelebrities(mbtiType));
        
        return recommendations;
    }
    
    /**
     * 조회수 증가
     */
    public void incrementViewCount(Long resultId) {
        Optional<TestResult> optionalResult = testResultRepository.findById(resultId);
        if (optionalResult.isPresent()) {
            TestResult result = optionalResult.get();
            result.setViewCount(result.getViewCount() + 1);
            testResultRepository.save(result);
        }
    }
    
    /**
     * 공유수 증가
     */
    public void incrementSharedCount(Long resultId) {
        Optional<TestResult> optionalResult = testResultRepository.findById(resultId);
        if (optionalResult.isPresent()) {
            TestResult result = optionalResult.get();
            result.setSharedCount(result.getSharedCount() + 1);
            testResultRepository.save(result);
        }
    }
    
    /**
     * 카테고리 점수 파싱
     */
    @Transactional(readOnly = true)
    public Map<String, Object> parseCategoryScores(String categoryScoresJson) {
        try {
            if (categoryScoresJson == null || categoryScoresJson.trim().isEmpty()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(categoryScoresJson, Map.class);
        } catch (JsonProcessingException e) {
            log.error("카테고리 점수 파싱 중 오류 발생: {}", categoryScoresJson, e);
            return new HashMap<>();
        }
    }
    
    /**
     * 답변 데이터 파싱
     */
    @Transactional(readOnly = true)
    public List<Integer> parseAnswerData(String answerDataJson) {
        try {
            if (answerDataJson == null || answerDataJson.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(answerDataJson, List.class);
        } catch (JsonProcessingException e) {
            log.error("답변 데이터 파싱 중 오류 발생: {}", answerDataJson, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * MBTI 타입 계산
     */
    private String calculateMbtiType(Map<String, Object> categoryScores) {
        StringBuilder mbti = new StringBuilder();
        
        // E vs I (외향 vs 내향)
        double eScore = ((Number) categoryScores.getOrDefault("E", 0)).doubleValue();
        double iScore = ((Number) categoryScores.getOrDefault("I", 0)).doubleValue();
        mbti.append(eScore > iScore ? "E" : "I");
        
        // S vs N (감각 vs 직관)
        double sScore = ((Number) categoryScores.getOrDefault("S", 0)).doubleValue();
        double nScore = ((Number) categoryScores.getOrDefault("N", 0)).doubleValue();
        mbti.append(sScore > nScore ? "S" : "N");
        
        // T vs F (사고 vs 감정)
        double tScore = ((Number) categoryScores.getOrDefault("T", 0)).doubleValue();
        double fScore = ((Number) categoryScores.getOrDefault("F", 0)).doubleValue();
        mbti.append(tScore > fScore ? "T" : "F");
        
        // J vs P (판단 vs 인식)
        double jScore = ((Number) categoryScores.getOrDefault("J", 0)).doubleValue();
        double pScore = ((Number) categoryScores.getOrDefault("P", 0)).doubleValue();
        mbti.append(jScore > pScore ? "J" : "P");
        
        return mbti.toString();
    }
    
    /**
     * 상세 점수 생성
     */
    private Map<String, Object> generateDetailedScores(Map<String, Object> categoryScores) {
        Map<String, Object> detailed = new HashMap<>();
        
        double eScore = ((Number) categoryScores.getOrDefault("E", 0)).doubleValue();
        double iScore = ((Number) categoryScores.getOrDefault("I", 0)).doubleValue();
        double sScore = ((Number) categoryScores.getOrDefault("S", 0)).doubleValue();
        double nScore = ((Number) categoryScores.getOrDefault("N", 0)).doubleValue();
        double tScore = ((Number) categoryScores.getOrDefault("T", 0)).doubleValue();
        double fScore = ((Number) categoryScores.getOrDefault("F", 0)).doubleValue();
        double jScore = ((Number) categoryScores.getOrDefault("J", 0)).doubleValue();
        double pScore = ((Number) categoryScores.getOrDefault("P", 0)).doubleValue();
        
        detailed.put("EI", Map.of("E", eScore, "I", iScore, "tendency", eScore > iScore ? "E" : "I"));
        detailed.put("SN", Map.of("S", sScore, "N", nScore, "tendency", sScore > nScore ? "S" : "N"));
        detailed.put("TF", Map.of("T", tScore, "F", fScore, "tendency", tScore > fScore ? "T" : "F"));
        detailed.put("JP", Map.of("J", jScore, "P", pScore, "tendency", jScore > pScore ? "J" : "P"));
        
        return detailed;
    }
    
    /**
     * MBTI 기본 정보 조회
     */
    private Map<String, String> getMbtiInfo(String mbtiType) {
        Map<String, String> info = new HashMap<>();
        
        // MBTI 타입별 설명과 색상 (간단한 예시)
        switch (mbtiType.toUpperCase()) {
            case "INTJ":
                info.put("description", "건축가 - 상상력이 풍부하고 전략적인 사고를 하는 사람");
                info.put("color", "#6C63FF");
                break;
            case "INTP":
                info.put("description", "논리술사 - 지식에 대한 갈증이 있는 혁신적인 발명가");
                info.put("color", "#9C88FF");
                break;
            case "ENTJ":
                info.put("description", "통솔자 - 대담하고 상상력이 풍부한 의지가 강한 지도자");
                info.put("color", "#FF6B6B");
                break;
            case "ENTP":
                info.put("description", "변론가 - 영리하고 호기심이 많은 사색가");
                info.put("color", "#4ECDC4");
                break;
            case "INFJ":
                info.put("description", "옹호자 - 선의의 옹호자이며 창의적이고 통찰력이 있는 사람");
                info.put("color", "#45B7D1");
                break;
            case "INFP":
                info.put("description", "중재자 - 충성스럽고 선의의 이상주의자");
                info.put("color", "#96CEB4");
                break;
            case "ENFJ":
                info.put("description", "주인공 - 카리스마 있고 영감을 주는 지도자");
                info.put("color", "#FFEAA7");
                break;
            case "ENFP":
                info.put("description", "활동가 - 열정적이고 창의적인 사회자");
                info.put("color", "#FD79A8");
                break;
            case "ISTJ":
                info.put("description", "물류사 - 사실과 믿을 만한 실용주의자");
                info.put("color", "#636E72");
                break;
            case "ISFJ":
                info.put("description", "수호자 - 헌신적이고 따뜻한 보호자");
                info.put("color", "#A29BFE");
                break;
            case "ESTJ":
                info.put("description", "경영자 - 우수한 관리자이자 전통과 질서의 수호자");
                info.put("color", "#E17055");
                break;
            case "ESFJ":
                info.put("description", "집정관 - 도움이 되고 인기가 많으며 배려심이 깊은 사람");
                info.put("color", "#FDCB6E");
                break;
            case "ISTP":
                info.put("description", "만능재주꾼 - 대담하면서도 현실적인 실험정신이 풍부한 사람");
                info.put("color", "#00B894");
                break;
            case "ISFP":
                info.put("description", "모험가 - 유연하고 매력적인 예술가");
                info.put("color", "#E84393");
                break;
            case "ESTP":
                info.put("description", "사업가 - 영리하고 활동적이며 인식이 뛰어난 사람");
                info.put("color", "#00CEC9");
                break;
            case "ESFP":
                info.put("description", "연예인 - 자발적이고 열정적이며 사교적인 사람");
                info.put("color", "#FF7675");
                break;
            default:
                info.put("description", "독특한 성격을 가진 사람");
                info.put("color", "#74B9FF");
        }
        
        return info;
    }
    
    /**
     * 강점 조회
     */
    private List<String> getStrengths(String mbtiType) {
        // 간단한 예시 (실제로는 DB나 설정 파일에서 가져올 수 있음)
        return Arrays.asList("창의적 사고", "문제 해결 능력", "리더십", "협업 능력");
    }
    
    /**
     * 약점 조회
     */
    private List<String> getWeaknesses(String mbtiType) {
        return Arrays.asList("완벽주의", "스트레스에 민감", "변화에 대한 저항");
    }
    
    /**
     * 추천 직업 조회
     */
    private List<String> getRecommendedCareers(String mbtiType) {
        return Arrays.asList("소프트웨어 개발자", "프로젝트 매니저", "컨설턴트", "연구원");
    }
    
    /**
     * 궁합 조회
     */
    private Map<String, List<String>> getCompatibility(String mbtiType) {
        Map<String, List<String>> compatibility = new HashMap<>();
        compatibility.put("best", Arrays.asList("ENFP", "ENTP"));
        compatibility.put("good", Arrays.asList("INFP", "INTP", "ENFJ"));
        return compatibility;
    }
    
    /**
     * 유명인 조회
     */
    private List<String> getCelebrities(String mbtiType) {
        return Arrays.asList("빌 게이츠", "일론 머스크", "마크 저커버그");
    }
    
    /**
     * MBTI 타입별 통계 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getMbtiTypeStatistics() {
        Map<String, Long> stats = new HashMap<>();
        List<Object[]> results = testResultRepository.countByMbtiType();
        
        for (Object[] result : results) {
            String mbtiType = (String) result[0];
            Long count = ((Number) result[1]).longValue();
            stats.put(mbtiType, count);
        }
        
        return stats;
    }
    
    /**
     * 특정 기간의 테스트 결과 조회
     */
    @Transactional(readOnly = true)
    public List<TestResult> getTestResultsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return testResultRepository.findByCreatedAtBetween(startDate, endDate);
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
                // X-Forwarded-For 헤더는 여러 IP를 포함할 수 있으므로 첫 번째 IP만 사용
                if (ipAddress.contains(",")) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
                return ipAddress.trim();
            }
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * 테스트 결과 업데이트 (AI 분석 추가 등)
     */
    public TestResult updateTestResult(Long resultId, String aiAnalysis) {
        Optional<TestResult> optionalResult = testResultRepository.findById(resultId);
        if (optionalResult.isPresent()) {
            TestResult testResult = optionalResult.get();
            testResult.setAiAnalysis(aiAnalysis);
            return testResultRepository.save(testResult);
        }
        throw new RuntimeException("테스트 결과를 찾을 수 없습니다. ID: " + resultId);
    }
    
    /**
     * 테스트 결과 삭제 (관리자용)
     */
    public void deleteTestResult(Long resultId) {
        if (testResultRepository.existsById(resultId)) {
            testResultRepository.deleteById(resultId);
            log.info("테스트 결과 삭제 완료 - ID: {}", resultId);
        } else {
            throw new RuntimeException("테스트 결과를 찾을 수 없습니다. ID: " + resultId);
        }
    }
}