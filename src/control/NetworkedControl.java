package control;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import java.io.IOException;
import network.messages.MoveMessage;

/**
 *
 * @author d
 */
public abstract class NetworkedControl implements Control {
    
    boolean enabled = true;
    private Client client;
    private Long entityId;
    private Vector3f lastTarget;
    
    public NetworkedControl() {        
    }
    
    public NetworkedControl(Client client, long entityId) {
        this.client = client;
        this.entityId = entityId;
    }
    
    //TODO syncManager contructor???
    
    public void setTarget(Vector3f target) {
        if(client != null && !target.equals(lastTarget)) {
            lastTarget = target;
            sendMoveSync();
        }
    }
    
    public abstract void moveToTarget(Vector3f target);
    
    public void sendMoveSync() {
        System.out.println("send move sync");
        System.out.println(entityId);
        System.out.println(lastTarget);
        System.out.println(client);
        client.send(new MoveMessage(entityId, lastTarget));
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    public void write(JmeExporter ex) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void read(JmeImporter im) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
