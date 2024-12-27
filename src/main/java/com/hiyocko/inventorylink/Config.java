package com.hiyocko.inventorylink;

import com.google.gson.Gson;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Config {
    public static String ADDRESS;
    public static int PORT;
    public static String DATABASE;
    public static String USERNAME;
    public static String PASSWORD;
    public static String SERVERNAME;


    private final Logger logger = Inventory_link.LOGGER;
    private final Path configFile;
    private final Gson gson = new Gson();
    public Config(Path path) {
        configFile = Paths.get(path.toString(), "inventory-management.json");
    }

    public void loadConfig() {
        try {
            if (!Files.exists(configFile)){
                createConfig();
            }

            String fileData = Files.readString(configFile);
            Map configData = gson.fromJson(fileData, Map.class);
            ADDRESS = (String) configData.get("address");
            PORT = Integer.parseInt((String) configData.get("port"));
            DATABASE = (String) configData.get("database");
            USERNAME = (String) configData.get("username");
            PASSWORD = (String) configData.get("password");
            SERVERNAME = (String) configData.get("servername");
            logger.info(configData.toString());
            logger.info("complete to load config.");
        } catch (IOException e) {
            logger.error("error to load config", e);
        }
    }

    private void createConfig() throws IOException {
        List<String> file = new ArrayList<>();
        file.add("{");
        file.add("  \"address\" : \"127.0.0.1\", ");
        file.add("  \"port\" : \"3306\", ");
        file.add("  \"database\" : \"\", ");
        file.add("  \"username\" : \"\", ");
        file.add("  \"password\" : \"\", ");
        file.add("  \"servername\" : \"servername\" ");
        file.add("}");
        Files.createFile(configFile);
        Files.write(configFile, file, StandardCharsets.UTF_8);
    }
}
