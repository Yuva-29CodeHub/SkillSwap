
package com.skillswap.skillswap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RequestResponseController {

    private final JdbcTemplate jdbcTemplate;

    public RequestResponseController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/requests/respond")
    public String respondToRequest(
            @RequestParam Long requestId,
            @RequestParam String decision,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (!"ACCEPTED".equals(decision)
                && !"REJECTED".equals(decision)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Invalid request decision.");
            return "redirect:/requests";
        }

        String sql = """
            UPDATE skill_exchange_requests
            SET status = ?
            WHERE request_id = ?
              AND receiver_id = (
                  SELECT student_id
                  FROM students
                  WHERE college_email = ?
              )
              AND status = 'PENDING'
            """;

        int updated = jdbcTemplate.update(
                sql,
                decision,
                requestId,
                authentication.getName());

        if (updated == 1) {
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "ACCEPTED".equals(decision)
                            ? "Exchange request accepted!"
                            : "Exchange request rejected!");
        } else {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Request not found, not assigned to you, or already processed.");
        }

        return "redirect:/requests";
    }
}
