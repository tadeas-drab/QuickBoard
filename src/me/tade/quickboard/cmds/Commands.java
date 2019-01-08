package me.tade.quickboard.cmds;

import me.tade.quickboard.PlayerBoard;
import me.tade.quickboard.QuickBoard;
import me.tade.quickboard.ScoreboardInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Commands implements CommandExecutor {

    private QuickBoard plugin;

    public Commands(QuickBoard plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§7[§6QuickBoard§7] §7Welcome §6" + sender.getName() + " §7to the QuickBoard commands!");
            sender.sendMessage("§7[§6QuickBoard§7] §7To show all commands use §6/quickboard cmds");
            sender.sendMessage("§7[§6QuickBoard§7] §7Plugin by §6The_TadeSK§7, version: §6" + plugin.getDescription().getVersion());
            return true;
        }
        if (args[0].equalsIgnoreCase("cmds")) {
            sender.sendMessage("§7[§6QuickBoard§7] Toggle scoreboard §6/quickboard toggle");
            sender.sendMessage("§7[§6QuickBoard§7] Reload config file §6/quickboard reload");
            sender.sendMessage("§7[§6QuickBoard§7] Toggle custom scoreboard with name(permission) §6/quickboard set <Player> <permission(name)>");
            sender.sendMessage("§7[§6QuickBoard§7] View all available scoreboard §6/quickboard list");
            sender.sendMessage("§7[§6QuickBoard§7] Create scoreboard with name(permission) §6/quickboard create <permission(name)>");
            sender.sendMessage("§7[§6QuickBoard§7] Edit every scoreboard with name(permission) §6/quickboard edit <permission(name)>");
        }
        if (args[0].equalsIgnoreCase("toggle")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getConfig().getString("messages.noperms").replace("&", "§"));
                return true;
            }
            Player p = (Player) sender;
            String text = "";
            if (plugin.getBoards().containsKey(p)) {
                plugin.getBoards().get(p).remove();
                text = plugin.getConfig().getString("messages.ontoggle.false");
            } else {
                text = plugin.getConfig().getString("messages.ontoggle.true");

                plugin.createDefaultScoreboard(p);
            }
            p.sendMessage(text.replace("&", "§"));
        }
        if (args[0].equalsIgnoreCase("on")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getConfig().getString("messages.noperms").replace("&", "§"));
                return true;
            }
            Player p = (Player) sender;
            String text = "";
            if (plugin.getBoards().containsKey(p)) {
                plugin.getBoards().get(p).remove();
                plugin.getBoards().remove(p);
            }
            if (!plugin.getBoards().containsKey(p)) {
                text = plugin.getConfig().getString("messages.ontoggle.true");

                plugin.createDefaultScoreboard(p);
            }
            p.sendMessage(text.replace("&", "§"));
        }
        if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("quickboard.edit")) {
                sender.sendMessage(plugin.getConfig().getString("messages.noperms").replace("&", "§"));
                return true;
            }
            File fo = new File(plugin.getDataFolder().getAbsolutePath() + "/scoreboards");
            plugin.loadScoreboards(fo);
            sender.sendMessage("§cAll Loaded scoreboards:");
            for (String name : plugin.getInfo().keySet()) {
                sender.sendMessage("§a- '" + name + "'");
            }
        }
        if (args[0].equalsIgnoreCase("check")) {
            if (!sender.hasPermission("quickboard.check")) {
                sender.sendMessage(plugin.getConfig().getString("messages.noperms").replace("&", "§"));
                return true;
            }
            Player player = (Player) sender;
            File fo = new File(plugin.getDataFolder().getAbsolutePath() + "/scoreboards");
            plugin.loadScoreboards(fo);
            sender.sendMessage("§cScoreboard and info:");
            for (String s : plugin.getInfo().keySet()) {
                String output = "Scoreboard='" + s + "'";
                ScoreboardInfo in = plugin.getInfo().get(s);
                output += " in enabled worlds=" + (in.getEnabledWorlds() != null && in.getEnabledWorlds().contains(player.getWorld().getName()));
                output += " has permission=" + player.hasPermission(s);
                sender.sendMessage(output);
            }
        }
        if (args[0].equalsIgnoreCase("set")) {
            if (!sender.hasPermission("quickboard.set")) {
                sender.sendMessage(plugin.getConfig().getString("messages.noperms").replace("&", "§"));
                return false;
            }
            if (args.length != 3) {
                sender.sendMessage("§cUse: /quickboard set <Player> <permission(name)>");
                return true;
            }
            Player p = Bukkit.getPlayer(args[1]);
            if (p == null) {
                sender.sendMessage("§cPlayer doesn't exists!");
                return true;
            }
            String perm = args[2];
            if (!plugin.getInfo().containsKey(perm)) {
                sender.sendMessage("§cScoreboard doesn't exists!");
                return true;
            }
            if (plugin.getBoards().containsKey(p)) {
                ScoreboardInfo in = plugin.getInfo().get(perm);
                plugin.getBoards().get(p).createNew(in.getText(), in.getTitle(), in.getTitleUpdate(), in.getTextUpdate());
            } else {
                new PlayerBoard(plugin, p, plugin.getInfo().get(perm));
            }
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("quickboard.reload")) {
                sender.sendMessage(plugin.getConfig().getString("messages.noperms").replace("&", "§"));
                return true;
            }
            sender.sendMessage("§7[§6QuickBoard§7] §cReloading config file");
            plugin.reloadConfig();
            File fo = new File(plugin.getDataFolder().getAbsolutePath() + "/scoreboards");
            plugin.loadScoreboards(fo);
            sender.sendMessage("§7[§6QuickBoard§7] §aConfig file reloaded");
            sender.sendMessage("§7[§6QuickBoard§7] §cCreating scoreboard for all players");
            sender.sendMessage(" ");
            plugin.setAllowedJoinScoreboard(plugin.getConfig().getBoolean("scoreboard.onjoin.use"));
            if (!plugin.isAllowedJoinScoreboard()) {
                sender.sendMessage("§7[§6QuickBoard§7] §cCreating scoreboard failed! Creating scoreboard when player join to server is cancelled!");
                return true;
            }
            plugin.disableFirstTimeUse();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (plugin.getBoards().containsKey(p)) {
                    plugin.getBoards().get(p).remove();
                    plugin.getBoards().remove(p);
                }
                plugin.createDefaultScoreboard(p);
            }
            sender.sendMessage("§7[§6QuickBoard§7] §aScoreboard created for all");
        }
        if (args[0].equalsIgnoreCase("create")) {
            if (!sender.hasPermission("quickboard.create")) {
                sender.sendMessage(plugin.getConfig().getString("messages.noperms").replace("&", "§"));
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("You must be player to perform this command!");
                return true;
            }
            Player player = (Player) sender;
            File def = new File(plugin.getDataFolder().getAbsolutePath() + "/scoreboards");
            if (args.length < 2) {
                sender.sendMessage("§7[§6QuickBoard§7] §cUse /qb create <Permission(Name)>");
                return true;
            }
            Bukkit.broadcastMessage(args.length + "");
            String name = args[1];
            File boardFile = new File(def, name + ".yml");
            if (boardFile.exists()) {
                sender.sendMessage("§7[§6QuickBoard§7] §cThis name is already in use!");
                return true;
            }
            try {
                boardFile.createNewFile();
                sender.sendMessage("§7[§6QuickBoard§7] §aFile created! Now edit scoreboard §6/qb edit " + name);

                YamlConfiguration cfg = YamlConfiguration.loadConfiguration(boardFile);
                cfg.set("title", Arrays.asList("This is default title!", "You can edit it any time!"));
                cfg.set("text", Arrays.asList("This is default text!", "You can edit it any time!"));
                cfg.set("updater.text", 20);
                cfg.set("updater.title", 30);
                cfg.set("enabledWorlds", Collections.singletonList(player.getWorld().getName()));

                cfg.save(boardFile);
            } catch (IOException e) {
                e.printStackTrace();
                sender.sendMessage("§7[§6QuickBoard§7] §cAn error was occured while creating file!");
            }
        }
        if (args[0].equalsIgnoreCase("edit")) {
            if (!sender.hasPermission("quickboard.edit")) {
                sender.sendMessage(plugin.getConfig().getString("messages.noperms").replace("&", "§"));
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage("You must be player to perform this command!");
                return true;
            }
            File def = new File(plugin.getDataFolder().getAbsolutePath() + "/scoreboards");
            if (args.length <= 2) {
                sender.sendMessage("§7[§6QuickBoard§7] §cUse /qb edit <Permission(Name)>");
                return true;
            }
            String name = args[1];
            File boardFile = new File(def, name + ".yml");
            if (!boardFile.exists()) {
                sender.sendMessage("§7[§6QuickBoard§7] §cThis name doesn't exist!");
                return true;
            }
            if (args.length == 2) {
                sender.sendMessage("§7[§6QuickBoard§7] §cOptions for scoreboard:");
                sender.sendMessage("§7[§6QuickBoard§7] §a/qb edit " + name + " addline <text>");
                sender.sendMessage("§7[§6QuickBoard§7] §a/qb edit " + name + " removeline <number>");
                sender.sendMessage("§7[§6QuickBoard§7] §a/qb edit " + name + " insertline <number> <text>");
                sender.sendMessage("§7[§6QuickBoard§7] §cOptions for title:");
                sender.sendMessage("§7[§6QuickBoard§7] §a/qb edit " + name + " addtitle <text>");
                sender.sendMessage("§7[§6QuickBoard§7] §a/qb edit " + name + " removetitle <number>");
                sender.sendMessage("§7[§6QuickBoard§7] §a/qb edit " + name + " inserttitle <number> <text>");
                return true;
            }
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(boardFile);
            if (args[2].equalsIgnoreCase("addline")) {
                if (args.length < 4) {
                    sender.sendMessage("§7[§6QuickBoard§7] §aAdded empty line!");
                    List<String> text = cfg.getStringList("text");
                    text.add(" ");
                    cfg.set("text", text);
                } else {
                    StringBuilder lineBuilder = new StringBuilder();
                    for (int i = 3; i < args.length; i++) {
                        lineBuilder.append(" ").append(args[i]);
                    }
                    String line = lineBuilder.toString();
                    line = line.substring(1);
                    List<String> text = cfg.getStringList("text");
                    text.add(line);
                    cfg.set("text", text);
                    sender.sendMessage("§7[§6QuickBoard§7] §aAdded line '" + line + "'!");
                }
                saveFileAndCreateBoard(cfg, boardFile, (Player) sender, name);
            } else if (args[2].equalsIgnoreCase("removeline")) {
                if (args.length < 4) {
                    sender.sendMessage("§7[§6QuickBoard§7] §cPlease specify a number!");
                } else {
                    int num = 1;
                    try {
                        num = Integer.parseInt(args[3]);
                    } catch (IllegalArgumentException ex) {
                        sender.sendMessage("§7[§6QuickBoard§7] §cThis is not a number!");
                        return true;
                    }
                    if (num <= 0) {
                        sender.sendMessage("§7[§6QuickBoard§7] §cThis number is not valid! Use 1, 2, 3, ...");
                        return true;
                    }
                    List<String> text = cfg.getStringList("text");
                    if (text.size() > (num - 1)) {
                        text.remove((num - 1));
                        sender.sendMessage("§7[§6QuickBoard§7] §aLine removed!");
                        cfg.set("text", text);
                    } else {
                        sender.sendMessage("§7[§6QuickBoard§7] §cYou are out of bound!");
                    }
                    saveFileAndCreateBoard(cfg, boardFile, (Player) sender, name);
                }
            } else if (args[2].equalsIgnoreCase("insertline")) {
                if (args.length < 5) {
                    int num = 1;
                    try {
                        num = Integer.parseInt(args[3]);
                    } catch (IllegalArgumentException ex) {
                        sender.sendMessage("§7[§6QuickBoard§7] §cThis is not a number!");
                        return true;
                    }
                    if (num <= 0) {
                        sender.sendMessage("§7[§6QuickBoard§7] §cThis number is not valid! Use 1, 2, 3, ...");
                        return true;
                    }
                    List<String> text = cfg.getStringList("text");
                    if (text.size() > (num - 1)) {
                        text.set((num - 1), " ");
                        cfg.set("text", text);
                        sender.sendMessage("§7[§6QuickBoard§7] §aInserted empty line!");
                    } else {
                        sender.sendMessage("§7[§6QuickBoard§7] §cYou are out of bound!");
                    }
                } else {
                    int num = 1;
                    try {
                        num = Integer.parseInt(args[3]);
                    } catch (IllegalArgumentException ex) {
                        sender.sendMessage("§7[§6QuickBoard§7] §cThis is not a number!");
                        return true;
                    }
                    if (num <= 0) {
                        sender.sendMessage("§7[§6QuickBoard§7] §cThis number is not valid! Use 1, 2, 3, ...");
                        return true;
                    }
                    StringBuilder lineBuilder = new StringBuilder();
                    for (int i = 4; i < args.length; i++) {
                        lineBuilder.append(" ").append(args[i]);
                    }
                    String line = lineBuilder.toString();
                    line = line.substring(1);
                    List<String> text = cfg.getStringList("text");
                    if (text.size() > (num - 1)) {
                        text.set((num - 1), line);
                        cfg.set("text", text);
                        sender.sendMessage("§7[§6QuickBoard§7] §aInserted '" + line + "' into line!");
                    } else {
                        sender.sendMessage("§7[§6QuickBoard§7] §cYou are out of bound!");
                    }
                }
                saveFileAndCreateBoard(cfg, boardFile, (Player) sender, name);
            }

            //Title
            if (args[2].equalsIgnoreCase("addtitle")) {
                if (args.length < 4) {
                    sender.sendMessage("§7[§6QuickBoard§7] §aAdded empty line!");
                    List<String> text = cfg.getStringList("title");
                    text.add(" ");
                    cfg.set("title", text);
                } else {
                    StringBuilder lineBuilder = new StringBuilder();
                    for (int i = 3; i < args.length; i++) {
                        lineBuilder.append(" ").append(args[i]);
                    }
                    String line = lineBuilder.toString();
                    line = line.substring(1);
                    List<String> text = cfg.getStringList("title");
                    text.add(line);
                    cfg.set("title", text);
                    sender.sendMessage("§7[§6QuickBoard§7] §aAdded line '" + line + "'!");
                }
                saveFileAndCreateBoard(cfg, boardFile, (Player) sender, name);
            } else if (args[2].equalsIgnoreCase("removetitle")) {
                if (args.length < 4) {
                    sender.sendMessage("§7[§6QuickBoard§7] §cPlease specify a number!");
                } else {
                    int num = 1;
                    try {
                        num = Integer.parseInt(args[3]);
                    } catch (IllegalArgumentException ex) {
                        sender.sendMessage("§7[§6QuickBoard§7] §cThis is not a number!");
                        return true;
                    }
                    if (num <= 0) {
                        sender.sendMessage("§7[§6QuickBoard§7] §cThis number is not valid! Use 1, 2, 3, ...");
                        return true;
                    }
                    List<String> text = cfg.getStringList("title");
                    if (text.size() > (num - 1)) {
                        text.remove((num - 1));
                        sender.sendMessage("§7[§6QuickBoard§7] §aLine removed!");
                        cfg.set("title", text);
                    } else {
                        sender.sendMessage("§7[§6QuickBoard§7] §cYou are out of bound!");
                    }
                    saveFileAndCreateBoard(cfg, boardFile, (Player) sender, name);
                }
            } else if (args[2].equalsIgnoreCase("inserttitle")) {
                if (args.length < 5) {
                    int num = 1;
                    try {
                        num = Integer.parseInt(args[3]);
                    } catch (IllegalArgumentException ex) {
                        sender.sendMessage("§7[§6QuickBoard§7] §cThis is not a number!");
                        return true;
                    }
                    if (num <= 0) {
                        sender.sendMessage("§7[§6QuickBoard§7] §cThis number is not valid! Use 1, 2, 3, ...");
                        return true;
                    }
                    List<String> text = cfg.getStringList("title");
                    if (text.size() > (num - 1)) {
                        text.set(num, " ");
                        cfg.set("title", text);
                        sender.sendMessage("§7[§6QuickBoard§7] §aInserted empty line!");
                    } else {
                        sender.sendMessage("§7[§6QuickBoard§7] §cYou are out of bound!");
                    }
                } else {
                    int num = 1;
                    try {
                        num = Integer.parseInt(args[3]);
                    } catch (IllegalArgumentException ex) {
                        sender.sendMessage("§7[§6QuickBoard§7] §cThis is not a number!");
                        return true;
                    }
                    if (num <= 0) {
                        sender.sendMessage("§7[§6QuickBoard§7] §cThis number is not valid! Use 1, 2, 3, ...");
                        return true;
                    }
                    StringBuilder lineBuilder = new StringBuilder();
                    for (int i = 4; i < args.length; i++) {
                        lineBuilder.append(" ").append(args[i]);
                    }
                    String line = lineBuilder.toString();
                    line = line.substring(1);
                    List<String> text = cfg.getStringList("title");
                    if (text.size() > (num - 1)) {
                        text.set((num - 1), line);
                        cfg.set("title", text);
                        sender.sendMessage("§7[§6QuickBoard§7] §aInserted '" + line + "' into line!");
                    } else {
                        sender.sendMessage("§7[§6QuickBoard§7] §cYou are out of bound!");
                    }
                }
                saveFileAndCreateBoard(cfg, boardFile, (Player) sender, name);
            }
        }
        return false;
    }

    public void saveFileAndCreateBoard(YamlConfiguration config, File file, Player player, String name){
        File def = new File(plugin.getDataFolder().getAbsolutePath() + "/scoreboards");
        try {
            config.save(file);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        plugin.loadScoreboards(def);
        if (plugin.getBoards().containsKey(player)) {
            ScoreboardInfo in = plugin.getInfo().get(name);
            plugin.getBoards().get(player).createNew(in.getText(), in.getTitle(), in.getTitleUpdate(), in.getTextUpdate());
        } else {
            new PlayerBoard(plugin, player, plugin.getInfo().get(name));
        }
    }

}
