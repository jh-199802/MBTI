package com.example.mbtitest.repository;

import com.example.mbtitest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 사용자명으로 조회
    Optional<User> findByUsername(String username);
    
    // 이메일로 조회
    Optional<User> findByEmail(String email);
    
    // 사용자명 또는 이메일로 조회 (로그인용)
    @Query("SELECT u FROM User u WHERE u.username = :loginId OR u.email = :loginId")
    Optional<User> findByUsernameOrEmail(@Param("loginId") String loginId);
    
    // 활성 사용자만 조회
    List<User> findByIsActiveOrderByCreatedAtDesc(String isActive);
    
    // 사용자명 중복 체크
    boolean existsByUsername(String username);
    
    // 이메일 중복 체크
    boolean existsByEmail(String email);
    
    // 특정 기간에 가입한 사용자 수
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    Long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                               @Param("endDate") LocalDateTime endDate);
    
    // MBTI 타입별 사용자 수
    @Query("SELECT u.mbtiType, COUNT(u) FROM User u WHERE u.mbtiType IS NOT NULL AND u.isActive = 'Y' GROUP BY u.mbtiType")
    List<Object[]> countByMbtiTypeGroupBy();
    
    // 최근 활동한 사용자들
    List<User> findTop10ByLastLoginIsNotNullOrderByLastLoginDesc();
    
    // 닉네임으로 검색 (부분 일치)
    List<User> findByNicknameContainingIgnoreCaseAndIsActive(String nickname, String isActive);
}
