package zephyr.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import zephyr.mail.service.MailService;
import zephyr.mail.vo.MailVo;
import zephyr.mail.vo.Pop3User;

public class POP3MailManagerTest {
    private static ApplicationContext ac;
    private static MailService manager;
    private static Folder inbox;

    @BeforeClass
    public static void initParam() {
        ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        manager = (MailService) ac.getBean("mailManager");
    }

    @Before
    public void connectPopServer() throws Exception {
        String host = "pop.naver.com";
        String id = "darkness65";
        String passwd = "wnsduf65";
        // connect to my pop3 inbox
        //        host = "pop.gmail.com";
        //        id = "star8076@gmail.com";
        //        passwd = "wnsduf19";
        host = "165.244.251.105";
        id = "bpmail10-arch";
        passwd = "bpmail10-arch!";
        inbox = manager.recvFolder("imap", host, 143, id, passwd, false, "WEXmail");
        System.out.println(inbox.getMessageCount());
        assertNotNull(inbox);
    }

    @After
    public void closePopserver() throws MessagingException {
        manager.closeFolder(inbox);
    }

    @Test
    public void connectionTest() throws MessagingException {
        String host = "pop.naver.com";
        String id = "darkness65";
        String passwd = "wnsduf65";
        // connect to my pop3 inbox
        //        host = "pop.gmail.com";
        //        id = "star8076@gmail.com";
        //        passwd = "wnsduf19";
        host = "165.244.251.77";
        id = "ext_user01";
        passwd = "ext_user01!";
        id = "wexuser11";
        passwd = "wexuser111";

        inbox = manager.recvFolder("pop3", host, 110, id, passwd, false, "Inbox");
        System.out.println("total : " + inbox.getMessageCount());
        assertNotNull(inbox);
    }

    @Test
    @Ignore
    public void multi241Pop3UserTest() throws MessagingException {
        String host = "165.244.235.241";
        String id = "ext_user01";
        String passwd = "ext_user01!";

        inbox = manager.recvFolder("pop3", host, 995, id, passwd, true, "Inbox");
        System.out.println("ext_user01 : " + inbox.getMessageCount());
        id = "int_user01";
        passwd = "int_user01!";
        inbox = manager.recvFolder("pop3", host, 995, id, passwd, true, "Inbox");
        System.out.println("int_user01 : " + inbox.getMessageCount());

        id = "bpmail10-arch";
        passwd = "bpmail10-arch!";
        host = "165.244.251.105";
        inbox = manager.recvFolder("imap", host, 143, id, passwd, false, "WEXmail");

        //        id = "wexuser10";
        //        passwd = "wexuser101";
        //        host = "165.244.251.105";
        //        inbox = manager.recvFolder("pop3", host, 110, id, passwd, false, "WEXmail");

        System.out.println("wexuser10 : " + inbox.getMessageCount());
        assertNotNull(inbox);

    }

    @Test
    public void multiPop3UserTest() throws MessagingException {
        List<Pop3User> list = new ArrayList<>();
        // ip : 165.244.251.74~80, 103~105
        for (int i = 1; i <= 10; i++) {
            String zero = "";
            if (i < 10) {
                zero = "0";
            }
            String host = "165.244.251.";
            if (i < 8) {
                host += 73 + i;
            } else {
                host += 95 + i;
            }
            String user = "wexuser" + zero + i;
            String password = "wexuser" + zero + i + "1";
            Pop3User pop3user = new Pop3User(user, password);
            System.out.println(host + "," + user + "," + password);
            pop3user.setHost(host);
            list.add(pop3user);
        }
        inbox = manager.recvFolder("pop3", "165.244.235.241", 995, "wexapp01", "wexapp011", true, "Inbox");
        System.out.println("pop3 appro total : " + inbox.getMessageCount());
        for (Pop3User pop3User : list) {
            try {
                inbox = manager.recvFolder("pop3", pop3User.getHost(), 110, pop3User.getUser(), pop3User.getPassword(),
                        false, "Inbox");
                System.out.println(pop3User.getUser() + " total : " + inbox.getMessageCount());
            } catch (AuthenticationFailedException e) {
                System.out.println(pop3User.getHost() + " failed");
            }
        }

    }

