package part2.rmiPooling.puzzle;

import part2.common.Tile;

import javax.swing.*;

public class SelectionManager {

	private boolean selectionActive = false;
	private Tile selectedTile;

	public void selectTile(final Tile tile, final Listener listener, Timer t) {

		if (t.isRunning()){
			t.stop();
		} else {
			t.restart();
		}

		if(selectionActive) {
			selectionActive = false;

			swap(selectedTile, tile);

			listener.onSwapPerformed();
		} else {
			selectionActive = true;
			selectedTile = tile;
		}
	}

	private void swap(final Tile t1, final Tile t2) {
		int pos = t1.getCurrentPosition();
		t1.setCurrentPosition(t2.getCurrentPosition());
		t2.setCurrentPosition(pos);
	}

	@FunctionalInterface
	interface Listener {
		void onSwapPerformed() ;
	}
}

