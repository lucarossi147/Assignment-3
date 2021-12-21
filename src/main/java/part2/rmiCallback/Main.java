package part2.rmiCallback;

import part2.rmiCallback.puzzle.PuzzleBoard;

import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

public class Main {
    public static String instanceName = "remoteInstance";

    public static void main(String[] args) {
        PuzzleBoard puzzle;
        int n = 2;
        int m = 2;

        try {
            Registry registry = LocateRegistry.getRegistry();
            if (Arrays.asList(registry.list()).contains(instanceName)) {
                System.out.println("Avviando il gioco come client...");
                puzzle = new PuzzleBoard(n, m, registry);
            } else {
                System.out.println("Avviando il gioco come master...");
                ServerRemote remoteInstance = new Server();
                ServerRemote stub = (ServerRemote) UnicastRemoteObject.exportObject(remoteInstance, 0);
                registry.rebind(instanceName, stub);
                puzzle = new PuzzleBoard(n, m, remoteInstance, registry);
            }
            puzzle.setVisible(true);
        } catch (ConnectException e){
            System.err.println("Impossible to connect");
            System.err.println("Try to start rmiregistry on classpath!");
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}