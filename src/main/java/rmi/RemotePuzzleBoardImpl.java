package rmi;

import rmi.puzzle.Tile;

import java.rmi.RemoteException;
import java.util.List;

public class RemotePuzzleBoardImpl implements RemotePuzzleBoard {
    @Override
    public List<Tile> getBoard() throws RemoteException {
        return null;
    }
}
