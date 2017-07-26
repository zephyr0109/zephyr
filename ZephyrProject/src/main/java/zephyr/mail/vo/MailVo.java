package zephyr.mail.vo;

import java.io.Serializable;
import java.sql.Date;

// EML에서 정보를 읽어오면 MailVo로 변환하도록 했는데, DB에 넣을 때 다시 변환해야하는 일이 생겨서 별도의 DTO를 생성함.
public class MailVo extends CommonVo implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String messageId;
    private String subject;
    private String fromUser;
    private String recvUser;
    private String cc;
    private String bcc;
    private String content;
    private Date sendDate;
    private Date recvDate;
    private String attach;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getRecvUser() {
        return recvUser;
    }

    public void setRecvUser(String recvUser) {
        this.recvUser = recvUser;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public void setSendDate(java.util.Date sendDate) {
        if (sendDate != null) {
            this.sendDate = new Date(sendDate.getTime());
        }
    }

    public Date getRecvDate() {
        return recvDate;
    }

    public void setRecvDate(java.util.Date recvDate) {
        if (recvDate != null) {
            this.sendDate = new Date(recvDate.getTime());
        }
    }

    public void setRecvDate(Date recvDate) {
        this.recvDate = recvDate;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public MailVo(String id, String messageId, String subject, String fromUser, String recvUser, String cc, String bcc,
            String content, Date sendDate, Date recvDate, String attach, String eml) {
        super();
        setId(id);
        this.messageId = messageId;
        this.subject = subject;
        this.fromUser = fromUser;
        this.recvUser = recvUser;
        this.cc = cc;
        this.bcc = bcc;
        this.content = content;
        this.sendDate = sendDate;
        this.recvDate = recvDate;
        this.attach = attach;
        setEml(eml);
    }

    public MailVo() {
    }

    // LG화학 요구사항에 따라 본문에 발신, 수신, 참조, 비밀참조, 제목을 포함
    public void formatContentMsg() {
        StringBuffer sb = new StringBuffer();
        if (fromUser != null && !"".equals(fromUser)) {
            sb.append("발신(From) : ").append(fromUser).append("\r\n");
        }
        if (recvUser != null && !"".equals(recvUser)) {
            sb.append("수신(To) : ").append(recvUser).append("\r\n");
        }
        if (cc != null && !"".equals(cc)) {
            sb.append("참조(CC) : ").append(cc).append("\r\n");
        }
        if (bcc != null && !"".equals(bcc)) {
            sb.append("비밀참조(BCC) : ").append(bcc).append("\r\n");
        }

        sb.append("\r\n");
        subject = ((subject == null) || "".equals(subject)) ? "[제목없음]" : subject;
        sb.append("[제목] ").append(subject).append("\r\n");
        sb.append("\r\n");
        sb.append("[본문] ").append(content).append("\r\n");
        setContent(sb.toString().trim());
    }

    public void delimParam(int length) {
        if (subject != null && subject.length() > length) {
            subject = subject.substring(0, length);
        }
        if (fromUser != null && fromUser.length() > length) {
            fromUser = fromUser.substring(0, length);
        }
        if (recvUser != null && recvUser.length() > length) {
            recvUser = recvUser.substring(0, length);
        }
        if (cc != null && cc.length() > length) {
            cc = cc.substring(0, length);
        }
        if (bcc != null && bcc.length() > length) {

            bcc = bcc.substring(0, length);
        }
        if (attach != null && attach.length() > length) {
            attach = attach.substring(0, length);

        }

    }

}
