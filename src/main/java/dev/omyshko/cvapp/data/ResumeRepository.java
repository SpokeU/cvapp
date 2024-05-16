package dev.omyshko.cvapp.data;

import dev.omyshko.cvapp.data.model.Resume;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ResumeRepository {

    public static final String DELIMITER = ",";
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ResumeRepository(NamedParameterJdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Resume save(Resume resume) {
        String sql = "INSERT INTO resume(name, profession, experience, skills, highlights, resume_file_location) VALUES (:name, :profession, :experience, :skills, :highlights, :resume_file_location)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", resume.getName())
                .addValue("profession", resume.getProfession())
                .addValue("experience", resume.getExperience())
                .addValue("skills", String.join(DELIMITER, resume.getSkills()))
                .addValue("highlights", String.join(DELIMITER, resume.getHighlights()))
                .addValue("resume_file_location", resume.getResumeFileLocation());

        jdbcTemplate.update(sql, parameters, keyHolder, new String[]{"id"});

        resume.setId(keyHolder.getKey().longValue());
        return resume;
    }

    public Resume findById(long id) {
        String sql = "SELECT * FROM resume WHERE id = :id";
        SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        Resume resume = namedParameterJdbcTemplate.queryForObject(sql, parameters, new BeanPropertyRowMapper<>(Resume.class));
        return resume;
    }

    public List<Resume> findAll() {
        String sql = "SELECT id, name, profession, experience, skills, highlights, resume_file_location FROM resume";
        List<Resume> resume = namedParameterJdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Resume.class));
        return resume;
    }

}
