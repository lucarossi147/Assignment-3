package part2.rmiPooling;

import part2.rmiPooling.puzzle.PuzzleBoard;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        final int n = 3;
        final int m = 5;

        final String OBJECT = "remoteInstance";

        try {
            Registry registry = LocateRegistry.getRegistry();
            if (Arrays.asList(registry.list()).contains(OBJECT)) {
                //Sono il "client" e ottengo la copia remota dal registry
                System.out.println("Avviando il gioco come client...");
                PuzzleBoard puzzle = new PuzzleBoard(n, m, registry);
                puzzle.setVisible(true);
            } else { //Sono il "master" e creo la copia remota
                System.out.println("Avviando il gioco come master...");
                ServerRemote remoteInstance = new Server();
                ServerRemote stub = (ServerRemote) UnicastRemoteObject.exportObject(remoteInstance, 0);
                registry.rebind(OBJECT, stub);
                PuzzleBoard puzzle = new PuzzleBoard(n, m, remoteInstance, registry);
                puzzle.setVisible(true);
            }

        } catch (ConnectException e){
            System.err.println("Impossible to connect");
            System.err.println("Try to start rmiregistry on classpath!");
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        /*
        Certe volte capita che il puzzle sbarelli, che le tessere cambino posizione in modo imprevisto...
        Questo è dovuto al fatto che viene serializzato un oggetto complesso come Tile e avvengono errori di rete..
        IO mi prendo la possibilità di questi errori, perchè se sbaglia il multiplayer di Fifa chi sono io per farlo corretto

        Non vengono notificati i giocatori del fatto che la partita sia finita

        Fare diverse prove e cercare di fare funzionare se il master si disconette...

         */
    }
}