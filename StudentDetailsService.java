
package com.skillswap.skillswap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class StudentDetailsService implements UserDetailsService {

    private final JdbcTemplate jdbcTemplate;

    public StudentDetailsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        String sql = """
            SELECT college_email, password_hash
            FROM students
            WHERE college_email = ?
            """;

        try {
            return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> User.withUsername(
                        rs.getString("college_email"))
                    .password(rs.getString("password_hash"))
                    .roles("STUDENT")
                    .build(),
                email.trim()
            );
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            throw new UsernameNotFoundException(
                "No student account found for this email."
            );
        }
    }
}
