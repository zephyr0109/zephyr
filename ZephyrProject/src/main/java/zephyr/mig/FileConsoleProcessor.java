package zephyr.mig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.mozilla.universalchardet.UniversalDetector;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.UncategorizedSQLException;

import zephyr.mail.service.MailManager;
import zephyr.mail.service.MailService;
import zephyr.mail.vo.CommonVo;
import zephyr.tibero.dao.CommonDao;
import zephyr.tibero.dao.SpJDBCTiberoMailDao;

public class FileConsoleProcessor implements MigrationProcessor {
    private static Logger logger = Logger.getLogger(FileConsoleProcessor.class);
    private ConsoleParam param;
    private int total;
    private int count;
    private int filterCount;
    private int duplCount;
    private long startTime;
    private File[] files;
    private Properties props;

    private CommonDao dao;
    private MailService manager;

    public FileConsoleProcessor() throws IOException {
        props = new Properties();
        props.load(getClass().getClassLoader().getResourceAsStream("eml.properties"));
    }

    public FileConsoleProcessor(Properties props) {
        this.props = props;
    }

    // 프로그램 실행. 콘솔용
    @Override
    public void migrationProcess(String[] args) throws IOException {
        param = new ConsoleParam();
        param.loadParam(args);
        logger.info("----------------------start process----------------------");
        logger.info("args length : " + args.length);
        startTime = System.nanoTime();
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        String dataType = props.getProperty("data.type");
        manager = (MailManager) ac.getBean("mailManager");
        dao = (SpJDBCTiberoMailDao) ac.getBean("mailDao");
        total = 0;
        count = 0;
        filterCount = 0;
        duplCount = 0;
        // 폴더 안의 파일들을 읽어옴
        files = manager.readEmlFolder(param.getPath());
        total = files.length;
        for (File file : files) {
            try {
                readFileAndInsert(file);
            } catch (IOException e) {
                // 파일을 읽던 도중 에러 발생
                logger.error("IO error");
                e.printStackTrace();
            }
        }
        // db 내용을 출력.
        summary();

    }

    // 파일을 읽어서 DB에 추가
    private void readFileAndInsert(File file) throws IOException {
        if (!file.getName().endsWith(".eml")) {
            logger.error(file.getName() + " is not eml file, continue another file");
            return;
        }
        InputStream is = new FileInputStream(file);
        Properties mimeProps = new Properties();
        mimeProps.setProperty("mail.mime.ignoreunknownencoding", "true");
        Session session = Session.getDefaultInstance(mimeProps);

        // file을 mime message로 변환. session은 필요없으나 환경설정이 필요할 수 있어 기본으로 설정
        try {
            MimeMessage msg = new MimeMessage(session, is);
            // 메세지에서 필요한 정보들을 vo로 저장. vo가 null인 경우는 로직상 필터에서 걸러진 경우 외에 존재하지 않음
            CommonVo vo = manager.convertToObject(msg, getCharset(msg.getInputStream()));
            if (vo == null) {
                filterCount++;
                logger.warn("excluded file : " + file.getAbsolutePath());
                is.close();
                return;
            }
            vo.setId(UUID.randomUUID().toString().replace("-", ""));
            vo.setEml(file.getAbsolutePath());
            // db에 정보 추가
            dao.insert(vo);
            count++;
            is.close();
            // 삭제처리
            if (param.isDeleteFlag()) {
                boolean deleteResult = file.delete();
                logger.info(file.getName() + " delete : " + deleteResult);

            }
        } catch (UncategorizedSQLException se) {
            String msg = se.getMessage();
            logger.warn(se.getClass().getName());
            logger.warn("msg : " + msg);
            if (msg != null && msg.indexOf("JDBC-10007:UNIQUE") != -1) {
                logger.warn("duplicate mail : " + file.getAbsolutePath());
                duplCount++;
            } else {
                se.printStackTrace();
                writeErrLog(se.getMessage(), "error file : " + file.getAbsolutePath(),
                        "insert error, continue another file");
            }
            if (param.isDeleteFlag()) {
                boolean deleteResult = file.delete();
                logger.info(file.getName() + " delete : " + deleteResult);

            }
            is.close();
        } catch (MessagingException | SQLException e) {
            e.printStackTrace();
            writeErrLog(e.getMessage(), "error file : " + file.getAbsolutePath(),
                    "insert error, continue another file");
            is.close();
            return;
        }
    }

    // error 로그 출력. 반복되는 패턴이 있어서 메소드로 뺌
    private void writeErrLog(String... messages) {
        for (int i = 0; i < messages.length; i++) {
            logger.error(messages[i]);
        }
    }

    // data input stream에서 charset을 가져오기 위한 메소드
    private String getCharset(InputStream is) throws IOException {
        byte[] buf = new byte[8192];
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

    // 처리결과를 로그로 출력 및 별도의 csv파일로 저장하는 메소드
    private void summary() throws IOException {
        // log4j를 이용한 출력 부분
        logger.info("----------------------process end----------------------");
        logger.info("total files : " + total);
        logger.info("success : " + count);
        logger.info("filtered files : " + filterCount);
        logger.info("duplecate file : " + duplCount);
        logger.info("failed : " + (total - count - filterCount - duplCount));
        long endTime = System.nanoTime();
        double processTime = (endTime - startTime) / 1000000.0;
        logger.info("process time : " + processTime + "(ms)");

        // csv로 저장하는 부분
        String title = "path,total files,success,filtered,duplecate,failed,process time,date";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");

        // 환경설정에서 csv 경로를 불러온다.
        File summaryFile = new File(props.getProperty("eml.summary.path"));
        FileWriter fw = new FileWriter(summaryFile, true);
        StringBuffer sb = new StringBuffer();
        // 이미 경로에 파일이 있는 경우 뒤에 추가하도록 설정
        if (summaryFile.exists() && summaryFile.isFile()) {
            FileReader fr = new FileReader(summaryFile);
            BufferedReader br = new BufferedReader(fr);
            String tempStr = br.readLine();
            if (!(tempStr != null && tempStr.length() > 0 && tempStr.indexOf(title) != -1)) {
                sb.append(title).append("\n");
            }
            br.close();
            fr.close();
        } else {
            // 없는 경우 새로운 파일 생성
            summaryFile.mkdirs();
            sb.append(title).append("\n");
        }
        sb.append(files[0].getParent()).append(",");
        sb.append(total).append(",");
        sb.append(count).append(",");
        sb.append(filterCount).append(",");
        sb.append(duplCount).append(",");
        sb.append(total - count - filterCount - duplCount).append(",");
        sb.append(processTime).append(",");
        sb.append(sdf.format(new Date())).append("\n");
        fw.write(sb.toString());
        fw.close();

    }
}
