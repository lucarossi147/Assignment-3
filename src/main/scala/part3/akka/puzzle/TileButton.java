package part3.akka.puzzle;

import part3.akka.Tile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

@SuppressWarnings("serial")
public class TileButton extends JButton{

	public TileButton(final Tile tile, final List<ImageIcon> images) {
		super( images.get(tile.currentPosition()));
		
		addMouseListener(new MouseAdapter() {            
            @Override
            public void mouseClicked(MouseEvent e) {
            	setBorder(BorderFactory.createLineBorder(Color.red));
            }
        });
	}
}
