package com.hiyocko.inventorylink;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerInventory {
    public static final ExecutorService es = Executors.newVirtualThreadPerTaskExecutor();
    private static final Logger logger = Inventory_link.LOGGER;

    public static void joinServer(ServerPlayNetworkHandler handler, MinecraftServer server, MySQL mySQL) {
        checkTable(mySQL);
        ServerPlayerEntity player = handler.getPlayer();
        RegistryWrapper.WrapperLookup playerWrapperLookup = new RegistryBuilder().createWrapperLookup(player.getRegistryManager());

        PlayerData playerData = mySQL.getPlayerData(player.getUuid());
        if (playerData == null) return;
        if (!playerData.isConnected()){
            es.submit(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                player.getInventory().readNbt((NbtList) playerData.getInventory().get("inventory"));
                player.getEnderChestInventory().readNbtList((NbtList) playerData.getEnderChest().get("enderchest"), playerWrapperLookup);
                player.experienceProgress = playerData.getProgress();
                player.setExperienceLevel(playerData.getLevel());
                player.setHealth(playerData.getHealth());
                player.getHungerManager().readNbt(playerData.getHunger());
                mySQL.setConnected(player.getUuid(), true);

                player.sendMessageToClient(Text.literal("§l§aインベントリの共有が完了しました。"), true);
            });
        } else if (playerData.isConnected() && playerData.getServerName().equalsIgnoreCase(Config.SERVERNAME)){
            NbtCompound hungerNBT = new NbtCompound();
            NbtCompound inventoryNbtList = new NbtCompound();
            NbtCompound enderChestNbtList = new NbtCompound();
            inventoryNbtList.put("inventory", player.getInventory().writeNbt(new NbtList()));
            enderChestNbtList.put("enderchest", player.getEnderChestInventory().toNbtList(playerWrapperLookup));
            player.getHungerManager().writeNbt(hungerNBT);
            String uuid = player.getUuid().toString();
            String name = player.getNameForScoreboard();
            String inventory = inventoryNbtList.asString();
            String ender_chest = enderChestNbtList.asString();
            int level = player.experienceLevel;
            float progress = player.experienceProgress;
            float health = player.getHealth();
            String hunger = hungerNBT.asString();
            String serverName = Config.SERVERNAME;
            boolean isConnected = true;

            PlayerData playerDataError = new PlayerData(uuid, name, inventory, ender_chest, level, progress, health, hunger, serverName, isConnected);
            mySQL.setPlayerData(playerDataError);

            player.sendMessageToClient(Text.literal("§l§4前回のインベントリの保存に失敗したので、現在のインベントリを保存しました。"), true);
        } else if (playerData.isConnected() && !playerData.getServerName().equalsIgnoreCase(Config.SERVERNAME)) {
            player.getInventory().readNbt(new NbtList());
            player.getEnderChestInventory().readNbtList(new NbtList(), playerWrapperLookup);
            player.experienceProgress = 0F;
            player.setExperienceLevel(0);
            player.setHealth(0F);
            player.getHungerManager().readNbt(new NbtCompound());

            player.sendMessageToClient(Text.literal("§l§4前回のインベントリの保存に失敗しています。<" +playerData.getServerName()+ ">に入り直してください。"), true);
        }
    }

    public static void leaveServer(ServerPlayNetworkHandler handler, MinecraftServer server, MySQL mySQL) {
        checkTable(mySQL);
        ServerPlayerEntity player = handler.getPlayer();
        RegistryWrapper.WrapperLookup playerWrapperLookup = new RegistryBuilder().createWrapperLookup(player.getRegistryManager());

        NbtCompound hungerNBT = new NbtCompound();
        NbtCompound inventoryNbtList = new NbtCompound();
        NbtCompound enderChestNbtList = new NbtCompound();
        inventoryNbtList.put("inventory", player.getInventory().writeNbt(new NbtList()));
        enderChestNbtList.put("enderchest", player.getEnderChestInventory().toNbtList(playerWrapperLookup));
        player.getHungerManager().writeNbt(hungerNBT);
        String uuid = player.getUuid().toString();
        String name = player.getNameForScoreboard();
        String inventory = inventoryNbtList.asString();
        String ender_chest = enderChestNbtList.asString();
        int level = player.experienceLevel;
        float progress = player.experienceProgress;
        float health = player.getHealth();
        String hunger = hungerNBT.asString();
        String serverName = Config.SERVERNAME;
        boolean isConnected = false;

        PlayerData playerData = new PlayerData(uuid, name, inventory, ender_chest, level, progress, health, hunger, serverName, isConnected);

        mySQL.setPlayerData(playerData);
    }

    private static void checkTable(MySQL mySQL) {
        try {
            if (!mySQL.existsTable()) {
                mySQL.reConnection();
            }
        } catch (SQLException e) {
            logger.error("MYSQLの接続検証の過程でエラーが発生しました。");
            logger.error("エラー内容 : ", e);
        }
    }
}
