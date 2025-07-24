package com.example.mbtitest.repository;

import com.example.mbtitest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // UUID로 사용자 찾기
    Optional<User> findByUserUuid(String userUuid);
    
    // 소셜 로그인으로 사용자 찾기
    Optional<User> findBySocialTypeAndSocialId(String socialType, String socialId);
    
    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);
    
    // 닉네임으로 사용자 찾기
    List<User> findByNicknameContaining(String nickname);
    
    // 소셜 로그인별 사용자 통계
    @Query("SELECT u.socialType as socialType, COUNT(u) as count " +
           "FROM User u " +
           "WHERE u.socialType IS NOT NULL " +
           "GROUP BY u.socialType " +
           "ORDER BY COUNT(u) DESC")
    List<Map<String, Object>> findUserStatsBySocialType();
    
    // 최근 가입한 사용자들
    @Query("SELECT u FROM User u " +
           "ORDER BY u.createdAt DESC")
    List<User> findRecentUsers();
    
    // 활동적인 사용자 (테스트를 많이 한)
    @Query("SELECT u.userUuid as userUuid, u.nickname as nickname, COUNT(tr) as testCount " +
           "FROM User u " +
           "LEFT JOIN TestResult tr ON u.userUuid = tr.userUuid " +
           "GROUP BY u.userUuid, u.nickname " +
           "ORDER BY COUNT(tr) DESC")
    List<Map<String, Object>> findMostActiveUsers();
    
    // 특정 기간 가입한 사용자 수
    @Query("SELECT COUNT(u) FROM User u " +
           "WHERE u.createdAt >= :since")
    Long countNewUsersSince(@Param("since") LocalDateTime since);
    
    // 일별 가입자 수 통계
    @Query(value = "SELECT TO_CHAR(CREATED_AT, 'YYYY-MM-DD') as date, COUNT(*) as count " +
           "FROM USERS " +
           "WHERE CREATED_AT >= :monthAgo " +
           "GROUP BY TO_CHAR(CREATED_AT, 'YYYY-MM-DD') " +
           "ORDER BY TO_CHAR(CREATED_AT, 'YYYY-MM-DD')", nativeQuery = true)
    List<Map<String, Object>> findDailySignupStats(@Param("monthAgo") LocalDateTime monthAgo);
    
    // 게스트 사용자 수
    @Query("SELECT COUNT(u) FROM User u WHERE u.socialType = 'GUEST' OR u.socialType IS NULL")
    Long countGuestUsers();
    
    // 회원 사용자 수 (소셜 로그인)
    @Query("SELECT COUNT(u) FROM User u WHERE u.socialType != 'GUEST' AND u.socialType IS NOT NULL")
    Long countMemberUsers();
    
    // 사용자별 선호 MBTI (가장 많이 받은 결과)
    @Query("SELECT " +
           "u.userUuid as userUuid, " +
           "u.nickname as nickname, " +
           "tr.mbtiType as mbtiType, " +
           "COUNT(tr) as count " +
           "FROM User u " +
           "JOIN TestResult tr ON u.userUuid = tr.userUuid " +
           "GROUP BY u.userUuid, u.nickname, tr.mbtiType " +
           "ORDER BY u.userUuid, COUNT(tr) DESC")
    List<Map<String, Object>> findUserMbtiPreferences();
    
    // 사용자별 테스트 참여 빈도
    @Query("SELECT " +
           "u.userUuid as userUuid, " +
           "u.nickname as nickname, " +
           "COUNT(tr) as testCount, " +
           "MIN(tr.createdAt) as firstTest, " +
           "MAX(tr.createdAt) as lastTest " +
           "FROM User u " +
           "LEFT JOIN TestResult tr ON u.userUuid = tr.userUuid " +
           "GROUP BY u.userUuid, u.nickname " +
           "HAVING COUNT(tr) > 0 " +
           "ORDER BY COUNT(tr) DESC")
    List<Map<String, Object>> findUserTestingActivity();
    
    // 비활성 사용자 (최근 30일 테스트 안함)
    @Query("SELECT u FROM User u " +
           "WHERE u.userUuid NOT IN (" +
           "  SELECT DISTINCT tr.userUuid FROM TestResult tr " +
           "  WHERE tr.createdAt >= :monthAgo" +
           ") " +
           "AND u.createdAt < :monthAgo")
    List<User> findInactiveUsers(@Param("monthAgo") LocalDateTime monthAgo);
    
    // UUID가 존재하는지 확인
    boolean existsByUserUuid(String userUuid);
    
    // 닉네임 중복 확인
    boolean existsByNickname(String nickname);
    
    // 이메일 중복 확인
    boolean existsByEmail(String email);
    
    // 소셜 계정 중복 확인
    boolean existsBySocialTypeAndSocialId(String socialType, String socialId);
    
    // 총 사용자 수
    @Query("SELECT COUNT(u) FROM User u")
    Long getTotalUserCount();
    
    // 월별 가입자 통계
    @Query(value = "SELECT " +
           "EXTRACT(YEAR FROM CREATED_AT) as year, " +
           "EXTRACT(MONTH FROM CREATED_AT) as month, " +
           "COUNT(*) as count " +
           "FROM USERS " +
           "WHERE CREATED_AT >= :yearAgo " +
           "GROUP BY EXTRACT(YEAR FROM CREATED_AT), EXTRACT(MONTH FROM CREATED_AT) " +
           "ORDER BY year, month", nativeQuery = true)
    List<Map<String, Object>> findMonthlySignupStats(@Param("yearAgo") LocalDateTime yearAgo);
}
