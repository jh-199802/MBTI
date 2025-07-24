package com.example.mbtitest.controller;

import com.example.mbtitest.entity.Comment;
import com.example.mbtitest.service.CommentService;
import com.example.mbtitest.service.ViewLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/comments")
public class CommentController {
    
    private final CommentService commentService;
    private final ViewLogService viewLogService;
    
    /**
     * 커뮤니티 페이지 (댓글 모음)
     */
    @GetMapping({"", "/index"})
    public String communityPage(@RequestParam(value = "mbti", required = false) String mbtiType,
                              HttpServletRequest request, Model model) {
        try {
            // 페이지 방문 로그 기록 (실패해도 페이지 로드에 영향 없음)
            try {
                viewLogService.recordCommunityPageView(mbtiType, request);
            } catch (Exception logError) {
                log.warn("커뮤니티 페이지 방문 로그 기록 실패: {}", logError.getMessage());
            }
            
            // 댓글 데이터 조회
            List<Comment> comments;
            if (mbtiType != null && !mbtiType.isEmpty()) {
                comments = commentService.getCommentsByMbtiType(mbtiType);
                model.addAttribute("selectedMbti", mbtiType.toUpperCase());
                model.addAttribute("pageTitle", mbtiType.toUpperCase() + " 커뮤니티");
            } else {
                comments = commentService.getRecentComments(50);
                model.addAttribute("pageTitle", "MBTI 커뮤니티");
            }
            
            model.addAttribute("comments", comments);
            
            // 인기 댓글들
            List<Comment> popularComments = commentService.getPopularComments(10);
            model.addAttribute("popularComments", popularComments);
            
            // MBTI 타입별 댓글 통계
            Map<String, Long> commentStats = commentService.getCommentStatsByMbtiType();
            model.addAttribute("commentStats", commentStats);
            
            // 전체 댓글 수 계산
            long totalComments = commentStats.values().stream().mapToLong(Long::longValue).sum();
            model.addAttribute("totalComments", totalComments);
            
            // 가장 활발한 MBTI 타입 계산
            String mostActiveMbti = commentStats.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("ENFP");
            model.addAttribute("mostActiveMbti", mostActiveMbti);
            
            // MBTI 타입 목록 (필터링용)
            String[] mbtiTypes = {"ENFP", "ENFJ", "ENTP", "ENTJ", "ESFP", "ESFJ", "ESTP", "ESTJ",
                                 "INFP", "INFJ", "INTP", "INTJ", "ISFP", "ISFJ", "ISTP", "ISTJ"};
            model.addAttribute("mbtiTypes", mbtiTypes);
            
            log.info("커뮤니티 페이지 접근 - MBTI: {}, IP: {}", mbtiType, getClientIp(request));
            return "community/index";
            
        } catch (Exception e) {
            log.error("커뮤니티 페이지 로드 중 오류 발생", e);
            model.addAttribute("errorMessage", "커뮤니티 데이터를 불러오는데 문제가 발생했습니다.");
            return "error/error";
        }
    }
    
    /**
     * 댓글 작성 API
     */
    @PostMapping("/api/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addComment(@RequestBody Map<String, String> request,
                                                        HttpServletRequest httpRequest) {
        try {
            String resultIdStr = request.get("resultId");
            String mbtiType = request.get("mbtiType");
            String nickname = request.get("nickname");
            String commentText = request.get("commentText");
            
            // 입력값 검증
            if (mbtiType == null || mbtiType.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "MBTI 타입을 선택해주세요."));
            }
            
