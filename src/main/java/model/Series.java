package model;

import crawler.JSoupHelper;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import util.Config;
import util.Constants;
import util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class Series {

    private int id;
    private String sid;
    private String title;
    private String posterUrl;
    private int episodeCount;
    private List<String> tags;
    private List<Episode> episodes;

    public String getSid() {
        return sid;
    }

    public void setEpisodes(List<Episode> episodes) {
        this.episodes = episodes;
    }

    // ------------------------ DB Operations

    public boolean notExists(Handle handle) {
        Long count = (Long) handle.select("select count(*) as count from series where sid = ?", this.sid)
                .get(0).get("count");
        return count == 0;
    }

    public void save(Handle handle) {
        //saveTagsIfNotExists
        this.tags.forEach
                (tag -> handle.execute("insert into tag (tag) select ? from dual where not exists " +
                        "(select * from tag where tag= ? )", tag, tag));
        //save
        handle.insert("insert into series (sid, title, poster_url, episode_count) values (?, ?, ?, ?)",
                this.sid, this.title, this.posterUrl, this.episodeCount);
        //linkWithTags
        Object seriesId = handle.select("select id from series where sid = ?", this.sid).get(0).get("id");

        for (String tag : this.tags) {
            Object tagId = handle.select("select id from tag where tag = ?", tag).get(0).get("id");
            handle.insert("insert into series_tag (series_id, tag_id) values (?, ?)", seriesId, tagId);
        }
    }

    public static List<Series> search(DBI dbi, String term) {
        return dbi.withHandle(h -> h.select("select * from series where title like ?", "%" + term + "%"))
                .stream()
                .map(Series::fromDb)
                .collect(toList());
    }

    public static List<Series> findAllByTag(DBI dbi, String tag) {
        return dbi.withHandle(h -> h.select("select series.* from series join series_tag on series.id = series_tag.series_id join tag on tag.id = series_tag.tag_id where tag.tag = ?", tag))
                .stream()
                .map(Series::fromDb)
                .collect(toList());
    }

    public static List<String> getAllTags(DBI dbi) {
        return dbi.withHandle(h -> h.select("select tag from tag order by tag asc"))
                .stream()
                .map(row -> (String) row.get("tag"))
                .collect(toList());
    }

    public static Series findById(DBI dbi, int id) {
        return dbi.withHandle(h -> h.select("select series.*, group_concat(tag.tag) as tags from series join series_tag on series.id = series_tag.series_id join tag on tag.id = series_tag.tag_id where series.id = ? group by series.id", id))
                .stream()
                .findFirst()
                .map(Series::fromDb)
                .orElse(null);
    }

    // --------------------- Crawling Operations

    public static List<Series> getSeriesList() throws Exception {
        List<Series> idList = new ArrayList<>();

        for (int i = 0; i < Config.TOTAL_PAGES; i++) {
            List<Series> internalList = JSoupHelper.connectAndGetDoc(Constants.SERIES_LIST_URL.replace("$1", i + ""))
                    .select("body > div")
                    .stream()
                    .map(Series::fromElement)
                    .collect(toList());
            idList.addAll(internalList);
        }
        return idList;
    }

    // ---------- private --------------

    private static Series fromElement(Element element) {
        Series series = new Series();

        Elements divs = element.children();
        Element firstDiv = divs.get(1);
        Element secondDiv = divs.get(2);

        series.sid = secondDiv.attr("id");
        series.title = element.select(".title > a").text();
        series.posterUrl = element.select(".photo > img").attr("src");
        String seriesCountText = element.select(".info span:last-child").text();
        if (seriesCountText.trim().length() > 0){
            series.episodeCount = Integer.parseInt(seriesCountText);
        }
        Category[] categories = Util.GSON.fromJson(firstDiv.attr("categories"), Category[].class);
        series.tags = Stream.of(categories).map(Category::getName).collect(toList());

        return series;
    }

    static Series fromDb(Map<String, Object> dbRow) {
        Series series = new Series();
        series.id = (Integer) dbRow.get("id");
        series.sid = (String) dbRow.get("sid");
        series.title = (String) dbRow.get("title");
        series.episodeCount = (int) dbRow.get("episode_count");
        series.posterUrl = (String) dbRow.get("poster_url");
        String tags = ((String) dbRow.get("tags"));
        if (tags != null) {
            series.tags = Stream.of(tags.split(",")).map(String::trim).collect(toList());
        }
        return series;
    }

    // ---------------------------

    @Override
    public String toString() {
        return "Series{" +
                "sid='" + sid + '\'' +
                ", title='" + title + '\'' +
                ", posterUrl='" + posterUrl + '\'' +
                ", tag=" + tags +
                '}';
    }
}
