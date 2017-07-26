package zephyr.tibero.dao;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import zephyr.mail.vo.CommonVo;
import zephyr.mail.vo.MailVo;

public class SpJDBCTiberoMailDao implements MailDao {

    private JdbcTemplate jdbcTemplate;
    private MailRowMapper mapper = new MailRowMapper();
    private BeanPropertyRowMapper<MailVo> propMailMapper = new BeanPropertyRowMapper<>(MailVo.class);

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private class MailRowMapper implements RowMapper<MailVo> {

        @Override
        public MailVo mapRow(ResultSet rs, int arg1) throws SQLException {
            String id = rs.getString("ID");
            String messageId = rs.getString("MESSAGEID");
            String subject = rs.getString("SUBJECT");
            String fromUser = rs.getString("FROMUSER");
            String recvUser = rs.getString("RECVUSER");
            String cc = rs.getString("CC");
            String bcc = rs.getString("BCC");
            String content = rs.getString("CONTENT");
            Date sendDate = rs.getDate("SENDDATE");
            Date recvDate = rs.getDate("RECVDATE");
            String attach = rs.getString("ATTACH");
            String eml = rs.getString("EML");
            MailVo mailDto = new MailVo(id, messageId, subject, fromUser, recvUser, cc, bcc, content, sendDate,
                    recvDate, attach, eml);
            return mailDto;
        }
    }

    @Override
    public void insertMail(MailVo vo) throws SQLException {
        String sql = MailDao.INSERT_MAIL;
        this.jdbcTemplate.update(sql,
                new Object[] { vo.getId(), vo.getMessageId(), vo.getSubject(), vo.getFromUser(), vo.getRecvUser(),
                        vo.getCc(), vo.getBcc(), vo.getContent(), vo.getSendDate(), vo.getRecvDate(), vo.getAttach(),
                        vo.getEml() });
    }

    @Override
    public void updateMail(MailVo vo) throws SQLException {
        String sql = MailDao.UPDATE_MAIL;
        this.jdbcTemplate.update(sql,
                new Object[] { vo.getSubject(), vo.getFromUser(), vo.getRecvUser(), vo.getCc(), vo.getBcc(),
                        vo.getContent(), vo.getSendDate(), vo.getRecvDate(), vo.getAttach(), vo.getMessageId(),
                        vo.getId() });
    }

    @Override
    public void deleteMail(String id) throws SQLException {
        String sql = MailDao.DELETE_MAIL;
        this.jdbcTemplate.update(sql, new Object[] { id });
    }

    @Override
    public List<MailVo> selectAllMail() throws SQLException {
        String sql = MailDao.SELECT_ALL_MAIL;
        List<MailVo> list = this.jdbcTemplate.query(sql, propMailMapper);
        return list;
    }

    @Override
    public MailVo selectMailById(String id) throws SQLException {
        String sql = MailDao.SELECT_MAIL_BY_ID;
        MailVo vo = this.jdbcTemplate.queryForObject(sql, mapper, id);
        return vo;
    }

    @Override
    public void insert(CommonVo obj) throws SQLException {
        if (obj instanceof MailVo) {
            insertMail((MailVo) obj);
        }
    }

    @Override
    public int selectCountByMessageId(String messageId) throws SQLException {
        return this.jdbcTemplate.queryForObject(MailDao.SELECT_COUNT_BY_MESSAGEID, Integer.class, messageId).intValue();
    }

}
