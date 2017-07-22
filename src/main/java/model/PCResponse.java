package model;

/**
 * Created by mhewedy on 7/22/17.
 */
class PCResponse {
    static class Resp {
        Data data;
    }

    static class Data {
        String url;
        int durationSeconds;
    }
}
