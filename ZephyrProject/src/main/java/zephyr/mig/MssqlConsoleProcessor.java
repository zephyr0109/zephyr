package zephyr.mig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.UncategorizedSQLException;

import zephyr.filter.ContentFilter;
import zephyr.mail.service.MailManager;
import zephyr.mail.service.MailService;
import zephyr.mail.vo.MailVo;
import zephyr.mail.vo.Pop3User;
import zephyr.mssql.dao.LogVo;
import zephyr.mssql.dao.MailLogDao;
import zephyr.tibero.dao.CommonDao;
import zephyr.tibero.dao.SpJDBCTiberoMailDao;

public class MssqlConsoleProcessor implements MigrationProcessor {
    private static Logger logger = Logger.getLogger(MssqlConsoleProcessor.class);
    private ConsoleParam param;
    private int total;
    private int count;
    private int filterCount;
    private int duplCount;
    private long startTime;
    private Properties props;

    private CommonDao dao;
    private MailService manager;
    private MailLogDao logDao;
    private ContentFilter cf;

    public MssqlConsoleProcessor() throws IOException {
        props = new Properties();
        props.load(getClass().getClassLoader().getResourceAsStream("eml.properties"));
    }

    public MssqlConsoleProcessor(Properties props) {
        this.props = props;
    }

    // 프로그램 실행. 콘솔용
    @Override
    public void migrationProcess(String[] args) throws IOException {
        initField(args);
        logger.info("----------------------start process----------------------");
        // 프로세스 반복 횟수
        cf = new ContentFilter("filter");
        total = 0;
        count = 0;
        filterCount = 0;
        duplCount = 0;
        startTime = System.nanoTime();
        try {
            logger.info("connection success");
            Calendar cal = Calendar.getInstance();
            DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            String to = df.format(cal.getTime());
            cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
            String from = df.format(cal.getTime());
            List<LogVo> list = logDao.selectMailLog(from, to);
            List<LogVo> mailLogList = logDao.selectMailLog(from, to);
            logger.info("loading done, total : " + mailLogList.size());
            for (LogVo logVo : mailLogList) {
                readMessageAndInsert(logVo);
            }
            if (total > 0) {
                summary();

            }
        } catch (MessagingException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } finally {
        }

    }

    // manager, dao, param 생성
    private void initField(String[] args) {
        param = new ConsoleParam(args);
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        String dataType = props.getProperty("data.type");
        // 프로세스가 전자결재인지 메일인지를 구분해서 별도의 처리를 하도록 함
        logDao = ac.getBean("logDao", MailLogDao.class);
        manager = (MailManager) ac.getBean("mailManager");
        dao = (SpJDBCTiberoMailDao) ac.getBean("mailDao");
    }

    // 파일을 읽어서 DB에 추가
    private void readMessageAndInsert(LogVo logVo) throws MessagingException {
        // 에러 발생 시 파일로 저장할 경로

        // 삭제처리
        try {
            // 메세지에서 필요한 정보들을 vo로 저장. vo가 null인 경우는 로직상 필터에서 걸러진 경우 외에 존재하지 않음
            MailVo vo = new MailVo();

            vo.setSubject(logVo.getSubject());
            if (cf.filterSubject(vo.getSubject())) {
                logger.info("filtered");
                return;
            }
            vo.setMessageId(logVo.getMessageid());
            vo.setFromUser(logVo.getSender() + "@lgchem.com");
            if (cf.filterFromUser(vo.getFromUser())) {
                logger.info("filtered");
                return;
            }
            vo.setRecvUser(logVo.getReceiver());
            vo.setRecvDate(logVo.getReceivedate());
            vo.setSendDate(logVo.getSenddate());
            vo.setId(UUID.randomUUID().toString().replace("-", ""));
            // db에 정보 추가
            dao.insert(vo);
            count++;
            logger.info("done");
        } catch (UncategorizedSQLException se) {
            String exMsg = se.getMessage();
            // 중복 발생의 경우
            if (exMsg != null && exMsg.indexOf("JDBC-10007:UNIQUE") != -1) {
                duplCount++;
            } else {
                logger.info("err");
            }
        } catch (SQLException e) {
            logger.info("err");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            //            try {
            //                Thread.sleep(10);
            //            } catch (InterruptedException e) {
            //                e.printStackTrace();
            //            }

        }
    }

