package main;

import com.jme3.network.serializing.Serializer;
import network.ClientLoginMessage;
import network.ServerAddPlayerMessage;
import network.ServerLoginMessage;
import network.VectorMessage;

/**
 *
 * @author d
 */
public class Util {

    public static void registerSerializers() {
        Serializer.registerClass(VectorMessage.class);
        Serializer.registerClass(ClientLoginMessage.class);
        Serializer.registerClass(ServerLoginMessage.class);
        Serializer.registerClass(ServerAddPlayerMessage.class);
    }
}
