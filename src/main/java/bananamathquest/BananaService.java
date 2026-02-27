package bananamathquest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class BananaService {

    private static final String API_URL = "https://marcconrad.com/uob/banana/api.php";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PuzzleDto fetchPuzzle() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = objectMapper.readTree(response.body());

            String q = root.get("question").asText().trim();
            int sol = root.get("solution").asInt();

            String image;
            if (q.startsWith("http://") || q.startsWith("https://")) {
                image = q; // URL image
            } else if (q.startsWith("data:image")) {
                image = q; // already data url
            } else {
                image = "data:image/png;base64," + q; // base64 image
            }

            return new PuzzleDto(image, sol);

        } catch (Exception e) {
            throw new RuntimeException("Banana API failed: " + e.getMessage());
        }
    }
}