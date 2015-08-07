package main;

import com.jme3.network.serializing.Serializer;
import network.messages.ChatMessage;
import network.messages.ClientLoginMessage;
import network.messages.ServerAddPlayerMessage;
import network.messages.ServerLoginMessage;
import network.messages.ServerRemovePlayerMessage;
import network.messages.StartGameMessage;
import network.messages.VectorMessage;

/**
 *
 * @author d
 */
public class Util {
    
    public static final Class[] CLIENT_SYNC_MESSAGES = {
        ServerAddPlayerMessage.class,
        ServerRemovePlayerMessage.class
    };    
    public static final Class[] SERVER_SYNC_MESSAGES = {
        ServerAddPlayerMessage.class
    }; 
    public static final Class[] CLIENT_MESSAGES = {
        VectorMessage.class,
        ClientLoginMessage.class,
        ServerLoginMessage.class,
        ServerAddPlayerMessage.class,
        ChatMessage.class,
        ServerRemovePlayerMessage.class,
        StartGameMessage.class
    };    
    public static final Class[] SERVER_MESSAGES = {
        VectorMessage.class,
        ClientLoginMessage.class,
        ServerLoginMessage.class,
        ServerAddPlayerMessage.class,
        ChatMessage.class,
        ServerRemovePlayerMessage.class,
        StartGameMessage.class
    };
 
    public static void registerSerializers() {
        Serializer.registerClass(VectorMessage.class);
        Serializer.registerClass(ClientLoginMessage.class);
        Serializer.registerClass(ServerLoginMessage.class);
        Serializer.registerClass(ServerAddPlayerMessage.class);
        Serializer.registerClass(ChatMessage.class);
        Serializer.registerClass(ServerRemovePlayerMessage.class);
        Serializer.registerClass(StartGameMessage.class);
    }
}
