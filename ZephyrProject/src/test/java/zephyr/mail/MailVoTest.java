package zephyr.mail;

import org.junit.Test;

import zephyr.mail.vo.MailVo;

public class MailVoTest {

    @Test
    public void delimTest() {
        MailVo vo = new MailVo();
        vo.setSubject("dfdf");
        vo.delimParam(4000);
        System.out.println(vo.getSubject());
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i < 4005; i++) {
            sb.append("a");
        }
        vo.setSubject(sb.toString());
        vo.delimParam(4000);
        System.out.println(vo.getSubject().length());
    }

}
