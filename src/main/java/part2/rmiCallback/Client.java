package part2.rmiCallback;

import part2.rmiCallback.puzzle.PuzzleBoard;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

public class Client {
    public static void main(String[] args) {
        PuzzleBoard puzzle;
        int n = 3;
        int m = 5;
        String OBJECT = "remoteInstance";


        try {
            Registry registry = LocateRegistry.getRegistry();
            if (Arrays.asList(registry.list()).contains(OBJECT)) {
                //Sono il "client" e ottengo la copia remota dal registry
                System.out.println("Avviando il gioco come client...");
                puzzle = new PuzzleBoard(n, m, registry);
            } else { //Sono il "master" e creo la copia remota
                System.out.println("Avviando il gioco come master...");
                RemoteManager remoteInstance = new RemoteManagerImpl();
                RemoteManager stub = (RemoteManager) UnicastRemoteObject.exportObject(remoteInstance, 0);
                registry.rebind(OBJECT, stub);
                puzzle = new PuzzleBoard(n, m, remoteInstance, registry);
            }
            puzzle.setVisible(true);

        } catch (ConnectException e){
            System.err.println("Impossible to connect");
            System.err.println("Try to start rmiregistry on classpath!");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO funziona in 2, però se si disconnette il master occorre anche che ci si registri di nuovo come callback!
     *
     */

}