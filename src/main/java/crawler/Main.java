package crawler;

import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import model.Episode;
import model.Series;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import util.Config;

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

            if (series.notExists(handle)){
                series.save(handle);
                List<Episode> listOfEpisodes = Episode.getEpisodesList(series.getSid());
                for (Episode episode : listOfEpisodes){
                    episode.save(handle, series.getSid());
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