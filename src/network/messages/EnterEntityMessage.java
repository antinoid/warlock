package network.messages;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import main.WorldManager;
import network.sync.PhysicsSyncMessage;

/**
 *
 * @author d
 */
@Serializable()
public class EnterEntityMessage extends PhysicsSyncMessage {
    public long playerId;
    public long entityId;
    
    public EnterEntityMessage() {
    }
    
    public EnterEntityMessage(long playerId, long entityId) {
        this.syncId = -1;
        this.playerId = playerId;
        this.entityId = entityId;
    }

    @Override
    public void applyData(Object obj) {
        WorldManager manager = (WorldManager) obj;
        manager.enterEntity(playerId, entityId);
    }
}
