package model;

import crawler.JSoupHelper;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.Config;
import util.Constants;
import util.Util;

import java.util.ArrayList;
import java.util.List;
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

    public String getTitle() {
        return title;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public List<String> getTags() {
        return tags;
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
}
