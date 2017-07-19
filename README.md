# shahid

Consist of two parts a REST Api and a crawler job.

* The crawler [job](https://github.com/mhewedy/shahid/blob/master/src/main/java/crawler/Main.java) connect to shahid.net website to get TV series info (list of series and list of m3u8 for each episode in the series) and save the data into mysql database.
* The REST [Api](https://github.com/mhewedy/shahid/blob/master/src/main/java/Api.java) is a simple Api (All GET) to expose mysql database table information. 

I've tried to make the domain model objects not to be anemic by including the Crawling and DB operations in the domain model classes. In the domain model, any method that takes `Handle` or `DBI` object, is a DB operation, otherwise it is crawling (http) operation.

The code is a fast-and-drity, written quickly just to achieve the purpose I wanted.

The mobile app is written quickly in ionic, find it [here](https://github.com/mhewedy/shahid-mobile), I've choosed ionic because I am a web guy, I am more familure with Angular than Android (it is ~4 years since I last worked on android). I can say, Ionic is so much faster in development than android java development.

I am [deploying](https://github.com/mhewedy/shahid/blob/master/run.sh) the this service on a Linux (ubuntu) box at home, and running the android app on both my wife's and mine devices.
