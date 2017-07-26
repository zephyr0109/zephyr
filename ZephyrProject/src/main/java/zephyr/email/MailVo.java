package zephyr.email;

import java.util.Date;
import java.util.List;

public class MailVo {

	private String messageId;
	private String subject;
	private String from;
	private List<String> sendTo;
	private List<String> cc;
	private List<String> bcc;
	private String body;
	private Date sendDate;
	private Date recvDate;
	private List<String> attach;

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

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public List<String> getSendTo() {
		return sendTo;
	}

	public void setSendTo(List<String> sendTo) {
		this.sendTo = sendTo;
	}

	public List<String> getCc() {
		return cc;
	}

	public void setCc(List<String> cc) {
		this.cc = cc;
	}

	public List<String> getBcc() {
		return bcc;
	}

	public void setBcc(List<String> bcc) {
		this.bcc = bcc;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Date getSendDate() {
		return sendDate;
	}

	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}

	public Date getRecvDate() {
		return recvDate;
	}

	public void setRecvDate(Date recvDate) {
		this.recvDate = recvDate;
	}


	public List<String> getAttach() {
		return attach;
	}

	public void setAttach(List<String> attach) {
		this.attach = attach;
	}

	public MailVo(String messageId, String subject, String from, List<String> sendTo, List<String> cc, List<String> bcc,
			String body, Date sendDate, Date recvDate, List<String> attach) {
		super();
		this.messageId = messageId;
		this.subject = subject;
		this.from = from;
		this.sendTo = sendTo;
		this.cc = cc;
		this.bcc = bcc;
		this.body = body;
		this.sendDate = sendDate;
		this.recvDate = recvDate;
		this.attach = attach;
	}

	@Override
	public String toString() {
		return "MailVo [messageId=" + messageId + ", subject=" + subject + ", from=" + from + ", sendTo=" + sendTo
				+ ", cc=" + cc + ", bcc=" + bcc + ", body=" + body + ", sendDate=" + sendDate + ", recvDate=" + recvDate
				+ ", attach=" + attach + "]";
	}

	public MailVo() {
		// TODO Auto-generated constructor stub
	}
}
