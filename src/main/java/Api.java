import model.Episode;
import model.Movie;
import model.Recent;
import model.Series;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.logging.PrintStreamLog;
import spark.ResponseTransformer;
import spark.utils.IOUtils;
import util.Config;
import util.Util;

import java.io.InputStream;
import java.util.List;

import static spark.Spark.*;

public class Api {

    public static void main(String[] args) {
        port(8801);

        DBI dbi = new DBI(Config.JDBC_URL, Config.JDBC_USERNAME, Config.JDBC_PASSWORD);
        dbi.setSQLLog(new PrintStreamLog());

        get("/movie/tags", (request, response) -> Movie.getAllTags(dbi), json());

        get("/movie/tag", (request, response) -> new SearchResult(null, Movie.findAllByTag(dbi, request.queryParams("tag"))), json());

        get("/series/tags", (request, response) -> Series.getAllTags(dbi), json());

        get("/series/tag", (request, response) -> new SearchResult(Series.findAllByTag(dbi, request.queryParams("tag")), null), json());

        get("/search", (request, response) ->
                        new SearchResult(Series.search(dbi, request.queryParams("term")),
                                Movie.search(dbi, request.queryParams("term")))
                , json());

        get("/series/recent", (request, response) -> new SearchResult(Recent.findAll(dbi), null), json());

        get("/episode/series/:id", (request, response) -> {
            int id = Integer.parseInt(request.params("id"));

            Series series = Series.findById(dbi, id);
            if (series != null) {
                series.setEpisodes(Episode.findAllBySeriesId(dbi, id));
                Recent.save(dbi, id);
            }
            return series;
        }, json());

        get("/episode/watched/:id", (request, response) -> {
            Episode.setAsWatched(dbi, Integer.parseInt(request.params("id")));
            return null;
        }, json());

        get("/download", (request, response) -> {
            InputStream resourceAsStream = Api.class.getClassLoader().getResourceAsStream("shahid-mobile.apk");
            response.header("Content-Disposition", "attachment; filename=shahid-mobile.apk");
            response.type("application/force-download");

            IOUtils.copy(resourceAsStream, response.raw().getOutputStream());

            response.raw().getOutputStream().flush();
            response.raw().getOutputStream().close();

            return response.raw();
        });

        after(((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Content-Type", "application/json; charset=utf-8");
        }));
    }

    private static ResponseTransformer json() {
        return (model) -> Util.GSON.toJson(model);
    }

    private static class SearchResult {
        private List<Series> seriesList;
        private List<Movie> movieList;

        SearchResult(List<Series> seriesList, List<Movie> movieList) {
            this.seriesList = seriesList;
            this.movieList = movieList;
        }
    }
}
