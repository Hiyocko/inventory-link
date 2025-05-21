package com.hiyocko.inventorylink;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Inventory {
    public static final ExecutorService es = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("Hiyocko mod thread").build());
    private static final Logger logger = Inventory_link.LOGGER;

    public static void joinServer(ServerPlayerEntity player, MySQL mySQL) {
        RegistryWrapper.WrapperLookup playerWrapperLookup = new RegistryBuilder().createWrapperLookup(player.getRegistryManager());

        PlayerData playerData = mySQL.getPlayerData(player.getUuid());
        if (playerData == null) return;
        if (!playerData.isConnected()){
            es.submit(() -> {
                clearInventory(player, playerWrapperLookup);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                player.getInventory().readNbt((NbtList) playerData.getInventory().get("inventory"));
                loadSubData(player, playerData.getInventory(), playerWrapperLookup);
                player.getEnderChestInventory().readNbtList((NbtList) playerData.getEnderChest().get("enderchest"), playerWrapperLookup);
                player.experienceProgress = playerData.getProgress();
                player.setExperienceLevel(playerData.getLevel());
                player.setHealth(playerData.getHealth());
                player.getHungerManager().readNbt(playerData.getHunger());
                mySQL.setConnected(player.getUuid(), true);
                mySQL.setLast_server(player.getUuid(), Config.SERVERNAME);

                player.sendMessageToClient(Text.literal("§l§aインベントリの共有が完了しました。"), true);
            });
        } else if (playerData.isConnected() && playerData.getServerName().equalsIgnoreCase(Config.SERVERNAME)){
            es.submit(() -> {
                NbtCompound hungerNBT = new NbtCompound();
                NbtCompound inventoryNbtList = new NbtCompound();
                NbtCompound enderChestNbtList = new NbtCompound();
                mergeInventory(player, inventoryNbtList, playerWrapperLookup);
                enderChestNbtList.put("enderchest", player.getEnderChestInventory().toNbtList(playerWrapperLookup));
                player.getHungerManager().writeNbt(hungerNBT);
                String uuid = player.getUuid().toString();
                String name = player.getNameForScoreboard();
                //String inventory = inventoryNbtList.asString();
                //String ender_chest = enderChestNbtList.asString();
                String inventory = inventoryNbtList.toString();
                String ender_chest = enderChestNbtList.toString();
                int level = player.experienceLevel;
                float progress = player.experienceProgress;
                float health = player.getHealth();
                //String hunger = hungerNBT.asString();
                String hunger = hungerNBT.toString();
                String serverName = Config.SERVERNAME;
                boolean isConnected = true;

                PlayerData playerDataError = new PlayerData(uuid, name, inventory, ender_chest, level, progress, health, hunger, serverName, isConnected);
                mySQL.setPlayerData(playerDataError);

                player.sendMessageToClient(Text.literal("§l§4前回のインベントリの保存に失敗したので、現在のインベントリを保存しました。"), true);
            });
        } else if (playerData.isConnected() && !playerData.getServerName().equalsIgnoreCase(Config.SERVERNAME)) {
            clearInventory(player, playerWrapperLookup);

            player.sendMessageToClient(Text.literal("§l§4前回のインベントリの保存に失敗しています。<" +playerData.getServerName()+ ">に入り直してください。"), true);
        } else {
            player.sendMessageToClient(Text.literal("§l§4例外な処理が発生しました。"), false);
            player.sendMessageToClient(Text.literal("§l§4管理者にお知らせください。"), false);
            logger.error("異常すぎるエラー : ");
        }
    }

    public static void leaveServer(ServerPlayerEntity player, MySQL mySQL) {
        RegistryWrapper.WrapperLookup playerWrapperLookup = new RegistryBuilder().createWrapperLookup(player.getRegistryManager());

        PlayerData readPlayerData = mySQL.getPlayerData(player.getUuid());
        if (readPlayerData == null) readPlayerData = new PlayerData(player.getUuid().toString(), player.getNameForScoreboard(), Config.SERVERNAME);
        if (readPlayerData.isConnected() && readPlayerData.getServerName().equalsIgnoreCase(Config.SERVERNAME)) {
            es.submit(() -> {
                NbtCompound hungerNBT = new NbtCompound();
                NbtCompound inventoryNbtList = new NbtCompound();
                NbtCompound enderChestNbtList = new NbtCompound();
                inventoryNbtList = mergeInventory(player, new NbtCompound(), playerWrapperLookup);
                enderChestNbtList.put("enderchest", player.getEnderChestInventory().toNbtList(playerWrapperLookup));
                player.getHungerManager().writeNbt(hungerNBT);
                String uuid = player.getUuid().toString();
                String name = player.getNameForScoreboard();
                //String inventory = inventoryNbtList.asString();
                //String ender_chest = enderChestNbtList.asString();
                String inventory = inventoryNbtList.toString();
                String ender_chest = enderChestNbtList.toString();
                int level = player.experienceLevel;
                float progress = player.experienceProgress;
                float health = player.getHealth();
                //String hunger = hungerNBT.asString();
                String hunger = hungerNBT.toString();
                String serverName = Config.SERVERNAME;
                boolean isConnected = false;

                PlayerData playerData = new PlayerData(uuid, name, inventory, ender_chest, level, progress, health, hunger, serverName, isConnected);

                mySQL.setPlayerData(playerData);
            });
        } /*else if (readPlayerData.isConnected() && !readPlayerData.getServerName().equalsIgnoreCase(Config.SERVERNAME)) {

        }*/
    }

    private static void loadSubData(ServerPlayerEntity p, NbtCompound c, RegistryWrapper.WrapperLookup regi) {
        p.equipStack(EquipmentSlot.OFFHAND, lo(c.get("off"), regi));
        p.equipStack(EquipmentSlot.FEET, lo(c.get("feet"), regi));
        p.equipStack(EquipmentSlot.LEGS, lo(c.get("legs"), regi));
        p.equipStack(EquipmentSlot.CHEST, lo(c.get("body"), regi));
        p.equipStack(EquipmentSlot.HEAD, lo(c.get("head"), regi));
    }

    private static ItemStack lo(NbtElement e, RegistryWrapper.WrapperLookup regi) {
        if (e == null) return ItemStack.EMPTY;
        return ItemStack.fromNbt(regi, e.asCompound().orElseThrow()).orElseThrow();
    }

    private static NbtCompound mergeInventory(ServerPlayerEntity p, NbtCompound n, RegistryWrapper.WrapperLookup regi) {
        NbtCompound off = eq(p, EquipmentSlot.OFFHAND, regi);
        NbtCompound feet = eq(p, EquipmentSlot.FEET,  regi);
        NbtCompound legs = eq(p, EquipmentSlot.LEGS,  regi);
        NbtCompound body = eq(p, EquipmentSlot.CHEST, regi);
        NbtCompound head = eq(p, EquipmentSlot.HEAD,  regi);
        n.put("inventory", p.getInventory().writeNbt(new NbtList()));
        n.put("off", off);
        n.put("feet", feet);
        n.put("legs", legs);
        n.put("body", body);
        n.put("head", head);
        return n;
    }

    private static NbtCompound eq(ServerPlayerEntity p, EquipmentSlot slot, RegistryWrapper.WrapperLookup regi) {
        return p.getEquippedStack(slot).toNbt(regi).asCompound().orElseThrow();
    }

    private static void clearInventory(ServerPlayerEntity player, RegistryWrapper.WrapperLookup a) {
        player.equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        player.equipStack(EquipmentSlot.FEET, ItemStack.EMPTY);
        player.equipStack(EquipmentSlot.LEGS, ItemStack.EMPTY);
        player.equipStack(EquipmentSlot.CHEST, ItemStack.EMPTY);
        player.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
        player.getInventory().readNbt(new NbtList());
        player.getEnderChestInventory().readNbtList(new NbtList(), a);
        player.experienceProgress = 0F;
        player.setExperienceLevel(0);
        player.setHealth(20F);
        player.getHungerManager().readNbt(new NbtCompound());
    }
}
