package network;

import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import java.util.concurrent.Callable;
import main.ClientMain;
import main.Globals;
import main.WorldManager;

/**
 *
 * @author d
 */

public class ClientListener implements MessageListener<Client>, ClientStateListener {

    private ClientMain app;
    private Client client;
    private String name = "";
    private WorldManager worldManager;
    
    //public ClientListener newClientListener(ClientMain app);
    public ClientListener(ClientMain app, Client client, WorldManager worldManager) {
        this.app = app;
        this.client = client;
        this.worldManager = worldManager;
        client.addClientStateListener(this);
        client.addMessageListener(this,
                VectorMessage.class,
                ClientLoginMessage.class,
                ServerLoginMessage.class,
                ServerAddPlayerMessage.class,
                ChatMessage.class);
                
    }
    
    @Override
    public void messageReceived(Client source, Message message) {
        if (message instanceof ServerLoginMessage) {
            //System.out.println("ServerLoginMessage received");
            final ServerLoginMessage msg = (ServerLoginMessage) message;
            if(!msg.rejected) {
                System.out.println("server accepted login");
                app.enqueue(new Callable<Void>() {
                    
                    public Void call() throws Exception {
                        worldManager.setMyPlayerId(msg.id);
                        worldManager.setMyGroupId(msg.group_id);
                        app.startGame();
                        return null;
                    }
                });
            } else {
                System.out.println("server rejected login");
            }
                
            
        } else if (message instanceof ServerAddPlayerMessage) {
            app.updatePlayerData();
        } else if (message instanceof VectorMessage) {            
            //System.out.println("VectorMessage received");
            VectorMessage vectorMessage = (VectorMessage) message;
            //System.out.println(vectorMessage.getVector());
            //app.updatePlayerData();
        } else if (message instanceof ChatMessage) {
            ChatMessage msg = (ChatMessage) message;
            app.updateChat(msg.getText(), msg.getPlayer());
        }
    }

    public void clientConnected(Client c) {
        setStatusText("connected - requesting login..");
        ClientLoginMessage msg = new ClientLoginMessage(name, Globals.VERSION);
        client.send(msg);
    }

    public void clientDisconnected(Client c, DisconnectInfo info) {
        System.out.println("kicked: " + info.reason);
    }
    
    private void setStatusText(String text) {
        app.setStatusText(text);
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
