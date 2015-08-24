package main;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.network.Network;
import com.jme3.network.NetworkClient;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import control.CameraState;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.controls.dynamic.TextCreator;
import de.lessvoid.nifty.controls.textfield.TextFieldControl;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.layout.align.HorizontalAlign;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import network.messages.ChatMessage;
import network.ClientListener;
import network.messages.ClientReadyMessage;
import network.messages.LoadGameMessage;
import network.sync.PhysicsSyncManager;

/**
 *
 * @author d
 */
public class ClientMain extends SimpleApplication implements ScreenController {
    
    private static ClientMain app;
    private NetworkClient client;
    private NiftyJmeDisplay niftyDisplay;
    private Nifty nifty;
    private WorldManager worldManager;
    private ClientListener clientListener;
    //private boolean connected = false;
    private PhysicsSyncManager syncManager;
    private TextField serverIpTextfield;
    private TextField portTextfield;
    private TextField nameTextfield;
    private TextRenderer statusText;
    private String name;
    private Label chatLabels[] = new Label[6];
    private int chatIndex = 0;
    private BulletAppState bulletState;
    private CameraState cameraState;
    
    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);               
        Util.registerSerializers();
        //settings.setFrameRate(Globals.FPS);
        //settings.setFullscreen(true);
        settings.setResolution(480, 360);
        app = new ClientMain();
        app.setSettings(settings);
        app.setPauseOnLostFocus(false);
        app.setDisplayFps(false);
        app.setDisplayStatView(false);
        //app.start();
        app.start(JmeContext.Type.Display);
    }    
    
    @Override
    public void simpleInitApp() {
        inputManager.setCursorVisible(true);
        flyCam.setEnabled(false);
        startNifty();
        client = Network.createClient();       
        
        bulletState = new BulletAppState();
        bulletState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
        stateManager.attach(bulletState);
        
        syncManager = new PhysicsSyncManager(app, client);
        syncManager.setMaxDelay(Globals.NETWORK_MAX_PHYSICS_DELAY);
        syncManager.setMessageTypes(Util.CLIENT_SYNC_MESSAGES);
        stateManager.attach(syncManager);
        
        worldManager = new WorldManager(app, rootNode, client);
        stateManager.attach(worldManager);
        
        syncManager.addObject(-1, worldManager);
        
        clientListener = new ClientListener(app, client, worldManager);
        client.addClientStateListener(clientListener);
        client.addMessageListener(clientListener, Util.CLIENT_MESSAGES);        
        
        cameraState = new CameraState(inputManager, app, worldManager);
        //stateManager.attach(new CameraState(inputManager, app, worldManager));
    }
    
    @Override
    public void simpleUpdate(float tpf) {
    }
    
    private void startNifty() {
        guiNode.detachAllChildren();
        niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
        nifty = niftyDisplay.getNifty();
        try {
            nifty.fromXml("Interface/screen.xml", "start", this);
        } catch(Exception e) {
            e.printStackTrace();
        }
        statusText = nifty.getScreen("start").findElementByName("foreground").findElementByName("loginpanel").findElementByName("loginbot").findElementByName("status_text").getRenderer(TextRenderer.class);
        statusText.setTextHAlign(HorizontalAlign.center);
        guiViewPort.addProcessor(niftyDisplay);
        //setStatusText("idle");
    }
    
    public void setStatusText(final String text) {
        enqueue(new Callable<Void>() {

            public Void call() throws Exception {
                statusText.setText(text);
                return null;
            }
        });
    }
    
    public String getPlayerName() {
        return System.getProperty("user.name");
    }
    
    /**
     * connect to server (called from gui)
     */
    public void login() {
        final String serverIp = nifty.getScreen("start").findElementByName("foreground").findElementByName("loginpanel").findElementByName("logintop").findElementByName("right").findElementByName("IPTextfield").getControl(TextFieldControl.class).getRealText();
        final int port = Integer.valueOf(nifty.getScreen("start").findElementByName("foreground").findElementByName("loginpanel").findElementByName("logintop").findElementByName("right").findElementByName("PortTextfield").getControl(TextFieldControl.class).getRealText());
        name = nifty.getScreen("start").findElementByName("foreground").findElementByName("loginpanel").findElementByName("logintop").findElementByName("right").findElementByName("NameTextfield").getControl(TextFieldControl.class).getRealText();
        if(name.trim().length() == 0) {
            setStatusText("invalid Username");
            // TODO check player names
            return;
        }
        clientListener.setName(name);
        setStatusText("logging in");
        try {
            client.connectToServer(serverIp, port, port);
            client.start();
        } catch (IOException ex) {
            setStatusText(ex.getMessage());
        }        
    }
    
    public void lobby() {
        nifty.gotoScreen("lobby");
    }
    
    /**
     * called from gui
     */
    public void sendChatMessage() {
        try {
            Element chatInput = nifty.getScreen("hud").findElementByName("layer").findElementByName("panel").findElementByName("input_panel").findElementByName("chat_textfield");
            String text = chatInput.getControl(TextFieldControl.class).getText();
            chatInput.getControl(TextFieldControl.class).setText("");
            ChatMessage msg = new ChatMessage(name + ": " + text);
            client.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * called from gui
     */
    public void startGame() {
        client.send(new LoadGameMessage());
    }
    
    public void loadLevel() {
        // TODO add loading screen
        final TextRenderer statusTextRenderer;// = nifty.getScreen("loading")

        new Thread(new Runnable() {

            public void run() {
                try {
                    enqueue(new Callable<Void>() {

                        public Void call() throws Exception {
                            //System.out.println("loading terrain");
                            //nifty.gotoScreen("load_level");
                            //statusText.setText("Loading Terrain..");
                            return null;
                        }
                    }).get();
                    worldManager.loadLevel("TODO");
                    enqueue(new Callable<Void>() {

                        public Void call() throws Exception {
                            worldManager.attachLevel();
                            nifty.gotoScreen("hud");      
                            return null;
                        }
                    }).get();
                    client.send(new ClientReadyMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        //stateManager.attach(worldManager);
        for(int i = 0; i < chatLabels.length; i++) {
            chatLabels[i] = nifty.getScreen("hud").findNiftyControl("chat_label" + (i+1), Label.class);
        }        
        stateManager.attach(cameraState);
    }
    @Override
    public void destroy() {
        try {
            client.close();
        } catch (Exception e) {}
        super.destroy();
    }
    
    @Deprecated
    public void updatePlayerData() {
        System.out.println("updating PlayerData");
        enqueue(new Callable<Void>() {

            public Void call() throws Exception {
                Screen screen = nifty.getScreen("lobby");
                Element panel = screen.findElementByName("foreground").findElementByName("panel").findElementByName("mid");
                List<PlayerData> players = PlayerData.getPlayers();
                
                for (Iterator<Element> it = new LinkedList<Element>(panel.getElements()).iterator(); it.hasNext();) {
                    Element element = it.next();
                    element.markForRemoval(); //TODO
                }
                TextCreator textCreator = new TextCreator("unknown player");
                textCreator.setStyle("list-player");
                for (Iterator<PlayerData> it = players.iterator(); it.hasNext();) {
                    //if(players.contains(cam))
                    PlayerData playerData = it.next();
                    textCreator.setText(playerData.getStringData("name"));
                    textCreator.create(nifty, screen, panel);
                }
                return null;
            }
        });
    }   

    public void updateLobby() {        
        enqueue(new Callable<Void>() {

            public Void call() throws Exception {
                Screen screen = nifty.getScreen("lobby");
                Element panel = screen.findElementByName("foreground").findElementByName("panel").findElementByName("mid");
                TextCreator textCreator = new TextCreator("unknown player");                
                textCreator.setStyle("list-player");
                
                List<Element> elements= panel.getElements();
                List<PlayerData> players = PlayerData.getPlayers();
                //check if text corresponds to left player and remove it
                for (Iterator<Element> it = elements.iterator(); it.hasNext();) {
                    Element element = it.next();
                    boolean mark = true;
                    for(Iterator<PlayerData> it1 = players.iterator(); it1.hasNext();) {
                        PlayerData playerData = it1.next();
                        if(playerData.getId() == Long.valueOf(element.getId())) {
                            mark = false;
                        }                        
                    }
                    if(mark)
                        element.markForRemoval();
                }
                 
                for (Iterator<PlayerData> it2 = players.iterator(); it2.hasNext();) {
                    PlayerData playerData = it2.next();
                    if(!elements.contains(panel.findElementByName(String.valueOf(playerData.getId())))) {
                        textCreator.setId(String.valueOf(playerData.getId()));
                        textCreator.setText(playerData.getStringData("name"));
                        textCreator.create(nifty, screen, panel);
                    }                    
                }        
                return null;
            }
        });
    }  
        
    public void updateChat(final String text) {        
        enqueue(new Callable<Void>() {

            public Void call() throws Exception {
                if(chatIndex < chatLabels.length) {
                    chatLabels[chatIndex].setText(text);
                    chatIndex++;
                } else {
                    for(int i = 0; i < chatLabels.length - 1; i++) {
                        chatLabels[i].setText(chatLabels[i+1].getText());
                    }
                    chatLabels[chatLabels.length - 1].setText(text);
                }
                return null;
            }
        });
    }
    
    public void bind(Nifty nifty, Screen screen) {
    }

    public void onStartScreen() {
    }

    public void onEndScreen() {
    }
}
