package model;

import org.skife.jdbi.v2.DBI;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class Recent {
//    private int seriesId;

    public static List<Integer> findAll(DBI dbi) {
        return dbi.withHandle(h -> h.select("select series_id from recent order by id desc"))
                .stream()
                .map(row -> (Integer) row.get("series_id"))
                .collect(toList());
    }

    public static void save(DBI dbi, int seriesId) {
        dbi.withHandle(h -> h.update("delete from recent where series_id = ?", seriesId));
        dbi.withHandle(h -> h.insert("insert into recent (series_id) values (?)", seriesId));
    }
}
