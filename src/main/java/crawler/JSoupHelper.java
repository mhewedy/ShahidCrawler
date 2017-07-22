package crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import util.Config;

import java.io.IOException;

public class JSoupHelper {

    public static Document connectAndGetDoc(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        sleep();
        return document;
    }

    public static String connectAndGetString(String url) throws IOException {
        String body = Jsoup.connect(url).cookie("JSESSIONID", Config.JSESSIONID).cookie("X-BE", Config.XBE).ignoreContentType(true)
                .execute().body();
        sleep();
        return body;
    }

    private static void sleep() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
