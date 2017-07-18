package crawler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import model.Episode;
import model.Series;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import util.Config;
import util.Constants;
import util.Util;

public class Main {

    public static void main(String[] args) throws Exception {
        startCrawler();
    }

    private static void startCrawler() throws Exception{

        DBI dbi = new DBI(Config.JDBC_URL, Config.JDBC_USERNAME, Config.JDBC_PASSWORD);
        Handle handle = dbi.open();

        skipSSL();

        List<Series> seriesList = Series.getSeriesList();

        for (Series series : seriesList){
            System.out.println(series);

            if (DbHelper.seriesNotExists(handle, series.getSid())){

                series.getTags().forEach(t -> DbHelper.insertTagIfNotExists(handle, t));
                DbHelper.insertSeries(handle, series);
                DbHelper.linkSeriesWithTags(handle, series.getSid(), series.getTags());

                List<Episode> listOfEpisodes = Episode.getEpisodesList(series.getSid());

                for (Episode episode : listOfEpisodes){
                    DbHelper.insertEpisode(handle, series.getSid(), episode);
                }
            }else{
                System.out.println("series " + series.getSid() + " already exists.");
            }
        }
        handle.close();
    }

    private static void skipSSL() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }
    }
}