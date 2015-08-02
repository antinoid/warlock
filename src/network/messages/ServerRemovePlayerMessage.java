package network.messages;

import com.jme3.network.serializing.Serializable;
import main.WorldManager;
import network.sync.PhysicsSyncMessage;

/**
 *
 * @author d
 */
@Serializable()
public class ServerRemovePlayerMessage extends PhysicsSyncMessage {

    public long playerId;
    
    public ServerRemovePlayerMessage() {}
    
    public ServerRemovePlayerMessage(long playerId) {
        this.syncId = -1;
        this.playerId = playerId;
    }
    @Override
    public void applyData(Object object) {
        WorldManager manager = (WorldManager) object;
        manager.removePlayer(playerId);
    }

}
