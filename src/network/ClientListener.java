package network;

import network.messages.ServerLoginMessage;
import network.messages.AddPlayerMessage;
import network.messages.ClientLoginMessage;
import network.messages.ChatMessage;
import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import java.util.concurrent.Callable;
import main.ClientMain;
import main.Globals;
import main.WorldManager;
import network.messages.RemovePlayerMessage;
import network.messages.LoadGameMessage;

/**
 *
 * @author d
 */

public class ClientListener implements MessageListener<Client>, ClientStateListener {

    private ClientMain app;
    private Client client;
    private String name = "";
    private WorldManager worldManager;
    
    public ClientListener(ClientMain app, Client client, WorldManager worldManager) {
        this.app = app;
        this.client = client;
        this.worldManager = worldManager;
    }
    
    public void clientConnected(Client c) {
        setStatusText("connected - requesting login..");
        ClientLoginMessage msg = new ClientLoginMessage(name, Globals.VERSION);
        client.send(msg);
    }

    public void clientDisconnected(Client c, DisconnectInfo info) {
        System.out.println("kicked: " + info.reason);
    }
    
    @Override
    public void messageReceived(Client source, Message message) {
        if (message instanceof ServerLoginMessage) {
            final ServerLoginMessage msg = (ServerLoginMessage) message;
            if(!msg.rejected) {
                System.out.println("server accepted login");
                app.enqueue(new Callable<Void>() {
                    
                    public Void call() throws Exception {
                        worldManager.setMyPlayerId(msg.id);
                        //worldManager.setMyGroupId(msg.group_id);
                        app.lobby();
                        return null;
                    }
                });
            } else {
                System.out.println("server rejected login");
            }            
        } else if (message instanceof AddPlayerMessage) {
            app.updateLobby();
        } else if (message instanceof RemovePlayerMessage) {
            app.updateLobby();
        } else if (message instanceof LoadGameMessage) {
            LoadGameMessage msg = (LoadGameMessage) message;
            app.loadLevel();
        }
        else if (message instanceof ChatMessage) {
            ChatMessage msg = (ChatMessage) message;
            // FIXME port to lobby chat
            // crash when player leaves lobby
            app.updateChat(msg.getText());
        }
    }
    
    private void setStatusText(String text) {
        app.setStatusText(text);
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
