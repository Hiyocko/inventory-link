package com.hiyocko.inventorylink;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Inventory_link implements ModInitializer {
    public static String MOD_ID = "inv-share";
    //public static String MOD_VERSION = "1.0.0";
    public static Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public FabricLoader fabricLoader = FabricLoader.getInstance();
    public Config config;
    public static MySQL mySQL = new MySQL();

    @Override
    public void onInitialize() {
        config = new Config(fabricLoader.getConfigDir());
        LOGGER.info("initialize inventory-management.");
        config.loadConfig();

        mySQL.openConnection();
    }
}

