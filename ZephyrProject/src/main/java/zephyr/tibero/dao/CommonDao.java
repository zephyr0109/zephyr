package zephyr.tibero.dao;

import java.sql.SQLException;

import zephyr.mail.vo.CommonVo;

public interface CommonDao {
    void insert(CommonVo obj) throws SQLException;

    int selectCountByMessageId(String messageId) throws SQLException;
}
