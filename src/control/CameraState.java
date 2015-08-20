package control;

import com.jme3.app.state.AbstractAppState;
import com.jme3.input.InputManager;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import main.ClientMain;
import main.PlayerData;
import main.WorldManager;

/**
 *
 * @author d
 */
public class CameraState extends AbstractAppState {

    private InputManager inputManager;
    private ClientMain app;
    private WorldManager worldManager;
    private Camera cam;
    private Spatial me;
    int width;
    int height;
    
    public CameraState(InputManager inputManager, ClientMain app, WorldManager worldManager) {
        this.inputManager = inputManager;
        this.app = app;
        this.worldManager = worldManager;
        cam = app.getCamera();
        width = app.getContext().getSettings().getWidth();
        height = app.getContext().getSettings().getHeight();
        me = worldManager.getEntity(PlayerData.getLongData(worldManager.getMyPlayerId(), "entity_id"));        
    }
    
    @Override
    public void update(float tpf) {
        me = worldManager.getEntity(PlayerData.getLongData(worldManager.getMyPlayerId(), "entity_id"));
        //System.out.println(me);
        //System.out.println("myid" + worldManager.getMyPlayerId());
        //Object object = worldManager.getEntity(width)
        Vector2f pos = inputManager.getCursorPosition();
        int border = 30;
        int speed = 0;
        if(pos.x >= width - border) {
            cam.setLocation(cam.getLocation().add(new Vector3f(tpf*speed, 0, 0)));
        }
        if(pos.x <= border) {
            cam.setLocation(cam.getLocation().add(new Vector3f(tpf*-speed, 0, 0)));
        }
        if(pos.y >= height - border) {
            cam.setLocation(cam.getLocation().add(new Vector3f(0, 0, tpf*-speed)));
        }
        if(pos.y <= border) {
            cam.setLocation(cam.getLocation().add(new Vector3f(0, 0, tpf*speed)));
        }
    }
}
