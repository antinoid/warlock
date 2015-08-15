package control;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.input.InputManager;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 *
 * @author d
 */
public class CameraState extends AbstractAppState {

    private InputManager inputManager;
    SimpleApplication app;
    Camera cam;
    int width;
    int height;
    
    public CameraState(InputManager inputManager, SimpleApplication app) {
        this.inputManager = inputManager;
        this.app = app;
        cam = app.getCamera();
        width = app.getContext().getSettings().getWidth();
        height = app.getContext().getSettings().getHeight();
    }
    
    @Override
    public void update(float tpf) {
        Vector2f pos = inputManager.getCursorPosition();
        if(pos.x >= width - 30) {
            cam.setLocation(cam.getLocation().add(new Vector3f(tpf*25, 0, 0)));
        }
        if(pos.x <= 30) {
            cam.setLocation(cam.getLocation().add(new Vector3f(tpf*-25, 0, 0)));
        }
        if(pos.y >= height - 30) {
            cam.setLocation(cam.getLocation().add(new Vector3f(0, 0, tpf*-25)));
        }
        if(pos.y <= 30) {
            cam.setLocation(cam.getLocation().add(new Vector3f(0, 0, tpf*25)));
        }
    }
}
