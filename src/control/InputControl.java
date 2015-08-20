package control;

import com.jme3.collision.CollisionResults;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import java.io.IOException;
import main.ClientMain;

/**
 *
 * @author d
 */
public class InputControl implements Control, ActionListener {

    private ClientMain app;
    private InputManager inputManager;
    private Node rootNode;
    private MoveControl control = null;
    private Spatial spatial = null;
    
    public InputControl(ClientMain app) {
        this.app = app;
        this.inputManager = app.getInputManager();
        this.rootNode = app.getRootNode();
        //spatial = 
        initKeys();
    }
    
    private void initKeys() {
        inputManager.addMapping("move", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("test", new KeyTrigger(KeyInput.KEY_F));
        inputManager.addListener(this,
                "move",
                "test");
    }
    
    // TODO
    
    public void setSpatial(Spatial spatial) {
        this.spatial = spatial;
        if(spatial == null) {
            control = null;
            return;
        }        
        control = spatial.getControl(MoveControl.class);
        if (control == null) {
            throw new IllegalStateException("Cannot add InputControl to spatial without ManualControl!");
        }
    }

    public void update(float tpf) {
    }
    
    public void onAction(String name, boolean isPressed, float tpf) {
        if(name.equals("move") && isPressed) {
            Camera cam = app.getCamera();
            CollisionResults results = new CollisionResults();
            Vector2f click2d = inputManager.getCursorPosition();
            Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
            Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
            Ray ray = new Ray(click3d, dir);
            rootNode.collideWith(ray, results);
            for (int i = 0; i < results.size(); i++) {
                float dist = results.getCollision(i).getDistance();
                Vector3f pt = results.getCollision(i).getContactPoint();
                String target = results.getCollision(i).getGeometry().getName();
                if(target.contains("terrainQuad")) {
                    control.setTarget(pt);
                }
            }            
        }
    }

    
    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void render(RenderManager rm, ViewPort vp) {
    }

    public void write(JmeExporter ex) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void read(JmeImporter im) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
