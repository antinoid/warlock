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
import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.Server;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Box;
import control.MoveControl;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.messages.AddEntityMessage;
import network.messages.EnterEntityMessage;
import network.messages.MoveMessage;
import network.messages.AddPlayerMessage;
import network.messages.RemovePlayerMessage;
import network.sync.PhysicsSyncManager;

/**
 * entity managing class
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
    private int newId = -1;
    private long myPlayerId = -2;
    //private long myGroupId;
    private PhysicsSyncManager syncManager;
    private List<Control> userControls = new LinkedList();
    private Map map;
    //private MoveControl control = null;
    
    public WorldManager(Application app, Node rootNode, Server server) {
        this.app = (ServerMain) app;
        this.rootNode = rootNode;        
        this.assetManager = app.getAssetManager();
        this.server = server;
        this.syncManager = this.app.getStateManager().getState(PhysicsSyncManager.class);
    }
    
    public WorldManager(Application app, Node rootNode, Client client) {
        this.app = (ClientMain) app;
        this.rootNode = rootNode;
        this.assetManager = app.getAssetManager();
        this.inputManager = app.getInputManager();
        this.client = client;        
        this.syncManager = this.app.getStateManager().getState(PhysicsSyncManager.class);
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
        Camera cam = app.getCamera();
        cam.setFrustumPerspective(50f, cam.getWidth() / cam.getHeight(), 1f, 1000f);
        cam.setLocation(new Vector3f(0f, 0f, 10f));
        cam.lookAt(new Vector3f(0f, -10f, 0f), Vector3f.UNIT_Y);
            app.getFlyByCamera().setEnabled(true);
        }
    }
    
    public long getMyPlayerId() {
        return myPlayerId;
    }
    
    /*
    public long getMyGroupId() {
        return myGroupId;
    }*/

    public PhysicsSyncManager getSyncManager() {
        return syncManager;
    }
    
    public void setMyPlayerId(long id) {
        this.myPlayerId = id;
    }
    
    /*
    public void setMyGroupId(long id) {
        this.myGroupId = id;
    }*/
        
    private boolean isServer() {
        return server != null;
    }
     /**
     * adds a new player with new id (used on server only)
     * @param id
     * @param groupId
     * @param name
     */
    public long addNewPlayer(int groupId, String name) {
        long playerId = PlayerData.getNew(name);
        addPlayer(playerId, name);
        return playerId;
    }
    
     /**
     * adds a player (sends message if server)
     * @param id
     * @param groupId
     * @param name
     */
    public void addPlayer(long id, String name) {
        //Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Adding player: {0}", id);
        if(isServer()) {
            syncManager.broadcast(new AddPlayerMessage(id, name));
        } else {
            PlayerData newPlayer;
            newPlayer = new PlayerData(id, name);
            PlayerData.add(id, newPlayer);
            ClientMain cm = (ClientMain) app;
            cm.updateLobby();
        }
    }
    
    public void removePlayer(long id) {
        if(isServer()) {
            syncManager.broadcast(new RemovePlayerMessage(id));
            //long entityId = PlayerData.getLongData(id, "entity_id");
        }
        PlayerData.remove(id);
        if(!isServer()) {
            // TODO ugly app cast ?
            ClientMain cm = (ClientMain) app;
            cm.updateLobby();
        }
    }
    
    /**
     * gets entity by id
     * @param id
     * @return 
     */
    public Spatial getEntity(long id) {
        return entities.get(id);
    }
    
    /**
     * find id for entity
     * -1 if not found
     * @param entity
     * @return 
     */
    public long getEntityId(Spatial entity) {
        for(Iterator<Entry<Long, Spatial>> it = entities.entrySet().iterator(); it.hasNext();) {
            Entry<Long, Spatial> entry = it.next();
            if(entry.getValue() == entity) {
                return entry.getKey();
            }
        }
        return -1;
    }
    
    /**
     * add new entity (only used on server)
     * @param location
     * @param rotation
     * @return
     */
    public long addNewEntity(Vector3f location, Quaternion rotation) {
        newId++;
        addEntity(newId, location, rotation);
        return newId;
    }
    
    /**
     * add new entity (sends message if server)
     * @param id
     * @param location
     * @param rotation
     */
    public void addEntity(long id, Vector3f location, Quaternion rotation) {
        if(isServer()) {
            syncManager.broadcast(new AddEntityMessage(id, location, rotation));
        }
        // TODO temp, load real model
        Box boxMesh = new Box(1f,1f,1f);
        Spatial entity = new Geometry("Player", boxMesh); 
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        entity.setMaterial(mat); 
        entity.setUserData("player_id", id);
        //entity.setUserData("group_id", id);
        entity.setUserData("entity_id", id);
        entity.setLocalTranslation(location);
        entity.setLocalRotation(rotation);
        entities.put(id, entity);
        syncManager.addObject(id, entity);
        // TODO add ctrls to all
/*        System.out.println(PlayerData.getLongData(myPlayerId, "character_entity_id"));
        if(PlayerData.getLongData(myPlayerId, "character_entity_id") == id) {
            player = entity;
            player.addControl(new MoveControl());
        }*/
        rootNode.attachChild(entity);
    }
    
    /**
     * handle player entering entity (sends message if server)
     * @param playerId
     * @param entityId
     */
    public void enterEntity(long playerId, long entityId) {
        if(isServer()) {
            syncManager.broadcast(new EnterEntityMessage(playerId, entityId));
        }
        long curEntity = PlayerData.getLongData(playerId, "entity_id");
        //int groupId = PlayerData.getIntData(playerId, "group_id");
        // reset current entity
        if(curEntity != -1) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Player {0} exiting current entity {1}", new Object[]{playerId, curEntity});
            Spatial curEntitySpat = getEntity(curEntity);
            curEntitySpat.setUserData("player_id", -1l);
            //curEntitySpat.setUserData("group_id", -1);
            if (playerId == myPlayerId) {
                removeUserControls(curEntitySpat);
            
            }        
        }
        PlayerData.setData(playerId, "entity_id", entityId);
        // enter entity, add controls, -1 is no entity
        if(entityId != -1) {
            Spatial spat = getEntity(entityId);
            spat.setUserData("player_id", playerId);
            //spat.setUserData("group_id", groupId);
            spat.addControl(new MoveControl());
        }
    }
    
    /**
     * add user controls to spatial
     * @param spat
     */    
    public void addUserControls(Spatial spat) {
        for(Iterator<Control> it = userControls.iterator(); it.hasNext();) {
            Control control = it.next();
            spat.addControl(control);
        }
    }
    
    /**
     * remove user controls from spatial
     * @param spat
     */
    public void removeUserControls(Spatial spat) {
        for(Iterator<Control> it = userControls.iterator(); it.hasNext();) {
            Control control = it.next();
            spat.removeControl(control);
        }
    }
    
    public void loadMap() {
        // TODO Map ugly
        map = new Map(assetManager, rootNode);
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
                    //control.setTarget(pt);
                    client.send(new MoveMessage(myPlayerId, pt));                    
                }
            }            
        } else if(name.equals("test") && isPressed) {
            //System.out.println(myPlayerId);
            //System.out.println(myGroupId);
            for(Iterator<PlayerData> it = PlayerData.getPlayers().iterator(); it.hasNext();) {
                PlayerData playerData = it.next();
                System.out.println("name: " + playerData.getStringData("name"));
                System.out.println("entity id: : " + playerData.getLongData("entity_id"));
            }
        }
    }    
}
