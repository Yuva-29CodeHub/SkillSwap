
package com.skillswap.skillswap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class DashboardController {

    private final JdbcTemplate jdbcTemplate;

    public DashboardController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/dashboard")
    public String showDashboard(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        String sql = """
            SELECT full_name, college_email, department, study_year
            FROM students
            WHERE college_email = ?
            """;

        Map<String, Object> student =
                jdbcTemplate.queryForMap(sql, email);

        model.addAttribute("student", student);

        return "dashboard";
    }
}
