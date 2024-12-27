package com.hiyocko.inventorylink;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

