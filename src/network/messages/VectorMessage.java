package network.messages;

import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author d
 */
@Serializable
public class VectorMessage extends AbstractMessage {
    private Vector3f v;
    public VectorMessage() {}
    public VectorMessage(Vector3f v) { this.v = v; }
    public void setVector(Vector3f v) { this.v = v; }
    public Vector3f getVector() { return v; }
}
