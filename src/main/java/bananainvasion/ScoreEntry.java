package bananainvasion;

public class ScoreEntry {
    private final String username;
    private final int highScore;

    public ScoreEntry(String username, int highScore) {
        this.username = username;
        this.highScore = highScore;
    }

    public String getUsername() {
        return username;
    }

    public int getHighScore() {
        return highScore;
    }
}