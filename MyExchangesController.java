
package com.skillswap.skillswap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class MyExchangesController {

    private final JdbcTemplate jdbcTemplate;

    public MyExchangesController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/my-exchanges")
    public String showMyExchanges(
            Authentication authentication,
            Model model) {

        String sql = """
            SELECT DISTINCT
                r.request_id,
                r.status,
                r.created_at,
                other.student_id AS other_student_id,
                other.full_name AS other_name,
                other.college_email AS other_email,
                other.department,
                other.study_year,
                their_teach.skill_name AS skill_they_teach,
                my_teach.skill_name AS skill_i_teach
            FROM skill_exchange_requests r

            JOIN students me
                ON me.college_email = ?

            JOIN students other
                ON (
                    (r.sender_id = me.student_id
                     AND r.receiver_id = other.student_id)
                    OR
                    (r.receiver_id = me.student_id
                     AND r.sender_id = other.student_id)
                )

            JOIN student_skills my_teach
                ON my_teach.student_id = me.student_id
                AND my_teach.skill_type = 'TEACH'

            JOIN student_skills their_learn
                ON their_learn.student_id = other.student_id
                AND their_learn.skill_type = 'LEARN'
                AND LOWER(TRIM(their_learn.skill_name))
                    = LOWER(TRIM(my_teach.skill_name))

            JOIN student_skills their_teach
                ON their_teach.student_id = other.student_id
                AND their_teach.skill_type = 'TEACH'

            JOIN student_skills my_learn
                ON my_learn.student_id = me.student_id
                AND my_learn.skill_type = 'LEARN'
                AND LOWER(TRIM(my_learn.skill_name))
                    = LOWER(TRIM(their_teach.skill_name))

            WHERE r.status = 'ACCEPTED'
              AND me.student_id <> other.student_id

            ORDER BY r.created_at DESC
            """;

        List<Map<String, Object>> exchanges =
                jdbcTemplate.queryForList(
                        sql,
                        authentication.getName()
                );

        model.addAttribute("exchanges", exchanges);

        return "my-exchanges";
    }
}
