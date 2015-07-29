package network;

import com.jme3.math.Vector2f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author d
 */
@Serializable
public class ChatMessage extends AbstractMessage {
    private String message;
    private String player;
    public ChatMessage() {}
    public ChatMessage(String s) { message = s; }
    public void setMessage(String s) { message = s; }
    public String getMessage() { return message; }
}
