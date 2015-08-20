package main;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Basic class to store data about players (Human and AI), could be replaced by
 * a database or similar, static access with synchronization. Access is assured
 * to be sequential during the game, so in theory syncing is not needed. Used on
 * server and on client.
 * @author d
 */
public class PlayerData {

    private static HashMap<Long, PlayerData> players = new HashMap<Long, PlayerData>();
    private long id;
    private String name;
    private HashMap<String, Float> floatData = new HashMap<String, Float>();
    private HashMap<String, Integer> intData = new HashMap<String, Integer>();
    private HashMap<String, Long> longData = new HashMap<String, Long>();
    private HashMap<String, Boolean> booleanData = new HashMap<String, Boolean>();
    private HashMap<String, String> stringData = new HashMap<String, String>();
    
    public static synchronized long getNew(String name) {
        long id = 0;
        while (players.containsKey(id)) {
            id++;
        }
        players.put(id, new PlayerData(id, name));
        return id;
    }
        
    public static synchronized List<PlayerData> getPlayers() {
        LinkedList<PlayerData> list = new LinkedList<PlayerData>(players.values());
        return list;
    }
    
    public static synchronized HashMap getPlayersHash() {
        return players;
    }
    
    public static synchronized void add(long id, PlayerData player) {
        players.put(id, player);
    }
    
    public static synchronized void remove(long id) {
        players.remove(id);
    }
    
    public static synchronized float getFloatData(long id, String key) {
        if (!players.containsKey(id)) return -1;
        return players.get(id).getFloatData(key);
    }

    public static synchronized void setData(long id, String key, float data) {
        if (!players.containsKey(id)) return;
        players.get(id).setData(key, data);
    }

    public static synchronized int getIntData(long id, String key) {
        if (!players.containsKey(id)) return -1;
        return players.get(id).getIntData(key);
    }

    public static synchronized void setData(long id, String key, int data) {
        if (!players.containsKey(id)) return;
        players.get(id).setData(key, data);
    }

    public static synchronized long getLongData(long id, String key) {
        if (!players.containsKey(id)) return -1;
        return players.get(id).getLongData(key);
    }

    public static synchronized void setData(long id, String key, long data) {
        if (!players.containsKey(id)) return;
        players.get(id).setData(key, data);
    }

    public static synchronized boolean getBooleanData(long id, String key) {
        if (!players.containsKey(id)) return false;
        return players.get(id).getBooleanData(key);
    }

    public static synchronized void setData(long id, String key, boolean data) {
        if (!players.containsKey(id)) return;
        players.get(id).setData(key, data);
    }

    public static synchronized String getStringData(long id, String key) {
        if (!players.containsKey(id)) return "unknown";
        return players.get(id).getStringData(key);
    }

    public static synchronized void setData(long id, String key, String data) {
        if (!players.containsKey(id)) return;
        players.get(id).setData(key, data);
    }

    public PlayerData(long id) {
        this.id = id;
    }

    /**
     * Object implementation of PlayerData
     */
    public PlayerData(long id, String name) {
        this.id = id;
        setData("name", name);
        setData("entity_id", (long) -1);
    }

    /*
    public PlayerData(long id, int groupId, String name) {
        this.id = id;
        setData("group_id", groupId);
        setData("name", name);
        setData("entity_id", (long) -1);
        System.out.println("new PD3");
    }*/

    public long getId() {
        return id;
    }

    public float getFloatData(String key) {
        return floatData.get(key);
    }

    public void setData(String key, float data) {
        floatData.put(key, data);
    }

    public int getIntData(String key) {
        return intData.get(key);
    }

    public void setData(String key, int data) {
        intData.put(key, data);
    }

    public long getLongData(String key) {
        return longData.get(key);
    }

    public void setData(String key, long data) {
        longData.put(key, data);
    }

    public boolean getBooleanData(String key) {
        return booleanData.get(key);
    }

    public void setData(String key, boolean data) {
        booleanData.put(key, data);
    }

    public String getStringData(String key) {
        return stringData.get(key);
    }

    public void setData(String key, String data) {
        stringData.put(key, data);
    }
}
