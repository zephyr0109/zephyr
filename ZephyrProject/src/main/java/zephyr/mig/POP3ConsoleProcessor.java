package zephyr.mig;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.UncategorizedSQLException;

import zephyr.mail.service.MailManager;
import zephyr.mail.service.MailService;
import zephyr.mail.vo.CommonVo;
import zephyr.mail.vo.Pop3User;
import zephyr.tibero.dao.CommonDao;
import zephyr.tibero.dao.SpJDBCTiberoMailDao;

public class POP3ConsoleProcessor implements MigrationProcessor {
    private static Logger logger = Logger.getLogger(POP3ConsoleProcessor.class);
    private ConsoleParam param;
    // 전체 처리 갯수
    private int total;
    // 성공한 처리 갯수
    private int count;
    // 필터로 걸러진 메일 갯수
    private int filterCount;
    // 중복 메일
    private int duplCount;
    // 프로세스 시작 시점. 로그용
    private long startTime;
    // 환경설정 파일. eml.properties
    private Properties props;

    // tibero 입력용 dao
    private CommonDao dao;
    // 메일 처리 서비스
    private MailService manager;
    // pop3/imap 사용 시 접근할 메일함
    private Folder inbox;

    public POP3ConsoleProcessor() throws IOException {
        props = new Properties();
        props.load(getClass().getClassLoader().getResourceAsStream("eml.properties"));
    }

    public POP3ConsoleProcessor(Properties props) {
        this.props = props;
    }

    // 프로그램 실행. 콘솔용
    @Override
    public void migrationProcess(String[] args) throws IOException {
        initField(args);
        logger.info("----------------------start process----------------------");
        // 프로세스 반복 횟수
        Properties pop3Properties = new Properties();
        String processMode = props.getProperty("process.mode");
        if (processMode.equals("pop3")) {
            pop3Properties.load(getClass().getClassLoader().getResourceAsStream("pop3.properties"));

        } else {
            pop3Properties.load(getClass().getClassLoader().getResourceAsStream("imap.properties"));
        }
        // 프로세스 반복 횟수. 메일함을 열고 닫을 때까지를 한 번으로 친다.
        int repeat = Integer.parseInt(props.getProperty("repeat.time"));
        for (int i = 0; i < repeat; i++) {
            total = 0;
            count = 0;
            filterCount = 0;
            duplCount = 0;
            startTime = System.nanoTime();
            try {
                // 메세지 배열을 가져올 때 사용할 변수들
                int step = Integer.parseInt(props.getProperty("mail.step"));
                // 배열의 시작점. 최초 1부터 시작
                int start = 1;
                // 배열의 마지막점
                int end = 0;
                String folderName = props.getProperty("folder.name", "Inbox");
                inbox = manager.recvFolder(pop3Properties, folderName);
                logger.info("connection success");
                // 지정한 전체 메세지 갯수를 가져옴. 지정한 숫자보다 메세지 수가 적으면 총 메세지수를 가져옴
                total = manager.getTotalCount(inbox.getMessageCount(), props.getProperty("mail.message.max"));
                logger.info("total messages : " + total);
                // 지정한 수 만큼 반복적으로 메일을 처리함
                while (start <= total) {
                    end += step;
                    if (end > total) {
                        end = total;
                    }
                    Message[] messages = inbox.getMessages(start, end);
                    for (Message message : messages) {
                        readMessageAndInsert(message);
                    }
                    start += step;
                }
                if (total > 0) {
                    summary();

                }
            } catch (MessagingException | IllegalStateException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    manager.closeFolder(inbox);
                } catch (MessagingException | IllegalStateException e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    // manager, dao, param 생성
    private void initField(String[] args) {
        param = new ConsoleParam(args);
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        String dataType = props.getProperty("data.type");
        // 프로세스가 전자결재인지 메일인지를 구분해서 별도의 처리를 하도록 함
        manager = (MailManager) ac.getBean("mailManager");
        dao = (SpJDBCTiberoMailDao) ac.getBean("mailDao");
    }

    // 파일을 읽어서 DB에 추가
    private void readMessageAndInsert(Message message) throws MessagingException {
        // 에러 발생 시 파일로 저장할 경로
        String errPath = props.getProperty("eml.down.path");

        MimeMessage mimeMessage = (MimeMessage) message;
        //MimeMessage mimeMessage = new MimeMessage((MimeMessage) message);
        // 삭제처리
        if (param.isDeleteFlag()) {
            message.setFlag(Flags.Flag.DELETED, true);
        }
        try {
            // 메세지에서 필요한 정보들을 vo로 저장. vo가 null인 경우는 로직상 필터에서 걸러진 경우 외에 존재하지 않음
            BufferedInputStream bis = new BufferedInputStream(mimeMessage.getInputStream());
            CommonVo vo = manager.convertToObject(mimeMessage, getCharset(bis));
            if (vo == null) {
                filterCount++;
                logger.warn("excluded mail : " + mimeMessage.getMessageID());
                return;
            }
            vo.setId(UUID.randomUUID().toString().replace("-", ""));
            // db에 정보 추가
            dao.insert(vo);
            count++;
        } catch (UncategorizedSQLException se) {
            String exMsg = se.getMessage();
            // 중복 발생의 경우
            if (exMsg != null && exMsg.indexOf("JDBC-10007:UNIQUE") != -1) {
                logger.info("duplicate mail : " + mimeMessage.getMessageID());
                duplCount++;
            } else {
                // 그 외의 다른 DB 에러
                mimeExceptionProcess(se, mimeMessage, errPath);
            }
        } catch (CannotGetJdbcConnectionException | SQLException | IOException | MessagingException e) {
            // 그 외의 발생 가능한 에러
            mimeExceptionProcess(e, mimeMessage, errPath);
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

        // 파일이 존재하면 해당 파일을 읽어온다.
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

        // 최대 라인을 기준으로 이전 내용들을 삭제
        if (summaryStrList.size() > maxList) {
            int removeCount = summaryStrList.size() - maxList;
            for (int i = 0; i < removeCount; i++) {
                summaryStrList.remove(0);
            }
        }

        sb.append(title).append("\r\n");
        // 이전 내용들을 먼저 추가/
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
