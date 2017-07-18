-- find series with missing video url for one or more of its episodes
select series.title, tag.tag from series join episode on series.id = episode.series_id join series_tag on series.id = series_tag.series_id join tag on tag.id = series_tag.tag_id where episode.video_url is null;

-- find series that have one or more episodes not inserted (may be all getToc return zero list)
select * from series left outer join episode on series.id = episode.series_id where episode.id is null


