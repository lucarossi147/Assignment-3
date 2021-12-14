package part2.rmiPooling;


import part2.rmiPooling.puzzle.Tile;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class RemoteManagerImpl implements RemoteManager {
    List<Tile> tiles = new ArrayList<>();

    @Override
    public List<Tile> getTiles() throws RemoteException {
        //System.out.println("Returning tiles from remote obj");
        return tiles;
    }

    @Override
    public void setTiles(List<Tile> newTiles) throws RemoteException {
        if(tiles != newTiles){
            //System.out.println("Tiles are different... Setting tiles in remote obj");
            tiles = newTiles;
        }  // else { System.err.println("Tiles are same as before"); }

    }
}
