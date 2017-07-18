package model;

import org.jsoup.nodes.Document;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import util.Constants;
import util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static crawler.JSoupHelper.connectAndGetDoc;
import static crawler.JSoupHelper.connectAndGetString;
import static java.util.stream.Collectors.toList;

public class Episode {

    private String sid;
    private String videoUrl;
    private long durationSeconds;

    public void save(Handle handle, String seriesSid) {
        Object seriesId = handle.select("select id from series where sid = ?", seriesSid).get(0).get("id");

        handle.insert("save into episode (sid, video_url, duration_seconds, series_id) values" +
                " (?, ?, ?, ?)", this.sid, this.videoUrl, this.durationSeconds, seriesId);
    }

    @Override
    public String toString() {
        return "Episode{" +
                "sid='" + sid + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", durationSeconds=" + durationSeconds +
                '}';
    }

    public static List<Episode> findAllBySeriesId(DBI dbi, int seriesId) {
        return dbi.withHandle(h -> h.select("select * from episode where series_id = ?", seriesId))
                .stream()
                .map(Episode::fromDb)
                .collect(toList());
    }

    public static List<Episode> getEpisodesList(String seriesId) throws Exception {
        System.out.println("start processing series: " + seriesId);

        List<Episode> episodesList = new ArrayList<>();

        List<String> toC = getToC(seriesId);

        for (String aToC : toC) {
            String body = connectAndGetString(Constants.GET_PLAYER_URL.replace("$1", aToC));
            Resp resp = Util.GSON.fromJson(body, Resp.class);
            if (resp.data.url == null) {
                System.err.println(body);
            }
            Episode episode = new Episode();
            episode.sid = aToC;
            episode.videoUrl = resp.data.url;
            episode.durationSeconds = resp.data.durationSeconds;
            episodesList.add(episode);
        }
        System.out.println("got episode list for series: " + seriesId + ", size: " + episodesList.size());
        return episodesList;
    }

    private static List<String> getToC(String seriesId) throws Exception {
        String episodesUrl = Constants.EPISODES_URL.replace("$1", seriesId).replace("$2", getSectionId(seriesId));

        List<String> idList = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            idList.addAll(connectAndGetDoc(episodesUrl.replace("$3", i + ""))
                    .select("body > div > .subitem").eachAttr("id"));
        }
        Collections.reverse(idList);
        System.out.println("got toc for series: " + seriesId + ", " + idList);
        return idList;
    }

    private static String getSectionId(String seriesId) throws Exception {
        Document doc = connectAndGetDoc(Constants.SERIES_URL.replace("$1", seriesId));
        String sectionText = doc.select("#main > div > div > div > div.pageing > ul > li.arrowlft > a").get(0).outerHtml();
        return sectionText.substring(sectionText.indexOf("showSection-") + 12, sectionText.indexOf("' + '.sort-' + "));
    }

    private static Episode fromDb(Map<String, Object> dbRow) {
        Episode episode = new Episode();
        episode.sid = (String) dbRow.get("sid");
        episode.videoUrl = (String) dbRow.get("video_url");
        episode.durationSeconds = (long) dbRow.get("duration_seconds");
        return episode;
    }

    private static class Resp {
        private Data data;
    }

    private static class Data {
        private String url;
        private long durationSeconds;
    }
}
