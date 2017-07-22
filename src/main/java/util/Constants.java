package util;

public interface Constants {

    String SERIES_LIST_URL = "https://shahid.mbc.net/ar/series-browser/autoGeneratedContent/seriesBrowserGrid~browse~-param-.sort-latest.pageNumber-$1.html";
    String GET_PLAYER_URL = "https://shahid.mbc.net/arContent/getPlayerContent-param-.id-$1.playList-true.type-player.html";
    String SERIES_URL = "https://shahid.mbc.net/ar/series/$1";
    String EPISODES_URL = "https://shahid.mbc.net/ar/series/autoGeneratedContent/relatedEpisodeListingDynamic~listing~-param-.ptype-series.seriesId-$1.showSection-$2.sort-number:DESC.pageNumber-$3.html";

    String MOVIE_LIST_URL = "https://shahid.mbc.net/ar/movie-browser/autoGeneratedContent/movieBrowserGrid~browse~-param-.sort-latest.pageNumber-$1.html";
}
