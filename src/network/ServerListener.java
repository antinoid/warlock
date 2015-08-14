package network;

import network.messages.VectorMessage;
import network.messages.ServerLoginMessage;
import network.messages.AddPlayerMessage;
import network.messages.ClientLoginMessage;
import network.messages.ChatMessage;
import com.jme3.math.Vector3f;
import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Server;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.PlayerData;
import main.ServerGameManager;
import main.ServerMain;
import main.WorldManager;
import network.messages.StartGameMessage;

/**
 *
 * @author d
 */
public class ServerListener implements MessageListener<HostedConnection>, ConnectionListener {
    
    ServerMain app;
    Server server;
    WorldManager worldManager;
    ServerGameManager gameManager;
    
    public ServerListener(ServerMain app, Server server, WorldManager worldManager, ServerGameManager gameManager) {
        this.app = app;
        this.server = server;
        this.worldManager = worldManager;
        this.gameManager = gameManager;
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
    public void connectionRemoved(Server serverr, HostedConnection client) {
        final int clientId = (int) client.getId();
        final long playerId = ServerClientData.getPlayerId(clientId);
        ServerClientData.remove(clientId);
        app.enqueue(new Callable<Void>() {

            public Void call() throws Exception {
                String name = PlayerData.getStringData(playerId, "name");
                worldManager.removePlayer(playerId);
                server.broadcast(new ChatMessage(name + " left the game"));                
                return null;
            }
        });
     }
    
    public void messageReceived(HostedConnection source, Message message) {
        
        if ( message instanceof ClientLoginMessage) {
            final ClientLoginMessage msg = (ClientLoginMessage) message;
            final int clientId = (int) source.getId();            
            if (!ServerClientData.exists(clientId)) {
                Logger.getLogger(ServerListener.class.getName()).log(Level.WARNING, "Receiving join message from unknown client");
                return;
            }
            final long newPlayerId = PlayerData.getNew(msg.name);
            ServerClientData.setConnected(clientId, true);
            ServerClientData.setPlayerId(clientId, newPlayerId);
            ServerLoginMessage serverLoginMessage = new ServerLoginMessage(false, newPlayerId, msg.name);
            source.send(serverLoginMessage);
            // add player
            app.enqueue(new Callable<Void>() {

                public Void call() throws Exception {
                    worldManager.addPlayer(newPlayerId, msg.name);
                    PlayerData.setData(newPlayerId, "client_id", clientId);
                    for(Iterator<PlayerData> it = PlayerData.getPlayers().iterator(); it.hasNext();) {
                        PlayerData playerData = it.next();
                        //worldManager.getSyncManager().send(clientId, new ServerAddPlayerMessage(playerData.getId(), playerData.getIntData("client_id"), playerData.getStringData("name")));
                        
                        if(playerData.getId() != newPlayerId) {
                            worldManager.getSyncManager().send(clientId, new AddPlayerMessage(playerData.getId(), playerData.getStringData("name")));
                            // TODO check
                            //server.getConnection(playerData.getIntData("client_id")).send(new ChatMessage(msg.name + " joined the game"));
                        }
                    }
                    return null;
                }
            });
        } else if (message instanceof StartGameMessage) {
            StartGameMessage msg = (StartGameMessage) message;
            app.enqueue(new Callable<Void>() {
                
                public Void call() {
                    gameManager.startGame();
                    return null;
                }
            });            
            server.broadcast(msg);
        } else if (message instanceof ChatMessage) {
            ChatMessage msg = (ChatMessage) message;
            server.broadcast(msg);
        } else if ( message instanceof VectorMessage) {
            VectorMessage msg = (VectorMessage) message;
            msg.setVector(new Vector3f(2, 2, 2));
            source.send(msg);
        }
    }
}
