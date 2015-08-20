package main;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import java.util.Iterator;
import network.sync.PhysicsSyncManager;

/**
 *
 * @author d
 */
public class ServerGameManager extends AbstractAppState {

    PhysicsSyncManager syncManager;
    WorldManager worldManager;
    private boolean running;
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {        
        super.initialize(stateManager, app);
        this.worldManager = app.getStateManager().getState(WorldManager.class);
        this.syncManager = worldManager.getSyncManager();
    }
    
    public synchronized void startGame() {
        worldManager.loadLevel("TODO");
        //worldManager.attachLevel();
        //FIXME player ready check!
        int i = 0;
        for(Iterator<PlayerData> it = PlayerData.getPlayers().iterator(); it.hasNext();) {
            PlayerData playerData = it.next();
            //long playerId = worldManager.addNewPlayer(playerData.getIntData("group_id"), 
            //        playerData.getStringData("name"));            
            long playerId = playerData.getId();
            long entityId = worldManager.addNewEntity(new Vector3f(i, -25, 0), Quaternion.ZERO);
            PlayerData.setData(playerId, "character_entity_id", entityId);
            worldManager.enterEntity(playerId, entityId);
            i++;
        }
    }
}
