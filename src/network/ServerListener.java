package network;

import com.jme3.math.Vector3f;
import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Server;
import java.util.Iterator;
import java.util.concurrent.Callable;
import main.PlayerData;
import main.ServerMain;
import main.WorldManager;

/**
 *
 * @author d
 */

public class ServerListener implements MessageListener<HostedConnection>, ConnectionListener {
    
    ServerMain app;
    Server server;
    WorldManager worldManager;
    
    public ServerListener(ServerMain app, Server server, WorldManager worldManager) {
        this.app = app;
        this.server = server;
        //this.worldManager = app.getStateManager().getState(WorldManager.class);
        this.worldManager = worldManager;
        server.addConnectionListener(this);
        server.addMessageListener(this,
                VectorMessage.class,
                ClientLoginMessage.class,
                ServerLoginMessage.class,
                ServerAddPlayerMessage.class);
    }
    
    @Override
    public void connectionAdded(Server server, HostedConnection client) {
        int clientId = (int) client.getId();
        if(!ServerClientData.exists(clientId)) {
            ServerClientData.add(clientId);
            System.out.println("client connected");
            System.out.println("id: " + client.getId());
        } else {
            System.out.println("duplicate clientId");
        }
    }   
    
    @Override
    public void connectionRemoved(Server server, HostedConnection client) {
        final int clientId = (int) client.getId();
        final long playerId = ServerClientData.getPlayerId(clientId);
        ServerClientData.remove(clientId);
        /* TODO
        app.enqueue(new Callable<Void>() {

            public Void call() throws Exception {
                String name = PlayerData.getStringData(playerId, "name");
                worldManager.removePlayer(playerId);
                server.broadcast(new ChatMessage("Server", name + " left the game"));
                Logger.getLogger(ServerNetListener.class.getName()).log(Level.INFO, "Broadcast player left message");
                if (PlayerData.getHumanPlayers().isEmpty()) {
                    gameManager.stopGame();
                }
                return null;
            }
        });*/
     }
    
    public void messageReceived(HostedConnection source, Message message) {
        
        if ( message instanceof ClientLoginMessage) {
            System.out.println("ClientLoginMessage received");
            final ClientLoginMessage msg = (ClientLoginMessage) message;
            final int clientId = (int) source.getId();
            /* TEST
            if (!ServerClientData.exists(clientId)) {
                Logger.getLogger(ServerNetListener.class.getName()).log(Level.WARNING, "Receiving join message from unknown client");
                return;
            }*/
            final long newPlayerId = PlayerData.getNew(msg.name);
            ServerClientData.setConnected(clientId, true);
            ServerClientData.setPlayerId(clientId, newPlayerId);
            System.out.println("new Player: " + msg.name);
            // TODO broadcast new player
            ServerLoginMessage serverLoginMessage = new ServerLoginMessage(false, newPlayerId, clientId, msg.name);
            source.send(serverLoginMessage);
            // add player
            app.enqueue(new Callable<Void>() {

                public Void call() throws Exception {
                    //System.out.println("wM: " + worldManager);
                    System.out.println("Id: " + newPlayerId);
                    //System.out.println("msg.name: "+ msg.name);
                    worldManager.addPlayer(newPlayerId, clientId, msg.name);
                    PlayerData.setData(newPlayerId, "client_id", clientId);
                    for(Iterator<PlayerData> it = PlayerData.getPlayers().iterator(); it.hasNext();) {
                        PlayerData playerData = it.next();
                        if(playerData.getId() != newPlayerId) {
                            worldManager.getSyncManager().send(clientId, new ServerAddPlayerMessage(playerData.getId(), playerData.getIntData("client_id"), playerData.getStringData("name")));
                        }
                    }
                    return null;
                }
            });
        } else if ( message instanceof VectorMessage) {            
            System.out.println("VectorMessage received");
            VectorMessage msg = (VectorMessage) message;
            msg.setVector(new Vector3f(2, 2, 2));
            source.send(msg);
        } 
    }



 
}
