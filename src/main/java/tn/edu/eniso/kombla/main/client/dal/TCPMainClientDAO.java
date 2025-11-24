package tn.edu.eniso.kombla.main.client.dal;

import tn.edu.eniso.kombla.main.shared.model.StartGameInfo;
import java.util.Map;

/**
 * TCP implementation placeholder - not used in CORBA version
 */
public class TCPMainClientDAO implements MainClientDAO {
    @Override
    public void start(MainClientDAOListener listener, Map<String, Object> properties) {
        System.err.println("TCP not implemented. Use Corba version.");
    }

    @Override
    public StartGameInfo connect() {
        return null;
    }

    @Override
    public void sendMoveLeft() {
    }

    @Override
    public void sendMoveRight() {
    }

    @Override
    public void sendMoveUp() {
    }

    @Override
    public void sendMoveDown() {
    }

    @Override
    public void sendFire() {
    }
}
