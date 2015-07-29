package network;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author d
 */
@Serializable
public class ClientLoginMessage extends AbstractMessage {
    public String name;
    public int version;
    //public String password;
    
    public ClientLoginMessage() {}
    public ClientLoginMessage(String name, int version) {
        this.name = name;
        this.version = version;
    }
}
