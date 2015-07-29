package network;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author d
 */
@Serializable
public class ChatMessage extends AbstractMessage {
    private String text = "";
    private String player = "";
    public ChatMessage() {}
    public ChatMessage(String player) { this.player = player; }
    public ChatMessage(String text, String player) { 
        this.text = text; 
        this.player = player;
    }
    public String getText() { return text; }
    public String getPlayer() { return player; }
}