    // 처리결과를 로그로 출력 및 별도의 csv파일로 저장하는 메소드
    private void summary() throws IOException {
        // log4j를 이용한 출력 부분
        double processTime = (System.nanoTime() - startTime) / 1000000.0;
        logger.info("----------------------process end----------------------");
        logger.info("total messages : " + total);
        logger.info("success : " + count);
        logger.info("filtered messages : " + filterCount);
        logger.info("duplecate messages : " + duplCount);
        logger.info("failed : " + (total - count - filterCount - duplCount));
        logger.info("process time : " + processTime + "(ms)");

        // 파일로 저장할 내용의 제목
        String title = "total files,success,filtered,duplecate,failed,process time,date";
        // 처리 날짜 포멧
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        // 환경설정에서 csv 경로를 불러온다.
        File summaryFile = new File(props.getProperty("eml.summary.path"));
        StringBuffer sb = new StringBuffer();
        // 파일이 존재하지 않는 경우 혹은 파일이 다른 양식으로 되어 있는 경우 새로운 내용으로 덮어쓰도록 처리. 그 외는 기존 내용에 추가
        ArrayList<String> summaryStrList = new ArrayList<>();
        int maxList = 15000;
        if (summaryFile.exists() && summaryFile.isFile()) {
            FileReader fr = new FileReader(summaryFile);
            BufferedReader br = new BufferedReader(fr);
            String tempStr = null;
            br.readLine();
            while ((tempStr = br.readLine()) != null) {
                summaryStrList.add(tempStr);
            }

            br.close();
            fr.close();
        }
        if (summaryStrList.size() > maxList) {
            int removeCount = summaryStrList.size() - maxList;
            for (int i = 0; i < removeCount; i++) {
                summaryStrList.remove(0);
            }
        }

        sb.append(title).append("\r\n");
        for (String string : summaryStrList) {
            sb.append(string).append("\r\n");
        }
        sb.append(total).append(",");
        sb.append(count).append(",");
        sb.append(filterCount).append(",");
        sb.append(duplCount).append(",");
        sb.append(total - count - filterCount - duplCount).append(",");
        sb.append(processTime).append(",");
        sb.append(sdf.format(new Date())).append("\n");

        FileWriter fw = new FileWriter(summaryFile, false);
        fw.write(sb.toString());
        fw.close();

    }

    // data input stream에서 charset을 가져오기 위한 메소드
    private String getCharset(InputStream is) throws IOException {
        byte[] buf = new byte[4096];
        UniversalDetector detector = new UniversalDetector(null);
        int nread;
        while ((nread = is.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread);
        }
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        if (encoding == null) {
            encoding = Charset.defaultCharset().name();
        }
        detector.reset();
        is.close();
        return encoding;

    }

    // 에러 출력 후 파일 처리
    private void mimeExceptionProcess(Exception e, MimeMessage mimeMessage, String errPath) {
        try {
            writeErrLog(e.getMessage(), "error mail : " + mimeMessage.getMessageID(),
                    "insert error, continue another mail");
            manager.writeEmlFile(mimeMessage, errPath);
        } catch (IOException | MessagingException e1) {
            e1.printStackTrace();
        }

    }

    // error 로그 출력. 반복되는 패턴이 있어서 메소드로 뺌
    private void writeErrLog(String... messages) {
        for (int i = 0; i < messages.length; i++) {
            logger.error(messages[i]);
        }
    }

    private List<Pop3User> loadUserList() throws IOException {
        List<Pop3User> userList = new ArrayList<>();

        BufferedReader br = new BufferedReader(
                new InputStreamReader(getClass().getClassLoader().getResourceAsStream("userList")));
        String temp = null;
        while ((temp = br.readLine()) != null) {
            String[] userArr = temp.split(",");
            userList.add(new Pop3User(userArr[0], userArr[1]));
        }
        br.close();
        return userList;
    }

}
