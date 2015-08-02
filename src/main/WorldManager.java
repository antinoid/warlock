package main;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResults;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.Server;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import java.util.Iterator;
import network.messages.ServerAddPlayerMessage;
import network.messages.ServerRemovePlayerMessage;
import network.messages.VectorMessage;
import network.sync.PhysicsSyncManager;

/**
 *
 * @author d
 */
public class WorldManager extends AbstractAppState implements ActionListener, AnalogListener {

    private Server server;
    private Client client;
    private SimpleApplication app;
    private AssetManager assetManager;
    private InputManager inputManager;
    private Node rootNode;
    private HashMap<Long, Spatial> entities = new HashMap<Long, Spatial>();
    private long myPlayerId;
    private long myGroupId;
    private PhysicsSyncManager syncManager;
    
    public WorldManager(Application app, Node rootNode, Server server) {
        this.app = (ServerMain) app;
        this.rootNode = rootNode;        
        this.assetManager = app.getAssetManager();
        this.server = server;
        syncManager = this.app.getStateManager().getState(PhysicsSyncManager.class);
    }
    
    public WorldManager(Application app, Node rootNode, Client client) {
        this.app = (ClientMain) app;
        this.rootNode = rootNode;
        this.assetManager = app.getAssetManager();
        this.inputManager = app.getInputManager();
        this.client = client;
        /*                
        Box b = new Box(1f,1f,1f); 
        player = new Geometry("Colored Box", b); 
        Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"); 
        boxMat.setColor("Color", ColorRGBA.Red);         
        player.setMaterial(boxMat); 
        rootNode.attachChild(player);*/
    }        
    
    @Override
     public void initialize(AppStateManager stateManager, Application app) {
        if(!isServer()) {
            //addUserControls(player);
            super.initialize(stateManager, app);
            this.app = (ClientMain) app;
            FlyByCamera flyCam = this.app.getFlyByCamera();
            flyCam.setMoveSpeed(100);
            
            initCamera();
            initKeys();
        }
    }
     
    private void initKeys() {
        inputManager.addMapping("move", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("test", new KeyTrigger(KeyInput.KEY_F));
        inputManager.addListener(this, "move");
        inputManager.addListener(this, "test");
    }
    
    private void initCamera(){
        if(!isServer()) {
        /*Camera cam = app.getCamera();
        cam.setFrustumPerspective(90f, (float)cam.getWidth() / cam.getHeight(), 1f, 1000f);
        cam.setLocation(new Vector3f(0f, 0f, 10f));
        cam.lookAt(new Vector3f(0f, -10f, 0f), Vector3f.UNIT_Y);*/
            app.getFlyByCamera().setEnabled(true);
        }
    }
    
    public long getMyPlayerId() {
        return myPlayerId;
    }
    
    public long getMyGroupId() {
        return myGroupId;
    }

    public PhysicsSyncManager getSyncManager() {
        return syncManager;
    }
    
    public void setMyPlayerId(long id) {
        this.myPlayerId = id;
    }
    
    public void setMyGroupId(long id) {
        this.myGroupId = id;
    }
        
    private boolean isServer() {
        return server != null;
    }
     /**
     * adds a new player with new id (used on server only)
     * @param id
     * @param groupId
     * @param name
     * @param aiId
     */
    public long addNewPlayer(int groupId, String name) {
        long playerId = PlayerData.getNew(name);
        addPlayer(playerId, groupId, name);
        return playerId;
    }
    
     /**
     * adds a player (sends message if server)
     * @param id
     * @param groupId
     * @param name
     * @param aiId
     */
    public void addPlayer(long id, int groupId, String name) {
        //Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Adding player: {0}", id);
        if(isServer()) {
            syncManager.broadcast(new ServerAddPlayerMessage(id, groupId, name));
        }
        PlayerData player = null;
        player = new PlayerData(id, groupId, name);
        PlayerData.add(id, player);
        if(!isServer()) {
            ClientMain cm = (ClientMain) app;
            cm.updateLobby();
        }
    }
    
    public void removePlayer(long id) {
        if(isServer()) {
            syncManager.broadcast(new ServerRemovePlayerMessage(id));
            //long entityId = PlayerData.getLongData(id, "entity_id");
        }
        PlayerData.remove(id);
        if(!isServer()) {
            ClientMain cm = (ClientMain) app;
            cm.updateLobby();
        }
    }
    /*
    public void addUserControl(Control control) {
        userControls.add(control);
    }    
    
    private void addUserControls(Spatial spat) {
        for (Iterator<Control> it = userControls.iterator(); it.hasNext();) {
            Control control = it.next();
            spat.addControl(control);
        }
    }*/
    
    @Override
    public void onAnalog(String name, float value, float tpf) {
        if(name.equals("asd")) {            
        }
     }
    
    @Override
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
                    client.send(new VectorMessage(pt));
                }
            }            
        } else if(name.equals("test") && isPressed) {
            //System.out.println(myPlayerId);
            //System.out.println(myGroupId);
            for(Iterator<PlayerData> it = PlayerData.getPlayers().iterator(); it.hasNext();) {
                PlayerData playerData = it.next();
                System.out.println("name: " + playerData.getStringData("name"));
            }
        }
    }    
}
