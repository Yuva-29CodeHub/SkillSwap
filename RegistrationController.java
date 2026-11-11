
package com.skillswap.skillswap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Controller
public class RegistrationController {

    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public RegistrationController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/register")
    public String showRegistrationPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerStudent(
            @RequestParam String fullName,
            @RequestParam String collegeEmail,
            @RequestParam String department,
            @RequestParam int studyYear,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {

        if (fullName.isBlank()
                || collegeEmail.isBlank()
                || department.isBlank()
                || password.isBlank()
                || studyYear < 1) {

            redirectAttributes.addFlashAttribute(
                    "error", "Please fill in all fields correctly.");
            return "redirect:/register";
        }

        try {
            String sql = """
                INSERT INTO students
                (full_name, college_email, department,
                 study_year, password_hash)
                VALUES (?, ?, ?, ?, ?)
                """;

            jdbcTemplate.update(
                    sql,
                    fullName.trim(),
                    collegeEmail.trim(),
                    department.trim(),
                    studyYear,
                    passwordEncoder.encode(password)
            );

            redirectAttributes.addFlashAttribute(
                    "success", "Registration successful!");

            return "redirect:/register";

        } catch (org.springframework.dao.DuplicateKeyException e) {
            redirectAttributes.addFlashAttribute(
                    "error", "This college email is already registered.");
            return "redirect:/register";
        }
    }
}
