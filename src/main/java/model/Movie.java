package model;

import crawler.JSoupHelper;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import util.Config;
import util.Constants;
import util.Util;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static crawler.JSoupHelper.connectAndGetString;
import static java.util.stream.Collectors.toList;

public class Movie {

    private int id;
    private String sid;
    private String title;
    private String posterUrl;
    private String videoUrl;
    private int durationSeconds;
    private List<String> tags;

    // ------------------------ DB Operations

    public static List<Movie> search(DBI dbi, String term) {
        return dbi.withHandle(h -> h.select("select * from movie where title like ?", "%" + term + "%"))
                .stream()
                .map(Movie::fromDb)
                .collect(toList());
    }

    public static List<Movie> findAllByTag(DBI dbi, String tag) {
        return dbi.withHandle(h -> h.select("select movie.* from movie join movie_tag on movie.id = movie_tag.movie_id join tag on tag.id = movie_tag.tag_id where tag.tag = ?", tag))
                .stream()
                .map(Movie::fromDb)
                .collect(toList());
    }

    public static List<String> getAllTags(DBI dbi) {
        return dbi.withHandle(h -> h.select("select distinct tag from tag join movie_tag on tag.id = movie_tag.tag_id order by tag asc"))
                .stream()
                .map(row -> (String) row.get("tag"))
                .collect(toList());
    }

    public static Movie findById(DBI dbi, int id) {
        return dbi.withHandle(h -> h.select("select movie.*, group_concat(tag.tag) as tags from movie join movie_tag on movie.id = movie_tag.movie_id join tag on tag.id = movie_tag.tag_id where movie.id = ? group by movie.id", id))
                .stream()
                .findFirst()
                .map(Movie::fromDb)
                .orElse(null);
    }

    public static void saveMovieList(Handle handle) throws Exception {
        for (int i = 0; i < Config.TOTAL_PAGES; i++) {
            JSoupHelper.connectAndGetDoc(Constants.MOVIE_LIST_URL.replace("$1", i + ""))
                    .select("body > div")
                    .stream()
                    .map(Movie::fromElement)
                    .filter(movie -> movie.notExists(handle))
                    .map(Movie::getVideoUrl)
                    .forEach(movie -> movie.save(handle));
        }
    }

    private static Movie fromElement(Element element) {
        Movie movie = new Movie();

        Elements divs = element.children();
        Element firstDiv = divs.get(1);
        Element secondDiv = divs.get(2);

        movie.sid = secondDiv.attr("id");
        movie.title = element.select(".title > a").text();
        movie.posterUrl = element.select(".photo > img").attr("src");
        Category[] categories = Util.GSON.fromJson(firstDiv.attr("categories"), Category[].class);
        movie.tags = Stream.of(categories).map(Category::getName).collect(toList());

        return movie;
    }

    // --------------------- Crawling Operations

    private static Movie getVideoUrl(Movie movie) {
        try {
            String body = connectAndGetString(Constants.GET_PLAYER_URL.replace("$1", movie.sid));
            PCResponse.Resp resp = Util.GSON.fromJson(body, PCResponse.Resp.class);
            if (resp.data.url == null) {
                System.err.println(body);
            }
            movie.videoUrl = resp.data.url;
            movie.durationSeconds = resp.data.durationSeconds;
        } catch (Exception ex) {
            System.err.println(ex.getMessage() + " movie sid: " + movie.sid);
        }
        System.out.println("working on movie : " + movie);
        return movie;
    }

    // ---------- private --------------

    private static Movie fromDb(Map<String, Object> dbRow) {
        Movie movie = new Movie();
        movie.id = (Integer) dbRow.get("id");
        movie.sid = (String) dbRow.get("sid");
        movie.title = (String) dbRow.get("title");
        movie.posterUrl = (String) dbRow.get("poster_url");
        movie.videoUrl = (String) dbRow.get("video_url");
        movie.durationSeconds = (int) dbRow.get("duration_seconds");
        String tags = ((String) dbRow.get("tags"));
        if (tags != null) {
            movie.tags = Stream.of(tags.split(",")).map(String::trim).collect(toList());
        }
        return movie;
    }

    private boolean notExists(Handle handle) {
        Long count = (Long) handle.select("select count(*) as count from movie where sid = ?", this.sid)
                .get(0).get("count");
        return count == 0;
    }

    private void save(Handle handle) {
        //saveTagsIfNotExists
        this.tags.forEach
                (tag -> handle.execute("insert into tag (tag) select ? from dual where not exists " +
                        "(select * from tag where tag= ? )", tag, tag));
        //save
        handle.insert("insert into movie (sid, title, poster_url, video_url, duration_seconds) values (?, ?, ?, ?, ?)",
                this.sid, this.title, this.posterUrl, this.videoUrl, this.durationSeconds);
        //linkWithTags
        Object movieId = handle.select("select id from movie where sid = ?", this.sid).get(0).get("id");

        for (String tag : this.tags) {
            Object tagId = handle.select("select id from tag where tag = ?", tag).get(0).get("id");
            handle.insert("insert into movie_tag (movie_id, tag_id) values (?, ?)", movieId, tagId);
        }
    }

    // ---------------------------

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", sid='" + sid + '\'' +
                ", title='" + title + '\'' +
                ", posterUrl='" + posterUrl + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", durationSeconds=" + durationSeconds +
                ", tags=" + tags +
                '}';
    }
}
