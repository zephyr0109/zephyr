package zephyr.mail.vo;

public class Pop3User {

    private String user;
    private String password;
    private String host;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Pop3User() {
        // TODO Auto-generated constructor stub
    }

    public Pop3User(String user, String password) {
        super();
        this.user = user;
        this.password = password;
    }

    @Override
    public String toString() {
        return "Pop3User [user=" + user + ", password=" + password + "]";
    }

}
