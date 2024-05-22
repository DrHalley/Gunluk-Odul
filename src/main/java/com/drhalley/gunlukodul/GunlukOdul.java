package com.drhalley.gunlukodul;

import com.drhalley.gunlukodul.database.Database;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GunlukOdul extends JavaPlugin implements Listener {
    private Database database;


    private void inititateFile(String name) throws IOException, InvalidConfigurationException {
        File folder = new File(getDataFolder(), "menus");
        folder.mkdirs();
        File file = new File(getDataFolder().getAbsolutePath() + "/menus", name +".yml");
        if(!file.exists()){
            file.createNewFile();
            YamlConfiguration modifyFile = YamlConfiguration.loadConfiguration(file);
            modifyFile.loadFromString(
                    "menu_title: DrHalley Rewards\n" +
                    "menu_size: 54\n" +
                    "default_actions:\n" +
                    "- '[console] give %player% stick %day%'\n" +
                    "- '[message] &a%day%. gün ödülünü aldınız!'\n" +
                    "default_items:\n" +
                    "  claimed:\n" +
                    "    material: LIME_STAINED_GLASS_PANE\n" +
                    "    amount: -1\n" +
                    "    displayName: '&bGün &a&l%day%'\n" +
                    "    lore:\n" +
                    "    - '&aBu günün ödülünü aldınız'\n" +
                    "    glow: false\n" +
                    "  canClaim:\n" +
                    "    material: LIME_STAINED_GLASS_PANE\n" +
                    "    amount: -1\n" +
                    "    displayName: '&bGün &a&l%day%'\n" +
                    "    lore:\n" +
                    "    - '&6Ödülleri almak için tıklayın'\n" +
                    "    glow: true\n" +
                    "  unclaimed:\n" +
                    "    material: RED_STAINED_GLASS_PANE\n" +
                    "    amount: -1\n" +
                    "    displayName: '&bGün &a&l%day%'\n" +
                    "    lore:\n" +
                    "    - '&cBu ödülü daha alamazsınız'\n" +
                    "    glow: false\n" +
                    "  empty:\n" +
                    "    material: GRAY_STAINED_GLASS_PANE\n" +
                    "    amount: 1\n" +
                    "    displayName: ' '\n" +
                    "    lore: []\n" +
                    "    glow: false\n" +
                    "  nextReward:\n" +
                    "    material: ORANGE_STAINED_GLASS_PANE\n" +
                    "    amount: -1\n" +
                    "    displayName: '&bGün &a&l%day%'\n" +
                    "    lore:\n" +
                    "    - ''\n" +
                    "    - '&6Bu ödülü %date% tarihinde, %time% '\n" +
                    "    - '&6saatinde alabilirsin'\n" +
                    "day-1:\n" +
                    "  actions:\n" +
                    "  - '[console] give %player% stick 1'\n" +
                    "  - '[message] &aBirinci gün ödülünü aldın!'\n" +
                    "day-2:\n" +
                    "  actions:\n" +
                    "  - '[console] give %player% stick 2'\n" +
                    "  - '[message] &aİkinci gün ödülünü aldın!'\n" +
                    "day-3:\n" +
                    "- '[console] give %player% stick 3'\n" +
                    "- '[message] &aÜçüncü gün ödülünü aldın!'\n" +
                    "day-4:\n" +
                    "  actions:\n" +
                    "  - '[console] give %player% stick 4'\n" +
                    "  - '[message] &aBeşinci günü tamamladın!'");

            modifyFile.save(file);
        }


    }


    @Override
    public void onEnable() {

        getConfig().options().copyDefaults();
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new MenuListener(this), this);
        getCommand("ödül").setExecutor(new OdulCommand(this));
        getCommand("ödül").setTabCompleter(new OdulTabTamamlayici());
        getCommand("odul-reload").setExecutor(new ReloadCommand(this));
        try {
            if(!getDataFolder().exists()){
                getDataFolder().mkdirs();
            }
            database = new Database(getDataFolder().getAbsolutePath() + "/playerdata.db");
            ResultSet rs = database.getConnection().getMetaData().getTables(null, null, null, null);
            while (rs.next()) {
                inititateFile(rs.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void onDisable() {
        try {
            database.closeConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Database getDatabase() {
        return database;
    }
    @EventHandler
    public void onJoinEvent(PlayerJoinEvent e) throws SQLException {

        if(!database.playerExists(e.getPlayer())){
            database.addPlayer(e.getPlayer());

        }

    }



}
