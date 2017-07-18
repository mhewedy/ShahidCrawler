
import model.Episode;
import model.Recent;
import model.Series;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.logging.PrintStreamLog;
import org.skife.jdbi.v2.tweak.SQLLog;
import spark.ResponseTransformer;
import util.Config;
import util.Util;

import java.util.List;

import static spark.Spark.*;

public class Api {

    public static void main(String[] args) {
        // default port 4567

        DBI dbi = new DBI(Config.JDBC_URL, Config.JDBC_USERNAME, Config.JDBC_PASSWORD);
        dbi.setSQLLog(new PrintStreamLog());

        // /series/search?term=xxx
        get("/series/search",  (request, response) -> {
            response.type("application/json");
            response.header("Access-Control-Allow-Origin", "*");
            return Series.search(dbi, request.queryParams("term"));
        }, new JsonTransformer());

        get("/episode/series/:id", (request, response) -> {
            response.type("application/json");
            response.header("Access-Control-Allow-Origin", "*");
            String id = request.params("id");
            List<Episode> allBySeriesId = Episode.findAllBySeriesId(dbi, Integer.parseInt(id));
            if (allBySeriesId.size() > 0){
                Recent.save(dbi, Integer.parseInt(id));
            }
            return allBySeriesId;
        }, new JsonTransformer());

        // /recent
        get("/recent", (request, response) -> {
            response.type("application/json");
            response.header("Access-Control-Allow-Origin", "*");
            return Recent.findAll(dbi);
        }, new JsonTransformer());

        // /tags
        get("/tags", (request, response) -> {
            response.type("application/json");
            response.header("Access-Control-Allow-Origin", "*");
            return Series.getAllTags(dbi);
        }, new JsonTransformer());


        // /tag?tag=xxx
        get("/tag", (request, response) -> {
            response.type("application/json");
            response.header("Access-Control-Allow-Origin", "*");
            return Series.findAllByTag(dbi, request.queryParams("tag"));
        }, new JsonTransformer());

    }

    private static class JsonTransformer implements ResponseTransformer {

        @Override
        public String render(Object model) {
            return Util.GSON.toJson(model);
        }

    }
}
