package zephyr.mail.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import zephyr.mail.vo.CommonVo;

public interface MailService {
    String APPRO = "appro";
    String MAIL = "mail";

    // 단순 메일 전송
    void sendSimpleMail(String from, String passwd, String subject, String message, String recp);

    // pop3 연결 후 폴더를 리턴함. 사용자 id, 비밀번호 필요
    Folder recvFolder(String protocol, String host, int port, String id, String passwd, boolean isSSL,
            String folderName) throws MessagingException;

    // pop3 연결. 속성을 이용
    Folder recvFolder(Properties pop3Properties, String folderName) throws MessagingException;

    // 메세지 조회 후 스토어와 폴더를 닫음
    void closeFolder(Folder folder) throws MessagingException;

    // Address[] 형태의 배열을 String[] 배열로 변환. email이 있을 경우 email, 없을 경우 개인 이름으로 저장한다.
    List<String> convertToStrAddr(Address[] addresses);

    CommonVo convertToObject(MimeMessage message, String charset) throws MessagingException, IOException;

    int getTotalCount(int total, String maxStr);

    String getSubjectFromMessage(MimeMessage message) throws MessagingException, IOException;

    String delNotAllowedFileChar(String ori);

    boolean writeEmlFile(MimeMessage message, String path) throws IOException, MessagingException;

    // 특정 pc 폴더에서 eml 파일 리스트를 리턴
    File[] readEmlFolder(String args) throws IOException;

}