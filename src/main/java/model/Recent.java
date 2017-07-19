package model;

import org.skife.jdbi.v2.DBI;
import util.Config;

import java.util.List;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class Recent {
    private int seriesId;

    // ------------------------ DB Operations

    public static List<Series> findAll(DBI dbi) {
        return dbi.withHandle(h -> h.select(Series.getSearchBaseQuery() + " where series.id in (select series_id from recent order by id desc)"))
                .stream()
                .map(Series::fromDb)
                .collect(toList());
    }

    public static synchronized void save(DBI dbi, int seriesId) {

        dbi.withHandle(h -> h.update("delete from recent where series_id = ?", seriesId));

        String recentIds = dbi.withHandle(h ->
                h.select("select id from recent order by id desc limit ?", Config.MAX_RECENT - 1))
                .stream()
                .map(row -> String.valueOf(row.get("id")))
                .collect(joining(", "));

        dbi.withHandle(h -> h.update("delete from recent where id not in (" + recentIds + ")"));
        dbi.withHandle(h -> h.insert("insert into recent (series_id) values (?)", seriesId));
    }
}
