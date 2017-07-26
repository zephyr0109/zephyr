package zephyr.mail.vo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import zephyr.mail.service.MailHeaderValues;

// 메일 본문 처리용 vo 및 관련 메소드
public class ContentVo {
    private String messageid;
    private String body;
    private List<String> fileNames;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }

    public String getMessageid() {
        return messageid;
    }

    public void setMessageid(String messageid) {
        this.messageid = messageid;
    }

    public void addFileName(String fileName) {
        if (fileNames == null) {
            fileNames = new ArrayList<>();
        }
        fileNames.add(fileName);
    }

    // message 안에 있는 본문의 내용 중 문자열만 추출하여 리턴
    public void setContentFromMsg(Message message) throws MessagingException, IOException {
        if (message.isMimeType(MailHeaderValues.TEXT_CALENDAR.getStr())) {
            // text/calendar의 경우 String으로 형변환이 안되어 일단 한줄씩 읽어 처리하도록 함.
            BufferedReader br = new BufferedReader(new InputStreamReader(message.getDataHandler().getInputStream()));
            StringBuffer sb = new StringBuffer();
            String temp = null;
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
            }
            setBody(sb.toString());
        } else if (message.isMimeType(MailHeaderValues.TEXT_HTML.getStr())) {
            body = convertToText((String) message.getContent());
        } else if (message.isMimeType(MailHeaderValues.TEXT_PLAIN.getStr())) {
            // 본문에 텍스트만 있는경우 바로 저장 후 리턴
            body = (String) message.getContent();
        } else if (message.isMimeType(MailHeaderValues.MULTIPART_ALL.getStr())) {
            // 본문이 multipart 형식으로 되어있는 경우 먼저 본문을 multipart로 변환
            setContentFromMultiPart((MimeMultipart) message.getContent());
        } else if (message.isMimeType(MailHeaderValues.APPLICATION.getStr())) {
            // appliation인 경우 파일명만 받아옴
            if (message.getFileName() != null) {
                addFileName(message.getFileName());
            }
        } else {
            // 현재까지는 text, multipart 형태만 분기. 그 외는 exception 발생. 다른 content type이
            // 나타날 경우 따로 처리해야함.
            throw new MessagingException("unknown type mail");
        }
    }

    // multipart에서 문자열만 추출. 재귀적 함수. 여기에 첨부파일이 2있으면 첨부파일 이름도 가져옴
    @SuppressWarnings("unchecked")
    private void setContentFromMultiPart(Multipart mPart) throws MessagingException, IOException {
        // multipart 안에 몇개의 body가 있는지 구함
        for (int i = 0; i < mPart.getCount(); i++) {
            // body를 순차적으로 구함
            BodyPart subPart = mPart.getBodyPart(i);
            // part가 첨부파일일 경우 파일명 저장 후 다음으로 넘어감
            String disp = subPart.getDisposition();
            if (disp != null && disp.equalsIgnoreCase(Part.ATTACHMENT) && subPart.getFileName() != null) {
                addFileName(subPart.getFileName());
                continue;
            }
            // 해당 body의 헤더를 enumeration형태로 구함
            // bodyFlag T : text, H : html, M : multiPart
            char bodyFlag = getBodyFlag(subPart.getAllHeaders());
            if (bodyFlag == 'T' && body == null) {
                // text이면서 body가 비어있을 경우 text를 바로 가져옴. 저장 후 다시 반복 시행
                body = (String) subPart.getContent();
            } else if (bodyFlag == 'H') {
                // html인 경우 테그 삭제 후 저장
                body = convertToText((String) subPart.getContent());
            } else if (bodyFlag == 'M') {
                // multipart일 경우 이 메소드를 다시 호출.
                setContentFromMultiPart((Multipart) subPart.getContent());
            } else if (bodyFlag == 'E') {
                Object content = subPart.getContent();
                if (content instanceof MimeMessage) {
                    body = null;
                    MimeMessage subMessage = (MimeMessage) subPart.getContent();
                    if (subMessage.getMessageID() != null) {
                        setMessageid(subMessage.getMessageID());
                    }
                    setContentFromMsg(subMessage);

                }
            }
        }
    }

    // 헤더에서 body가 어떤 형태인지를 반환함 T : text, H : html, M : multiPart
    private char getBodyFlag(Enumeration<Header> headers) {
        // 헤더를 순차적으로 돌림
        while (headers.hasMoreElements()) {
            // 해당 body가 text형태인지 html인지를 확인
            String value = headers.nextElement().getValue();
            if (value.startsWith(MailHeaderValues.TEXT_PLAIN.getStr())) {
                return 'T';
            } else if (value.startsWith(MailHeaderValues.TEXT_HTML.getStr())) {
                return 'H';
            } else if (value.startsWith(MailHeaderValues.MULTIPART.getStr())) {
                return 'M';
            } else if (value.startsWith(MailHeaderValues.MESSAGE.getStr())) {
                return 'E';
            }
        }
        return 0;
    }

    // html에서 body의 text만 가져옴
    private String convertToText(String html) throws UnsupportedEncodingException {
        Document document = Jsoup.parse(html);
        // html의 body 부분만 가져와서 body 안의 내용만 추출
        String bodyStr = document.select("body").html();
        return delTag(bodyStr);

    }

    // 문자열이 html 테그로 되어있는 경우 모든 테그를 학제하는 메소드
    private String delTag(String bodyStr) {
        if (bodyStr != null && !"".equals(bodyStr)) {
            // String htmlRegex = "<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>";
            String allTagRegex = "<[^>]*>";
            Matcher mat = null;
            Pattern styleTag = Pattern.compile("<style[^>]*>.*</style>", Pattern.DOTALL);
            Pattern allTag = Pattern.compile(allTagRegex, Pattern.DOTALL);
            Pattern wspace = Pattern.compile("(\\s\\s)+");
            Pattern tab = Pattern.compile("\\t");
            Pattern rn = Pattern.compile("\\r\\n(\\r\\n)+");
            bodyStr = bodyStr.replace("&quot;", "\"");
            bodyStr = bodyStr.replace("&nbsp;", " ");
            bodyStr = bodyStr.replace("&plusmn;", "±");
            bodyStr = bodyStr.replace("&amp;", "&");
            bodyStr = bodyStr.replace("&gt;", ">");
            bodyStr = bodyStr.replace("&lt;", "<");
            bodyStr = bodyStr.replace("</td>", " ");
            bodyStr = bodyStr.replace("</div>", "\r\n");
            bodyStr = bodyStr.replace("</p>", "\r\n");
            bodyStr = bodyStr.replace("</tr>", "\r\n");
            bodyStr = bodyStr.replace("<br>", "\r\n");
            bodyStr = bodyStr.replace("<br />", "\r\n");
            bodyStr = bodyStr.replace("<br/>", "\r\n");
            // bodyStr = bodyStr.replaceAll(htmlRegex, "");
            bodyStr = checkStyle(bodyStr);
            mat = styleTag.matcher(bodyStr);
            mat.replaceAll("");
            mat = allTag.matcher(bodyStr);
            bodyStr = mat.replaceAll("");
            mat = wspace.matcher(bodyStr);
            bodyStr = mat.replaceAll("\r\n");
            mat = tab.matcher(bodyStr);
            bodyStr = mat.replaceAll("");
            mat = rn.matcher(bodyStr);
            bodyStr = mat.replaceAll("\r\n");
            bodyStr = bodyStr.trim();
        }
        return bodyStr;
    }

    // style 테그와 그 안의 모든 내용을 삭제하는 메소드. 재귀적 함수
    private String checkStyle(String bodyStr) {
        int startStyle = bodyStr.indexOf("<style");
        int endStyle = bodyStr.indexOf("</style>");
        if (startStyle != -1 && endStyle != -1 && startStyle < endStyle) {
            String styleStr = bodyStr.substring(startStyle, endStyle + "</style>".length());
            bodyStr = bodyStr.replace(styleStr, "");
            return checkStyle(bodyStr);
        } else {
            return bodyStr;
        }
    }

    @Override
    public String toString() {
        return "ContentVo [body=" + body + ", fileNames=" + fileNames + "]";
    }

}