    @Test
    public void multiImapUserTest() throws MessagingException {
        List<Pop3User> list = new ArrayList<>();
        // ip : 165.244.251.74~80, 103~105
        for (int i = 1; i <= 10; i++) {
            String zero = "";
            if (i < 10) {
                zero = "0";
            }
            String host = "165.244.251.";
            if (i < 8) {
                host += 73 + i;
            } else {
                host += 95 + i;
            }
            String user = "bpmail" + zero + i + "-arch";
            String password = "bpmail" + zero + i + "-arch!";
            Pop3User pop3user = new Pop3User(user, password);
            System.out.println(host + "," + user + "," + password);
            pop3user.setHost(host);
            list.add(pop3user);
        }
        inbox = manager.recvFolder("pop3", "165.244.235.241", 995, "wexapp01", "wexapp011", true, "Inbox");
        System.out.println("pop3 appro total : " + inbox.getMessageCount());
        for (Pop3User pop3User : list) {
            try {
                inbox = manager.recvFolder("imap", pop3User.getHost(), 143, pop3User.getUser(), pop3User.getPassword(),
                        false, "WEXmail");
                System.out.println(pop3User.getUser() + " total : " + inbox.getMessageCount());
            } catch (MessagingException e) {
                System.out.println(pop3User.getHost() + " failed");
            }
        }

    }

    @Test
    @Ignore
    public void recvFolderTest() {
        String host = "pop.naver.com";
        String id = "darkness65";
        String passwd = "wnsduf65";
        // connect to my pop3 inbox
        try {
            inbox = manager.recvFolder("pop3", host, 995, id, passwd, true, "Inbox");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        assertNotNull(inbox);
    }

    @Test
    @Ignore
    public void pop3ConnectionFromprops() throws IOException, MessagingException {
        Properties pop3Properties = new Properties();
        pop3Properties.load(getClass().getClassLoader().getResourceAsStream("pop3.properties"));
        Folder inbox = manager.recvFolder(pop3Properties, "Inbox");
        assertNotNull(inbox);
    }

    @Test
    @Ignore
    public void convertToMailVoTest() throws MessagingException, IOException {
        for (int i = 1; i < inbox.getMessageCount(); i++) {
            Message message = inbox.getMessage(i);
            MimeMessage mimeMessage = (MimeMessage) message;
            MailVo vo = (MailVo) manager.convertToObject(mimeMessage, "utf-8");
            if (vo == null) {
                continue;
            } else {
                assertEquals(mimeMessage.getMessageID(), vo.getMessageId());
                break;

            }
        }
    }

    @Test
    @Ignore
    public void convertToMailVoMultiTest() throws MessagingException, IOException {
        Message[] messages = inbox.getMessages(1, 10);
        for (Message message : messages) {
            MimeMessage mimeMessage = (MimeMessage) message;
            MailVo vo = (MailVo) manager.convertToObject(mimeMessage, "utf-8");
            if (mimeMessage.getSubject() != null && vo != null) {
                assertEquals(mimeMessage.getMessageID(), vo.getMessageId());
            }
        }
    }

    @Test
    @Ignore
    public void getsubjecttest() throws MessagingException, IOException {
        Message message = inbox.getMessage(1);
        System.out.println(message.getSubject());
        String subject = manager.getSubjectFromMessage((MimeMessage) message);
        System.out.println(subject);

    }

    @Test
    @Ignore
    public void writeEmlFileTest() throws MessagingException, IOException {
        for (Message msg : inbox.getMessages(1, 10)) {
            MimeMessage message = (MimeMessage) msg;
            System.out.println(message.getSubject());
            boolean result = manager.writeEmlFile(message, "d:\\temp");
            assertTrue(result);
        }
    }

    @Test
    @Ignore
    public void fileNameTest() {
        String fileName = "/\\:*?\"<>|";
        char[] fileChar = fileName.toCharArray();
        fileName = manager.delNotAllowedFileChar(fileName);
        System.out.println(fileName);
        fileName = "<제목 테스트 입니다.> * ? 중간에 특수문자가 사라져야 합니다. \\ \r\n 개행은 어떻게 될까요?";
        fileName = manager.delNotAllowedFileChar(fileName);
        System.out.println(fileName);
        for (char c : fileChar) {
            assertTrue(fileName.indexOf(c) == -1);
        }

    }

    @Test
    @Ignore
    public void maxTest() {
        int total = 1000;
        String maxStr = "100";
        total = manager.getTotalCount(total, maxStr);
        assertTrue(total == 100);
        maxStr = null;
        total = manager.getTotalCount(total, maxStr);
        assertTrue(total == 100);
        total = 10;
        total = manager.getTotalCount(total, maxStr);
        assertTrue(total == 10);

    }

    @Test
    @Ignore
    public void loadUserListTest() throws IOException {
        List<Pop3User> userList = new ArrayList<>();

        BufferedReader br = new BufferedReader(
                new InputStreamReader(getClass().getClassLoader().getResourceAsStream("userList")));
        String temp = null;
        while ((temp = br.readLine()) != null) {
            String[] userArr = temp.split(",");
            userList.add(new Pop3User(userArr[0], userArr[1]));
        }
        br.close();
    }

}
