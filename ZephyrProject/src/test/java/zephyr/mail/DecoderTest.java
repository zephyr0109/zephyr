package zephyr.mail;


import static org.junit.Assert.*;

import java.io.IOException;

import javax.mail.MessagingException;

import org.junit.BeforeClass;
import org.junit.Test;

import zephyr.mail.service.MailDecoder;

public class DecoderTest {
	
	private static MailDecoder md;

	@BeforeClass
	public static void setField(){
		md = new MailDecoder();
	}
	
	@Test
	public void testSimpleText() throws MessagingException, IOException{
		String testStr = "테스트";
		String decodedStr = md.decodeMimeEncdString(testStr, null);
		System.out.println(testStr);
		System.out.println(decodedStr);
		assertEquals(testStr,decodedStr);
	}
	
	@Test
	public void testMimeEncodedText() throws MessagingException, IOException{
		String testStr = "=?euc-kr?B?ua7BprChILv9seK0wiCwzcC6ILOqu9sgwM/AzCC+xrTPtNku?=";
		String decodedStr = md.decodeMimeEncdString(testStr, "utf-8");
		System.out.println(testStr);
		System.out.println(decodedStr);
	}
	
	
}
