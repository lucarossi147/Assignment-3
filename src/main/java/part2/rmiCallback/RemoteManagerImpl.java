package part2.rmiCallback;


import part2.rmiCallback.puzzle.Tile;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RemoteManagerImpl implements RemoteManager {
    List<Tile> tiles = new ArrayList<>();
    List<CallbackRemote> clients = new ArrayList<>();

    @Override
    public List<Tile> getTiles() throws RemoteException {
        return tiles;
    }

    @Override
    public void setTiles(List<Tile> newTiles) throws RemoteException {
        if(tiles != newTiles){
            tiles = newTiles;
            notifyClients();
        }
    }

    @Override
    public void registerClient(CallbackRemote c) throws RemoteException {
        if(!clients.contains(c)){
            clients.add(c);
            System.out.println("Added a new client!");
        }
    }

    @Override
    public void unregisterClient(CallbackRemote c) throws RemoteException {
        if(clients.remove(c)){
            System.out.println("Removed client");
        } else {
            System.out.println("Probably this client isn't registered");
        }
    }

    @Override
    public void notifyClients() throws RemoteException {
        for (CallbackRemote client : clients) {
            client.update();
        }
    }
}
