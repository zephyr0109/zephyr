package zephyr.mail.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.log4j.Logger;

import zephyr.filter.ContentFilter;
import zephyr.mail.vo.CommonVo;
import zephyr.mail.vo.ContentVo;
import zephyr.mail.vo.MailVo;

public class MailManager extends AbstractMailManager {
    private static Logger logger = Logger.getLogger(MailManager.class);

    private ContentFilter cf;

    public ContentFilter getCf() {
        return cf;
    }

    public void setCf(ContentFilter cf) {
        this.cf = cf;
    }

    // 환경설정과 필터역할 객체를 초기화
    public MailManager() throws IOException {
        super();
    }

    // 단순 메일 전송
    @Override
    public void sendSimpleMail(String from, String passwd, String subject, String message, String recp) {
    }

    // message 객체에서 필요한 데이터를 vo에 저장하여 리턴함.본문 내용은 텍스트만 저장. null일 경우 필터에 걸린 eml 파일
    public MailVo convertToMailVo(MimeMessage message, String charset) throws MessagingException, IOException {
        MailVo mailVo = new MailVo();
        ContentVo contentVo = new ContentVo();
        if (charset != null) {
            setCharset(charset);
        }
        mailVo.setMessageId(message.getMessageID());

        // 제목 처리 부분
        String subject = getSubjectFromMessage(message);
        // 제목 필터
        if (cf.filterSubject(subject)) {
            logger.warn("excluded by filterSubject, subject : (" + subject + ")");
            return null;
        }
        mailVo.setSubject(subject);
        // end

        Address[] fromAddresses = message.getFrom();
        if (fromAddresses != null && fromAddresses.length > 0) {
            String from = convertToStrAddr(fromAddresses).get(0);
            // 발신인 필터
            if (cf.filterFromUser(from)) {
                logger.warn("excluded by filterFromUser, from : (" + from + ")");
                return null;
            } else {
                mailVo.setFromUser(from);
            }
        }

        //발신자 및 날짜 처리
        mailVo.setRecvUser(getStrFromList(getSendList(message, RecipientType.TO)));
        mailVo.setCc(getStrFromList(getSendList(message, RecipientType.CC)));
        mailVo.setBcc(getStrFromList(getSendList(message, RecipientType.BCC)));
        mailVo.setRecvDate(message.getReceivedDate());
        mailVo.setSendDate(message.getSentDate());

        // content 처리 부분
        contentVo.setContentFromMsg(message);
        mailVo.setContent(cf.filteredString(contentVo.getBody()));
        if (contentVo.getMessageid() != null) {
            mailVo.setMessageId(contentVo.getMessageid());
        }
        // 첨부파일명은 인코딩 된 상태로 가져오기 때문에 디코딩 처리
        if (contentVo.getFileNames() != null) {
            List<String> tempList = new ArrayList<>();
            for (String encodedFileName : contentVo.getFileNames()) {
                tempList.add(getDecodedFileName(encodedFileName));
            }
            mailVo.setAttach(getStrFromList(tempList));
        }
        // 본문을 LG화학에서 요구한 양식으로 변경
        mailVo.formatContentMsg();
        if (!checkParam(mailVo)) {
            //            throw new MessagingException("data is too long");
            mailVo.delimParam(4000);
        }
        return mailVo;
    }

    // 파라미터 확인. 글자수 체크
    public boolean checkParam(MailVo vo) {
        // attach varchar2(4000) null,
        // eml varchar2(4000) null
        boolean result = true;
        if (vo.getMessageId() != null && vo.getMessageId().length() > 1000) {
            logger.warn("messageId is more than 1000");
            result = false;
        }

        if (vo.getSubject() != null && vo.getSubject().length() > 4000) {
            logger.warn("Subject is more than 4000");
            result = false;
        }

        if (vo.getFromUser() != null && vo.getFromUser().length() > 4000) {
            logger.warn("FromUser is more than 4000");
            result = false;
        }

        if (vo.getRecvUser() != null && vo.getRecvUser().length() > 4000) {
            logger.warn("RecvUser is more than 4000");
            result = false;
        }

        if (vo.getCc() != null && vo.getCc().length() > 4000) {
            logger.warn("Cc is more than 4000");
            result = false;
        }

        if (vo.getBcc() != null && vo.getBcc().length() > 4000) {
            logger.warn("Bcc is more than 4000");
            result = false;
        }

        if (vo.getAttach() != null && vo.getAttach().length() > 4000) {
            logger.warn("Attach is more than 4000");
            result = false;
        }
        return result;
    }

    // String List의 모든 요소를 ","로 구분하여 하나의 문자열로 변경
    private String getStrFromList(List<String> list) {
        String str = null;
        if (list != null && list.size() > 0) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < list.size() - 1; i++) {
                sb.append(list.get(i));
                sb.append(",");
            }
            sb.append(list.get(list.size() - 1));
            str = sb.toString();
        }
        return str;
    }

    // 수신, 참조등을 가져오기 위한 메소드. 반복패턴이 있어 별도의 메소드로 뺌
    private List<String> getSendList(MimeMessage message, javax.mail.Message.RecipientType recipientType)
            throws MessagingException {
        List<String> sendList = null;
        Address[] toAddresses = message.getRecipients(recipientType);
        if (toAddresses != null) {
            sendList = convertToStrAddr(toAddresses);
        }
        return sendList;
    }

    // 파일명을 디코딩해서 가져오는 메소드
    private String getDecodedFileName(String fileName) throws MessagingException, IOException {
        fileName = fileName.replace(" ", "\r\n");
        fileName = getMd().decodeMimeEncdString(fileName, getCharset());
        if (fileName == null || "".equals(fileName)) {
            fileName = getMd().getConvertedStr(fileName, getCharset());
        }
        return fileName;
    }

    @Override
    public CommonVo convertToObject(MimeMessage message, String charset) throws MessagingException, IOException {
        return convertToMailVo(message, charset);
    }

}
