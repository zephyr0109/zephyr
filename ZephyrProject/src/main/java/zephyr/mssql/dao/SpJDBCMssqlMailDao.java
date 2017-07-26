package zephyr.mssql.dao;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class SpJDBCMssqlMailDao implements MailLogDao {

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    private class MailRowMapper implements RowMapper<LogVo> {

        @Override
        public LogVo mapRow(ResultSet rs, int arg1) throws SQLException {
            String messageid = rs.getString("messageid");
            Date receivedate = rs.getDate("receivedate");
            String receiver = rs.getString("receiver");
            Date senddate = rs.getDate("senddate");
            String sender = rs.getString("sender");
            String subject = rs.getString("subject");
            LogVo vo = new LogVo(messageid, sender, receiver, senddate, receivedate, subject);
            return vo;
        }
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<LogVo> selectMailLog(String from, String to) {
        return this.jdbcTemplate.query(MailLogDao.SELECT_MAIL, new Object[] { from, to }, new MailRowMapper());
    }

}
