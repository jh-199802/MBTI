package com.example.mbtitest.controller;

import com.example.mbtitest.entity.User;
import com.example.mbtitest.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/user")
public class UserController {
    
    private final UserService userService;
    
    /**
     * 회원가입 페이지
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("pageTitle", "회원가입 - MBTI 테스트");
        return "user/register";
    }
    
    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "redirectUrl", required = false) String redirectUrl,
                           Model model) {
        model.addAttribute("pageTitle", "로그인 - MBTI 테스트");
        model.addAttribute("redirectUrl", redirectUrl);
        return "user/login";
    }
    
    /**
     * 회원가입 처리 API
     */
    @PostMapping("/api/register")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody Map<String, String> request,
                                                           HttpServletRequest httpRequest) {
        try {
            String username = request.get("username");
            String email = request.get("email");
            String password = request.get("password");
            String nickname = request.get("nickname");
            
            log.info("회원가입 요청 - username: {}, email: {}", username, email);
            
            User user = userService.registerUser(username, email, password, nickname);
            
            // 회원가입 성공 시 자동 로그인
            HttpSession session = httpRequest.getSession();
            session.setAttribute("currentUser", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getUsername());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "회원가입이 완료되었습니다! 환영합니다! 🎉",
                "user", Map.of(
                    "userId", user.getUserId(),
                    "username", user.getUsername(),
                    "nickname", user.getDisplayName(),
                    "email", user.getEmail()
                )
            ));
            
        } catch (RuntimeException e) {
            log.warn("회원가입 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "회원가입 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 로그인 처리 API
     */
    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> request,
                                                        HttpServletRequest httpRequest) {
        try {
            String loginId = request.get("loginId");
            String password = request.get("password");
            
            log.info("로그인 요청 - loginId: {}", loginId);
            
            Optional<User> userOpt = userService.authenticateUser(loginId, password);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // 세션에 사용자 정보 저장
                HttpSession session = httpRequest.getSession();
                session.setAttribute("currentUser", user);
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("username", user.getUsername());
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "로그인되었습니다! 환영합니다! 👋",
                    "user", Map.of(
                        "userId", user.getUserId(),
                        "username", user.getUsername(),
                        "nickname", user.getDisplayName(),
                        "email", user.getEmail(),
                        "mbtiType", user.getMbtiType() != null ? user.getMbtiType() : ""
                    )
                ));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "아이디 또는 비밀번호가 일치하지 않습니다."));
            }
            
        } catch (Exception e) {
            log.error("로그인 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "로그인 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 로그아웃 처리
     */
    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String username = (String) session.getAttribute("username");
            session.invalidate();
            log.info("로그아웃 완료 - username: {}", username);
        }
        return "redirect:/";
    }
    
    /**
     * 사용자명 중복 체크 API
     */
    @GetMapping("/api/check-username")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String username) {
        try {
            boolean available = userService.isUsernameAvailable(username);
            return ResponseEntity.ok(Map.of(
                "available", available,
                "message", available ? "사용 가능한 사용자명입니다." : "이미 사용 중인 사용자명입니다."
            ));
        } catch (Exception e) {
            log.error("사용자명 중복 체크 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("available", false, "message", "확인 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 이메일 중복 체크 API
     */
    @GetMapping("/api/check-email")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        try {
            boolean available = userService.isEmailAvailable(email);
            return ResponseEntity.ok(Map.of(
                "available", available,
                "message", available ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다."
            ));
        } catch (Exception e) {
            log.error("이메일 중복 체크 오류", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("available", false, "message", "확인 중 오류가 발생했습니다."));
        }
    }
    
    /**
     * 현재 로그인 사용자 정보 API
     */
    @GetMapping("/api/current")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User currentUser = (User) session.getAttribute("currentUser");
            if (currentUser != null) {
                return ResponseEntity.ok(Map.of(
                    "loggedIn", true,
                    "user", Map.of(
                        "userId", currentUser.getUserId(),
                        "username", currentUser.getUsername(),
                        "nickname", currentUser.getDisplayName(),
                        "email", currentUser.getEmail(),
                        "mbtiType", currentUser.getMbtiType() != null ? currentUser.getMbtiType() : ""
                    )
                ));
            }
        }
        return ResponseEntity.ok(Map.of("loggedIn", false));
    }
    
    /**
     * 마이페이지
     */
    @GetMapping("/profile")
    public String profilePage(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            return "redirect:/user/login?redirectUrl=/user/profile";
        }
        
        User currentUser = (User) session.getAttribute("currentUser");
        model.addAttribute("user", currentUser);
        model.addAttribute("pageTitle", "마이페이지 - MBTI 테스트");
        
        return "user/profile";
    }
}
