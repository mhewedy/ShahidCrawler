package util;

public interface Config {

    int TOTAL_PAGES = 13;      // change me to include more pages, currently there's only 13 pages
    String JSESSIONID = "AE38CCCD5D6AC60ADE419D51D9266F83";    // change me with a working session id
    String XBE = "N1"; // change me for working XBE parameter (I don't know what is it, but the session won't be valid without it)

    String JDBC_URL = "jdbc:mysql://192.168.1.10:3306/shahid?useUnicode=true&characterEncoding=UTF-8";
    String JDBC_USERNAME = "root";
    String JDBC_PASSWORD = "system";
    int MAX_RECENT = 10;
}
