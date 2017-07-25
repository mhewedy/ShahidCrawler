-- find series that have all episode not inserted
select * from series left outer join episode on series.id = episode.series_id where episode.id is null;

-- find series with missing video url for one or more of its episodes
select distinct series.id, series.title, series.sid from series join episode on series.id = episode.series_id where episode.video_url is null;

-- episode_count <> actual count
select series.id, series.title, (series.episode_count - 1 -  count(*) ) as diff from series join episode on series.id = episode.series_id group by series.id having diff <> 0;

-- delete all invalid series data
delete from series where id in ();
delete from episode where series_id in ();
