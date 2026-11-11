
package com.skillswap.skillswap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class DiscoverController {

    private final JdbcTemplate jdbcTemplate;

    public DiscoverController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/discover")
    public String discoverStudents(
            Authentication authentication,
            Model model) {

        String sql = """
            SELECT
                s.student_id,
                s.full_name,
                s.department,
                s.study_year,
                GROUP_CONCAT(
                    CASE WHEN ss.skill_type = 'TEACH'
                    THEN CONCAT(ss.skill_name, ' (',
                                ss.proficiency, ')')
                    END SEPARATOR ', '
                ) AS teach_skills,
                GROUP_CONCAT(
                    CASE WHEN ss.skill_type = 'LEARN'
                    THEN CONCAT(ss.skill_name, ' (',
                                ss.proficiency, ')')
                    END SEPARATOR ', '
                ) AS learn_skills
            FROM students s
            LEFT JOIN student_skills ss
                ON s.student_id = ss.student_id
            WHERE s.college_email <> ?
            GROUP BY
                s.student_id,
                s.full_name,
                s.department,
                s.study_year
            ORDER BY s.full_name
            """;

        List<Map<String, Object>> students =
                jdbcTemplate.queryForList(
                        sql, authentication.getName());

        model.addAttribute("students", students);

        return "discover";
    }
}
