package network.messages;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Spatial;
import control.MoveControl;
import network.sync.PhysicsSyncMessage;

/**
 *
 * @author d
 */
@Serializable()
public class MoveMessage extends PhysicsSyncMessage {

    public Vector3f target;
    
    public MoveMessage() {        
    }
    
    public MoveMessage(long id, Vector3f target) {
        this.syncId = id;
        this.target = target;
    }
    
    @Override
    public void applyData(Object object) {
        MoveControl control = ((Spatial)object).getControl(MoveControl.class);
        control.setTarget(target);
    }
}
