package zephyr.mail.vo;

// 공통 속성을 처리하기 위한 상위 vo 클래스
public abstract class CommonVo {
    private String id;
    private String eml;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEml() {
        return eml;
    }

    public void setEml(String eml) {
        this.eml = eml;
    }

    public abstract String getMessageId();

    public abstract void setMessageId(String messageId);

}
