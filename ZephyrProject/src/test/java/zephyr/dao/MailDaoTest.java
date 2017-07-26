package zephyr.dao;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import zephyr.tibero.dao.MailDao;

public class MailDaoTest {

    private static ApplicationContext ac;
    private static MailDao mailDao;

    @BeforeClass
    public static void initParam() {
        ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        mailDao = ac.getBean("mailDao", MailDao.class);
    }

    @Test
    public void countMessageIdTest() {
        String messageId = "<1497400848615.7f7a5.3770973@cvowl033.nm>";
        try {
            int count = mailDao.selectCountByMessageId(messageId);
            assertTrue(count == 1);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
