package part3.akka.puzzle;

import part3.akka.Tile;

public class SelectionManager {

	private boolean selectionActive = false;
	private Tile selectedTile;

	public void selectTile(final Tile tile, final Listener listener) {
		
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
		int pos = t1.currentPosition();
		t1.currentPosition_(t2.currentPosition());
		t2.currentPosition_(pos);
	}
	
	@FunctionalInterface
	interface Listener{
		void onSwapPerformed();
	}
}
