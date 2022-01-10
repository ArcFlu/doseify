import java.util.*;
import java.io.*;
import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;
import org.apache.hc.core5.http.ParseException;


public class tenSecond {
    private static final String accessToken = "BQDF9nefCvvnAfuffHvA7d_CQCeXyXUwve9M6vVdeyRSBy24BzdGcf4kUATd-nxropfq_ZtMfL_R2Kf8qrEod2_qpj_Ne2sXzd3kuu2Z2wOoECxrGrZlG3hF5P6DnBdFzeWIso2kbz7AhMpAhKwf9s-0_trTKxoQSB5_g_1to1ZMj1eOlz6cP475eps";
    private static final String q = "Come Around Me";

    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
        .setAccessToken(accessToken)
        .build();

    private static final SearchTracksRequest searchTracksRequest = spotifyApi.searchTracks(q)
        .build();

public static void main(String[] args) {
            try {
          final Paging<Track> trackPaging = searchTracksRequest.execute();

          System.out.println("ID: " + trackPaging.getId());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
          System.out.println("Error: " + e.getMessage());
        }
    }
}
