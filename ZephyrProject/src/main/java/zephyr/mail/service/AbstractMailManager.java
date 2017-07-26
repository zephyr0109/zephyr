package zephyr.mail.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

public abstract class AbstractMailManager implements MailService {

    private static Logger logger = Logger.getLogger(MailManager.class);

    private Properties props;
    private MailDecoder md;
    private String charset;

    private Session session;

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public MailDecoder getMd() {
        return md;
    }

    public void setMd(MailDecoder md) {
        this.md = md;
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
    }

    public AbstractMailManager() throws IOException {
        props = new Properties();
        props.load(getClass().getClassLoader().getResourceAsStream("eml.properties"));
        md = new MailDecoder();
    }

    public AbstractMailManager(MailDecoder md, String charset) {
        super();
        this.md = md;
        this.charset = charset;
    }

    @Override
    public Folder recvFolder(String protocol, String host, int port, String id, String passwd, boolean isSSL,
            String folderName) throws MessagingException {
        // 기본 속성들을 설정
        session = null;
        Properties mimeProps = new Properties();
        mimeProps.setProperty("mail.store.protocol", protocol);
        mimeProps.setProperty("mail." + protocol + ".port", Integer.toString(port));
        mimeProps.setProperty("mail." + protocol + ".user", id);
        mimeProps.setProperty("mail." + protocol + ".host", host);
        mimeProps.setProperty("mail." + protocol + ".password", passwd);
        mimeProps.setProperty("mail.mime.ignoreunknownencoding", "true");
        mimeProps.setProperty("mail." + protocol + ".timeout", "5000");
        if (isSSL) {
            mimeProps.setProperty("mail." + protocol + ".socketFactory.port", Integer.toString(port));
            mimeProps.setProperty("mail." + protocol + ".socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            mimeProps.setProperty("mail." + protocol + ".socketFactory.fallback", "false");
            mimeProps.setProperty("mail." + protocol + ".starttls.enable", "true");

        }
        return recvFolder(mimeProps, folderName);
    }

    @Override
    public Folder recvFolder(Properties properties, String folderName) throws MessagingException {
        String user = properties.getProperty("mail.pop3.user");
        String passwd = properties.getProperty("mail.pop3.password");
        if (user == null || passwd == null) {
            user = properties.getProperty("mail.imap.user");
            passwd = properties.getProperty("mail.imap.password");
        }
        final String authUser = user;
        final String authPassword = passwd;
        // authenticator를 활용하는 것이 인증 통과에 더 효율적이라는 얘기가 있어서 활용함.
        if (session == null) {
            session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(authUser, authPassword);
                }
            });
        }
        //        Session session = Session.getDefaultInstance(pop3Properties);

        Store store = session.getStore();
        store.connect();
        //        store.connect(user, passwd);
        // 받은 편지함을 읽고 쓰기 가능한 형태로 염
        Folder folder = store.getFolder(folderName);
        folder.open(Folder.READ_WRITE);
        return folder;
    }

    // smtp로 메일 전송하는 부분. 불필요하여 구현하지 않음
    @Override
    public void sendSimpleMail(String from, String passwd, String subject, String message, String recp) {
    }

    // pop3폴더 닫음
    @Override
    public void closeFolder(Folder folder) throws MessagingException {
        if (folder != null) {
            Store store = folder.getStore();
            try {
                folder.close(true);
                store.close();
            } catch (IllegalStateException e) {
                throw new MessagingException(e.getMessage());
            }
        }
    }

    // address에서 email이 있으면 email주소를, 없으면 이름을 가져온다.
    @Override
    public List<String> convertToStrAddr(Address[] addresses) {
        List<String> strList = new ArrayList<>();
        for (int i = 0; i < addresses.length; i++) {
            Address address = addresses[i];
            if (address instanceof InternetAddress) {
                InternetAddress ia = (InternetAddress) address;
                String addr = ia.getAddress();
                if (addr != null && !"".equals(addr)) {
                    strList.add(ia.getAddress());
                } else {
                    strList.add(ia.getPersonal());
                }
            }
        }
        return strList;
    }

    @Override
    public File[] readEmlFolder(String path) throws IOException {
        File[] files = null;
        if (path == null || "".equals(path)) {
            path = props.getProperty("eml.root.path");
        }
        logger.info("read files from : " + path);
        File rootFolder = new File(path);
        if (rootFolder.isDirectory()) {
            files = rootFolder.listFiles();
            // 파일 하나만 지정한 경우
        } else if (rootFolder.exists()) {
            files = new File[] { rootFolder };
        }
        return files;
    }

    // 파일로 저장시 사용할 수 없는 문자를 지운다.
    @Override
    public String delNotAllowedFileChar(String ori) {
        if (ori != null) {
            ori = ori.replace("/", "").replace("\\", "").replace(":", "").replace("?", "").replace("\"", "")
                    .replace("<", "").replace(">", "").replace("|", "").replace("*", "").replaceAll("\r\n", "").trim();
        }
        return ori;
    }

    // 지정된 경로에 eml파일로 저장
    @Override
    public boolean writeEmlFile(MimeMessage message, String path) throws IOException, MessagingException {
        File backupPath = new File(path);
        if (!backupPath.exists()) {
            backupPath.mkdirs();
        }

        String messageId = delNotAllowedFileChar(message.getMessageID());
        String fullFilePath = path + File.separator + messageId + ".eml";
        String tempFilePath = path + File.separator + messageId + ".tmp";
        File file = new File(fullFilePath);
        File tempFile = new File(tempFilePath);
        if (!file.exists() && !tempFile.exists()) {
            tempFile.createNewFile();
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile));
            message.writeTo(bos);
            bos.close();
            tempFile.renameTo(file);

        }
        return file.exists();
    }

    @Override
    public int getTotalCount(int total, String maxStr) {
        int result = total;
        int max = 0;
        boolean checkEmpty = (maxStr != null) && !"".equals(maxStr);
        if (checkEmpty) {
            max = Integer.parseInt(maxStr);
            if (max < total) {
                result = max;
            }
        }
        return result;
    }

    // 제목 처리 부분. message의 헤더에서 가져와 수동으로 처리함
    @Override
    public String getSubjectFromMessage(MimeMessage message) throws MessagingException, IOException {
        String subject = null;
        String[] subjects = message.getHeader("Subject");
        if (subjects != null && subjects.length > 0) {
            String oriSubject = subjects[0];
            Pattern spaceReg = Pattern.compile("(\\s)+");
            Pattern rnReg = Pattern.compile("(\\r\\n)+");
            oriSubject = spaceReg.matcher(oriSubject).replaceAll("\r\n");
            oriSubject = rnReg.matcher(oriSubject).replaceAll("\r\n");
            String decdSubject = md.decodeMimeEncdString(oriSubject, charset);

            if (decdSubject == null || "".equals(decdSubject)) {
                subject = md.getConvertedStr(subjects[0], charset);
            } else {
                subject = decdSubject;
            }
        }
        if (subject == null || "".equals(subject)) {
            subject = message.getSubject();
        }
        return subject;
    }

}
