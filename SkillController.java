
package com.skillswap.skillswap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SkillController {

    private final JdbcTemplate jdbcTemplate;

    public SkillController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


@GetMapping("/skills")
public String showSkillsPage(
        Authentication authentication,
        Model model) {

    String sql = """
        SELECT ss.skill_id, ss.skill_name,
               ss.skill_type, ss.proficiency
        FROM student_skills ss
        JOIN students s ON ss.student_id = s.student_id
        WHERE s.college_email = ?
        ORDER BY ss.created_at DESC
        """;

    model.addAttribute(
            "skills",
            jdbcTemplate.queryForList(sql, authentication.getName())
    );

    return "skills";
}


    @PostMapping("/skills")
    public String addSkill(
            @RequestParam String skillName,
            @RequestParam String skillType,
            @RequestParam String proficiency,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (skillName.isBlank()
                || !(skillType.equals("TEACH")
                     || skillType.equals("LEARN"))
                || !(proficiency.equals("BEGINNER")
                     || proficiency.equals("INTERMEDIATE")
                     || proficiency.equals("ADVANCED"))) {

            redirectAttributes.addFlashAttribute(
                    "error", "Please enter valid skill details.");
            return "redirect:/skills";
        }

        String email = authentication.getName();

        String sql = """
            INSERT INTO student_skills
                (student_id, skill_name, skill_type, proficiency)
            SELECT student_id, ?, ?, ?
            FROM students
            WHERE college_email = ?
            """;

        int rows = jdbcTemplate.update(
                sql,
                skillName.trim(),
                skillType,
                proficiency,
                email
        );

        if (rows == 1) {
            redirectAttributes.addFlashAttribute(
                    "success", "Skill added successfully!");
        } else {
            redirectAttributes.addFlashAttribute(
                    "error", "Could not find your student account.");
        }

        return "redirect:/skills";
    }
}
