
package com.skillswap.skillswap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class IncomingRequestController {

    private final JdbcTemplate jdbcTemplate;

    public IncomingRequestController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/requests")
    public String showRequests(
            Authentication authentication,
            Model model) {

        String sql = """
            SELECT
                r.request_id,
                r.status,
                r.created_at,

                sender.student_id AS sender_id,
                sender.full_name AS full_name,
                sender.department AS department,
                sender.study_year AS study_year,

                receiver.student_id AS receiver_id,

                sender_teach.skill_name AS sender_teaches,
                sender_learn.skill_name AS sender_wants_to_learn

            FROM skill_exchange_requests r

            JOIN students sender
                ON sender.student_id = r.sender_id

            JOIN students receiver
                ON receiver.student_id = r.receiver_id

            LEFT JOIN student_skills sender_teach
                ON sender_teach.skill_id = r.sender_teach_skill_id
               AND sender_teach.student_id = sender.student_id
               AND sender_teach.skill_type = 'TEACH'

            LEFT JOIN student_skills sender_learn
                ON sender_learn.skill_id = r.sender_learn_skill_id
               AND sender_learn.student_id = sender.student_id
               AND sender_learn.skill_type = 'LEARN'

            WHERE receiver.college_email = ?
              AND r.status = 'PENDING'

            ORDER BY r.created_at DESC, r.request_id DESC
            """;

        List<Map<String, Object>> requests =
                jdbcTemplate.queryForList(
                        sql,
                        authentication.getName()
                );

        model.addAttribute("requests", requests);

        return "requests";
    }
}
