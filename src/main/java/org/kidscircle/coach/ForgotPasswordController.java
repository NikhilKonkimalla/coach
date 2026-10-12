package org.kidscircle.coach;

import org.kidscircle.coach.db.UserService;
import org.kidscircle.coach.model.User;
import org.kidscircle.coach.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Controller
public class ForgotPasswordController {

    private static final Logger logger = LoggerFactory.getLogger(ForgotPasswordController.class);
    private static final int TOKEN_BYTES = 32;
    private static final long TOKEN_VALID_HOURS = 1;

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired private UserService userService;
    @Autowired private EmailService emailService;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam String email,
                                        HttpServletRequest request,
                                        RedirectAttributes ra) {
        User user = userService.findByEmail(email);
        if (user != null) {
            String token = generateToken();
            user.setResetToken(token);
            user.setResetTokenExpiresAt(LocalDateTime.now().plusHours(TOKEN_VALID_HOURS));
            userService.saveUser(user);

            String resetLink = ServletUriComponentsBuilder.fromContextPath(request)
                    .path("/reset-password")
                    .queryParam("token", token)
                    .toUriString();

            try {
                emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
            } catch (Exception e) {
                logger.error("Failed to send password reset email to {}", user.getEmail(), e);
            }
        }

        // Always show the same message, whether or not the email exists,
        // so this endpoint can't be used to enumerate registered accounts.
        ra.addFlashAttribute("success",
                "If an account exists for that email, we've sent a link to reset your password.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        User user = userService.findByResetToken(token);
        if (!isTokenValid(user)) {
            model.addAttribute("error", "This reset link is invalid or has expired. Please request a new one.");
            return "forgot-password";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam String token,
                                       @RequestParam String newPassword,
                                       @RequestParam String confirmPassword,
                                       Model model,
                                       RedirectAttributes ra) {
        User user = userService.findByResetToken(token);
        if (!isTokenValid(user)) {
            model.addAttribute("error", "This reset link is invalid or has expired. Please request a new one.");
            return "forgot-password";
        }

        if (newPassword.length() < 8) {
            model.addAttribute("error", "New password must be at least 8 characters.");
            model.addAttribute("token", token);
            return "reset-password";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "New password and confirmation do not match.");
            model.addAttribute("token", token);
            return "reset-password";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);
        userService.saveUser(user);

        ra.addFlashAttribute("success", "Your password has been reset. Please sign in.");
        return "redirect:/login";
    }

    private boolean isTokenValid(User user) {
        return user != null
                && user.getResetTokenExpiresAt() != null
                && user.getResetTokenExpiresAt().isAfter(LocalDateTime.now());
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
