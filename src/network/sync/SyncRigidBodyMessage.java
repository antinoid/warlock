/*
 * Copyright (c) 2009-2011 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package network.sync;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Spatial;

/**
 * sync message for physics objects (RigidBody + Vehicle)
 * @author normenhansen
 */
@Serializable()
public class SyncRigidBodyMessage extends PhysicsSyncMessage {

    public Vector3f location;
    public Matrix3f rotation;
    public Vector3f linearVelocity;
    public Vector3f angularVelocity;

    public SyncRigidBodyMessage() {
    }

    public SyncRigidBodyMessage(long id, PhysicsRigidBody body) {
//        setReliable(false);
        this.syncId = id;
        location = body.getPhysicsLocation(new Vector3f());
        rotation = body.getPhysicsRotationMatrix(new Matrix3f());
        linearVelocity = new Vector3f();
        body.getLinearVelocity(linearVelocity);
        angularVelocity = new Vector3f();
        body.getAngularVelocity(angularVelocity);
    }

    public void readData(PhysicsRigidBody body) {
        location = body.getPhysicsLocation(new Vector3f());
        rotation = body.getPhysicsRotationMatrix(new Matrix3f());
        linearVelocity = new Vector3f();
        body.getLinearVelocity(linearVelocity);
        angularVelocity = new Vector3f();
        body.getAngularVelocity(angularVelocity);
    }

    public void applyData(Object body) {
        if (body == null) {
            return;
        }
        PhysicsRigidBody rigidBody = ((Spatial) body).getControl(RigidBodyControl.class);
        if (rigidBody == null) {
            rigidBody = ((Spatial) body).getControl(VehicleControl.class);
        }
        rigidBody.setPhysicsLocation(location);
        rigidBody.setPhysicsRotation(rotation);
        rigidBody.setLinearVelocity(linearVelocity);
        rigidBody.setAngularVelocity(angularVelocity);
    }
}
