package com.example.mbtitest.controller;

import com.example.mbtitest.entity.TestResult;
import com.example.mbtitest.service.TestResultService;
import com.example.mbtitest.service.ViewLogService;
import com.example.mbtitest.service.StatisticsService;
import com.example.mbtitest.service.CommentService;
import com.example.mbtitest.entity.Comment;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {
    
    private final TestResultService testResultService;
    private final ViewLogService viewLogService;
    private final StatisticsService statisticsService;
    private final CommentService commentService;
    private final Environment environment;
    
    /**
     * AI_KEY 값을 가져오는 헬퍼 메서드
     */
    private String getAiKey() {
        return environment.getProperty("AI_KEY", "NOT_SET");
    }
    
    /**
     * 메인 페이지 (인덱스)
     */
    @GetMapping({"/", "/main", "/index", "/home"})
    public String main(HttpServletRequest request, Model model) {
        try {
            // 페이지 방문 로그 기록 (실패해도 페이지 로드에 영향 없음)
            try {
                viewLogService.recordMainPageView(request);
            } catch (Exception logError) {
                log.warn("메인 페이지 방문 로그 기록 실패: {}", logError.getMessage());
            }
            
            // 통계 초기화 (오늘 날짜 레코드가 없으면 생성)
            try {
                statisticsService.initializeTodayStats();
            } catch (Exception statsError) {
                log.warn("통계 초기화 실패: {}", statsError.getMessage());
            }
            
            // 간단한 통계 정보 추가 (안전한 방식으로)
            Map<String, Object> quickStats = new HashMap<>();
            
            try {
                quickStats.put("totalTests", statisticsService.getDashboardStats().getOrDefault("totalTests", 1000L));
                quickStats.put("mostPopularMbti", statisticsService.getMostPopularMbtiType());
            } catch (Exception e) {
                log.warn("통계 조회 실패, 기본값 사용: {}", e.getMessage());
                quickStats.put("totalTests", 1000L);
                quickStats.put("mostPopularMbti", "ENFP");
            }
            
            model.addAttribute("quickStats", quickStats);
            model.addAttribute("pageTitle", "정확한 MBTI 성격 테스트");
            
            return "main";
        } catch (Exception e) {
            log.error("메인 페이지 로드 중 오류", e);
            return "main"; // 오류가 있어도 페이지는 보여주기
        }
    }
    

    
    /**
     * 테스트 페이지
     */
    @GetMapping("/test")
    public String test(HttpServletRequest request, Model model) {
        try {
            // 페이지 방문 로그 기록
            viewLogService.recordTestPageView(request);
            
            model.addAttribute("pageTitle", "MBTI 성격 테스트 시작");
            
            return "test";
        } catch (Exception e) {
            log.error("테스트 페이지 로드 중 오류", e);
            return "test";
        }
    }
    
    /**
     * 결과 페이지
     */
    @GetMapping("/result/{resultId}")
    public String result(@PathVariable Long resultId, HttpServletRequest request, Model model) {
        try {
            Optional<TestResult> testResultOpt = testResultService.getTestResult(resultId);
            
            if (testResultOpt.isEmpty()) {
                model.addAttribute("errorMessage", "존재하지 않는 테스트 결과입니다.");
                return "error/404";
            }
            
            TestResult testResult = testResultOpt.get();
            
            // 페이지 방문 로그 기록
            viewLogService.recordResultPageView(testResult.getMbtiType(), request);
            
            // 모델에 데이터 추가
            model.addAttribute("testResult", testResult);
            model.addAttribute("mbtiType", testResult.getMbtiType());
            model.addAttribute("aiAnalysis", testResult.getAiAnalysis());
            model.addAttribute("categoryScores", testResultService.parseCategoryScores(testResult.getCategoryScores()));
            model.addAttribute("resultId", resultId);
            model.addAttribute("pageTitle", testResult.getMbtiType() + " - MBTI 테스트 결과");
            
            // 해당 결과의 댓글들 조회
            List<Comment> comments = commentService.getCommentsByResultId(resultId);
            model.addAttribute("comments", comments);
            
            // 같은 MBTI 타입의 다른 댓글들
            List<Comment> sameTypeComments = commentService.getCommentsByMbtiType(testResult.getMbtiType());
            model.addAttribute("sameTypeComments", sameTypeComments.subList(0, Math.min(5, sameTypeComments.size())));
            
            // MBTI 타입 정보 추가
            model.addAttribute("mbtiInfo", getMbtiTypeInfo(testResult.getMbtiType()));
            
            return "result";
        } catch (NumberFormatException e) {
            log.warn("잘못된 결과 ID: {}", resultId);
            model.addAttribute("errorMessage", "올바르지 않은 결과 ID입니다.");
            return "error/400";
        } catch (Exception e) {
            log.error("결과 페이지 로드 중 오류", e);
            model.addAttribute("errorMessage", "결과를 불러오는데 문제가 발생했습니다.");
            return "error/error";
        }
    }
    

    
    /**
     * 개인정보처리방침 페이지
     */
    @GetMapping("/privacy")
    public String privacy(Model model) {
        model.addAttribute("pageTitle", "개인정보처리방침");
        return "privacy";
    }
    
    /**
     * 이용약관 페이지
     */
    @GetMapping("/terms")
    public String terms(Model model) {
        model.addAttribute("pageTitle", "이용약관");
        return "terms";
    }
    
    /**
     * About 페이지 (서비스 소개)
     */
    @GetMapping("/about")
    public String about(Model model) {
        log.info("About 페이지 요청");
        model.addAttribute("pageTitle", "서비스 소개");
        return "about"; // templates/about.html
    }
    
    /**
     * Contact 페이지 (문의)
     */
    @GetMapping("/contact")
    public String contact(Model model) {
        log.info("Contact 페이지 요청");
        model.addAttribute("pageTitle", "문의하기");
        return "contact"; // templates/contact.html
    }
    
    /**
     * MBTI 타입별 상세 페이지
     */
    @GetMapping("/mbti/{mbtiType}")
    public String mbtiTypePage(@PathVariable String mbtiType, HttpServletRequest request, Model model) {
        try {
            String upperMbtiType = mbtiType.toUpperCase();
            
            // MBTI 타입 유효성 검증
            if (!isValidMbtiType(upperMbtiType)) {
                model.addAttribute("errorMessage", "올바르지 않은 MBTI 타입입니다.");
                return "error/400";
            }
            
            // 페이지 방문 로그 기록
            viewLogService.recordMbtiPageView(upperMbtiType, request);
            
            // MBTI 타입 정보
            model.addAttribute("mbtiType", upperMbtiType);
            model.addAttribute("mbtiInfo", getMbtiTypeInfo(upperMbtiType));
            model.addAttribute("pageTitle", upperMbtiType + " 성격 유형 상세");
            
            // 해당 타입의 최근 테스트 결과들
            List<TestResult> recentResults = testResultService.getRecentTestResultsByMbti(upperMbtiType);
            model.addAttribute("recentResults", recentResults.subList(0, Math.min(5, recentResults.size())));
            
            // 해당 타입의 댓글들
            List<Comment> typeComments = commentService.getCommentsByMbtiType(upperMbtiType);
            model.addAttribute("typeComments", typeComments.subList(0, Math.min(10, typeComments.size())));
            
            // 타입별 통계
            Map<String, Long> mbtiStats = testResultService.getMbtiTypeStatistics();
            model.addAttribute("mbtiStats", mbtiStats);
            model.addAttribute("typeCount", mbtiStats.getOrDefault(upperMbtiType, 0L));
            
            return "mbti-type";
        } catch (Exception e) {
            log.error("MBTI 타입 페이지 로드 중 오류", e);
            model.addAttribute("errorMessage", "페이지를 불러오는데 문제가 발생했습니다.");
            return "error/error";
        }
    }
    
    /**
     * AI 분석 요청 처리 (서버에서 직접 API 호출 + DB 저장)
     * 클라이언트는 답변만 전송, API 키는 서버에서만 사용
     */
    @PostMapping("/api/analyze")
    @ResponseBody
    public Map<String, Object> analyzePersonality(@RequestBody Map<String, Object> requestData,
                                                 HttpServletRequest request) {
        try {
            // 클라이언트에서 받은 데이터
            String prompt = (String) requestData.get("prompt");
            String mbtiType = (String) requestData.get("mbtiType");
            Integer testDuration = (Integer) requestData.get("testDuration");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> categoryScores = (Map<String, Object>) requestData.get("categoryScores");
            @SuppressWarnings("unchecked")
            List<Integer> answers = (List<Integer>) requestData.get("answers");
            
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
            String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + getAiKey();
            
            ResponseEntity<Map> response = restTemplate.exchange(
                geminiUrl,
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            // AI 분석 결과 추출
            String aiAnalysis = extractAiAnalysisFromResponse(response.getBody());
            
            // DB에 결과 저장
            TestResult savedResult = null;
            if (mbtiType != null && categoryScores != null && answers != null) {
                try {
                    // UUID 생성
                    String userUuid = UUID.randomUUID().toString();
                    
                    savedResult = testResultService.saveTestResult(
                        userUuid, categoryScores, answers, aiAnalysis, testDuration, request);
                } catch (Exception e) {
                    log.error("DB 저장 실패", e);
                    // DB 저장 실패해도 AI 분석 결과는 반환
                }
            }
            
            // 성공 응답 반환
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response.getBody());
            result.put("aiAnalysis", aiAnalysis);
            result.put("mbtiType", mbtiType);
            
            if (savedResult != null) {
                result.put("resultId", savedResult.getResultId());
                result.put("resultUrl", "/result/" + savedResult.getResultId());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("AI 분석 중 오류 발생", e);
            // 에러 응답 반환
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    /**
     * 사용자 테스트 이력 조회
     */
    @GetMapping("/api/history")
    @ResponseBody
    public ResponseEntity<List<TestResult>> getUserHistory(HttpServletRequest request) {
        try {
            // TODO: 실제 환경에서는 세션에서 userUuid를 가져와야 함
            // 현재는 빈 리스트로 처리 (사용자 인증 시스템이 없으므로)
            List<TestResult> history = new ArrayList<>();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("사용자 이력 조회 중 오류", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * MBTI 타입별 통계 API
     */
    @GetMapping("/api/mbti-stats")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getMbtiTypeStatistics() {
        try {
            Map<String, Long> stats = testResultService.getMbtiTypeStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("MBTI 통계 조회 중 오류", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 대시보드 통계 API (테스트 페이지용)
     */
    @GetMapping("/api/stats/dashboard")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardStatsForTest() {
        try {
            // 기본 통계 데이터 (DB 연결이 안 되어도 작동하도록)
            Map<String, Object> stats = new HashMap<>();
            
            try {
                // 실제 통계 조회 시도
                stats.put("totalTests", testResultService.getMbtiTypeStatistics().values().stream()
                    .mapToLong(Long::longValue).sum());
                stats.put("totalShares", 0L); // 임시값
                stats.put("mostPopularMbti", statisticsService.getMostPopularMbtiType());
                stats.put("mbtiStats", testResultService.getMbtiTypeStatistics());
            } catch (Exception dbError) {
                log.warn("DB 연결 오류로 인해 기본값 사용: {}", dbError.getMessage());
                // DB 연결 실패 시 기본값
                stats.put("totalTests", 1500L);
                stats.put("totalShares", 850L);
                stats.put("mostPopularMbti", "ENFP");
                
                // 기본 MBTI 통계
                Map<String, Long> defaultMbtiStats = new HashMap<>();
                defaultMbtiStats.put("ENFP", 187L);
                defaultMbtiStats.put("INFP", 156L);
                defaultMbtiStats.put("ENTP", 143L);
                defaultMbtiStats.put("INTJ", 128L);
                defaultMbtiStats.put("INFJ", 115L);
                defaultMbtiStats.put("ENFJ", 98L);
                defaultMbtiStats.put("INTP", 89L);
                defaultMbtiStats.put("ISFP", 87L);
                defaultMbtiStats.put("ESFP", 75L);
                defaultMbtiStats.put("ISFJ", 67L);
                defaultMbtiStats.put("ENTJ", 58L);
                defaultMbtiStats.put("ESFJ", 52L);
                defaultMbtiStats.put("ISTP", 45L);
                defaultMbtiStats.put("ESTP", 38L);
                defaultMbtiStats.put("ESTJ", 32L);
                defaultMbtiStats.put("ISTJ", 28L);
                stats.put("mbtiStats", defaultMbtiStats);
            }
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("대시보드 통계 API 오류", e);
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("totalTests", 0L);
            errorStats.put("totalShares", 0L);
            errorStats.put("mostPopularMbti", "ENFP");
            errorStats.put("mbtiStats", new HashMap<>());
            return ResponseEntity.ok(errorStats);
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
        String currentAiKey = getAiKey();
        boolean apiKeyConfigured = currentAiKey != null && !currentAiKey.equals("NOT_SET") && !currentAiKey.isEmpty();
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
            String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + getAiKey();
            
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
    
    /**
     * 샘플 데이터로 DB 저장 테스트
     */
    @PostMapping("/api/test-db-save")
    @ResponseBody
    public Map<String, Object> testDBSave(HttpServletRequest request) {
        try {
            log.info("🧪 DB 저장 테스트 시작");
            
            // 샘플 데이터 생성
            String userUuid = UUID.randomUUID().toString();
            
            Map<String, Object> categoryScores = new HashMap<>();
            categoryScores.put("E", 70);
            categoryScores.put("I", 30);
            categoryScores.put("S", 25);
            categoryScores.put("N", 75);
            categoryScores.put("T", 40);
            categoryScores.put("F", 60);
            categoryScores.put("J", 20);
            categoryScores.put("P", 80);
            
            List<Integer> answers = Arrays.asList(1, 2, 3, 2, 1, 3, 2, 1, 3, 2, 1, 3, 2, 1, 3);
            
            String aiAnalysis = """
                {
                  "mbti": {
                    "type": "ENFP",
                    "percentages": {"E": 70, "I": 30, "S": 25, "N": 75, "T": 40, "F": 60, "J": 20, "P": 80},
                    "description": "활발하고 창의적인 성격으로, 새로운 가능성을 추구하는 열정가입니다."
                  },
                  "dnd": {
                    "alignment": "혼돈 선",
                    "description": "자유로우면서도 선한 마음을 가진 성향입니다."
                  },
                  "enneagram": {
                    "type": "7번",
                    "description": "다양한 경험과 즐거움을 추구하는 열정가 유형입니다."
                  },
                  "comprehensive": {
                    "summary": "테스트용 샘플 분석 결과입니다.",
                    "strengths": ["창의성", "사교성", "열정"],
                    "weaknesses": ["집중력 부족", "계획성 부족"],
                    "growth_areas": ["체계적 사고", "인내심"],
                    "one_line_summary": "세상을 밝게 만드는 자유로운 영혼",
                    "similar_characters": {
                      "name": "나루토",
                      "source": "나루토",
                      "reason": "밝고 긍정적인 에너지가 비슷합니다."
                    },
                    "recommendations": "다양한 활동에 참여해보세요."
                  }
                }
                """;
            
            Integer testDuration = 240;
            
            // DB에 저장
            TestResult savedResult = testResultService.saveTestResult(
                userUuid, categoryScores, answers, aiAnalysis, testDuration, request);
            
            log.info("✅ DB 저장 성공! 결과 ID: {}", savedResult.getResultId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "테스트용 데이터가 성공적으로 DB에 저장되었습니다!");
            result.put("resultId", savedResult.getResultId());
            result.put("resultUrl", "/result/" + savedResult.getResultId());
            result.put("h2Console", "http://localhost:10000/h2-console");
            result.put("jdbcUrl", "jdbc:h2:mem:testdb");
            result.put("query", "SELECT * FROM TEST_RESULTS ORDER BY CREATED_AT DESC;");
            
            return result;
            
        } catch (Exception e) {
            log.error("❌ DB 저장 테스트 실패", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    /**
     * AI 응답에서 분석 텍스트 추출
     */
    private String extractAiAnalysisFromResponse(Map<String, Object> responseBody) {
        try {
            if (responseBody == null) return null;
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> firstCandidate = candidates.get(0);
                
                @SuppressWarnings("unchecked")
                Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
                
                if (content != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    
                    if (parts != null && !parts.isEmpty()) {
                        Map<String, Object> firstPart = parts.get(0);
                        return (String) firstPart.get("text");
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("AI 응답 파싱 오류", e);
            return null;
        }
    }
    
    /**
     * MBTI 타입 정보 반환
     */
    private Map<String, String> getMbtiTypeInfo(String mbtiType) {
        Map<String, String> info = new HashMap<>();
        
        switch (mbtiType) {
            case "ENFP":
                info.put("name", "재기발랄한 활동가");
                info.put("description", "열정적이고 창의적인 성격으로 항상 새로운 가능성을 보는 사람");
                info.put("strengths", "창의성, 열정, 사교성, 적응력");
                info.put("weaknesses", "집중력 부족, 스트레스 민감, 세부사항 놓침");
                break;
            case "ENFJ":
                info.put("name", "정의로운 사회운동가");
                info.put("description", "카리스마 있고 영감을 주는 지도자");
                info.put("strengths", "리더십, 공감능력, 소통능력, 이타적");
                info.put("weaknesses", "완벽주의, 타인 의존성, 자기희생");
                break;
            case "ENTP":
                info.put("name", "뜨거운 토론가");
                info.put("description", "똑똑하고 호기심 많은 사상가");
                info.put("strengths", "창의성, 논리성, 유연성, 학습능력");
                info.put("weaknesses", "집중력 부족, 루틴 싫어함, 세부사항 무시");
                break;
            case "ENTJ":
                info.put("name", "대담한 통솔자");
                info.put("description", "천성적인 지도자로 목표 달성에 집중");
                info.put("strengths", "리더십, 결단력, 전략적 사고, 목표지향");
                info.put("weaknesses", "완고함, 감정 무시, 성급함");
                break;
            case "ESFP":
                info.put("name", "자유로운 영혼의 연예인");
                info.put("description", "즉흥적이고 열정적인 연예인");
                info.put("strengths", "사교성, 실용성, 열정, 유연성");
                info.put("weaknesses", "계획성 부족, 충동적, 집중력 부족");
                break;
            case "ESFJ":
                info.put("name", "사교적인 외교관");
                info.put("description", "인기있고 인정받는 사람들을 돌보는 성격");
                info.put("strengths", "협조성, 실용성, 사교성, 책임감");
                info.put("weaknesses", "갈등 회피, 변화 거부, 비판 민감");
                break;
            case "ESTP":
                info.put("name", "모험을 즐기는 사업가");
                info.put("description", "현재 순간을 즐기는 행동파");
                info.put("strengths", "실용성, 적응력, 사교성, 문제해결");
                info.put("weaknesses", "계획성 부족, 충동적, 장기적 사고 부족");
                break;
            case "ESTJ":
                info.put("name", "엄격한 관리자");
                info.put("description", "전통과 질서를 중시하는 관리자");
                info.put("strengths", "조직력, 책임감, 현실적, 효율성");
                info.put("weaknesses", "완고함, 변화 거부, 감정 무시");
                break;
            case "INFP":
                info.put("name", "열정적인 중재자");
                info.put("description", "이상주의적이고 충성스러운 성격");
                info.put("strengths", "창의성, 공감능력, 이상주의, 개방성");
                info.put("weaknesses", "완벽주의, 자기비판, 스트레스 민감");
                break;
            case "INFJ":
                info.put("name", "선의의 옹호자");
                info.put("description", "조용하지만 의지가 강한 이상주의자");
                info.put("strengths", "통찰력, 결단력, 이상주의, 조직력");
                info.put("weaknesses", "완벽주의, 번아웃, 과민함");
                break;
            case "INTP":
                info.put("name", "논리적인 사색가");
                info.put("description", "혁신적인 발명가로 지식에 목마른 성격");
                info.put("strengths", "논리성, 창의성, 객관성, 독립성");
                info.put("weaknesses", "사교성 부족, 실용성 부족, 감정 무시");
                break;
            case "INTJ":
                info.put("name", "용의주도한 전략가");
                info.put("description", "상상력이 풍부하고 결단력 있는 성격");
                info.put("strengths", "전략적 사고, 독립성, 결단력, 완벽주의");
                info.put("weaknesses", "사교성 부족, 완고함, 감정 표현 어려움");
                break;
            case "ISFP":
                info.put("name", "호기심 많은 예술가");
                info.put("description", "유연하고 매력적인 예술가");
                info.put("strengths", "창의성, 공감능력, 유연성, 실용성");
                info.put("weaknesses", "갈등 회피, 스트레스 민감, 계획성 부족");
                break;
            case "ISFJ":
                info.put("name", "용감한 수호자");
                info.put("description", "따뜻하고 헌신적인 수호자");
                info.put("strengths", "헌신적, 책임감, 실용적, 협조적");
                info.put("weaknesses", "변화 거부, 자기희생, 갈등 회피");
                break;
            case "ISTP":
                info.put("name", "만능 재주꾼");
                info.put("description", "대담하고 실용적인 실험정신의 소유자");
                info.put("strengths", "실용성, 독립성, 논리성, 유연성");
                info.put("weaknesses", "감정 표현 어려움, 장기적 계획 부족, 사교성 부족");
                break;
            case "ISTJ":
                info.put("name", "청렴결백한 논리주의자");
                info.put("description", "사실과 신뢰성을 중시하는 실용주의자");
                info.put("strengths", "책임감, 신뢰성, 체계성, 실용성");
                info.put("weaknesses", "변화 거부, 완고함, 감정 무시");
                break;
            default:
                info.put("name", "알 수 없음");
                info.put("description", "알 수 없는 MBTI 타입입니다.");
                info.put("strengths", "-");
                info.put("weaknesses", "-");
        }
        
        return info;
    }
    
    /**
     * MBTI 타입 유효성 검증
     */
    private boolean isValidMbtiType(String mbtiType) {
        String[] validTypes = {"ENFP", "ENFJ", "ENTP", "ENTJ", "ESFP", "ESFJ", "ESTP", "ESTJ",
                              "INFP", "INFJ", "INTP", "INTJ", "ISFP", "ISFJ", "ISTP", "ISTJ"};
        return Arrays.asList(validTypes).contains(mbtiType);
    }
}
