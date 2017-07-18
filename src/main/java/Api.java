
import model.Episode;
import model.Series;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import spark.ResponseTransformer;
import util.Config;
import util.Util;

import java.util.ArrayList;
import java.util.Arrays;

import static spark.Spark.*;

public class Api {

    public static void main(String[] args) {

        DBI dbi = new DBI(Config.JDBC_URL, Config.JDBC_USERNAME, Config.JDBC_PASSWORD);

        get("/series/search",  (request, response) -> {
            response.type("application/json");

            String term = request.queryParams("term");
            System.out.println("search series with term: " + term);

            return Series.search(dbi, term);
        }, new JsonTransformer());

        get("/episode/series/:id", (request, response) -> {
            response.type("application/json");

            String id = request.params("id");
            System.out.println("getting series with id: " + id);

            // Save to recent

            return new ArrayList<Episode>();
        }, new JsonTransformer());

        get("/recent", (request, response) -> {
            response.type("application/json");

            return new ArrayList<Series>();
        }, new JsonTransformer());

        get("/tags", (request, response) -> {
            response.type("application/json");

            return Arrays.asList("tag1", "tag2");
        }, new JsonTransformer());


        get("/tag", (request, response) -> {
            response.type("application/json");

            String tag = request.queryParams("tag");
            System.out.println("get list of series for tag: " + tag);

            return new ArrayList<Series>();
        }, new JsonTransformer());

    }

    public static class JsonTransformer implements ResponseTransformer {

        @Override
        public String render(Object model) {
            return Util.GSON.toJson(model);
        }

    }
}
