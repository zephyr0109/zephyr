package zephyr.tibero.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import com.tmax.tibero.jdbc.ext.TbDataSource;

import zephyr.mail.vo.MailVo;

public class TiberoMailDao {

    private TbDataSource ds;

    private Connection conn;
    private Properties props;

    public TiberoMailDao() throws IOException, SQLException, ClassNotFoundException {
        props = new Properties();
        props.load(getClass().getClassLoader().getResourceAsStream("jdbc.properties"));
        Class.forName(props.getProperty("tibero.classname"));
        ds = new TbDataSource();
        ds.setURL(props.getProperty("tibero.url"));
        ds.setUser(props.getProperty("tibero.user"));
        ds.setPassword(props.getProperty("tibero.password"));

    }

    public void insertMail(MailVo vo) throws SQLException {
        // TODO Auto-generated method stub
        conn = ds.getConnection();
        PreparedStatement pstmt = conn.prepareStatement("insert into mail values(?,?,?,?,?,?,?,?,?)");
        pstmt.setString(1, vo.getSubject());
        pstmt.setString(2, vo.getFromUser());
        pstmt.setString(3, vo.getRecvUser());
        pstmt.setString(4, vo.getCc());
        pstmt.setString(5, vo.getBcc());
        pstmt.setString(6, vo.getContent());
        pstmt.setDate(7, vo.getSendDate());
        pstmt.setDate(8, vo.getRecvDate());
        pstmt.setString(9, vo.getAttach());
        pstmt.executeUpdate();
        pstmt.close();
        conn.close();

    }

    public void updateMail(MailVo vo) throws SQLException {
        // TODO Auto-generated method stub
        conn = ds.getConnection();
        String sql = "update mail set subject = ?, fromUser = ?, recvUser = ?, cc = ?, bcc = ?, content = ?, sendDate = ?, recvDate = ? where messageId = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, vo.getSubject());
        pstmt.setString(2, vo.getFromUser());
        pstmt.setString(3, vo.getRecvUser());
        pstmt.setString(4, vo.getCc());
        pstmt.setString(5, vo.getBcc());
        pstmt.setString(6, vo.getContent());
        pstmt.setDate(7, vo.getSendDate());
        pstmt.setDate(8, vo.getRecvDate());
        pstmt.setString(9, vo.getMessageId());
        pstmt.executeUpdate();
        pstmt.close();
        conn.close();
    }

    public void deleteMail(int id) throws SQLException {
        // TODO Auto-generated method stub
        conn = ds.getConnection();
        PreparedStatement pstmt = conn.prepareStatement("delete from mail where mailId = ?");
        pstmt.setInt(1, id);
        pstmt.executeUpdate();
        pstmt.close();
        conn.close();
    }

    public ArrayList<MailVo> selectAllMail() throws SQLException {
        // TODO Auto-generated method stub
        conn = ds.getConnection();
        PreparedStatement pstmt = conn.prepareStatement("select * from mail");
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            System.out.println(rs.getInt(1));
            System.out.println(rs.getString(2));
            System.out.println(rs.getString("content"));
        }
        rs.close();
        pstmt.close();
        conn.close();
        return null;
    }
}
