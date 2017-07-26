package zephyr.mail.service;

public enum MailHeaderValues {
    TEXT_PLAIN("text/plain"), TEXT_HTML("text/html"), MULTIPART("multipart/"), TEXT_ALL("text/*"), MULTIPART_ALL(
            "multipart/*"), ATTACH(
                    "attachment"), APPLICATION("application/*"), TEXT_CALENDAR("text/calendar"), MESSAGE("message");
    private String str;

    private MailHeaderValues() {
        // TODO Auto-generated constructor stub
    }

    private MailHeaderValues(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }
}
