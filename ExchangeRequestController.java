
package com.skillswap.skillswap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ExchangeRequestController {

    private final JdbcTemplate jdbcTemplate;

    public ExchangeRequestController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/requests/send")
    public String sendRequest(
            @RequestParam Long receiverId,
            @RequestParam Long myTeachSkillId,
            @RequestParam Long myLearnSkillId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Long senderId = jdbcTemplate.queryForObject(
                """
                SELECT student_id
                FROM students
                WHERE college_email = ?
                """,
                Long.class,
                authentication.getName());

        if (senderId == null || senderId.equals(receiverId)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Invalid exchange recipient.");
            return "redirect:/matches";
        }

        Integer validSkills = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM student_skills teach
                JOIN student_skills learn
                  ON learn.student_id = teach.student_id
                WHERE teach.skill_id = ?
                  AND teach.student_id = ?
                  AND teach.skill_type = 'TEACH'
                  AND learn.skill_id = ?
                  AND learn.student_id = ?
                  AND learn.skill_type = 'LEARN'
                """,
                Integer.class,
                myTeachSkillId, senderId,
                myLearnSkillId, senderId);

        if (validSkills == null || validSkills != 1) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "The selected skills are invalid.");
            return "redirect:/matches";
        }

        Integer reciprocalMatch = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM student_skills myTeach
                JOIN student_skills myLearn
                  ON myLearn.student_id = myTeach.student_id
                JOIN student_skills theirTeach
                  ON theirTeach.student_id = ?
                 AND theirTeach.skill_type = 'TEACH'
                 AND LOWER(TRIM(theirTeach.skill_name))
                     = LOWER(TRIM(myLearn.skill_name))
                JOIN student_skills theirLearn
                  ON theirLearn.student_id = ?
                 AND theirLearn.skill_type = 'LEARN'
                 AND LOWER(TRIM(theirLearn.skill_name))
                     = LOWER(TRIM(myTeach.skill_name))
                WHERE myTeach.skill_id = ?
                  AND myLearn.skill_id = ?
                  AND myTeach.skill_type = 'TEACH'
                  AND myLearn.skill_type = 'LEARN'
                """,
                Integer.class,
                receiverId, receiverId,
                myTeachSkillId, myLearnSkillId);

        if (reciprocalMatch == null || reciprocalMatch == 0) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "This is no longer a valid skill match.");
            return "redirect:/matches";
        }

        Integer existing = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM skill_exchange_requests
                WHERE sender_id = ?
                  AND receiver_id = ?
                  AND sender_teach_skill_id = ?
                  AND sender_learn_skill_id = ?
                  AND status IN ('PENDING', 'ACCEPTED')
                """,
                Integer.class,
                senderId, receiverId,
                myTeachSkillId, myLearnSkillId);

        if (existing != null && existing > 0) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "A pending or accepted request already exists for these skills.");
            return "redirect:/matches";
        }

        jdbcTemplate.update(
                """
                INSERT INTO skill_exchange_requests
                    (sender_id, receiver_id, status,
                     sender_teach_skill_id, sender_learn_skill_id)
                VALUES (?, ?, 'PENDING', ?, ?)
                """,
                senderId, receiverId,
                myTeachSkillId, myLearnSkillId);

        redirectAttributes.addFlashAttribute(
                "successMessage", "Exchange request sent successfully!");

        return "redirect:/matches";
    }
}
