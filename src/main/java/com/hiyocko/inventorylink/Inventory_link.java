package com.hiyocko.inventorylink;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class Inventory_link implements ModInitializer {
    public static String MOD_ID = "inventory-management";
    //public static String MOD_VERSION = "1.0.0";
    public static Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public FabricLoader fabricLoader = FabricLoader.getInstance();
    public Config config;
    public MySQL mySQL = new MySQL();

    @Override
    public void onInitialize() {
        config = new Config(fabricLoader.getConfigDir());

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            LOGGER.info("initialize inventory-management.");
            config.loadConfig();

            mySQL.openConnection();
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            PlayerInventory.es.shutdown();
            mySQL.closeConnection();
            config = null;
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> PlayerInventory.joinServer(handler, server, mySQL));

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> PlayerInventory.leaveServer(handler, server, mySQL));

    }
}

