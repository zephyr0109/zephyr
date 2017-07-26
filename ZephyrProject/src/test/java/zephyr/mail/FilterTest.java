package zephyr.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;

import org.junit.BeforeClass;
import org.junit.Test;

import zephyr.filter.ContentFilter;

public class FilterTest {

    private static ContentFilter cf;

    @BeforeClass
    public static void setParam() {
        cf = new ContentFilter();
    }

    @Test
    public void testSubjectFilter() throws UnsupportedEncodingException {
        String subject = "(광고)";
        assertTrue(cf.filterSubject(subject));
        subject = "test 쿠팡 테스트";
        assertTrue(cf.filterSubject(subject));
        subject = "쿠 팡";
        assertTrue(!cf.filterSubject(subject));
    }

    @Test
    public void testFromFilter() throws UnsupportedEncodingException {
        assertTrue(cf.filterFromUser("webmaster@godowon.com"));
        assertTrue(cf.filterFromUser("godowon.com"));
        assertTrue(!cf.filterFromUser("naver.com"));
    }

    @Test
    public void testStrFilter() {
        String ori = "test";
        String filtered = cf.filteredString(ori);
        String test = "";
        assertEquals(ori, cf.filteredString(ori));

        ori = "테스트 문장입니다. 본 메일을 무단으로 열람,복사,활용,배포하는 행위는 금지되어 있습니다. 이 뒤에도 나옵니다.";
        filtered = cf.filteredString(ori);
        test = "테스트 문장입니다. 이 뒤에도 나옵니다.";
        System.out.println(filtered);
        assertEquals(test, filtered);

    }

    @Test
    public void testCollection() {

    }

}
