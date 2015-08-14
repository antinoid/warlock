package control;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import java.io.IOException;

/**
 *
 * @author d
 */
public class MoveControl implements Control {

    private Spatial spatial;
    private Vector3f target = null;
    
    public void setSpatial(Spatial spatial) {
        if(spatial != null) {
            this.spatial = spatial;
        }
    }

    public void setTarget(Vector3f target) {
        this.target = target;
    }
    
    public void update(float tpf) {
        if(target != null) {
            Vector3f v = spatial.getLocalTranslation();
            Vector3f vn;
            vn = v.interpolate(target, 1*tpf);
            spatial.setLocalTranslation(vn);
        }
    }

    public void render(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void write(JmeExporter ex) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void read(JmeImporter im) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
