package com.skillswap.skillswap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class MatchController {

    private final JdbcTemplate jdbcTemplate;

    public MatchController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/matches")
    public String showMatches(Authentication authentication, Model model) {

        String sql = """
            SELECT
                me.student_id AS current_student_id,
                other.student_id AS student_id,
                other.full_name,
                other.department,
                other.study_year,

                my_learn.skill_id AS my_learn_skill_id,
                my_learn.skill_name AS skill_you_want_to_learn,

                my_teach.skill_id AS my_teach_skill_id,
                my_teach.skill_name AS skill_you_can_teach,

                their_teach.skill_id AS their_teach_skill_id,
                their_learn.skill_id AS their_learn_skill_id,

                (
                    SELECT r.request_id
                    FROM skill_exchange_requests r
                    WHERE (
                        (
                            r.sender_id = me.student_id
                            AND r.receiver_id = other.student_id
                            AND r.sender_teach_skill_id = my_teach.skill_id
                            AND r.sender_learn_skill_id = my_learn.skill_id
                        )
                        OR
                        (
                            r.sender_id = other.student_id
                            AND r.receiver_id = me.student_id
                            AND r.sender_teach_skill_id = their_teach.skill_id
                            AND r.sender_learn_skill_id = their_learn.skill_id
                        )
                    )
                    AND r.status = 'PENDING'
                    ORDER BY r.request_id DESC
                    LIMIT 1
                ) AS pending_request_id,

                (
                    SELECT r.sender_id
                    FROM skill_exchange_requests r
                    WHERE (
                        (
                            r.sender_id = me.student_id
                            AND r.receiver_id = other.student_id
                            AND r.sender_teach_skill_id = my_teach.skill_id
                            AND r.sender_learn_skill_id = my_learn.skill_id
                        )
                        OR
                        (
                            r.sender_id = other.student_id
                            AND r.receiver_id = me.student_id
                            AND r.sender_teach_skill_id = their_teach.skill_id
                            AND r.sender_learn_skill_id = their_learn.skill_id
                        )
                    )
                    AND r.status = 'PENDING'
                    ORDER BY r.request_id DESC
                    LIMIT 1
                ) AS pending_request_sender_id,

                CASE
                    WHEN EXISTS (
                        SELECT 1
                        FROM skill_exchange_requests r
                        WHERE (
                            (
                                r.sender_id = me.student_id
                                AND r.receiver_id = other.student_id
                                AND r.sender_teach_skill_id = my_teach.skill_id
                                AND r.sender_learn_skill_id = my_learn.skill_id
                            )
                            OR
                            (
                                r.sender_id = other.student_id
                                AND r.receiver_id = me.student_id
                                AND r.sender_teach_skill_id = their_teach.skill_id
                                AND r.sender_learn_skill_id = their_learn.skill_id
                            )
                        )
                        AND r.status = 'ACCEPTED'
                    )
                    THEN 'ACCEPTED'

                    WHEN EXISTS (
                        SELECT 1
                        FROM skill_exchange_requests r
                        WHERE (
                            (
                                r.sender_id = me.student_id
                                AND r.receiver_id = other.student_id
                                AND r.sender_teach_skill_id = my_teach.skill_id
                                AND r.sender_learn_skill_id = my_learn.skill_id
                            )
                            OR
                            (
                                r.sender_id = other.student_id
                                AND r.receiver_id = me.student_id
                                AND r.sender_teach_skill_id = their_teach.skill_id
                                AND r.sender_learn_skill_id = their_learn.skill_id
                            )
                        )
                        AND r.status = 'REJECTED'
                    )
                    THEN 'REJECTED'

                    WHEN EXISTS (
                        SELECT 1
                        FROM skill_exchange_requests r
                        WHERE (
                            (
                                r.sender_id = me.student_id
                                AND r.receiver_id = other.student_id
                                AND r.sender_teach_skill_id = my_teach.skill_id
                                AND r.sender_learn_skill_id = my_learn.skill_id
                            )
                            OR
                            (
                                r.sender_id = other.student_id
                                AND r.receiver_id = me.student_id
                                AND r.sender_teach_skill_id = their_teach.skill_id
                                AND r.sender_learn_skill_id = their_learn.skill_id
                            )
                        )
                        AND r.status = 'PENDING'
                    )
                    THEN 'PENDING'

                    ELSE 'NO'
                END AS exchange_accepted,

                (
                    SELECT r.status
                    FROM skill_exchange_requests r
                    WHERE (
                        (
                            r.sender_id = me.student_id
                            AND r.receiver_id = other.student_id
                            AND r.sender_teach_skill_id = my_teach.skill_id
                            AND r.sender_learn_skill_id = my_learn.skill_id
                        )
                        OR
                        (
                            r.sender_id = other.student_id
                            AND r.receiver_id = me.student_id
                            AND r.sender_teach_skill_id = their_teach.skill_id
                            AND r.sender_learn_skill_id = their_learn.skill_id
                        )
                    )
                    ORDER BY r.request_id DESC
                    LIMIT 1
                ) AS latest_request_status

            FROM students me

            JOIN student_skills my_learn
                ON my_learn.student_id = me.student_id
                AND my_learn.skill_type = 'LEARN'

            JOIN student_skills my_teach
                ON my_teach.student_id = me.student_id
                AND my_teach.skill_type = 'TEACH'

            JOIN students other
                ON other.student_id <> me.student_id

            JOIN student_skills their_teach
                ON their_teach.student_id = other.student_id
                AND their_teach.skill_type = 'TEACH'
                AND LOWER(TRIM(their_teach.skill_name))
                    = LOWER(TRIM(my_learn.skill_name))

            JOIN student_skills their_learn
                ON their_learn.student_id = other.student_id
                AND their_learn.skill_type = 'LEARN'
                AND LOWER(TRIM(their_learn.skill_name))
                    = LOWER(TRIM(my_teach.skill_name))

            WHERE me.college_email = ?

            ORDER BY
                other.full_name,
                my_learn.skill_name,
                my_teach.skill_name
            """;

        List<Map<String, Object>> matches =
                jdbcTemplate.queryForList(sql, authentication.getName());

        model.addAttribute("matches", matches);

        return "matches";
    }
}