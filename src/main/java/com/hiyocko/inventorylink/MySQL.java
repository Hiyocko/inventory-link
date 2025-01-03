package com.hiyocko.inventorylink;

import org.slf4j.Logger;

import java.sql.*;
import java.util.UUID;

public class MySQL {
    private Connection connection;
    private PreparedStatement getPlayerData;
    private PreparedStatement setPlayerData;
    private PreparedStatement setConnected;
    private PreparedStatement existsTable;
    private final Logger logger = Inventory_link.LOGGER;

    public MySQL() {
    }

    public boolean openConnection() {
        try {
            if (connection != null && !connection.isClosed()){
                throw new IllegalStateException();
            }
            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection("jdbc:mysql://"+Config.ADDRESS+":"+Config.PORT+"/"+Config.DATABASE+"?characterEncoding=utf-8&characterSetResults=utf-8&autoReconnect=true", Config.USERNAME, Config.PASSWORD);

            PreparedStatement playerDataTable = connection.prepareStatement("CREATE TABLE IF NOT EXISTS playerdata(uuid char(36) primary key, name text, inventory longtext, enderchest longtext, level int, progress float, health float, hunger longtext, last_server text, isconnected boolean);");
            playerDataTable.executeUpdate();

            getPlayerData = connection.prepareStatement("SELECT * FROM playerdata WHERE uuid = ?;");
            setPlayerData = connection.prepareStatement("REPLACE INTO playerdata (uuid, name, inventory, enderchest, level, progress, health, hunger, last_server, isconnected) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
            setConnected = connection.prepareStatement("UPDATE playerdata set isconnected = ? where uuid =?;");
            existsTable = connection.prepareStatement("SHOW TABLES LIKE 'playerdata';");

            logger.info("MYSQLの接続に成功しました。");
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            logger.error("MYSQLの接続に失敗しました。");
            logger.error("エラー内容 : ", e);
            return false;
        } catch (IllegalStateException e) {
            logger.error("MYSQLにすでに接続されてます。");
            logger.error("エラー内容 : ", e);
            return false;
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()){
                connection.close();
                setPlayerData.close();
                getPlayerData.close();
                setConnected.close();

                connection = null;
                setPlayerData = null;
                getPlayerData = null;
                setConnected = null;

                logger.info("MYSQLの接続の切断に成功しました。");
            } else {
                logger.warn("MYSQLに接続がありませんでした。");
            }
        } catch (SQLException e){
            logger.error("MYSQLの接続の切断に失敗しました。");
            logger.error("エラー内容 : ", e);
        }
    }

    public void reConnection() {
        closeConnection();
        openConnection();
    }

    public boolean existsTable() throws SQLException{
        existsTable.clearParameters();

        ResultSet rs = existsTable.executeQuery();
        return rs.next();
    }

    public PlayerData getPlayerData(UUID uuid) {
        try {
            getPlayerData.clearParameters();
            getPlayerData.setString(1, uuid.toString());

            ResultSet resultSet = getPlayerData.executeQuery();

            PlayerData playerData = null;
            while (resultSet.next()) {
                String uuid1 = resultSet.getString("uuid");
                String name = resultSet.getString("name");
                String inventory = resultSet.getString("inventory");
                String enderChest = resultSet.getString("enderchest");
                int level = resultSet.getInt("level");
                float progress = resultSet.getFloat("progress");
                float health = resultSet.getFloat("health");
                String hunger = resultSet.getString("hunger");
                String serverName = resultSet.getString("last_server");
                boolean isConnected = resultSet.getBoolean("isconnected");

                playerData = new PlayerData(uuid1, name, inventory, enderChest, level, progress, health , hunger, serverName, isConnected);
            }

            resultSet.close();
            logger.info(uuid + "のデータの読み込みに成功しました。");
            if (playerData == null) {
                logger.warn(uuid+"のデータがテーブルに保存されていません。\n今回の切断時に新規保存します。");
            }
            return playerData;
        } catch (SQLException e) {
            logger.error("データの読み込みに失敗しました。");
            logger.error("エラー内容 : ", e);
            return null;
        }
    }

    public void setPlayerData(PlayerData playerData) {
        try {
            setPlayerData.clearParameters();
            setPlayerData.setString(1, playerData.getUuidAsString());
            setPlayerData.setString(2, playerData.getName());
            setPlayerData.setString(3, playerData.getInventory().toString());
            setPlayerData.setString(4, playerData.getEnderChest().toString());
            setPlayerData.setInt(5, playerData.getLevel());
            setPlayerData.setFloat(6, playerData.getProgress());
            setPlayerData.setFloat(7, playerData.getHealth());
            setPlayerData.setString(8, playerData.getHunger().toString());
            setPlayerData.setString(9, playerData.getServerName());
            setPlayerData.setBoolean(10, playerData.isConnected());

            setPlayerData.executeUpdate();
            logger.info(playerData.getName() + "のデータの書き込みに成功しました。");
        } catch (SQLException e) {
            logger.error("データの書き込みに失敗しました。");
            logger.error("エラー内容 : ", e);
        }
    }

    public void setConnected(UUID uuid, boolean isConnected){
        try {
            setConnected.clearParameters();
            setConnected.setBoolean(1, isConnected);
            setConnected.setString(2, uuid.toString());

            setConnected.executeUpdate();
            logger.info(uuid + "のデータの書き込みに成功しました。");
        } catch (SQLException e) {
            logger.error("データの書き込みに失敗しました。");
            logger.error("エラー内容 : ", e);
        }
    }
}
