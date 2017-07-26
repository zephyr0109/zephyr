package zephyr.mail.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class MailDecoder {
    private static Logger logger = Logger.getLogger(MailDecoder.class);

    // mime encoding 되어있는 문자열을 decode
    public String decodeMimeEncdString(String mimeStr, String charset) throws MessagingException, IOException {
        String strEncodedMessage = "";
        String[] strLines = null;
        String[] strTemps = null;
        String strCharSet = "";
        String fullDecodedStr = "";
        strEncodedMessage = mimeStr;
        strLines = strEncodedMessage.split("\r\n");
        // json object로 인코딩된 문자를 저장. flag : (p : plain, b : base64, q : quated
        // printable), str : 문자열
        ArrayList<JSONObject> encodeArr = new ArrayList<>();
        // 줄 단위로 나눠서 실행
        for (int i = 0; i < strLines.length; i++) {
            String strLine = strLines[i];
            if (strLine.indexOf("=?") == -1) {
                encodeArr = setEncodeArr(encodeArr, "p", getConvertedStr(strLine, charset) + " ", charset);
                continue;
            }
            strTemps = strLine.split("\\?");
            if (strTemps[2].equalsIgnoreCase("B")) {
                encodeArr = setEncodeArr(encodeArr, "b", strTemps[3], strCharSet);

            } else if (strTemps[2].equalsIgnoreCase("Q")) {
                encodeArr = setEncodeArr(encodeArr, "q", strTemps[3], strCharSet);

            } else {
                break;
            }
            strCharSet = strTemps[1];

        }

        fullDecodedStr = decodeEncodedArr(encodeArr, strCharSet);
        return fullDecodedStr.trim();
    }

    // encodeArr에 인코딩된 문자열을 저장. 추가 직전 문자열이 같은 형식일 경우 이전 문자열에 추가
    private ArrayList<JSONObject> setEncodeArr(ArrayList<JSONObject> encodeArr, String flag, String str,
            String charset) {
        // 이전 문자열이 없는경우
        if (encodeArr.size() == 0) {
            JSONObject obj = new JSONObject();
            obj.put("flag", flag);
            obj.put("str", str);
            encodeArr.add(obj);
        } else {
            // 바로 직전에 추가된 문자열을 가져움
            JSONObject preObj = encodeArr.get(encodeArr.size() - 1);
            // 이전과 동일한 형태의 인코딩 방식이면 둘을 합침
            if (preObj.get("flag").equals(flag)) {
                String preStr = preObj.getString("str");
                preStr += str;
                preObj.put("str", preStr);
                encodeArr.remove(encodeArr.size() - 1);
                encodeArr.add(preObj);
            } else {
                JSONObject obj = new JSONObject();
                obj.put("flag", flag);
                obj.put("str", str);
                encodeArr.add(obj);
            }
        }
        return encodeArr;
    }

    // 인코딩된 문자열들을 디코딩해서 하나의 문자열로 변환
    private String decodeEncodedArr(ArrayList<JSONObject> encodeArr, String strCharSet)
            throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();
        for (JSONObject encodeObj : encodeArr) {
            String flag = encodeObj.getString("flag");
            String str = encodeObj.getString("str");
            if (flag.equals("b")) {
                String[] encdStrArr = str.split("(=)+");
                for (int i = 0; i < encdStrArr.length; i++) {
                    String subEncodedStr = encdStrArr[i];
                    String strDecode = null;
                    byte[] barr = null;
                    subEncodedStr = subEncodedStr.replaceAll(" ", "");
                    barr = Base64.decodeBase64(subEncodedStr);
                    strDecode = new String(barr, strCharSet);
                    sb.append(strDecode);
                }
            } else if (flag.equals("q")) {
                try {
                    byte[] barr = null;
                    barr = QuotedPrintableCodec.decodeQuotedPrintable(str.getBytes());
                    sb.append(new String(barr, strCharSet));
                } catch (DecoderException e) {
                    logger.error("quoted printable codec err");
                    e.printStackTrace();
                }
            } else if (flag.equals("p")) {
                sb.append(str);
            }
        }
        return sb.toString();
    }

    // java mail api를 사용한 디코딩. mime 형태가 아닌 경우 charset으로 수동 변환
    public String getConvertedStr(String oriStr, String charset) throws MessagingException, IOException {
        if (oriStr.startsWith("=") || oriStr.startsWith("\"=")) {
            oriStr = MimeUtility.decodeText(oriStr);
        } else {
            if (charset != null) {
                charset = charset.replace("\"", "");
                String temp = new String(oriStr.getBytes("ISO-8859-1"), charset);
                oriStr = temp;
            }

        }
        return oriStr;
    }
}
