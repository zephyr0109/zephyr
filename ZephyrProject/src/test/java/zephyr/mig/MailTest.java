package zephyr.mig;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Provider;
import javax.mail.Session;
import javax.mail.Store;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import zephyr.mail.service.MailManager;
import zephyr.mail.service.MailService;
import zephyr.tibero.dao.SpJDBCTiberoMailDao;

public class MailTest {

    private static ApplicationContext ac;
    private static SpJDBCTiberoMailDao dao;
    private static MailService manager;

    @BeforeClass
    public static void initParam() {
        ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        dao = (SpJDBCTiberoMailDao) ac.getBean("mailDao");
        manager = (MailService) ac.getBean("mailManager");
    }

    @Test
    public void checkParam() {
        assertNotNull(ac);
        assertNotNull(manager);
        assertNotNull(dao);
        assertTrue(manager instanceof MailManager);
        assertTrue(dao instanceof SpJDBCTiberoMailDao);
        assertTrue(ac instanceof ClassPathXmlApplicationContext);
    }

    @Test
    @Ignore
    public void pop3ConnectionTest() throws Exception {
        // mail server connection parameters
        String host = "pop.naver.com";
        String id = "darkness65";
        String passwd = "wnsduf65";
        String protocol = "pop3";

        host = "165.244.235.241";
        id = "wexuser02";
        passwd = "wexuser021";
        int port = 995;

        // connect to my pop3 inbox
        Folder inbox = manager.recvFolder(protocol, host, port, id, passwd, true, "Inbox");
        assertNotNull(inbox);
        manager.closeFolder(inbox);
    }

    @Test
    public void imapConnectionTest() throws MessagingException {
        String host = "165.244.251.105";
        String id = "wexuser10";
        String passwd = "wexuser101";
        String protocol = "pop3";

        Properties mimeProps = System.getProperties();
        mimeProps.setProperty("mail.mime.ignoreunknownencoding", "true");
        //mimeProps.put("mail.imap.starttls.enable", "true");
        //        Session session = Session.getInstance(mimeProps, new Authenticator() {
        //            @Override
        //            protected PasswordAuthentication getPasswordAuthentication() {
        //                return new PasswordAuthentication(id, passwd);
        //            }
        //        });
        Session session = Session.getDefaultInstance(mimeProps);
        session.setDebug(false);
        Store store = session.getStore(protocol);
        store.connect(host, id, passwd);
        Folder[] list = store.getDefaultFolder().list();
        System.out.println("list size : " + list.length);

        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_WRITE);
        System.out.println(inbox.getMessageCount());
        inbox.close(false);
        System.out.println();

        for (Folder folder : list) {
            System.out.println(folder.getFullName() + " : " + folder.getMessageCount());
        }
    }

    @Test
    @Ignore
    public void summaryTest() throws IOException {
        String title = "total files,success,filtered,duplecate,failed,process time,date";
        // 처리 날짜 포멧
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        // 환경설정에서 csv 경로를 불러온다.
        File summaryFile = new File("C:\\Users\\LG\\Desktop\\summary.csv");
        StringBuffer sb = new StringBuffer();
        // 파일이 존재하지 않는 경우 혹은 파일이 다른 양식으로 되어 있는 경우 새로운 내용으로 덮어쓰도록 처리. 그 외는 기존 내용에 추가
        ArrayList<String> summaryStrList = new ArrayList<>();
        if (summaryFile.exists() && summaryFile.isFile()) {
            FileReader fr = new FileReader(summaryFile);
            BufferedReader br = new BufferedReader(fr);
            String tempStr = null;
            while ((tempStr = br.readLine()) != null) {
                summaryStrList.add(tempStr);
            }

            br.close();
            fr.close();
        } else {
            // 없는 경우 새로운 파일 생성
            summaryFile.mkdirs();
            sb.append(title).append("\n");
        }
        if (summaryStrList.size() > 1000) {
            int removeCount = summaryStrList.size() - 1000;
            for (int i = 0; i < removeCount; i++) {
                System.out.println(summaryStrList.get(0));
                summaryStrList.remove(0);
            }
        }
        System.out.println();
        sb.append(title).append("\r\n");
        for (String string : summaryStrList) {
            sb.append(string).append("\r\n");
        }
        System.out.println(sb.toString());
    }

}
