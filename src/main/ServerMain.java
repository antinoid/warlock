package main;

import com.jme3.app.SimpleApplication;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.system.JmeContext;
import network.messages.ServerAddPlayerMessage;
import network.ServerListener;
import network.sync.PhysicsSyncManager;

/**
 *
 * @author d
 */
public class ServerMain extends SimpleApplication {
    
    private static ServerMain app;
    private Server server;
    private ServerListener serverListener;
    private WorldManager worldManager;
    private PhysicsSyncManager syncManager;
    
    public static void main(String[] args) {
        app = new ServerMain();
        Util.registerSerializers();
        app.start(JmeContext.Type.Headless);
    }
    
    public void simpleInitApp() {        
        try {
            server = Network.createServer(Globals.DEFAULT_PORT);
            server.start();
        } catch (Exception e) {}
        syncManager = new PhysicsSyncManager(app, server);
        syncManager.setMaxDelay(Globals.NETWORK_MAX_PHYSICS_DELAY);
        // TODO
        syncManager.setMessageTypes(Util.SERVER_SYNC_MESSAGES);
        stateManager.attach(syncManager);
        syncManager.addObject(-1, worldManager);
        worldManager = new WorldManager(app, rootNode, server);
        stateManager.attach(worldManager);
        serverListener = new ServerListener(app, server, worldManager);
        server.addConnectionListener(serverListener);
        server.addMessageListener(serverListener, Util.SERVER_MESSAGES);
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        //for(HostedConnection con : server.getConnections()) {
            
       
    }
    
    @Override
    public void destroy() {
        try {
            server.close();
        } catch (Exception e) {}
        super.destroy();
    }
}
