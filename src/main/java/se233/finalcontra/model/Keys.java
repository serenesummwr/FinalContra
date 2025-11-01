package se233.finalcontra.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javafx.scene.input.KeyCode;

public class Keys {
    private HashMap<KeyCode, Boolean> keys;
    private Set<KeyCode> pressedKeys;
    public Keys() {
        keys = new HashMap<>();
        pressedKeys = new HashSet<>();
    }

    public void add(KeyCode key) {
        keys.put(key, true);
    }
    
    public void addPressed(KeyCode key) {
    	pressedKeys.add(key);
    }

    public void remove(KeyCode key) {
        keys.put(key, false);
    }

    public boolean isPressed(KeyCode key) {
        return keys.getOrDefault(key, false);
    }
    
    public boolean isJustPressed(KeyCode key) {
    	return pressedKeys.contains(key);
    }
    
    public void clear() {
    	pressedKeys.clear();
    }
}