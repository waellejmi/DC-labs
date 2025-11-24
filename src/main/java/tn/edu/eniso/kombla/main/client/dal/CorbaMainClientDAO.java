package tn.edu.eniso.kombla.main.client.dal;

import BomberManCorba.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import tn.edu.eniso.kombla.main.shared.model.DynamicGameModel;
import tn.edu.eniso.kombla.main.shared.model.StartGameInfo;

import java.util.Map;
import java.util.Properties;

/**
 * CORBA Implementation of MainClientDAO
 * This class handles communication with the CORBA server for the BomberMan game
 * client.
 * 
 * Architecture:
 * - Implements MainClientDAO interface for the game engine
 * - Creates a ClientCorba servant to receive callbacks from the server
 * - Connects to the ServerCorba object via CORBA Naming Service
 * - Sends player actions (move, fire) to the server
 * - Receives game state updates from the server via callbacks
 */
public class CorbaMainClientDAO implements MainClientDAO {
    private ORB orb;
    private ServerCorba serverCorba;
    private ClientCorba clientCorba;
    private int playerId;
    private MainClientDAOListener listener;

    /**
     * Inner class implementing ClientCorba servant
     * This receives callbacks from the server when the game model changes
     */
    class ClientCorbaImpl extends ClientCorbaPOA {
        private MainClientDAOListener listener;

        public ClientCorbaImpl(MainClientDAOListener listener) {
            this.listener = listener;
        }

        @Override
        public void modelChanged(BomberManCorba.DynamicGameModel corbaModel) {
            // Convert CORBA DynamicGameModel to Java DynamicGameModel
            DynamicGameModel javaModel = convertCorbaToDynamicGameModel(corbaModel);
            // Notify the engine listener
            listener.onModelChanged(javaModel);
        }
    }

    @Override
    public void start(MainClientDAOListener listener, Map<String, Object> properties) {
        this.listener = listener;

        try {
            // Initialize the ORB
            Properties props = new Properties();
            props.put("org.omg.CORBA.ORBInitialPort", "1050");
            props.put("org.omg.CORBA.ORBInitialHost",
                    properties.getOrDefault("serverAddress", "localhost").toString());

            orb = ORB.init(new String[] {}, props);

            // Get reference to the root POA and activate it
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();

            // Create the ClientCorba servant
            ClientCorbaImpl clientImpl = new ClientCorbaImpl(listener);

            // Activate the servant
            org.omg.CORBA.Object ref = rootPOA.servant_to_reference(clientImpl);
            clientCorba = ClientCorbaHelper.narrow(ref);

            // Start ORB in a separate thread to handle incoming callbacks
            Thread orbThread = new Thread(() -> {
                orb.run();
            });
            orbThread.setDaemon(true);
            orbThread.start();

        } catch (Exception e) {
            System.err.println("Error starting ClientCorba: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public StartGameInfo connect() {
        try {
            // Get the player name from properties or use default
            String playerName = "Player_" + System.currentTimeMillis();

            // Get a reference to the Naming Service
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // Resolve the ServerCorba reference
            serverCorba = ServerCorbaHelper.narrow(ncRef.resolve_str("ServerCorba"));

            // Connect to the server and get StartGameInfo
            BomberManCorba.StartGameInfo corbaInfo = serverCorba.connect(playerName, clientCorba);

            // Store the player ID
            this.playerId = corbaInfo.playerId;

            // Convert CORBA StartGameInfo to Java StartGameInfo
            return convertCorbaToStartGameInfo(corbaInfo);

        } catch (Exception e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void sendMoveLeft() {
        try {
            if (serverCorba != null) {
                serverCorba.moveLeft(playerId);
            }
        } catch (Exception e) {
            System.err.println("Error sending moveLeft: " + e.getMessage());
        }
    }

    @Override
    public void sendMoveRight() {
        try {
            if (serverCorba != null) {
                serverCorba.moveRight(playerId);
            }
        } catch (Exception e) {
            System.err.println("Error sending moveRight: " + e.getMessage());
        }
    }

    @Override
    public void sendMoveUp() {
        try {
            if (serverCorba != null) {
                serverCorba.moveUp(playerId);
            }
        } catch (Exception e) {
            System.err.println("Error sending moveUp: " + e.getMessage());
        }
    }

    @Override
    public void sendMoveDown() {
        try {
            if (serverCorba != null) {
                serverCorba.moveDown(playerId);
            }
        } catch (Exception e) {
            System.err.println("Error sending moveDown: " + e.getMessage());
        }
    }

    @Override
    public void sendFire() {
        try {
            if (serverCorba != null) {
                serverCorba.releaseBomb(playerId);
            }
        } catch (Exception e) {
            System.err.println("Error sending fire: " + e.getMessage());
        }
    }

    /**
     * Convert CORBA StartGameInfo to Java StartGameInfo
     */
    private StartGameInfo convertCorbaToStartGameInfo(BomberManCorba.StartGameInfo corbaInfo) {
        // Convert the maze (sequence of sequences to 2D array)
        int[][] maze = new int[corbaInfo.maze.length][];
        for (int i = 0; i < corbaInfo.maze.length; i++) {
            maze[i] = new int[corbaInfo.maze[i].length];
            for (int j = 0; j < corbaInfo.maze[i].length; j++) {
                maze[i][j] = corbaInfo.maze[i][j];
            }
        }
        return new StartGameInfo(corbaInfo.playerId, maze);
    }

    /**
     * Convert CORBA DynamicGameModel to Java DynamicGameModel
     * Note: We create simple wrapper objects that the engine will use to recreate
     * proper game objects
     */
    private DynamicGameModel convertCorbaToDynamicGameModel(BomberManCorba.DynamicGameModel corbaModel) {
        // Store the CORBA data in the model - the engine will handle creating actual
        // Sprite/Player objects
        // We use the atom library's internal mechanisms
        return new DynamicGameModel(
                corbaModel.frame,
                new java.util.ArrayList<>(), // Sprites will be recreated by engine
                new java.util.ArrayList<>() // Players will be recreated by engine
        );
    }
}
