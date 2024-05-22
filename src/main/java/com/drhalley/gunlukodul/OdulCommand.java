package com.drhalley.gunlukodul;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class OdulCommand implements CommandExecutor {

    private GunlukOdul gunlukOdul;

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

    public static List translateColorsList(List<String> listo) {
        for (int i = 0; i < listo.toArray().length; i++) {
            listo.set(i, translateColors(listo.get(i)));

        }
        return listo;
    }

    public OdulCommand(GunlukOdul gunlukOdul) {
        this.gunlukOdul = gunlukOdul;
    }

    public List<String> replacetoList(List<String> lores, long next_claim_starto) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy");
        SimpleDateFormat sdftime = new SimpleDateFormat("HH:mm");
        Date resultdate = new Date(next_claim_starto);
        Date resulttime = new Date(next_claim_starto);
        String date = sdf.format(resultdate);
        String time = sdftime.format(resulttime);
        for (int i = 0; i < lores.toArray().length; i++) {

            lores.set(i, lores.get(i).replace("%date%", date).replace("%time%", time));
        }
        return lores;

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;

            try {
                if (!gunlukOdul.getDatabase().playerExists(p)) {
                    gunlukOdul.getDatabase().addPlayer(p);

                }
                long last_claimed = gunlukOdul.getDatabase().getPlayerLastClaimed(p);
                long next_claim_start = gunlukOdul.getDatabase().getPlayerNextClaimStart(p);
                long next_claim_end = gunlukOdul.getDatabase().getPlayerNextClaimEnd(p);
                int streak = gunlukOdul.getDatabase().getPlayerStreak(p);
                if (args.length == 0) {
                    File file = new File(gunlukOdul.getDataFolder().getAbsolutePath() + "/menus", "varsayilan.yml");
                    YamlConfiguration f = YamlConfiguration.loadConfiguration(file);
                    Material claimed_material = Material.getMaterial(f.getString("default_items.claimed.material"));
                    ItemStack claimed = new ItemStack(claimed_material);
                    ItemMeta claimedMeta = claimed.getItemMeta();
                    claimedMeta.setDisplayName(translateColors(f.getString("default_items.claimed.displayName")));
                    claimedMeta.setLore(translateColorsList(f.getStringList("default_items.claimed.lore")));
                    claimed.setItemMeta(claimedMeta);

                    Material canClaim_material = Material.getMaterial(f.getString("default_items.canClaim.material"));
                    ItemStack canClaim = new ItemStack(canClaim_material);
                    ItemMeta canClaimMeta = canClaim.getItemMeta();
                    canClaimMeta.setDisplayName(translateColors(f.getString("default_items.canClaim.displayName")));
                    canClaimMeta.setLore(translateColorsList(f.getStringList("default_items.canClaim.lore")));
                    canClaim.setItemMeta(canClaimMeta);

                    Material unclaimed_material = Material.getMaterial(f.getString("default_items.unclaimed.material"));
                    ItemStack unclaimed = new ItemStack(unclaimed_material);
                    ItemMeta unclaimedMeta = unclaimed.getItemMeta();
                    unclaimedMeta.setDisplayName(translateColors(f.getString("default_items.unclaimed.displayName")));
                    unclaimedMeta.setLore(translateColorsList(f.getStringList("default_items.unclaimed.lore")));
                    unclaimed.setItemMeta(unclaimedMeta);

                    Material empty_material = Material.getMaterial(f.getString("default_items.empty.material"));
                    ItemStack empty = new ItemStack(empty_material);
                    ItemMeta emptyMeta = empty.getItemMeta();
                    emptyMeta.setDisplayName(translateColors(f.getString("default_items.empty.displayName")));
                    emptyMeta.setLore(translateColorsList(f.getStringList("default_items.empty.lore")));
                    empty.setItemMeta(emptyMeta);

                    Material nextReward_material = Material.getMaterial(f.getString("default_items.nextReward.material"));
                    ItemStack nextReward = new ItemStack(nextReward_material);
                    ItemMeta nextRewardMeta = nextReward.getItemMeta();
                    nextRewardMeta.setDisplayName(translateColors(f.getString("default_items.nextReward.displayName")));
                    nextRewardMeta.setLore(translateColorsList(f.getStringList("default_items.nextReward.lore")));
                    nextReward.setItemMeta(nextRewardMeta);
                    Inventory inv = Bukkit.createInventory(p, 54, "varsayilan");
                    for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53}) {
                        inv.setItem(i, empty);
                    }
                    if ((System.currentTimeMillis() - last_claimed) > 15000) {
                        if (streak == 0) {
                            ItemMeta canclaimeddays = canClaimMeta;
                            canclaimeddays.setDisplayName(canClaim.getItemMeta().getDisplayName().replace("%day%", "1"));
                            canClaim.setItemMeta(canclaimeddays);
                            inv.setItem(streak + 12, canClaim);

                            for (int i = 13; i < 42; i++) {
                                Material unclaimed_material1 = Material.getMaterial(f.getString("default_items.unclaimed.material"));
                                ItemStack unclaimed1 = new ItemStack(unclaimed_material1);
                                ItemMeta unclaimedMeta1 = unclaimed.getItemMeta();
                                unclaimedMeta1.setDisplayName((translateColors(f.getString("default_items.unclaimed.displayName"))).replace("%day%", String.valueOf(i - 11)));
                                unclaimedMeta1.setLore(translateColorsList(f.getStringList("default_items.unclaimed.lore")));
                                unclaimed1.setItemMeta(unclaimedMeta1);
                                inv.setItem(i, unclaimed1);

                            }
                        } else {
                            for (int i = 12; i < streak + 12; i++) {
                                Material claimed_material1 = Material.getMaterial(f.getString("default_items.claimed.material"));
                                ItemStack claimed1 = new ItemStack(claimed_material1);
                                ItemMeta claimedMeta1 = claimed1.getItemMeta();
                                claimedMeta1.setDisplayName((translateColors(f.getString("default_items.claimed.displayName"))).replace("%day%", String.valueOf(i - 11)));
                                claimedMeta1.setLore(translateColorsList(f.getStringList("default_items.claimed.lore")));
                                claimed1.setItemMeta(claimedMeta1);
                                inv.setItem(i, claimed1);
                            }
                            for (int i = streak + 13; i < 42; i++) {
                                Material unclaimed_material1 = Material.getMaterial(f.getString("default_items.unclaimed.material"));
                                ItemStack unclaimed1 = new ItemStack(unclaimed_material1);
                                ItemMeta unclaimedMeta1 = unclaimed1.getItemMeta();
                                unclaimedMeta1.setDisplayName((translateColors(f.getString("default_items.unclaimed.displayName"))).replace("%day%", String.valueOf(i - 11)));
                                unclaimedMeta1.setLore((translateColorsList(f.getStringList("default_items.unclaimed.lore"))));
                                unclaimed1.setItemMeta(unclaimedMeta1);
                                inv.setItem(i, unclaimed1);
                            }
                            Material canClaim_material1 = Material.getMaterial(f.getString("default_items.canClaim.material"));
                            ItemStack canClaim1 = new ItemStack(canClaim_material1);
                            ItemMeta canClaimMeta1 = canClaim1.getItemMeta();
                            canClaimMeta1.setDisplayName((translateColors(f.getString("default_items.canClaim.displayName"))).replace("%day%", String.valueOf(streak + 1)));
                            canClaimMeta1.setLore(translateColorsList(f.getStringList("default_items.canClaim.lore")));
                            canClaim1.setItemMeta(canClaimMeta1);
                            inv.setItem(streak + 12, canClaim1);
                        }

                        p.openInventory(inv);
                    } else {
                        for (int i = 12; i < streak + 12; i++) {
                            Material claimed_material1 = Material.getMaterial(f.getString("default_items.claimed.material"));
                            ItemStack claimed1 = new ItemStack(claimed_material1);
                            ItemMeta claimedMeta1 = claimed1.getItemMeta();
                            claimedMeta1.setDisplayName((translateColors(f.getString("default_items.claimed.displayName"))).replace("%day%", String.valueOf(i - 11)));
                            claimedMeta1.setLore(translateColorsList(f.getStringList("default_items.claimed.lore")));
                            claimed1.setItemMeta(claimedMeta1);
                            inv.setItem(i, claimed1);
                        }
                        Material nextReward_material1 = Material.getMaterial(f.getString("default_items.nextReward.material"));
                        ItemStack nextReward1 = new ItemStack(nextReward_material1);
                        ItemMeta nextRewardMeta1 = nextReward1.getItemMeta();
                        nextRewardMeta1.setDisplayName(translateColors((f.getString("default_items.nextReward.displayName"))).replace("%day%", String.valueOf(streak + 1)));
                        nextRewardMeta1.setLore(translateColorsList(replacetoList(f.getStringList("default_items.nextReward.lore"), next_claim_start)));
                        nextReward1.setItemMeta(nextRewardMeta1);
                        inv.setItem(streak + 12, nextReward1);
                        for (int i = streak + 13; i < 42; i++) {
                            Material unclaimed_material1 = Material.getMaterial(f.getString("default_items.unclaimed.material"));
                            ItemStack unclaimed1 = new ItemStack(unclaimed_material1);
                            ItemMeta unclaimedMeta1 = unclaimed.getItemMeta();
                            unclaimedMeta1.setDisplayName((translateColors(f.getString("default_items.unclaimed.displayName"))).replace("%day%", String.valueOf(i - 11)));
                            unclaimedMeta1.setLore(translateColorsList(f.getStringList("default_items.unclaimed.lore")));
                            unclaimed1.setItemMeta(unclaimedMeta1);
                            inv.setItem(i, unclaimed1);
                        }
                        p.openInventory(inv);
                    }


                } else if (args.length == 1 && args[0].equalsIgnoreCase("test")) {
                    gunlukOdul.getDatabase().setPlayerLastClaimed(p, System.currentTimeMillis());
                    gunlukOdul.getDatabase().setPlayerNextClaimStart(p, System.currentTimeMillis() + 15000);
                }

                if (args.length == 2 && args[0].equalsIgnoreCase("oluştur")) {

                    if (p.hasPermission("op")) {

                        String menuName = args[1];
                        File file = new File(gunlukOdul.getDataFolder().getAbsolutePath() + "/menus", menuName + ".yml");

                        if (!file.exists()) {

                            try (Statement statement = gunlukOdul.getDatabase().getConnection().createStatement();) {
                                statement.execute("CREATE TABLE IF NOT EXISTS " + menuName + " ("
                                        + "uuid TEXT PRIMARY KEY, "
                                        + "username TEXT NOT NULL, "
                                        + "last_claimed LONG NOT NULL DEFAULT 0, "
                                        + "next_claim_start LONG NOT NULL DEFAULT 0, "
                                        + "next_claim_end LONG NOT NULL DEFAULT 0, "
                                        + "streak INT NOT NULL DEFAULT 0)");

                            }
                            file.createNewFile();
                            YamlConfiguration f = YamlConfiguration.loadConfiguration(file);
                            f.loadFromString(
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

                            f.save(file);
                            p.sendMessage(gunlukOdul.getConfig().getString("menu_created").replace("%menu%", args[1]));
                        } else if (file.exists()) {
                            p.sendMessage(gunlukOdul.getConfig().getString("menu_already_exists").replace("%menu%", args[1]));
                        }
                    } else {
                        p.sendMessage(gunlukOdul.getConfig().getString("no_permission"));
                    }


                } else if (args.length == 1 && args[0].equalsIgnoreCase("oluştur")) {
                    if (p.hasPermission("op")) {
                        p.sendMessage(gunlukOdul.getConfig().getString("not_enough_args"));
                    } else {
                        p.sendMessage(gunlukOdul.getConfig().getString("no_permission"));
                    }

                } else if (args.length == 1 && !args[0].equalsIgnoreCase("oluştur")) {
                    String menuName = args[0];
                    File file = new File(gunlukOdul.getDataFolder().getAbsolutePath() + "/menus", menuName + ".yml");
                    if (file.exists()) {
                        try (PreparedStatement preparedStatement = gunlukOdul.getDatabase().getConnection().prepareStatement("SELECT * FROM " + menuName + " WHERE uuid = ?")) {
                            preparedStatement.setString(1, p.getUniqueId().toString());
                            ResultSet resultSet = preparedStatement.executeQuery();
                            if (!resultSet.next()) {
                                try (PreparedStatement preparedStatement2 = gunlukOdul.getDatabase().getConnection().prepareStatement("INSERT INTO " + menuName + " (uuid, username) VALUES (?, ?)")) {
                                    preparedStatement2.setString(1, p.getUniqueId().toString());
                                    preparedStatement2.setString(2, p.getName());
                                    preparedStatement2.executeUpdate();
                                }
                            }
                            try (PreparedStatement preparedStatement1 = gunlukOdul.getDatabase().getConnection().prepareStatement("SELECT * FROM " + menuName + " WHERE uuid = ?")) {
                                preparedStatement1.setString(1, p.getUniqueId().toString());
                                ResultSet resultSet1 = preparedStatement1.executeQuery();

                                long last_claimed1 = resultSet1.getLong("last_claimed");
                                long next_claim_start1 = resultSet1.getLong("next_claim_start");
                                long next_claim_end1 = resultSet1.getLong("next_claim_end");
                                int streak1 = resultSet1.getInt("streak");
                                YamlConfiguration f = YamlConfiguration.loadConfiguration(file);
                                Material claimed_material = Material.getMaterial(f.getString("default_items.claimed.material"));
                                ItemStack claimed = new ItemStack(claimed_material);
                                ItemMeta claimedMeta = claimed.getItemMeta();
                                claimedMeta.setDisplayName(translateColors(f.getString("default_items.claimed.displayName")));
                                claimedMeta.setLore(translateColorsList(f.getStringList("default_items.claimed.lore")));
                                claimed.setItemMeta(claimedMeta);

                                Material canClaim_material = Material.getMaterial(f.getString("default_items.canClaim.material"));
                                ItemStack canClaim = new ItemStack(canClaim_material);
                                ItemMeta canClaimMeta = canClaim.getItemMeta();
                                canClaimMeta.setDisplayName(translateColors(f.getString("default_items.canClaim.displayName")));
                                canClaimMeta.setLore(translateColorsList(f.getStringList("default_items.canClaim.lore")));
                                canClaim.setItemMeta(canClaimMeta);

                                Material unclaimed_material = Material.getMaterial(f.getString("default_items.unclaimed.material"));
                                ItemStack unclaimed = new ItemStack(unclaimed_material);
                                ItemMeta unclaimedMeta = unclaimed.getItemMeta();
                                unclaimedMeta.setDisplayName(translateColors(f.getString("default_items.unclaimed.displayName")));
                                unclaimedMeta.setLore(translateColorsList(f.getStringList("default_items.unclaimed.lore")));
                                unclaimed.setItemMeta(unclaimedMeta);

                                Material empty_material = Material.getMaterial(f.getString("default_items.empty.material"));
                                ItemStack empty = new ItemStack(empty_material);
                                ItemMeta emptyMeta = empty.getItemMeta();
                                emptyMeta.setDisplayName(translateColors(f.getString("default_items.empty.displayName")));
                                emptyMeta.setLore(translateColorsList(f.getStringList("default_items.empty.lore")));
                                empty.setItemMeta(emptyMeta);

                                Material nextReward_material = Material.getMaterial(f.getString("default_items.nextReward.material"));
                                ItemStack nextReward = new ItemStack(nextReward_material);
                                ItemMeta nextRewardMeta = nextReward.getItemMeta();
                                nextRewardMeta.setDisplayName(translateColors(f.getString("default_items.nextReward.displayName")));
                                nextRewardMeta.setLore(translateColorsList(f.getStringList("default_items.nextReward.lore")));
                                nextReward.setItemMeta(nextRewardMeta);
                                Inventory inv = Bukkit.createInventory(p, 54, menuName);
                                for (int i : new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53}) {
                                    inv.setItem(i, empty);
                                }
                                if ((System.currentTimeMillis() - last_claimed1) > 15000) {
                                    if (streak1 == 0) {
                                        ItemMeta canclaimeddays = canClaimMeta;
                                        canclaimeddays.setDisplayName(canClaim.getItemMeta().getDisplayName().replace("%day%", "1"));
                                        canClaim.setItemMeta(canclaimeddays);
                                        inv.setItem(streak1 + 12, canClaim);

                                        for (int i = 13; i < 42; i++) {
                                            Material unclaimed_material1 = Material.getMaterial(f.getString("default_items.unclaimed.material"));
                                            ItemStack unclaimed1 = new ItemStack(unclaimed_material1);
                                            ItemMeta unclaimedMeta1 = unclaimed.getItemMeta();
                                            unclaimedMeta1.setDisplayName((translateColors(f.getString("default_items.unclaimed.displayName"))).replace("%day%", String.valueOf(i - 11)));
                                            unclaimedMeta1.setLore(translateColorsList(f.getStringList("default_items.unclaimed.lore")));
                                            unclaimed1.setItemMeta(unclaimedMeta1);
                                            inv.setItem(i, unclaimed1);

                                        }
                                    } else {
                                        for (int i = 12; i < streak1 + 12; i++) {
                                            Material claimed_material1 = Material.getMaterial(f.getString("default_items.claimed.material"));
                                            ItemStack claimed1 = new ItemStack(claimed_material1);
                                            ItemMeta claimedMeta1 = claimed1.getItemMeta();
                                            claimedMeta1.setDisplayName((translateColors(f.getString("default_items.claimed.displayName"))).replace("%day%", String.valueOf(i - 11)));
                                            claimedMeta1.setLore(translateColorsList(f.getStringList("default_items.claimed.lore")));
                                            claimed1.setItemMeta(claimedMeta1);
                                            inv.setItem(i, claimed1);
                                        }
                                        for (int i = streak1 + 13; i < 42; i++) {
                                            Material unclaimed_material1 = Material.getMaterial(f.getString("default_items.unclaimed.material"));
                                            ItemStack unclaimed1 = new ItemStack(unclaimed_material1);
                                            ItemMeta unclaimedMeta1 = unclaimed1.getItemMeta();
                                            unclaimedMeta1.setDisplayName((translateColors(f.getString("default_items.unclaimed.displayName"))).replace("%day%", String.valueOf(i - 11)));
                                            unclaimedMeta1.setLore((translateColorsList(f.getStringList("default_items.unclaimed.lore"))));
                                            unclaimed1.setItemMeta(unclaimedMeta1);
                                            inv.setItem(i, unclaimed1);
                                        }
                                        Material canClaim_material1 = Material.getMaterial(f.getString("default_items.canClaim.material"));
                                        ItemStack canClaim1 = new ItemStack(canClaim_material1);
                                        ItemMeta canClaimMeta1 = canClaim1.getItemMeta();
                                        canClaimMeta1.setDisplayName((translateColors(f.getString("default_items.canClaim.displayName"))).replace("%day%", String.valueOf(streak1 + 1)));
                                        canClaimMeta1.setLore(translateColorsList(f.getStringList("default_items.canClaim.lore")));
                                        canClaim1.setItemMeta(canClaimMeta1);
                                        inv.setItem(streak1 + 12, canClaim1);
                                    }

                                    p.openInventory(inv);
                                } else {
                                    for (int i = 12; i < streak1 + 12; i++) {
                                        Material claimed_material1 = Material.getMaterial(f.getString("default_items.claimed.material"));
                                        ItemStack claimed1 = new ItemStack(claimed_material1);
                                        ItemMeta claimedMeta1 = claimed1.getItemMeta();
                                        claimedMeta1.setDisplayName((translateColors(f.getString("default_items.claimed.displayName"))).replace("%day%", String.valueOf(i - 11)));
                                        claimedMeta1.setLore(translateColorsList(f.getStringList("default_items.claimed.lore")));
                                        claimed1.setItemMeta(claimedMeta1);
                                        inv.setItem(i, claimed1);
                                    }
                                    Material nextReward_material1 = Material.getMaterial(f.getString("default_items.nextReward.material"));
                                    ItemStack nextReward1 = new ItemStack(nextReward_material1);
                                    ItemMeta nextRewardMeta1 = nextReward1.getItemMeta();
                                    nextRewardMeta1.setDisplayName(translateColors((f.getString("default_items.nextReward.displayName"))).replace("%day%", String.valueOf(streak + 1)));
                                    nextRewardMeta1.setLore(translateColorsList(replacetoList(f.getStringList("default_items.nextReward.lore"), next_claim_start)));
                                    nextReward1.setItemMeta(nextRewardMeta1);
                                    inv.setItem(streak1 + 12, nextReward1);
                                    for (int i = streak1 + 13; i < 42; i++) {
                                        Material unclaimed_material1 = Material.getMaterial(f.getString("default_items.unclaimed.material"));
                                        ItemStack unclaimed1 = new ItemStack(unclaimed_material1);
                                        ItemMeta unclaimedMeta1 = unclaimed.getItemMeta();
                                        unclaimedMeta1.setDisplayName((translateColors(f.getString("default_items.unclaimed.displayName"))).replace("%day%", String.valueOf(i - 11)));
                                        unclaimedMeta1.setLore(translateColorsList(f.getStringList("default_items.unclaimed.lore")));
                                        unclaimed1.setItemMeta(unclaimedMeta1);
                                        inv.setItem(i, unclaimed1);
                                    }
                                    p.openInventory(inv);
                                }
                            }


                        }
                    } else {
                        p.sendMessage(gunlukOdul.getConfig().getString("menu_not_found"));
                    }

                } else if (args.length == 2 && args[0].equalsIgnoreCase("sil")) {
                    if (p.hasPermission("op")) {
                        File file = new File(gunlukOdul.getDataFolder().getAbsolutePath() + "/menus", args[1] + ".yml");
                        if (file.exists()) {
                            file.delete();
                            try (PreparedStatement preparedStatement1 = gunlukOdul.getDatabase().getConnection().prepareStatement("DELETE " + args[1])) {

                            }
                            p.sendMessage(gunlukOdul.getConfig().getString("menu_deleted"));
                        } else {
                            p.sendMessage(gunlukOdul.getConfig().getString("menu_not_found"));

                        }
                    } else {
                        p.sendMessage("Yetkin yok");
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InvalidConfigurationException e) {
                throw new RuntimeException(e);
            }
        } else {
            sender.sendMessage("Sadece oyuncular bu komudu kullanabilir");
        }
        return false;
    }

}


