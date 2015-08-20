package main;

import com.jme3.network.serializing.Serializer;
import network.messages.AddEntityMessage;
import network.messages.ChatMessage;
import network.messages.ClientLoginMessage;
import network.messages.EnterEntityMessage;
import network.messages.MoveMessage;
import network.messages.AddPlayerMessage;
import network.messages.ServerLoginMessage;
import network.messages.RemovePlayerMessage;
import network.messages.StartGameMessage;
import network.messages.VectorMessage;
import network.sync.SyncCharacterMessage;

/**
 *
 * @author d
 */
public class Util {
    
    public static final Class[] CLIENT_SYNC_MESSAGES = {
        AddPlayerMessage.class,
        RemovePlayerMessage.class,
        MoveMessage.class,
        AddEntityMessage.class,
        EnterEntityMessage.class
    };    
    public static final Class[] SERVER_SYNC_MESSAGES = {
        AddPlayerMessage.class,
        MoveMessage.class,
        AddEntityMessage.class,
        EnterEntityMessage.class,
        SyncCharacterMessage.class
    }; 
    public static final Class[] CLIENT_MESSAGES = {
        VectorMessage.class,
        ClientLoginMessage.class,
        ServerLoginMessage.class,
        AddPlayerMessage.class,
        ChatMessage.class,
        RemovePlayerMessage.class,
        StartGameMessage.class
    };    
    public static final Class[] SERVER_MESSAGES = {
        VectorMessage.class,
        ClientLoginMessage.class,
        ServerLoginMessage.class,
        AddPlayerMessage.class,
        ChatMessage.class,
        RemovePlayerMessage.class,
        StartGameMessage.class
    };
 
    public static void registerSerializers() {
        Serializer.registerClass(VectorMessage.class);
        Serializer.registerClass(ClientLoginMessage.class);
        Serializer.registerClass(ServerLoginMessage.class);
        Serializer.registerClass(AddPlayerMessage.class);
        Serializer.registerClass(ChatMessage.class);
        Serializer.registerClass(RemovePlayerMessage.class);
        Serializer.registerClass(StartGameMessage.class);
        Serializer.registerClass(MoveMessage.class);
        Serializer.registerClass(AddEntityMessage.class);
        Serializer.registerClass(EnterEntityMessage.class);
        Serializer.registerClass(SyncCharacterMessage.class);
    }
}
