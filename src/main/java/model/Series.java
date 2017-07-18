package model;

import crawler.JSoupHelper;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import sun.reflect.generics.tree.ReturnType;
import util.Config;
import util.Constants;
import util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Series {

    private String sid;
    private String title;
    private String posterUrl;
    private List<String> tags;

    public String getSid() {
        return sid;
    }

    public boolean notExists(Handle handle) {
        Long count = (Long) handle.select("select count(*) as count from series where sid = ?", this.sid)
                .get(0).get("count");
        return count == 0;
    }

    public void save(Handle handle) {
        //saveTagsIfNotExists
        this.tags.forEach
                (tag -> handle.execute("save into tag (tag) select ? from dual where not exists " +
                        "(select * from tag where tag= ? )", tag, tag));
        //save
        handle.insert("save into series (sid, title, poster_url) values (?, ?, ?)",
                this.sid, this.title, this.posterUrl);
        //linkWithTags
        Object seriesId = handle.select("select id from series where sid = ?", this.sid).get(0).get("id");

        for (String tag : this.tags) {
            Object tagId = handle.select("select id from tag where tag = ?", tag).get(0).get("id");
            handle.insert("save into series_tag (series_id, tag_id) values (?, ?)", seriesId, tagId);
        }
    }

    @Override
    public String toString() {
        return "Series{" +
                "sid='" + sid + '\'' +
                ", title='" + title + '\'' +
                ", posterUrl='" + posterUrl + '\'' +
                ", tag=" + tags +
                '}';
    }

    public static List<Series> search(DBI dbi, String term) {
        return dbi.withHandle(h -> h.select("select * from series where title like '%?%'", term))
                .stream()
                .map(Series::fromDb)
                .collect(Collectors.toList());
    }

    public static List<Series> findByTag(DBI dbi, String tag) {
        return dbi.withHandle(h -> h.select("select * from series where tag = ?", tag))
                .stream()
                .map(Series::fromDb)
                .collect(Collectors.toList());
    }

    public static List<Series> getSeriesList() throws Exception {
        List<Series> idList = new ArrayList<>();

        for (int i = 0; i < Config.TOTAL_PAGES; i ++){
            List<Series> internalList = JSoupHelper.connectAndGetDoc(Constants.SERIES_LIST_URL.replace("$1", i + ""))
                    .select("body > div")
                    .stream()
                    .map(Series::fromElement)
                    .collect(Collectors.toList());
            idList.addAll(internalList);
        }
        return idList;
    }

     // ---------- private --------------

    private static Series fromElement(Element element){
        Series series = new Series();

        Elements divs = element.children();
        Element firstDiv = divs.get(1);
        Element secondDiv = divs.get(2);

        series.sid = secondDiv.attr("id");
        series.title = element.select(".title > a").text();
        series.posterUrl = element.select(".photo > img").attr("src");

        Category[] categories = Util.GSON.fromJson(firstDiv.attr("categories"), Category[].class);
        series.tags = Stream.of(categories).map(Category::getName).collect(Collectors.toList());

        return series;
    }

    private static Series fromDb(Map<String, Object> dbRow){
        Series series = new Series();
        series.sid = (String) dbRow.get("sid");
        series.title = (String) dbRow.get("title");
        series.posterUrl = (String) dbRow.get("poster_url");
        return series;
    }
}
