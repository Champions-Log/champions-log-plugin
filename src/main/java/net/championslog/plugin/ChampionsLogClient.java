package net.championslog.plugin;

import com.google.gson.Gson;
import net.championslog.plugin.events.DisplayNameEvent;
import net.championslog.plugin.events.LogEvent;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStreamReader;

@Singleton
public class ChampionsLogClient {

    private static final String BASE_HOST = "https://api.championslog.net";
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    private final OkHttpClient okHttpClient;
    private final Gson gson;

    @Inject
    public ChampionsLogClient(OkHttpClient okHttpClient, Gson gson) {
        this.okHttpClient = okHttpClient;
        this.gson = gson;
    }

    public int fetchRemoteConfigVersion() throws IOException {
        var request = new Builder()
                .url(BASE_HOST + "/config/version")
                .get()
                .build();

        try (var response = okHttpClient.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch remote config version, response code: " + response.code());
            }

            try (var body = response.body()) {
                if (body == null) {
                    throw new IOException("Failed to fetch remote config version, null response body");
                }
                return gson.fromJson(new InputStreamReader(body.byteStream()), int.class);
            }
        }
    }

    public RemoteConfig fetchRemoteConfig() throws IOException {
        var request = new Builder()
                .url(BASE_HOST + "/config")
                .get()
                .build();

        try (var response = okHttpClient.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch remote config, response code: " + response.code());
            }

            try (var body = response.body()) {
                if (body == null) {
                    throw new IOException("Failed to fetch remote config, null response body");
                }
                return gson.fromJson(new InputStreamReader(body.byteStream()), RemoteConfig.class);
            }
        }
    }

    public void submitLogEvent(LogEvent event) throws IOException {
        var eventJson = gson.toJson(event);

        var request = new Builder()
                .url(BASE_HOST + "/events")
                .post(RequestBody.create(MEDIA_TYPE_JSON, eventJson))
                .build();

        try (var response = okHttpClient.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new IOException("Failed to submit log event, response code: " + response.code());
            }
        }
    }

    public void submitNameEvent(DisplayNameEvent event) throws IOException {
        var eventJson = gson.toJson(event);

        var request = new Builder()
                .url(BASE_HOST + "/display-name")
                .post(RequestBody.create(MEDIA_TYPE_JSON, eventJson))
                .build();

        try (var response = okHttpClient.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new IOException("Failed to submit log event, response code: " + response.code());
            }
        }
    }
}
