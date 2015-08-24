package main;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.Server;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.scene.control.LightControl;
import control.InputControl;
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
    private Node worldRoot;
    private HashMap<Long, Spatial> entities = new HashMap<Long, Spatial>();
    private int newId = -1;
    private long myPlayerId = -2;
    //private long myGroupId;
    private PhysicsSyncManager syncManager;
    private List<Control> userControls = new LinkedList();
    private Map map;
    //private MoveControl control = null;
    private PhysicsSpace space;
    
    public WorldManager(Application app, Node rootNode, Server server) {
        this.app = (ServerMain) app;
        this.rootNode = rootNode;        
        this.assetManager = app.getAssetManager();
        this.server = server;
        this.syncManager = this.app.getStateManager().getState(PhysicsSyncManager.class);
        space = app.getStateManager().getState(BulletAppState.class).getPhysicsSpace();
    }
    
    public WorldManager(Application app, Node rootNode, Client client) {
        this.app = (ClientMain) app;
        this.rootNode = rootNode;
        this.assetManager = app.getAssetManager();
        this.inputManager = app.getInputManager();
        this.client = client;        
        this.syncManager = this.app.getStateManager().getState(PhysicsSyncManager.class);
        space = app.getStateManager().getState(BulletAppState.class).getPhysicsSpace();
    }        
    
    @Override
     public void initialize(AppStateManager stateManager, Application app) {
        if(!isServer()) {
            //addUserControls(player);
            super.initialize(stateManager, app);
            this.app = (ClientMain) app;            
            initCamera();
            initKeys();
        }
    }
    
    // debug stuff
    private void initKeys() {
        inputManager.addMapping("debug", new KeyTrigger(KeyInput.KEY_F));
        inputManager.addListener(this, "debug");
    }
    /*
    private void initKeys() {
        inputManager.addMapping("move", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        inputManager.addMapping("test", new KeyTrigger(KeyInput.KEY_F));
        inputManager.addListener(this, "move");
        inputManager.addListener(this, "test");
    }*/
    
    private void initCamera(){
        if(!isServer()) {
        Camera cam = app.getCamera();
        cam.setFrustumPerspective(45f, cam.getWidth() / cam.getHeight(), 10f, 1000f);
        cam.setLocation(new Vector3f(0f, 10f, 10f));
        cam.lookAt(new Vector3f(0f, -10f, 0f), Vector3f.UNIT_Y);
        app.getFlyByCamera().setEnabled(false);
        }
    }
        
    private boolean isServer() {
        return server != null;
    }
    
    public PhysicsSyncManager getSyncManager() {
        return syncManager;
    }
    
    public PhysicsSpace getPhysicSpace() {
        return space;
    }
    
    public long getMyPlayerId() {
        return myPlayerId;
    }
    
    public void setMyPlayerId(long id) {
        myPlayerId = id;
    }    
    
    /*
    public long getMyGroupId() {
        return myGroupId;
    }*/
    
    /*
    public void setMyGroupId(long id) {
        this.myGroupId = id;
    }*/
    
    /**
     * loads specified level node
     * @param name 
     */
    public void loadLevel(String name) {
        Logger.getLogger(WorldManager.class.getName()).log(Level.INFO, "loading level", worldRoot);            
        Map map = new Map(assetManager, name);
        worldRoot = new Node("world Root");
        worldRoot.attachChild(map.getTerrain());
    }
    
    /**
     * detach level and clear cache
     */
    public void closeLevel() {
        for(Iterator<PlayerData> it = PlayerData.getPlayers().iterator(); it.hasNext();) {
            PlayerData playerData = it.next();
            playerData.setData("entity_id", -1l);
        }
        for (Iterator<Long> it = entities.keySet().iterator(); it.hasNext();) {
            Long entry = it.next();
            syncManager.removeObject(entry);
        }
        syncManager.clearObjects();
        entities.clear();
        newId = -1;
        space.removeAll(worldRoot);
        rootNode.detachChild(worldRoot);
        ((DesktopAssetManager) assetManager).clearCache();
    }
    
    /**
     * attach level node to rootNode
     */
    public void attachLevel() {
        space.addAll(worldRoot);
        AmbientLight al = new AmbientLight();
        DirectionalLight sun = new DirectionalLight();
        al.setColor(ColorRGBA.White);        
        sun.setColor(ColorRGBA.White);
        sun.setDirection(new Vector3f(-.5f,-.5f,-.5f).normalizeLocal());
        rootNode.addLight(al);
        rootNode.addLight(sun);
       // rootNode.addLight(al);
        rootNode.attachChild(worldRoot);
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
    
    /**
     * removes a player (sends message if server)
     * @param id 
     */
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
        /*
        Box boxMesh = new Box(2f,2f,2f);
        Spatial entity = new Geometry("Player", boxMesh);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        entity.setMaterial(mat);*/
        Spatial entity = assetManager.loadModel("Models/Blender/aphrodite/SM_Aphrodite.j3o");
        entity.setLocalScale(0.12f);
        PointLight myLight = new PointLight();
        myLight.setColor(ColorRGBA.White);
        rootNode.addLight(myLight);
        LightControl lightControl = new LightControl(myLight);
        entity.addControl(lightControl);
        // Create collision shape for entity
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
        
        entity.addControl(new CharacterControl(capsuleShape, 0.01f));
        entity.setUserData("player_id", -1l);
        //entity.setUserData("group_id", -1);
        entity.setUserData("entity_id", id);
        entity.setLocalTranslation(location);
        entity.setLocalRotation(rotation);        
        entities.put(id, entity);
        syncManager.addObject(id, entity);
        space.addAll(entity);
        //System.out.println(worldRoot);
        //System.out.println(entity);
        worldRoot.attachChild(entity);
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
            if(!isServer()) {
                makeControl(entityId, client);
                //System.out.println("ctrl for " + entityId);
            } else {
                makeControl(entityId, null);
            }
            if(playerId == myPlayerId) {
                // addUserControls
                spat.addControl(new InputControl((ClientMain) app));
            }
        }
    }
    
    /**
     * 
     */
    private void makeControl(long entityId, Client client) {
        Spatial spat = getEntity(entityId);
        if(spat.getControl(CharacterControl.class) != null) {
            if((Long) spat.getUserData("player_id") == myPlayerId) {
                spat.addControl(new MoveControl(client, entityId));
            } else {
                spat.addControl(new MoveControl());
            }
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
        if(name.equals("debug") && isPressed) {
            System.out.println("myPlayerId: " + myPlayerId);
            System.out.println("entities: " + entities);
            System.out.println("sync Objects: " + syncManager.syncObjects);
            System.out.println("getPlayers: " + PlayerData.getPlayers());
            
        }
    }    
}
