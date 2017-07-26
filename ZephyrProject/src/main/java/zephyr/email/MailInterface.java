package zephyr.email;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public interface MailInterface {

	// 단순 메일 전송
	void sendSimpleMail(String from, String passwd, String subject, String message, String recp);

	// pop3 연결 후 폴더를 리턴함. 사용자 id, 비밀번호 필요
	Folder recvInboxFolder(String id, String passwd, String folderName) throws Exception;

	// 메세지 조회 후 스토어와 폴더를 닫음
	void closeFolder(Folder folder) throws MessagingException;

	// message 객체에서 필요한 데이터를 vo에 저장하여 리턴함.본문 내용은 텍스트만 저장
	MailVo converToMailVo(MimeMessage message) throws MessagingException, IOException;

	//Address[] 형태의 배열을 String[] 배열로 변환. email이 있을 경우 email, 없을 경우 개인 이름으로 저장한다.
	List<String> converToStrAddr(Address[] addresses);

	File[] readEmlFolder(String args) throws IOException;

}