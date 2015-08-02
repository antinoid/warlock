package main;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import network.sync.PhysicsSyncManager;

/**
 *
 * @author d
 */
public class ServerGameManager extends AbstractAppState {

    PhysicsSyncManager syncManager;
    WorldManager worldManager;
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        
        super.initialize(stateManager, app);
        this.worldManager = app.getStateManager().getState(WorldManager.class);
        this.syncManager = worldManager.getSyncManager();
    }
}
