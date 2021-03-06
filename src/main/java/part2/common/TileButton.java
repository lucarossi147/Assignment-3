package part2.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TileButton extends JButton {

	public TileButton(final Tile tile) {
		super(new ImageIcon(tile.getImage()));

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setBorder(BorderFactory.createLineBorder(Color.red));
			}
		});
	}
}
