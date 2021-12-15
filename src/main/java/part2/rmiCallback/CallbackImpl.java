package part2.rmiCallback;

import java.io.Serializable;
import java.rmi.RemoteException;

public class CallbackImpl implements CallbackRemote, Serializable {

    Runnable r;

    public CallbackImpl(Runnable r) {
        this.r = r;
    }

    @Override
    public void update() throws RemoteException {
        r.run();
    }

}
