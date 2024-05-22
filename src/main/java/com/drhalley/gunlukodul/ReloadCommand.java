package com.drhalley.gunlukodul;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReloadCommand implements CommandExecutor {

    private GunlukOdul gunlukOdul;
    public ReloadCommand(GunlukOdul gunlukOdul){
        this.gunlukOdul = gunlukOdul;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        gunlukOdul.reloadConfig();
        ResultSet rs = null;
        try {
            rs = gunlukOdul.getDatabase().getConnection().getMetaData().getTables(null, null, null, null);
            while (rs.next()) {
                String fileName = rs.getString("TABLE_NAME");
                File file = new File(gunlukOdul.getDataFolder().getAbsolutePath() + "/menus", fileName + ".yml");
                YamlConfiguration f = YamlConfiguration.loadConfiguration(file);
                f.setDefaults(f);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false;
    }
}
