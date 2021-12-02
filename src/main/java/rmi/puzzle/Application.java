package rmi.puzzle;

/**
 * 
 * Simple Puzzle Game - Centralized version.
 * 
 * By A. Croatti 
 * 
 * @author acroatti
 *
 */
public class Application {

	public static void main(final String[] args) {
		final int n = 3;
		final int m = 5;

		final String imagePath = "src/main/java/rmi/park.jpg";
		
		final PuzzleBoard puzzle = new PuzzleBoard(n, m, imagePath);
        puzzle.setVisible(true);
	}
}
