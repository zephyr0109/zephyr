package zephyr.mssql.dao;

import java.sql.Date;

public class LogVo {
    private String messageid;
    private String sender;
    private String receiver;
    private Date senddate;
    private Date receivedate;
    private String subject;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessageid() {
        return messageid;
    }

    public void setMessageid(String messageid) {
        this.messageid = messageid;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Date getSenddate() {
        return senddate;
    }

    public void setSenddate(Date senddate) {
        this.senddate = senddate;
    }

    public Date getReceivedate() {
        return receivedate;
    }

    public void setReceivedate(Date receivedate) {
        this.receivedate = receivedate;
    }

    @Override
    public String toString() {
        return "LogVo [messageid=" + messageid + ", sender=" + sender + ", receiver=" + receiver + ", senddate="
                + senddate + ", receivedate=" + receivedate + ", subject=" + subject + "]";
    }

    public LogVo(String messageid, String sender, String receiver, Date senddate, Date receivedate, String subject) {
        super();
        this.messageid = messageid;
        this.sender = sender;
        this.receiver = receiver;
        this.senddate = senddate;
        this.receivedate = receivedate;
        this.subject = subject;
    }

}
