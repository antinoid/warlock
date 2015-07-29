package old;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioRenderer;
import com.jme3.input.InputManager;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.ViewPort;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import main.ClientMain;

/**
 *
 * @author d
 */
public class LoginScreen extends AbstractAppState implements ScreenController {

    private ClientMain app;
    private AssetManager assetManager;
    private InputManager inputManager;
    private AudioRenderer audioRenderer;
    private ViewPort guiViewPort;
    private NiftyJmeDisplay niftyDisplay;
    private Nifty nifty;
    private TextField serverIp;
    private TextField port;
    private TextField name;
    
    /*
    public LoginScreen(SimpleApplication app) {
        
        this.app = app;
        assetManager = app.getAssetManager();
        inputManager = app.getInputManager();
        audioRenderer = app.getAudioRenderer();
        guiViewPort = app.getGuiViewPort();
        
        niftyDisplay = new NiftyJmeDisplay(
                assetManager,
                inputManager,
                audioRenderer,
                guiViewPort);
        nifty = niftyDisplay.getNifty();
        System.out.println(nifty);
        //nifty.registerScreenController(this);
        
        nifty.fromXml("Interface/Screen4.xml", "start");
        guiViewPort.addProcessor(niftyDisplay);
    }*/
    
    @Override
     public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (ClientMain) app;       
        niftyDisplay = new NiftyJmeDisplay(
                this.app.getAssetManager(),
                this.app.getInputManager(),
                this.app.getAudioRenderer(),
                this.app.getGuiViewPort());
        
        niftyDisplay.getNifty().fromXml("Interface/screen4.xml", "start", this);
        this.app.getFlyByCamera().setEnabled(false);
        this.app.getInputManager().setCursorVisible(true);
        this.app.getGuiViewPort().addProcessor(niftyDisplay);
    }
  
    
    public void bind(Nifty nifty, Screen screen) {
        serverIp = screen.findNiftyControl("IPTextfield", TextField.class);
        port = screen.findNiftyControl("PortTextfield", TextField.class);
        name = screen.findNiftyControl("NameTextfield", TextField.class);
    }

    public void onStartScreen() {
    }

    public void onEndScreen() {
    }
    
  public void login() {
      if( app.connect(serverIp.getRealText(), Integer.valueOf(port.getRealText()), name.getRealText() ) ) {
        app.getGuiViewPort().removeProcessor(niftyDisplay);
        app.getStateManager().detach(this);
        //app.startGame();
      }
    }
}
