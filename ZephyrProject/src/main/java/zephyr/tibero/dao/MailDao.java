package zephyr.tibero.dao;

import java.sql.SQLException;
import java.util.List;

import zephyr.mail.vo.MailVo;

public interface MailDao extends CommonDao {

    /*
     * id varchar2(32) primary key, messageId varchar2(100) unique, subject
     * varchar2(1000) null, fromUser varchar2(100) null, recvUser varchar2(5000)
     * null, cc varchar2(5000) null, bcc varchar2(5000) null, content
     * varchar2(50000) null, sendDate date null, recvDate date null, attach
     * varchar2(1000) null, eml varchar2(1000) null
     */

    String INSERT_MAIL = "insert into mail_test(ID,MESSAGEID,SUBJECT,FROMUSER,RECVUSER,CC,BCC,CONTENT,SENDDATE,RECVDATE,ATTACH,EML) values(?,?,?,?,?,?,?,?,?,?,?,?)";
    //    String INSERT_MAIL = "insert into mail_char(ID,MESSAGEID,SUBJECT,FROMUSER,RECVUSER,CC,BCC,CONTENT,SENDDATE,RECVDATE,ATTACH,EML) values(?,?,?,?,?,?,?,?,?,?,?,?)";
    //    String INSERT_MAIL = "insert into mail_test(ID,MESSAGEID,SUBJECT,FROMUSER,RECVUSER,CC,BCC,CONTENT,SENDDATE,RECVDATE,ATTACH,EML) values(?,?,?,?,?,?,?,?,?,?,?,?)";
    String UPDATE_MAIL = "update mail set subject = ?, fromUser = ?, recvUser = ?, cc = ?, bcc = ?, content = ?, sendDate = ?, recvDate = ?, attach=?, messageId = ? where id = ?";
    String DELETE_MAIL = "delete from mail where id = ?";
    String SELECT_ALL_MAIL = "select * from mail";
    String SELECT_MAIL_BY_ID = "select * from mail where id = ?";
    String SELECT_COUNT_BY_MESSAGEID = "select count(*) from mail where messageid=?";

    void insertMail(MailVo vo) throws SQLException;

    void updateMail(MailVo vo) throws SQLException;

    void deleteMail(String id) throws SQLException;

    List<MailVo> selectAllMail() throws SQLException;

    MailVo selectMailById(String id) throws SQLException;

    @Override
    int selectCountByMessageId(String messageId) throws SQLException;

}