            if (commentText == null || commentText.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "댓글 내용을 입력해주세요."));
            }
            
            Long resultId = null;
            if (resultIdStr != null && !resultIdStr.trim().isEmpty()) {
                try {
                    resultId = Long.parseLong(resultIdStr);
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "올바르지 않은 결과 ID입니다."));
                }
            }
            
            Comment comment = commentService.addComment(resultId, mbtiType, nickname, commentText, httpRequest);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "댓글이 성공적으로 작성되었습니다.",
                "comment", Map.of(
                    "id", comment.getCommentId(),
                    "mbtiType", comment.getMbtiType(),
                    "nickname", comment.getNickname() != null ? comment.getNickname() : "익명",
                    "commentText", comment.getCommentText(),
                    "likesCount", comment.getLikesCount(),
                    "createdAt", comment.getCreatedAt().toString()
                )
            ));
            
        } catch (RuntimeException e) {
            log.warn("댓글 작성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("댓글 작성 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "댓글 작성 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 특정 MBTI 타입의 댓글들 조회 API
     */
    @GetMapping("/api/mbti/{mbtiType}")
    @ResponseBody
    public ResponseEntity<Object> getCommentsByMbtiType(@PathVariable String mbtiType) {
        try {
            List<Comment> comments = commentService.getCommentsByMbtiType(mbtiType);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("MBTI 타입별 댓글 조회 중 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "댓글 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 특정 테스트 결과의 댓글들 조회 API
     */
    @GetMapping("/api/result/{resultId}")
    @ResponseBody
    public ResponseEntity<Object> getCommentsByResultId(@PathVariable Long resultId) {
        try {
            List<Comment> comments = commentService.getCommentsByResultId(resultId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("테스트 결과별 댓글 조회 중 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "댓글 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 최근 댓글들 조회 API
     */
    @GetMapping("/api/recent")
    @ResponseBody
    public ResponseEntity<Object> getRecentComments(@RequestParam(defaultValue = "20") int limit) {
        try {
            List<Comment> comments = commentService.getRecentComments(limit);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("최근 댓글 조회 중 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "최근 댓글 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 인기 댓글들 조회 API
     */
    @GetMapping("/api/popular")
    @ResponseBody
    public ResponseEntity<Object> getPopularComments(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Comment> comments = commentService.getPopularComments(limit);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("인기 댓글 조회 중 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "인기 댓글 조회 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 댓글 좋아요 API
     */
    @PostMapping("/api/{commentId}/like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> likeComment(@PathVariable Long commentId) {
        try {
            Comment comment = commentService.likeComment(commentId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "좋아요가 추가되었습니다.",
                "likesCount", comment.getLikesCount()
            ));
            
        } catch (RuntimeException e) {
            log.warn("댓글 좋아요 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("댓글 좋아요 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "좋아요 처리 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 댓글 수정 API
     */
    @PutMapping("/api/{commentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateComment(@PathVariable Long commentId,
                                                           @RequestBody Map<String, String> request,
                                                           HttpServletRequest httpRequest) {
        try {
            String newCommentText = request.get("commentText");
            
            if (newCommentText == null || newCommentText.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "댓글 내용을 입력해주세요."));
            }
            
            Comment comment = commentService.updateComment(commentId, newCommentText, httpRequest);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "댓글이 성공적으로 수정되었습니다.",
                "comment", Map.of(
                    "id", comment.getCommentId(),
                    "commentText", comment.getCommentText(),
                    "updatedAt", comment.getUpdatedAt().toString()
                )
            ));
            
        } catch (RuntimeException e) {
            log.warn("댓글 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("댓글 수정 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "댓글 수정 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 댓글 삭제 API
     */
    @DeleteMapping("/api/{commentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable Long commentId,
                                                           HttpServletRequest httpRequest) {
        try {
            commentService.deleteComment(commentId, httpRequest);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "댓글이 성공적으로 삭제되었습니다."
            ));
            
        } catch (RuntimeException e) {
            log.warn("댓글 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("댓글 삭제 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "댓글 삭제 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 댓글 통계 API
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getCommentStats() {
        try {
            Map<String, Long> stats = commentService.getCommentStatsByMbtiType();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("댓글 통계 조회 중 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", -1L));
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
