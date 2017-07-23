package model;

/**
 * Created by mhewedy on 7/22/17.
 */
class Dto {
    static class PlayerContent {
        Data data;
    }

    static class Data {
        String url;
        String signature;
        int durationSeconds;
    }
}
