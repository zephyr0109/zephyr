package zephyr.mail;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	DecoderTest.class, POP3MailManagerTest.class, FilterTest.class
})
public class ServiceTestSuit {

}
