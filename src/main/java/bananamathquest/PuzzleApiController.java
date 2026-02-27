package bananamathquest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class PuzzleApiController {

    private final BananaService bananaService;

    public PuzzleApiController(BananaService bananaService) {
        this.bananaService = bananaService;
    }

    @GetMapping("/api/puzzle")
    public ResponseEntity<?> puzzle() {
        try {
            PuzzleDto dto = bananaService.fetchPuzzle();
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            // Return JSON instead of crashing (no Whitelabel page)
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "error", "Banana API unavailable",
                            "message", e.getMessage() == null ? "unknown error" : e.getMessage()
                    ));
        }
    }
}