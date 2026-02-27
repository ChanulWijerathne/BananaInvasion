package bananamathquest;

public class PuzzleDto {

    private String imageDataUrl;
    private int solution;

    public PuzzleDto() {}

    public PuzzleDto(String imageDataUrl, int solution) {
        this.imageDataUrl = imageDataUrl;
        this.solution = solution;
    }

    public String getImageDataUrl() {
        return imageDataUrl;
    }

    public void setImageDataUrl(String imageDataUrl) {
        this.imageDataUrl = imageDataUrl;
    }

    public int getSolution() {
        return solution;
    }

    public void setSolution(int solution) {
        this.solution = solution;
    }
}
