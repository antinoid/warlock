package network.messages;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import main.WorldManager;
import network.sync.PhysicsSyncMessage;

/**
 *
 * @author d
 */
@Serializable()
public class AddEntityMessage extends PhysicsSyncMessage {
    public long id;
    public Vector3f location;
    public Quaternion rotation;
    
    public AddEntityMessage() {
    }
    
    public AddEntityMessage(long id, Vector3f location, Quaternion rotation) {
        //this.syncId = -1;
        this.id = id;
        this.location = location;
        this.rotation = rotation;
    }

    @Override
    public void applyData(Object obj) {
        WorldManager manager = (WorldManager) obj;
        manager.addEntity(id, location, rotation);
    }
}
