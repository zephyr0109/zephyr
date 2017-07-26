package zephyr.login;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;


public class HttpTest {

    /**
     * @param args
     * @throws IOException
     * @throws ClientProtocolException
     */
    public static void main(String[] args) throws ClientProtocolException,
            IOException {
        String id = "215011";
        String encPwd ="";
        System.out.println(encPwd);
        String url = "http://backup.saerom.co.kr/";
    	HttpClientBuilder hcb = HttpClientBuilder.create();
        HttpClient hc = hcb.build();

        String result = getSession(hc, url, id);
        result = interfaceTest(hc, url, id);
        result = sessionInvTest(hc, url, id);
        result = getSession(hc, url, id);
        result = interfaceTest(hc, url, id);
        //result = interfaceTest(url, id);
        System.out.println(result);
    }

    public static String sendIdAndPwd(String url, String id, String pwd) throws ClientProtocolException, IOException{
    	HttpPost hp = new HttpPost(url);
    	HttpClientBuilder hcb = HttpClientBuilder.create();
        CloseableHttpClient hc = hcb.build();
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("inputId", id));
        params.add(new BasicNameValuePair("inputPwd", pwd));

        hp.setEntity(new UrlEncodedFormEntity(params));
        HttpResponse hr = hc.execute(hp);
        BufferedInputStream is = new BufferedInputStream(hr.getEntity()
                .getContent());
        byte[] temp = new byte[65535];
        is.read(temp, 0, 65535);
        String str = new String(temp);
    	return str.trim();
    }

    public static String getSession(HttpClient hc, String url, String id) throws ClientProtocolException, IOException{
    	HttpGet hg = new HttpGet(url+"getSession.do?id="+id);
        HttpResponse hr = hc.execute(hg);
        BufferedInputStream is = new BufferedInputStream(hr.getEntity()
                .getContent());
        byte[] temp = new byte[65535];
        is.read(temp, 0, 65535);
        String str = new String(temp);
        for (Header h : hr.getAllHeaders()) {
			System.out.println(h.toString());
		}
        hg.releaseConnection();
        System.out.println(str.trim());
        System.out.println("-------------------------------------------------");
        return str.trim();
    }

    public static String interfaceTest(HttpClient hc, String url, String id) throws ClientProtocolException, IOException{
    	HttpGet hg= new HttpGet(url+"interface.do?servicename=tag_list&empno="+id);
        HttpResponse hr = hc.execute(hg);
        BufferedInputStream is = new BufferedInputStream(hr.getEntity()
                .getContent());
        byte[] temp = new byte[65535];
        is.read(temp, 0, 65535);
        String str = new String(temp);
        for (Header h : hr.getAllHeaders()) {
			System.out.println(h.toString());
		}
        hg.releaseConnection();
        System.out.println(str.trim());
        System.out.println("-------------------------------------------------");
    	return str.trim();
    }

    public static String sessionInvTest(HttpClient hc, String url, String id) throws ClientProtocolException, IOException{
    	HttpGet hg= new HttpGet(url+"session_invalidate.do");
        HttpResponse hr = hc.execute(hg);
        BufferedInputStream is = new BufferedInputStream(hr.getEntity()
                .getContent());
        for (Header h : hr.getAllHeaders()) {
			System.out.println(h.toString());
		}
        byte[] temp = new byte[65535];
        is.read(temp, 0, 65535);
        String str = new String(temp,"UTF-8");
        hg.releaseConnection();
        System.out.println(str);
        System.out.println("-------------------------------------------------");
    	return str.trim();
    }


}
