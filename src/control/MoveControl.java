package control;

import com.jme3.bullet.control.CharacterControl;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import java.io.IOException;
import main.Globals;

/**
 *
 * @author d
 */
public class MoveControl extends NetworkedControl {

    private Spatial spatial;
    private CharacterControl characterControl;
    private Vector3f walkDirection = new Vector3f(Vector3f.ZERO);
    private Vector3f viewDirection = new Vector3f(Vector3f.UNIT_Z);
    private Vector3f directionLeft = new Vector3f(Vector3f.UNIT_X);
    private Quaternion directionQuat = new Quaternion();
    private float walkAmount = 0;
    private float speed = 10f * Globals.PHYSICS_FPS;
    private Vector3f tempVec = new Vector3f();
    
    private Vector3f target = null;
    
    public MoveControl() {        
    }
    
    public MoveControl(Client client, long entityId) {
        super(client, entityId);
    }    

    public void setSpatial(Spatial spatial) {
        this.spatial = spatial;
        if(spatial == null) {
            return;
        }
        characterControl = spatial.getControl(CharacterControl.class);
        if(characterControl != null) {
            speed = 1;
        } else {
            throw new IllegalStateException("Cannot add MoveControl to Spatial without CharacterControl");
        }
    }

    
    public void moveToTarget(Vector3f target) {
        this.target = target;
    }
    
    public void update(float tpf) {
        if(target != null && enabled) {
            
            //update if sync changed direction
            if(!characterControl.getWalkDirection().equals(walkDirection) || !characterControl.getViewDirection().equals(viewDirection)) {
                walkDirection.set(characterControl.getWalkDirection());
                viewDirection.set(characterControl.getViewDirection()).normalizeLocal();;
            }            
            walkDirection.set(target.subtract(spatial.getLocalTranslation()).normalizeLocal());
            characterControl.setWalkDirection(walkDirection);
            characterControl.setViewDirection(viewDirection);
        }        
        /* old
        if(target != null && enabled) {
            Vector3f v = spatial.getLocalTranslation();
            Vector3f vn;
            vn = v.interpolate(target, 1*tpf);
            spatial.setLocalTranslation(vn);
        }*/
    }    

    public void render(RenderManager rm, ViewPort vp) {
    }
}
