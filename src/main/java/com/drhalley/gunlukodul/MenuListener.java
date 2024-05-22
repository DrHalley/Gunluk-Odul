package com.drhalley.gunlukodul;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MenuListener implements Listener {

    private GunlukOdul gunlukOdul;

    public MenuListener(GunlukOdul gunlukOdul){
        this.gunlukOdul = gunlukOdul;
    }

    public static String translateColors(String string) {
        if (string == null) return "";
        String parsedStr = string.replaceAll("\\{(#[0-9A-f]{6})\\}", "&$1");

        if (Pattern.compile("&#[0-9A-f]{6}").matcher(parsedStr).find()) {
            Matcher matcher = Pattern.compile("&(#[0-9A-f]{6})").matcher(parsedStr);
            while (matcher.find()) {
                parsedStr = parsedStr.replaceFirst(
                        matcher.group(),
                        ChatColor.of(matcher.group(1)).toString()
                );
            }
        }
        return ChatColor.translateAlternateColorCodes('&', parsedStr);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) throws SQLException {
        File file = new File(gunlukOdul.getDataFolder().getAbsolutePath() + "/menus", e.getView().getTitle() + ".yml");
        if(file.exists() && e.getCurrentItem() != null){
            YamlConfiguration f = YamlConfiguration.loadConfiguration(file);
            int slot = e.getRawSlot();

            Player p = (Player) e.getWhoClicked();
            try (PreparedStatement preparedStatement = gunlukOdul.getDatabase().getConnection().prepareStatement("SELECT * FROM "+e.getView().getTitle()+" WHERE uuid = ?")) {
                preparedStatement.setString(1, p.getUniqueId().toString());
                ResultSet resultSet = preparedStatement.executeQuery();

                long last_claimed = resultSet.getLong("last_claimed");
                long next_claim_start = resultSet.getLong("next_claim_start");
                long next_claim_end = resultSet.getLong("next_claim_end");
                int streak = resultSet.getInt("streak");
                if(slot == streak+12){
                    if(System.currentTimeMillis() > last_claimed+15000){


                        if(!f.getStringList("default_actions").isEmpty()){
                            for (int i = 0; i < f.getStringList("default_actions").toArray().length; i++) {
                                String action = f.getStringList("default_actions").get(i).toString();
                                String arr[] = action.split(" ", 2);
                                if(arr[0].equalsIgnoreCase("[console]")){
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), arr[1].replace("%player%", p.getDisplayName()).replace("%day%", String.valueOf(streak + 1)));
                                }else if(arr[0].equalsIgnoreCase("[message]")){
                                    p.sendMessage(translateColors(arr[1].replace("%day%", String.valueOf(streak+1))));
                                }
                            }
                        }
                        int day = streak+1;
                        List<String> actions = f.getStringList("day-"+ day+ ".actions");
                        if(!actions.isEmpty()){
                            for (int i = 0; i < actions.toArray().length; i++) {
                                String action = actions.get(i).toString();
                                String arr[] = action.split(" ", 2);
                                if(arr[0].equalsIgnoreCase("[console]")){
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), arr[1].replace("%player%", p.getDisplayName()).replace("%day%", String.valueOf(streak + 1)));
                                }else if(arr[0].equalsIgnoreCase("[message]")){
                                    p.sendMessage(translateColors(arr[1]).replace("%day%", String.valueOf(streak+1)));
                                }
                            }
                        }
                        try (PreparedStatement preparedStatement1 = gunlukOdul.getDatabase().getConnection().prepareStatement("UPDATE "+e.getView().getTitle()+" SET last_claimed = ? WHERE uuid = ?")) {
                            preparedStatement1.setLong(1, System.currentTimeMillis());
                            preparedStatement1.setString(2, p.getUniqueId().toString());
                            preparedStatement1.executeUpdate();
                        }
                        try (PreparedStatement preparedStatement1 = gunlukOdul.getDatabase().getConnection().prepareStatement("UPDATE "+e.getView().getTitle()+" SET next_claim_start = ? WHERE uuid = ?")) {
                            preparedStatement1.setLong(1, System.currentTimeMillis() + 15000);
                            preparedStatement1.setString(2, p.getUniqueId().toString());
                            preparedStatement1.executeUpdate();
                        }
                        try (PreparedStatement preparedStatement1 = gunlukOdul.getDatabase().getConnection().prepareStatement("UPDATE "+e.getView().getTitle()+" SET streak = ? WHERE uuid = ?")) {
                            preparedStatement1.setInt(1, streak +1);
                            preparedStatement1.setString(2, p.getUniqueId().toString());
                            preparedStatement1.executeUpdate();
                        }

                    }
                }else{
                    e.setCancelled(true);
                }
            }
            p.closeInventory();

        }
    }
}

