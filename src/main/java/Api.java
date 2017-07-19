
import model.Episode;
import model.Recent;
import model.Series;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.logging.PrintStreamLog;
import spark.ResponseTransformer;
import util.Config;
import util.Util;

import java.util.List;

import static spark.Spark.*;

public class Api {

    public static void main(String[] args) {
        port(8801);

        DBI dbi = new DBI(Config.JDBC_URL, Config.JDBC_USERNAME, Config.JDBC_PASSWORD);
        dbi.setSQLLog(new PrintStreamLog());

        get("/tags", (request, response) -> Series.getAllTags(dbi), json());

        get("/series/search",  (request, response) -> Series.search(dbi, request.queryParams("term")), json());

        get("/recent", (request, response) -> Recent.findAll(dbi), json());

        get("/tag", (request, response) -> Series.findAllByTag(dbi, request.queryParams("tag")), json());

        get("/episode/series/:id", (request, response) -> {
            int id = Integer.parseInt(request.params("id"));
            List<Episode> allBySeriesId = Episode.findAllBySeriesId(dbi, id);
            if (allBySeriesId.size() > 0){
                Recent.save(dbi, id);
            }
            return allBySeriesId;
        }, json());

        get("/episode/watched/:id", (request, response) -> {
            Episode.setAsWatched(dbi, Integer.parseInt(request.params("id")));
            return null;
        }, json());

        after(((request, response) -> {
            response.type("application/json");
            response.header("Access-Control-Allow-Origin", "*");
        }));

    }

    private static ResponseTransformer json(){
        return (model) -> Util.GSON.toJson(model);
    }

}
