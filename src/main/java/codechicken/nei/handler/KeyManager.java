package codechicken.nei.handler;

import java.util.LinkedList;

/**
 * Good old key ticker
 */
public class KeyManager {

    public interface IKeyStateTracker {

        void tickKeyStates();
    }

    public static LinkedList<IKeyStateTracker> trackers = new LinkedList<>();

    public static void tickKeyStates() {
        for (IKeyStateTracker tracker : trackers) {
            tracker.tickKeyStates();
        }
    }
}
