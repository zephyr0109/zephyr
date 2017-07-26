package zephyr.mssql;

import static org.junit.Assert.assertNotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import zephyr.mssql.dao.LogVo;
import zephyr.mssql.dao.MailLogDao;

public class MssqlTest {
    private static ApplicationContext ac;
    private static MailLogDao logDao;

    @BeforeClass
    public static void initParam() {
        ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        logDao = ac.getBean("logDao", MailLogDao.class);
    }

    @Test
    public void connTest() {
        Calendar cal = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        String to = df.format(cal.getTime());
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
        String from = df.format(cal.getTime());
        System.out.println(from);
        System.out.println(to);
        List<LogVo> list = logDao.selectMailLog(from, to);
        assertNotNull(list);
        System.out.println(list.size());
    }

}
