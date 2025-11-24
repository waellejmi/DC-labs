package tn.edu.eniso.kombla.main.server.dal;

import tn.edu.eniso.kombla.main.shared.model.DynamicGameModel;
import java.util.Map;

/**
 * TCP implementation placeholder - not used in CORBA version
 */
public class TCPMainServerDAO implements MainServerDAO {
    @Override
    public void start(MainServerDAOListener listener, Map<String, Object> properties) {
        System.err.println("TCP not implemented. Use Corba version.");
    }

    @Override
    public void sendModelChanged(DynamicGameModel dynamicGameModel) {
    }
}
