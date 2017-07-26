package zephyr.email;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class EmlMailManager implements MailInterface {
	private static Logger logger = Logger.getLogger(EmlMailManager.class);

	private Properties props;

	private ArrayList fileNames;

	public Properties getProps() {
		return props;
	}

	public void setProps(Properties props) {
		this.props = props;
	}

	public EmlMailManager() throws IOException {
		super();
		props = new Properties();
		props.load(getClass().getClassLoader().getResourceAsStream("eml.properties"));
	}

	// 메일 서버 인증용 클래스
	private class MailAuthenticator extends Authenticator {

		private final String id;
		private final String pwd;

		public MailAuthenticator(String id, String pwd) {
			this.id = id;
			this.pwd = pwd;
		};

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(id, pwd);
		}
	}

	// 단순 메일 전송
	@Override
	public void sendSimpleMail(String from, String passwd, String subject, String message, String recp) {

	}

	// pop3 연결 후 폴더를 리턴함. 사용자 id, 비밀번호 필요
	@Override
	public Folder recvInboxFolder(String id, String passwd, String folderName) throws Exception {
		return null;
	}

	// 메세지 조회 후 스토어와 폴더를 닫음
	@Override
	public void closeFolder(Folder folder) throws MessagingException {
		if (folder != null) {
			Store store = folder.getStore();
			folder.close(true);
			store.close();
		}
	}

	// message 객체에서 필요한 데이터를 vo에 저장하여 리턴함.본문 내용은 텍스트만 저장
	@Override
	public MailVo converToMailVo(MimeMessage message) throws MessagingException, IOException {
		MailVo mailVo = new MailVo();
		String messageId = message.getMessageID();
		mailVo.setMessageId(messageId);
		String subject = message.getSubject();
		/*if (subject != null && subject.startsWith("\"")) {
			subject = MimeUtility.decodeText(subject.replace("\"", ""));
		}
		String[] subjects=message.getHeader("Subject");
		if(subjects!=null && subjects.length>0){
			String charset = getCharsetFromSubject(subjects);
			if(charset.length()>0 && (charset.startsWith("euc-kr")||charset.startsWith("EUC-KR"))){
				subject=getEuckrSubject(message, charset);
			}
		}*/

		String[] subjects = message.getHeader("Subject");
		logger.debug("subjects size : " + subjects.length);
		if(subject.length() > 0 ) {
			String decdSubject = decodeMimeEncdString(subjects[0]);
			logger.debug("decdSubject : [" + decdSubject + "]");
			subject = decdSubject;
		}
		if(subject == null || "".equals(subject) ) {
			subject = message.getSubject();
		}

		logger.debug("subject : " + subject);
		mailVo.setSubject(subject);

		Address[] fromAddresses = message.getFrom();
		if (fromAddresses.length > 0) {
			String from = converToStrAddr(fromAddresses).get(0);
			mailVo.setFrom(from);
		}

		Address[] toAddresses = message.getRecipients(RecipientType.TO);
		if (toAddresses != null) {
			List<String> sendTo = converToStrAddr(toAddresses);
			mailVo.setSendTo(sendTo);
		}

		Address[] ccAddresses = message.getRecipients(RecipientType.CC);
		if (ccAddresses != null) {
			List<String> cc = converToStrAddr(ccAddresses);
			mailVo.setCc(cc);
		}

		Address[] bccAddresses = message.getRecipients(RecipientType.BCC);
		if (bccAddresses != null) {
			List<String> bcc = converToStrAddr(bccAddresses);
			mailVo.setBcc(bcc);
		}

		Date recvDate = message.getReceivedDate();
		Date sendDate = message.getSentDate();
		mailVo.setRecvDate(recvDate);
		mailVo.setSendDate(sendDate);

		// content 처리 부분
		Map<String, Object> contentMap = getContentFromMsg(message);
		String body = (String) contentMap.get("body");
		mailVo.setBody(body);
		Object objFileNames = contentMap.get("fileNames");
		if (objFileNames != null) {
			if (objFileNames instanceof ArrayList) {
				List<String> fileNames = (ArrayList<String>) objFileNames;
				mailVo.setAttach(fileNames);
				logger.debug("filenames : " + fileNames);
			}
		}
		// body = getBodyFromMsg(message);
		// content end
		return mailVo;
	}

	private String decodeMimeEncdString(String mimeStr) throws UnsupportedEncodingException {
    	String strEncodedMessage = "";
    	String[] strLines	= null;
    	String[] strTemps	= null;
    	String	strCharSet	= "";
    	String	strSubject	= "";
    	String strEncodedSubject = "";
    	String strDecode = "";

    	//strEncodedMessage = "=?utf-8?B?7LKt7KO86rO17J6lIOuztO2YuO2VhOumhChUM0IwKSDsmrTshqHrjIDtlons?=\r\n =?utf-8?B?l4XssrQg7IiY7KCVIOyalOyyrQ==?=";
    	strEncodedMessage = mimeStr;

    	strLines = strEncodedMessage.split("\r\n");
    	logger.debug("strLines size : [" + strLines.length + "]");

    	for ( String strLine : strLines )
    	{
    		strTemps = strLine.split("\\?");
    		logger.debug("strTemps size : [" + strTemps.length + "]");

    		if ( strTemps.length != 5 )
    			break;

    		for ( int i = 0; i < strTemps.length; i++ )
    		{
    			logger.debug(String.format("strTemps[%d] : [%s]", i, strTemps[i]));
    		}

    		if ( !strTemps[2].equals("B") )
    			break;

    		strCharSet = strTemps[1];
    		logger.debug("strCharSet : [" + strCharSet + "]");

    		strEncodedSubject += strTemps[3];
    	}
    	logger.debug("strEncodedSubject : [" + strEncodedSubject + "]");
    	if(strEncodedSubject != null && !"".equals(strEncodedSubject)){
			byte[] barr = Base64.getDecoder().decode(strEncodedSubject);
			strDecode = new String(barr, strCharSet);
	    	logger.debug("strDecode : [" + strDecode + "]");
    	}
		return strDecode;
	}

	//제목에서 charset을 가져옴
	private String getCharsetFromSubject(String[] subjects) {
		String charset="";
		for (String sub : subjects) {
			int encodeFlag=sub.indexOf("=?");
			if(encodeFlag!=-1){
				String temp = sub.substring(encodeFlag+"=?".length());
				for (int i = 0; i < temp.length(); i++) {
					char c= temp.charAt(i);
					if(c =='?'){
						break;
					}
					charset+=c;
				}
				charset = charset.replace("\"", "");
			}
		}
		logger.debug("charset : " + charset);
		return charset;
	}

	// euc-kr로 되어있는 제목일 경우 별도로 처리
	private String getEuckrSubject(MimeMessage message, String charset) throws MessagingException, UnsupportedEncodingException {
		String subject="";
		String[] array = message.getHeader("Subject");
		for (String string : array) {
			string = string.replace("\"", "");
			if (string.startsWith("=?")) {
				String spliter = ".." + charset + ".B.";
				String end = "?=";
				string = MimeUtility.unfold(string);
				String[] stringarr = string.split(spliter);
				String tempstr = "";
				for (String string2 : stringarr) {
					if (string2.length() > 0) {
						tempstr += string2.substring(0, string2.lastIndexOf(end));
					}
				}
				logger.debug("tempstr : " + tempstr);
				subject = MimeUtility.decodeText(tempstr = "=?" + charset + "?B?" + tempstr + end);
			} else {
				subject = string;
			}

		}
		return subject;
	}

	// message 안에 있는 본문의 내용 중 문자열만 추출하여 리턴
	private Map<String, Object> getContentFromMsg(Message message) throws MessagingException, IOException {
		Map<String, Object> contentMap;
		String body = null;
		logger.debug("content type : " + message.getContentType());
		fileNames = new ArrayList<>();
		if (message.isMimeType(MailHeaderValues.TEXT_HTML.getStr())) {
			body = convertToText((String) message.getContent());
			contentMap = new HashMap<>();
			contentMap.put("body", body);
		} else if (message.isMimeType(MailHeaderValues.TEXT_ALL.getStr())) {
			// 본문에 텍스트만 있는경우 바로 저장 후 리턴
			body = (String) message.getContent();
			contentMap = new HashMap<>();
			contentMap.put("body", body);
		} else if (message.isMimeType(MailHeaderValues.MULTIPART_ALL.getStr())) {
			// 본문이 multipart 형식으로 되어있는 경우 먼저 본문을 multipart로 변환
			MimeMultipart mPart = (MimeMultipart) message.getContent();
			contentMap = getContentFromMultiPart(mPart);
		} else {
			// 현재까지는 text, multipart 형태만 분기. 그 외는 exception 발생. 다른 content type이
			// 나타날 경우 따로 처리해야함.
			throw new MessagingException("unknown type mail");
		}
		return contentMap;
	}

	// multipart에서 문자열만 추출. 재귀적 함수. 여기에 첨부파일이 있으면 첨부파일 이름도 가져옴
	@SuppressWarnings("unchecked")
	private Map<String, Object> getContentFromMultiPart(Multipart mPart) throws MessagingException, IOException {
		// multipart 안에 몇개의 body가 있는지 구함
		String body = null;
		int count = mPart.getCount();
		Map<String, Object> contentMap = new HashMap<>();
		logger.debug("multipart count : " + count);

		for (int i = 0; i < count; i++) {
			logger.debug("body part : " + i);
			// body를 순차적으로 구함
			BodyPart subPart = mPart.getBodyPart(i);
			logger.debug("s_filename : "+subPart.getFileName());
			String filename = subPart.getFileName();
			if(filename != null){
				fileNames.add(MimeUtility.decodeText(filename));
			}
			// part가 첨부파일일 경우 파일 설정만 하고 넘어가게 처리해보자.
			/*
			String disp = subPart.getDisposition();
			logger.debug("disp : " + disp);
			if (disp != null && disp.equalsIgnoreCase(Part.ATTACHMENT)) {
				//String fileName = MimeUtility.decodeText(subPart.getFileName());
				String fileName = MimeUtility.decodeText(subPart.getFileName());
				logger.debug("file name : " + fileName);
				fileNames.add(fileName);
				continue;
			}
			*/
			// 해당 body의 헤더를 enumeration형태로 구함
			Enumeration<Header> headers = subPart.getAllHeaders();
			boolean htmlflag = false;
			boolean textflag = false;
			boolean multiflag = false;
			// 헤더를 순차적으로 돌림
			while (headers.hasMoreElements()) {
				Header header = headers.nextElement();
				logger.debug(header.getName() + " : " + header.getValue());
				// 해당 body가 text형태인지 html인지를 확인
				String value = header.getValue();
				if (value.startsWith(MailHeaderValues.TEXT_PLAIN.getStr())) {
					textflag = true;
				} else if (value.startsWith(MailHeaderValues.TEXT_HTML.getStr())) {
					htmlflag = true;
				} else if (value.startsWith(MailHeaderValues.MULTIPART.getStr())) {
					multiflag = true;
				}
			}
			logger.debug(subPart.getContentType());
			if (textflag) {
				// text인 경우 바로 리턴하도록 처리
				body = (String) subPart.getContent();
			} else if (htmlflag && body == null) {
				// text인 경우 리턴할 변수에 일단 저장후 다시 반복 시행. html의 body만 빼온다.
				body = convertToText((String) subPart.getContent());
			} else if (multiflag) {
				// multipart일 경우 이 메소드를 다시 호출.
				Map<String, Object> subContentMap = getContentFromMultiPart((Multipart) subPart.getContent());
				body = (String) subContentMap.get("body");
				Object objFileNames = subContentMap.get("fileNames");
				if (objFileNames != null) {
					if (objFileNames instanceof ArrayList) {
						fileNames = (ArrayList<String>) objFileNames;
					}
				}
			}
		}
		contentMap.put("body", body);
		if (fileNames.size() > 0) {
			contentMap.put("fileNames", fileNames);
		}
		return contentMap;
	}

	// Address[] 형태의 배열을 String[] 배열로 변환. email이 있을 경우 email, 없을 경우 개인 이름으로
	// 저장한다.
	@Override
	public List<String> converToStrAddr(Address[] addresses) {
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

	// eml 폴더에서 eml 파일 리스트를 가져옴
	@Override
	public File[] readEmlFolder(String path) throws IOException {
		String rootPath = props.getProperty("eml.root.path");
		if (path == null || "".equals(path)) {
			path = rootPath;
		}
		logger.info("read files from : " + path);
		File rootFolder = new File(path);
		File[] files = rootFolder.listFiles();
		return files;
	}

	// html에서 body의 text만 가져옴
	public String convertToText(String html) throws UnsupportedEncodingException {
		//logger.debug("html : " + html);
		Document document = Jsoup.parse(html);
		StringBuilder stringBuilder = new StringBuilder();
		String text;
		for (Element element : document.select("body")) {
			stringBuilder.append(element.text().trim()).append('\n');
		}
		if (stringBuilder.length() == 0) {
			text = html;
		} else {
			text = stringBuilder.toString();
		}
		logger.debug("convert html to text : " + text);
		return text;
	}

}
