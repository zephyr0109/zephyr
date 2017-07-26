package zephyr.mssql.dao;

import java.sql.Date;
import java.util.List;

public interface MailLogDao {
    String SELECT_MAIL = "select subject, messageid, sender, receiver, senddate, receivedate from [eWM].[dbo].[TB_MAIL_RECEIVECHECK](nolock) where senddate between ? and ?";

    List<LogVo> selectMailLog(String from, String to);

}
