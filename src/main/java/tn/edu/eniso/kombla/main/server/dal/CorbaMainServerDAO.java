package tn.edu.eniso.kombla.main.server.dal;

import BomberManCorba.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import tn.edu.eniso.kombla.main.shared.model.DynamicGameModel;
import tn.edu.eniso.kombla.main.shared.model.StartGameInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * CORBA Implementation of MainServerDAO
 * This class implements the server-side CORBA communication for the BomberMan
 * game.
 * 
 * Architecture:
 * - Implements MainServerDAO interface for the game engine
 * - Creates a ServerCorba servant to handle client requests
 * - Registers the servant with the CORBA Naming Service
 * - Manages a list of connected clients (ClientCorba references)
 * - Broadcasts game state updates to all connected clients
 * - Handles player actions and notifies the game engine via listeners
 */
public class CorbaMainServerDAO implements MainServerDAO {
    private ORB orb;
    private MainServerDAOListener listener;
    private List<ClientCorba> connectedClients = new ArrayList<>();

    /**
     * Inner class implementing ServerCorba servant
     * This handles incoming requests from clients
     */
    class ServerCorbaImpl extends ServerCorbaPOA {
        private MainServerDAOListener listener;

        public ServerCorbaImpl(MainServerDAOListener listener) {
            this.listener = listener;
        }

        @Override
        public BomberManCorba.StartGameInfo connect(String playerName, ClientCorba client) {
            System.out.println("Player connecting: " + playerName);

            // Add client to the list for broadcasting
            synchronized (connectedClients) {
                connectedClients.add(client);
            }

            // Notify the engine that a player joined
            StartGameInfo javaInfo = listener.onReceivePlayerJoined(playerName);

            // Convert Java StartGameInfo to CORBA StartGameInfo
            return convertJavaToCorbaStartGameInfo(javaInfo);
        }

        @Override
        public void moveLeft(int playerId) {
            listener.onReceiveMoveLeft(playerId);
        }

        @Override
        public void moveRight(int playerId) {
            listener.onReceiveMoveRight(playerId);
        }

        @Override
        public void moveUp(int playerId) {
            listener.onReceiveMoveUp(playerId);
        }

        @Override
        public void moveDown(int playerId) {
            listener.onReceiveMoveDown(playerId);
        }

        @Override
        public void releaseBomb(int playerId) {
            listener.onReceiveReleaseBomb(playerId);
        }
    }

    @Override
    public void start(MainServerDAOListener listener, Map<String, Object> properties) {
        this.listener = listener;

        try {
            // Initialize the ORB
            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBInitialPort", "1050");
            props.put("org.omg.CORBA.ORBInitialHost", "localhost");

            // Create and initialize the ORB
            orb = ORB.init(new String[] {}, props);

            // Get reference to the root POA and activate it
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();

            // Create the ServerCorba servant
            ServerCorbaImpl serverImpl = new ServerCorbaImpl(listener);

            // Activate the servant and get the object reference
            org.omg.CORBA.Object ref = rootPOA.servant_to_reference(serverImpl);
            ServerCorba href = ServerCorbaHelper.narrow(ref);

            // Get the Naming Service reference
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // Bind the ServerCorba object in the Naming Service
            NameComponent[] path = ncRef.to_name("ServerCorba");
            ncRef.rebind(path, href);

            System.out.println("ServerCorba ready and waiting for clients...");

            // Start the ORB in a separate thread
            Thread orbThread = new Thread(() -> {
                orb.run();
            });
            orbThread.setDaemon(true);
            orbThread.start();

        } catch (Exception e) {
            System.err.println("Error starting ServerCorba: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendModelChanged(DynamicGameModel dynamicGameModel) {
        // Convert Java DynamicGameModel to CORBA DynamicGameModel
        BomberManCorba.DynamicGameModel corbaModel = convertJavaToCorbaModel(dynamicGameModel);

        // Broadcast to all connected clients
        synchronized (connectedClients) {
            List<ClientCorba> disconnectedClients = new ArrayList<>();

            for (ClientCorba client : connectedClients) {
                try {
                    client.modelChanged(corbaModel);
                } catch (Exception e) {
                    System.err.println("Error sending model to client, removing from list: " + e.getMessage());
                    disconnectedClients.add(client);
                }
            }

            // Remove disconnected clients
            connectedClients.removeAll(disconnectedClients);
        }
    }

    /**
     * Convert Java StartGameInfo to CORBA StartGameInfo
     */
    private BomberManCorba.StartGameInfo convertJavaToCorbaStartGameInfo(StartGameInfo javaInfo) {
        // Convert 2D array to CORBA sequence of sequences
        int[][] javaMaze = javaInfo.getMaze();
        int[][] corbaMaze = new int[javaMaze.length][];
        for (int i = 0; i < javaMaze.length; i++) {
            corbaMaze[i] = javaMaze[i].clone();
        }

        return new BomberManCorba.StartGameInfo(javaInfo.getPlayerId(), corbaMaze);
    }

    /**
     * Convert Java DynamicGameModel to CORBA DynamicGameModel
     */
    private BomberManCorba.DynamicGameModel convertJavaToCorbaModel(DynamicGameModel javaModel) {
        // Convert sprites
        List<net.vpc.gaming.atom.model.Sprite> javaSprites = javaModel.getSprites();
        SpriteData[] corbaSprites = new SpriteData[javaSprites.size()];

        for (int i = 0; i < javaSprites.size(); i++) {
            net.vpc.gaming.atom.model.Sprite js = javaSprites.get(i);
            corbaSprites[i] = new SpriteData(
                    js.getKind() != null ? js.getKind() : "",
                    js.getLocation() != null ? js.getLocation().getX() : 0.0,
                    js.getLocation() != null ? js.getLocation().getY() : 0.0,
                    js.getId());
        }

        // Convert players
        List<net.vpc.gaming.atom.model.Player> javaPlayers = javaModel.getPlayers();
        PlayerData[] corbaPlayers = new PlayerData[javaPlayers.size()];

        for (int i = 0; i < javaPlayers.size(); i++) {
            net.vpc.gaming.atom.model.Player jp = javaPlayers.get(i);
            corbaPlayers[i] = new PlayerData(
                    jp.getId(),
                    jp.getName() != null ? jp.getName() : "",
                    0, // score not available in this version
                    true); // alive - simplified
        }

        return new BomberManCorba.DynamicGameModel(
                javaModel.getFrame(),
                corbaSprites,
                corbaPlayers);
    }
}
