package me.tade.quickboard;

import me.tade.quickboard.api.QuickBoardAPI;
import me.tade.quickboard.cmds.Commands;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuickBoard extends JavaPlugin implements Listener {

    private HashMap<Player, PlayerBoard> boards = new HashMap<>();
    private List<PlayerBoard> allboards = new ArrayList<>();
    private HashMap<String, ScoreboardInfo> info = new HashMap<>();
    private boolean allowedJoinScoreboard;
    private boolean MVdWPlaceholderAPI, PlaceholderAPI;
    private HashMap<Player, Long> playerWorldTimer = new HashMap<>();
    private PluginUpdater pluginUpdater;
    private boolean firstTimeUsePlugin = false;
    private Metrics metrics;

    @Override
    public void onEnable() {
        metrics = new Metrics(this);

        new QuickBoardAPI(this);

        getLogger().info("------------------------------");
        getLogger().info("          QuickBoard          ");
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("Listeners registered");

        pluginUpdater = new PluginUpdater(this);

        getLogger().info("Loading scoreboards");

        File fo = new File(getDataFolder().getAbsolutePath() + "/scoreboards/");
        if (!fo.exists()) {
            fo.mkdirs();

            firstTimeUsePlugin = true;

            File scFile = new File(getDataFolder().getAbsolutePath() + "/scoreboards/scoreboard.default.yml");
            try {
                scFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            InputStream str = getResource("scoreboard.default.yml");
            try {
                copy(str, scFile);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        loadScoreboards(fo);
        getLogger().info("Scoreboards loaded");

        getConfig().options().copyDefaults(true);
        saveConfig();
        getLogger().info("Config registered");

        allowedJoinScoreboard = getConfig().getBoolean("scoreboard.onjoin.use");
        MVdWPlaceholderAPI = getConfig().getBoolean("scoreboard.MVdWPlaceholderAPI", false);
        PlaceholderAPI = getConfig().getBoolean("scoreboard.PlaceholderAPI", true);

        if (isMVdWPlaceholderAPI()) {
            if (!Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
                setMVdWPlaceholderAPI(false);
            }
        }
        if (isPlaceholderAPI()) {
            if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                setPlaceholderAPI(false);
            }
        }

        metrics.addCustomChart(new Metrics.SingleLineChart("scoreboards") {
            public int getValue() {
                return info.size();
            }
        });

        getLogger().info("Update Tasks started");

        try {
            getCommand("quickboard").setExecutor(new Commands(this));
            getLogger().info("Command 'quickboard' enabled");
        } catch (NullPointerException ex) {
            getLogger().info("Command 'quickboard' disabled, error was occurred");
        }
        getLogger().info("          QuickBoard          ");
        getLogger().info("------------------------------");

        for (Player p : Bukkit.getOnlinePlayers()) {
            playerWorldTimer.put(p, System.currentTimeMillis() + 5000);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : playerWorldTimer.keySet()) {
                    long time = playerWorldTimer.get(player);

                    if (time < System.currentTimeMillis())
                        continue;

                    if (isAllowedJoinScoreboard())
                        createDefaultScoreboard(player);
                }
            }
        }.runTaskTimer(this, 0, 20);
    }

    @Override
    public void onDisable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final Player player = e.getPlayer();

        if(firstTimeUsePlugin && (player.isOp() || player.hasPermission("quickboard.creator"))){
            new BukkitRunnable(){
                @Override
                public void run() {
                    player.sendMessage(" ");

                    player.sendMessage("                        §6§lQuickBoard                        ");
                    player.sendMessage("§7Hi, thank you for using §6§lQuickBoard§7! Version: " + getDescription().getVersion());

                    if(!metrics.isEnabled()){
                        player.sendMessage("§7If you want to keep me motivated to create more for free, please enable your bStats! It will help me a lot in future development!");
                    }

                    player.sendMessage(" ");
                    player.sendMessage("§7Plugin just created a new default scoreboard! Your default scoreboard is named §6scoreboard.default §7with permission as the §6name of the scoreboard!");
                    player.sendMessage(" ");
                    player.sendMessage("§7In QuickBoard files you can see this scoreboard as §6scoreboard.default.yml");
                    player.sendMessage(" ");
                    player.sendMessage("§7Default scoreboard is being displayed on the right side of your Minecraft client!");
                    player.sendMessage(" ");
                    for (String s : info.keySet()) {
                        ScoreboardInfo in = info.get(s);

                        new PlayerBoard((QuickBoard) metrics.getPlugin(), player, in);
                    }

                    if(isPlaceholderAPI()){
                        player.sendMessage("§7To parse Placeholders, please download them using command §6/papi ecloud download <Expansion name>");
                        player.sendMessage("§7For example download Player placeholders: §6/papi ecloud download Player");
                        player.sendMessage("§7and download Server placeholders: §6/papi ecloud download Server");
                        player.sendMessage(" ");
                        player.sendMessage("§7After that please restart your server and changes will be made!");
                    }else {
                        player.sendMessage("§aIf you want more placeholders, please download PlaceholderAPI from here: §6https://www.spigotmc.org/resources/6245/");
                        player.sendMessage(" ");
                        player.sendMessage("§aIf you are using some of the §6Maximvdw §aplugins, use MVdWPlaceholders from here: §6https://www.spigotmc.org/resources/11182/");
                    }

                    player.sendMessage(" ");
                    player.sendMessage("§cIf you find an error or a bug, please report it on QuickBoard Discussion - https://www.spigotmc.org/threads/105755/ or on the GitHub - https://github.com/TheTadeSK/QuickBoard/issues");
                }
            }.runTaskLater(this, 45);
        }else
            playerWorldTimer.put(player, System.currentTimeMillis() + 3000);

        if(!pluginUpdater.needUpdate())
            return;

        if(player.isOp() || player.hasPermission("quickboard.update.info")){
            sendUpdateMessage();
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        final Player p = e.getPlayer();

        if (!e.getFrom().getWorld().getName().equals(e.getTo().getWorld().getName()))
            playerWorldTimer.put(p, System.currentTimeMillis() + 3000);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        final Player p = e.getPlayer();

        playerWorldTimer.put(p, System.currentTimeMillis() + 3000);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        playerWorldTimer.remove(e.getPlayer());

        if (boards.containsKey(e.getPlayer())) {
            boards.get(e.getPlayer()).stopTasks();
            boards.remove(e.getPlayer());
            allboards.remove(this);
        }
    }

    public void createDefaultScoreboard(Player player) {
        for (String s : info.keySet()) {
            ScoreboardInfo in = info.get(s);
            if (in.getEnabledWorlds() != null && in.getEnabledWorlds().contains(player.getWorld().getName())) {
                if (player.hasPermission(s)) {
                    if (boards.containsKey(player)) {
                        if (boards.get(player).getList().equals(in.getText())) {
                            player.setScoreboard(boards.get(player).getBoard());
                            return;
                        }
                        boards.get(player).createNew(in.getText(), in.getTitle(), in.getTitleUpdate(), in.getTextUpdate());
                    } else {
                        new PlayerBoard(this, player, info.get(s));
                    }
                    return;
                }
            }
        }
    }


    public void loadScoreboards(File fo) {
        if (fo.listFiles() == null) {
            return;
        }

        if (fo.listFiles().length <= 0) {
            return;
        }

        for (File f : fo.listFiles()) {
            if (f.getName().endsWith(".yml")) {
                String perm = f.getName().replace(".yml", "");
                YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
                ScoreboardInfo info = new ScoreboardInfo(cfg, perm);
                this.info.put(perm, info);
                getLogger().info("Loaded '" + f.getName() + "' with permission '" + perm + "'");
            } else {
                getLogger().warning("File '" + f.getName() + "' is not accepted! Accepted only '.yml' files (YAML)");
            }
        }
    }

    public void copy(InputStream src, File dst) throws IOException {
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                byte[] buf = new byte[1024];
                int len;
                while ((len = src.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            src.close();
        }
    }

    public boolean isAllowedJoinScoreboard() {
        return allowedJoinScoreboard;
    }

    public void setAllowedJoinScoreboard(boolean allowedJoinScoreboard) {
        this.allowedJoinScoreboard = allowedJoinScoreboard;
    }

    public boolean isMVdWPlaceholderAPI() {
        return MVdWPlaceholderAPI;
    }

    public boolean isPlaceholderAPI() {
        return PlaceholderAPI;
    }

    public void setMVdWPlaceholderAPI(boolean MVdWPlaceholderAPI) {
        this.MVdWPlaceholderAPI = MVdWPlaceholderAPI;
    }

    public void setPlaceholderAPI(boolean PlaceholderAPI) {
        this.PlaceholderAPI = PlaceholderAPI;
    }

    public void sendUpdateMessage(){
        new BukkitRunnable(){
            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()){
                    if(player.isOp() || player.hasPermission("quickboard.update.info")){
                        player.sendMessage(" ");
                        player.sendMessage("§7[§6QuickBoard§7]  §6A new update has come! Released on §a" + pluginUpdater.getUpdateInfo()[1]);
                        player.sendMessage("§7[§6QuickBoard§7]  §6New version number/your current version §a" + pluginUpdater.getUpdateInfo()[0] + "§7/§c" + getDescription().getVersion());
                        player.sendMessage("§7[§6QuickBoard§7]  §6Download update here: §ahttps://www.spigotmc.org/resources/15057/");
                    }
                }
            }
        }.runTaskLater(this, 20);
    }

    public HashMap<Player, PlayerBoard> getBoards() {
        return boards;
    }

    public List<PlayerBoard> getAllboards() {
        return allboards;
    }

    public HashMap<String, ScoreboardInfo> getInfo() {
        return info;
    }

    public HashMap<Player, Long> getPlayerWorldTimer() {
        return playerWorldTimer;
    }

    public PluginUpdater getPluginUpdater() {
        return pluginUpdater;
    }

    public void disableFirstTimeUse(){
        firstTimeUsePlugin = false;
    }
}
