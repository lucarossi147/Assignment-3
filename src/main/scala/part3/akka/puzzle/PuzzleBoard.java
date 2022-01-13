package part3.akka.puzzle;

import part3.akka.Tile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class PuzzleBoard extends JFrame {
	
	final int rows;
    final int columns;
	private List<Tile> tiles = new ArrayList<>();
	private final JPanel board = new JPanel();
	private final SelectionManager selectionManager = new SelectionManager();
	private final List<ImageIcon> imageTiles = new ArrayList<>();
    private PropertyChangeSupport support;

    public PuzzleBoard(final int rows, final int columns, final String imagePath) {
    	this.rows = rows;
		this.columns = columns;
    	
    	setTitle("Puzzle");
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        board.setBorder(BorderFactory.createLineBorder(Color.gray));
        board.setLayout(new GridLayout(rows, columns, 0, 0));
        getContentPane().add(board, BorderLayout.CENTER);
        
        createTiles(imagePath);
        paintPuzzle(board);
        support = new PropertyChangeSupport(tiles);
        setVisible(true);
    }

    
    private void createTiles(final String imagePath) {
		final BufferedImage image;

        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not load image", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final int imageWidth = image.getWidth(null);
        final int imageHeight = image.getHeight(null);

        int position = 0;

        final List<Integer> randomPositions = new ArrayList<>();
        IntStream.range(0, rows*columns).forEach(randomPositions::add);
        Collections.shuffle(randomPositions);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
            	final ImageIcon imagePortion = new ImageIcon( createImage(new FilteredImageSource(image.getSource(),
                        new CropImageFilter(j * imageWidth / columns,
                        					i * imageHeight / rows,
                        					(imageWidth / columns),
                        					imageHeight / rows))));
                tiles.add(new Tile(position, randomPositions.get(position)));
                imageTiles.add(imagePortion);
                position++;
            }
        }
	}

    public void paintPuzzle(final JPanel board) {
    	board.removeAll();

    	Collections.sort(tiles);

    	tiles.forEach(tile -> {
    		final TileButton btn = new TileButton(tile, imageTiles);
            board.add(btn);
            btn.setBorder(BorderFactory.createLineBorder(Color.gray));
            btn.addActionListener(actionListener -> selectionManager.selectTile(tile, () -> {
                paintPuzzle(board);
                support.firePropertyChange("tiles", List.of(tile), tiles);
                checkSolution();
            }));
    	});

    	pack();
    }

    private void checkSolution() {
    	if(tiles.stream().allMatch(Tile::isInRightPlace)) {
    		JOptionPane.showMessageDialog(this, "Puzzle Completed!", "", JOptionPane.INFORMATION_MESSAGE);
    	}
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public void setTiles(List<Tile> tiles) {
        this.tiles = tiles;
    }

    public void rePaintPuzzle(){
        paintPuzzle(board);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl){
        support.addPropertyChangeListener(pcl);
    }

    public List<ImageIcon> getTilesImages() { return this.imageTiles;}
}
