package com.hiyocko.inventorylink;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerData {
    @NotNull
    private final UUID uuid;
    @NotNull
    private final String name;
    private final NbtCompound inventory;
    private final NbtCompound enderChest;
    private final int level;
    private final float progress;
    private final float health;
    private final NbtCompound hunger;
    private final String serverName;
    private final boolean isConnected;
    public PlayerData(@NotNull String uuid, @NotNull String name, String inventory, String enderChest, int level, float progress, float health, String hunger, String serverName, boolean isConnected) {

        this.uuid = UUID.fromString(uuid);
        this.name = name;
        this.inventory = convertNbtElement(inventory);
        this.enderChest = convertNbtElement(enderChest);
        this.level = level;
        this.progress = progress;
        this.health = health;
        this.hunger = convertNbtElement(hunger);
        this.serverName = serverName;
        this.isConnected = isConnected;
    }

    public @NotNull UUID getUuid(){
        return this.uuid;
    }

    public String getUuidAsString() {
        return this.uuid.toString();
    }

    public @NotNull String getName() {
        return this.name;
    }

    public NbtCompound getInventory(){
        return this.inventory;
    }

    public NbtCompound getEnderChest() {
        return this.enderChest;
    }

    public int getLevel() {
        return this.level;
    }

    public float getProgress(){
        return this.progress;
    }

    public float getHealth() {
        return health;
    }

    public NbtCompound getHunger(){
        return this.hunger;
    }

    public String getServerName(){
        return this.serverName;
    }

    public boolean isConnected(){
        return this.isConnected;
    }

    private NbtCompound convertNbtElement(String nbt) {
        //StringNbtReader snr = new StringNbtReader(new StringReader(nbt));
        try {
            return StringNbtReader.parse(nbt);
        } catch (CommandSyntaxException e) {
            return new NbtCompound();
        }
    }
}
