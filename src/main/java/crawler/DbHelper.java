package crawler;

import org.skife.jdbi.v2.Handle;

import java.util.List;

class DbHelper {

    static void insertTagIfNotExists(Handle handle, String tag) {
        handle.execute("insert into tag (tag)  select '" + tag
                + "' from dual where not exists (select * from tag where tag='" + tag + "')");
    }

    static void insertSeries(Handle handle, Main.Series series) {
        handle.insert("insert into series (sid, title, poster_url) values (?, ?, ?)",
                series.sid, series.title, series.posterUrl);
    }

    static void linkSeriesWithTags(Handle handle, String seriesSid, List<String> tags) {
        Object seriesId = handle.select("select id from series where sid = ?", seriesSid).get(0).get("id");

        for (String tag : tags) {
            Object tagId = handle.select("select id from tag where tag = ?", tag).get(0).get("id");
            handle.insert("insert into series_tag (series_id, tag_id) values (?, ?)", seriesId, tagId);
        }
    }

    static void insertEpisode(Handle handle, String seriesSid, Main.Episode episode) {
        Object seriesId = handle.select("select id from series where sid = ?", seriesSid).get(0).get("id");

        handle.insert("insert into episode (sid, video_url, duration_seconds, series_id) values" +
                " (?, ?, ?, ?)", episode.sid, episode.videoUrl, episode.durationSeconds, seriesId);
    }

    static boolean seriesNotExists(Handle handle, String seriesSid) {
        Long count = (Long) handle.select("select count(*) as count from series where sid = ?", seriesSid)
                .get(0).get("count");
        return count == 0;
    }
}
