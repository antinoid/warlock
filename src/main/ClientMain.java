package main;

import com.jme3.app.SimpleApplication;
import com.jme3.network.Message;
import com.jme3.network.Network;
import com.jme3.network.NetworkClient;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.controls.textfield.TextFieldControl;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import network.ClientListener;
import network.ClientLoginMessage;
import network.ServerAddPlayerMessage;
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
    private Spatial player;
    //private boolean connected = false;
    private PhysicsSyncManager syncManager;
    private TextField serverIpTextfield;
    private TextField portTextfield;
    private TextField nameTextfield;
    private TextRenderer statusText;
    private String name;
    
    public static void main(String[] args) {
        AppSettings settings = new AppSettings(true);
        //settings.setFrameRate(Globals.FPS);
        //settings.setFullscreen(true);
        Util.registerSerializers();
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
        syncManager = new PhysicsSyncManager(app, client);
        syncManager.setMaxDelay(Globals.NETWORK_MAX_PHYSICS_DELAY);
        // TODO
        syncManager.setMessageTypes(ServerAddPlayerMessage.class);
        stateManager.attach(syncManager);
        worldManager = new WorldManager(app, rootNode, client);
        //stateManager.attach(new LoginScreen());
        //inputManager.addListener(new, mappingNames);
        //worldManager.addUserControl(new InputControl(inputManager, rootNode, cam));
        //listenerManager = new ClientListener();
        //stateManager.attach(new WorldManager(app, rootNode, client));
        /*
        stateManager.attach(new LoginScreen());
        Message message = new VectorMessage(new Vector2f(0, 0));
        try {
            client = Network.connectToServer(Globals.DEFAULT_SERVER, Globals.DEFAULT_PORT);
            client.start();
            client.addMessageListener(new ClientListener(),
                    VectorMessage.class);
            client.send(message);
        } catch (Exception e) {}*/
        //LoginScreen login = new LoginScreen(assetManager, inputManager, audioRenderer, guiViewPort);
        
        Map map = new Map(assetManager, rootNode);
        clientListener = new ClientListener(app, client, worldManager);
        syncManager.addObject(-1, worldManager);
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
    
    /**
     * connect to server (called from gui)
     */
    public void login() {
        System.out.println("asdfsdfsdf");
        final String serverIp = nifty.getScreen("start").findElementByName("foreground").findElementByName("loginpanel").findElementByName("logintop").findElementByName("right").findElementByName("IPTextfield").getControl(TextFieldControl.class).getRealText();
        final int port = Integer.valueOf(nifty.getScreen("start").findElementByName("foreground").findElementByName("loginpanel").findElementByName("logintop").findElementByName("right").findElementByName("PortTextfield").getControl(TextFieldControl.class).getRealText());
        final String name = nifty.getScreen("start").findElementByName("foreground").findElementByName("loginpanel").findElementByName("logintop").findElementByName("right").findElementByName("NameTextfield").getControl(TextFieldControl.class).getRealText();
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
    /*
    final String userName = nifty.getScreen("start ").
                findElementByName("foreground").
                findElementByName("loginpanel").
                findElementByName("logintop").
                findElementByName("right").
                findElementByName("NameTextfield").
                getControl(TextFieldControl.class).getText();*/    
    public boolean connect(String server, int port, String name) {
        try {
            //client = Network.connectToServer(server, port);
            client.connectToServer(server, port, port);       
            client.start();
            //Message loginMessage = new ClientLoginMessage(name);
            //client.send(loginMessage);
            return true;
        } catch(Exception e) {
            e.printStackTrace(System.err);
            System.out.println("unable to connect");
            return false;
        }
    }
    
    public void startGame() {        
        //guiViewPort.removeProcessor(niftyDisplay);
        //nifty.gotoScreen("asdf");
        try {
            nifty.fromXml("Interface/chat.xml", "chat", this);
        } catch(Exception e) {
            e.printStackTrace();
        }
        stateManager.attach(worldManager);
    }
    
    @Override
    public void destroy() {
        try {
            client.close();
        } catch (Exception e) {}
        super.destroy();
    }
    
    public void updatePlayerData() {
        System.out.println("updating PlayerData");
        enqueue(new Callable<Void>() {

            public Void call() throws Exception {
                List<PlayerData> players = PlayerData.getPlayers();
                //System.out.println("players:" + players);
                for (Iterator<PlayerData> it = players.iterator(); it.hasNext();) {
                    PlayerData data = it.next();
                    
                }
                return null;
            }
        });
    }   

    public void test() {
        System.out.println("TEEEST");
    }
    public void bind(Nifty nifty, Screen screen) {
       // serverIpTextfield = screen.findNiftyControl("IPTextfield", TextField.class);
        //portTextfield = screen.findNiftyControl("PortTextfield", TextField.class);
        //nameTextfield = screen.findNiftyControl("NameTextfield", TextField.class);
    }

    public void onStartScreen() {
    }

    public void onEndScreen() {
    }
}
