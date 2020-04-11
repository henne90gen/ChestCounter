package de.henne90gen.chestcounter.db.entities;

public class ChestStorage {
    public static final int CURRENT_VERSION = 2;

    public int version = CURRENT_VERSION;
    public ChestConfig config;
    public ChestWorlds worlds;
}
