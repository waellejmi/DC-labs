package tn.edu.eniso.kombla.main.server.dal;

import tn.edu.eniso.kombla.main.shared.model.DynamicGameModel;
import java.util.Map;

/**
 * UDP implementation placeholder - not used in CORBA version
 */
public class UDPMainServerDAO implements MainServerDAO {
    @Override
    public void start(MainServerDAOListener listener, Map<String, Object> properties) {
        System.err.println("UDP not implemented. Use Corba version.");
    }

    @Override
    public void sendModelChanged(DynamicGameModel dynamicGameModel) {
    }
}
