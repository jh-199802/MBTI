package com.example.mbtitest.service;

import com.example.mbtitest.entity.Comment;
import com.example.mbtitest.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentService {
    
    private final CommentRepository commentRepository;
    
    // 스팸 방지를 위한 상수들
    private static final int MAX_COMMENTS_PER_IP_PER_HOUR = 5;
    private static final int MAX_COMMENT_LENGTH = 1000;
    private static final int MIN_COMMENT_LENGTH = 3;
    
    /**
     * 댓글 작성
     */
    public Comment addComment(Long resultId, String mbtiType, String nickname, 
                            String commentText, HttpServletRequest request) {
        
        log.debug("댓글 작성 서비스 시작 - resultId: {}, mbtiType: {}, nickname: {}, commentText 길이: {}", 
            resultId, mbtiType, nickname, commentText != null ? commentText.length() : 0);
        
        // 입력 값 검증
        validateCommentInput(commentText, mbtiType);
        
        String userIp = getClientIpAddress(request);
        log.debug("클라이언트 IP: {}", userIp);
        
        // 스팸 체크
        checkSpamPrevention(userIp);
        
        try {
            // resultId가 null인 경우는 커뮤니티 댓글로 처리
            if (resultId != null) {
                log.info("특정 테스트 결과({})에 대한 댓글 작성", resultId);
            } else {
                log.info("커뮤니티 댓글 작성 (resultId = null)");
            }
            
            Comment comment = Comment.builder()
                .resultId(resultId)  // null 허용 (커뮤니티 댓글)
                .mbtiType(mbtiType.toUpperCase())
                .nickname(nickname != null ? nickname.trim() : null)
                .commentText(commentText.trim())
                .userIp(userIp)
                .likesCount(0)
                .isDeleted("N")
                .build();
            
            log.debug("댓글 엔티티 생성 완료: {}", comment);
            
            Comment savedComment = commentRepository.save(comment);
            log.info("댓글 작성 완료 - ID: {}, MBTI: {}, IP: {}, resultId: {}", 
                savedComment.getCommentId(), mbtiType, userIp, resultId);
            
            return savedComment;
            
        } catch (Exception e) {
            log.error("댓글 저장 중 오류 발생 - mbtiType: {}, commentText: {}", mbtiType, commentText, e);
            throw new RuntimeException("댓글 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    
    /**
     * 특정 MBTI 타입의 댓글들 조회
     */
    @Transactional(readOnly = true)
    public List<Comment> getCommentsByMbtiType(String mbtiType) {
        return commentRepository.findByMbtiTypeAndIsDeletedOrderByCreatedAtDesc(
            mbtiType.toUpperCase(), "N");
    }
    
    /**
     * 특정 테스트 결과의 댓글들 조회
     */
    @Transactional(readOnly = true)
    public List<Comment> getCommentsByResultId(Long resultId) {
        return commentRepository.findByResultIdAndIsDeletedOrderByCreatedAtDesc(resultId, "N");
    }
    
    /**
     * 최근 댓글들 조회
     */
    @Transactional(readOnly = true)
    public List<Comment> getRecentComments(int limit) {
        List<Comment> allComments = commentRepository.findByIsDeletedOrderByCreatedAtDesc("N");
        return allComments.subList(0, Math.min(limit, allComments.size()));
    }
    
    /**
     * 인기 댓글들 조회 (좋아요 수 기준)
     */
    @Transactional(readOnly = true)
    public List<Comment> getPopularComments(int limit) {
        return commentRepository.findTopCommentsByLikes(limit);
    }
    
    /**
     * 댓글 좋아요 증가
     */
    public Comment likeComment(Long commentId) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        if (optionalComment.isPresent()) {
            Comment comment = optionalComment.get();
            if ("N".equals(comment.getIsDeleted())) {
                comment.setLikesCount(comment.getLikesCount() + 1);
                Comment updatedComment = commentRepository.save(comment);
                log.info("댓글 좋아요 증가 - ID: {}, 현재 좋아요: {}", 
                    commentId, updatedComment.getLikesCount());
                return updatedComment;
            } else {
                throw new RuntimeException("삭제된 댓글입니다.");
            }
        }
        throw new RuntimeException("댓글을 찾을 수 없습니다. ID: " + commentId);
    }
    
    /**
     * 댓글 삭제 (소프트 삭제)
     */
    public void deleteComment(Long commentId, HttpServletRequest request) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        if (optionalComment.isPresent()) {
            Comment comment = optionalComment.get();
            
            // IP 확인 (본인 댓글만 삭제 가능)
            String userIp = getClientIpAddress(request);
            if (!comment.getUserIp().equals(userIp)) {
                throw new RuntimeException("본인의 댓글만 삭제할 수 있습니다.");
            }
            
            comment.setIsDeleted("Y");
            commentRepository.save(comment);
            log.info("댓글 삭제 완료 - ID: {}, IP: {}", commentId, userIp);
        } else {
            throw new RuntimeException("댓글을 찾을 수 없습니다. ID: " + commentId);
        }
    }
    
    /**
     * MBTI 타입별 댓글 통계
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getCommentStatsByMbtiType() {
        Map<String, Long> stats = new HashMap<>();
        List<Object[]> results = commentRepository.getCommentStatsByMbtiType();
        
        for (Object[] result : results) {
            String mbtiType = (String) result[0];
            Long count = ((Number) result[1]).longValue();
            stats.put(mbtiType, count);
        }
        
        return stats;
    }
    
    /**
     * 댓글 수정
     */
    public Comment updateComment(Long commentId, String newCommentText, HttpServletRequest request) {
        // 입력 값 검증
        if (newCommentText == null || newCommentText.trim().length() < MIN_COMMENT_LENGTH) {
            throw new RuntimeException("댓글은 최소 " + MIN_COMMENT_LENGTH + "자 이상이어야 합니다.");
        }
        if (newCommentText.trim().length() > MAX_COMMENT_LENGTH) {
            throw new RuntimeException("댓글은 최대 " + MAX_COMMENT_LENGTH + "자까지 가능합니다.");
        }
        
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        if (optionalComment.isPresent()) {
            Comment comment = optionalComment.get();
            
            // IP 확인 (본인 댓글만 수정 가능)
            String userIp = getClientIpAddress(request);
            if (!comment.getUserIp().equals(userIp)) {
                throw new RuntimeException("본인의 댓글만 수정할 수 있습니다.");
            }
            
            if ("Y".equals(comment.getIsDeleted())) {
                throw new RuntimeException("삭제된 댓글은 수정할 수 없습니다.");
            }
            
            comment.setCommentText(newCommentText.trim());
            Comment updatedComment = commentRepository.save(comment);
            log.info("댓글 수정 완료 - ID: {}, IP: {}", commentId, userIp);
            
            return updatedComment;
        }
        throw new RuntimeException("댓글을 찾을 수 없습니다. ID: " + commentId);
    }
    
    /**
     * 입력 값 검증
     */
    private void validateCommentInput(String commentText, String mbtiType) {
        if (commentText == null || commentText.trim().isEmpty()) {
            throw new RuntimeException("댓글 내용을 입력해주세요.");
        }
        
        if (commentText.trim().length() < MIN_COMMENT_LENGTH) {
            throw new RuntimeException("댓글은 최소 " + MIN_COMMENT_LENGTH + "자 이상이어야 합니다.");
        }
        
        if (commentText.trim().length() > MAX_COMMENT_LENGTH) {
            throw new RuntimeException("댓글은 최대 " + MAX_COMMENT_LENGTH + "자까지 가능합니다.");
        }
        
        if (mbtiType == null || mbtiType.trim().length() != 4) {
            throw new RuntimeException("올바른 MBTI 타입을 선택해주세요.");
        }
        
        // 금지어 체크 (간단한 예시)
        String[] bannedWords = {"스팸", "광고", "홍보", "도박", "성인"};
        String lowerCommentText = commentText.toLowerCase();
        for (String bannedWord : bannedWords) {
            if (lowerCommentText.contains(bannedWord)) {
                throw new RuntimeException("부적절한 내용이 포함되어 있습니다.");
            }
        }
    }
    
    /**
     * 스팸 방지 체크
     */
    private void checkSpamPrevention(String userIp) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<Comment> recentComments = commentRepository
            .findByUserIpAndCreatedAtAfterOrderByCreatedAtDesc(userIp, oneHourAgo);
        
        if (recentComments.size() >= MAX_COMMENTS_PER_IP_PER_HOUR) {
            throw new RuntimeException("시간당 댓글 작성 한도를 초과했습니다. 잠시 후 다시 시도해주세요.");
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
}
