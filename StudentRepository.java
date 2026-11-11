
package com.skillswap.skillswap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StudentRepository {

    private final JdbcTemplate jdbcTemplate;

    public StudentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int save(Student student) {
        String sql = """
            INSERT INTO students
            (full_name, college_email, department, study_year, password_hash)
            VALUES (?, ?, ?, ?, ?)
            """;

        return jdbcTemplate.update(
            sql,
            student.getFullName(),
            student.getCollegeEmail(),
            student.getDepartment(),
            student.getStudyYear(),
            student.getPasswordHash()
        );
    }
}
