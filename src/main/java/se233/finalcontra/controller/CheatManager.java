package se233.finalcontra.controller;

public class CheatManager {

    private static final CheatManager instance = new CheatManager();

    private boolean cheatsActive = false;

    private CheatManager() {
    }


    public static CheatManager getInstance() {
        return instance;
    }

    public void toggleCheats() {
        this.cheatsActive = !this.cheatsActive;
        System.out.println("Cheat state toggled! Cheats are now: " + (cheatsActive ? "ON" : "OFF"));
    }
    public boolean areCheatsActive() {
        return this.cheatsActive;
    }
}
