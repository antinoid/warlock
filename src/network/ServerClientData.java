/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import java.util.HashMap;

/**
 *
 * @author d
 */
public class ServerClientData {
    
    private static HashMap<Integer, ServerClientData> players = new HashMap<Integer, ServerClientData>();
    private int id;
    private long playerId;
    private boolean connected;
    
    public static synchronized void add(int id) {
        players.put(id, new ServerClientData(id));
    }

    public static synchronized void remove(int id) {
        players.remove(id);
    }

    public static synchronized boolean exists(int id) {
        return players.containsKey(id);
    }

    public static synchronized boolean isConnected(int id) {
        return players.get(id).isConnected();
    }

    public static synchronized void setConnected(int id, boolean connected) {
        players.get(id).setConnected(connected);
    }

    public static synchronized long getPlayerId(int id) {
        return players.get(id).getPlayerId();
    }

    public static synchronized void setPlayerId(int id, long playerId) {
        players.get(id).setPlayerId(playerId);
    }

    /**
     * Object implementation of ClientData
     */
    public ServerClientData(int id) {
        this.id = id;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}