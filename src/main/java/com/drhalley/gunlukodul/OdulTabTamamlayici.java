package com.drhalley.gunlukodul;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OdulTabTamamlayici implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if(args.length >= 1){
            ArrayList<String> list = new ArrayList<>();
            list.add("oluştur");
            list.add("sil");
            return list;
        }
        return null;
    }
}
