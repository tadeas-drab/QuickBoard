package me.tade.quickboard;

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

    public static QuickBoard instance;
    public HashMap<Player, PlayerBoard> boards = new HashMap<>();
    public List<PlayerBoard> allboards = new ArrayList<>();
    public HashMap<String, ScoreboardInfo> info = new HashMap<>();
    private boolean allowedJoinScoreboard;
    private boolean MVdWPlaceholderAPI, PlaceholderAPI = false;
    private HashMap<Player, Long> playerWorldTimer = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        Metrics metrics = new Metrics(this);

        getLogger().info("------------------------------");
        getLogger().info("          QuickBoard          ");
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("Listeners registered");

        getLogger().info("Loading scoreboards");

        File fo = new File(getDataFolder().getAbsolutePath() + "/scoreboards/");
        if (!fo.exists()) {
            fo.mkdirs();

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
        MVdWPlaceholderAPI = getConfig().getBoolean("scoreboard.MVdWPlaceholderAPI");
        PlaceholderAPI = getConfig().getBoolean("scoreboard.PlaceholderAPI");

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
            getCommand("quickboard").setExecutor(new Commands());
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
        final Player p = e.getPlayer();

        playerWorldTimer.put(p, System.currentTimeMillis() + 5000);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        final Player p = e.getPlayer();

        if (!e.getFrom().getWorld().getName().equals(e.getTo().getWorld().getName()))
            playerWorldTimer.put(p, System.currentTimeMillis() + 5000);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        final Player p = e.getPlayer();

        playerWorldTimer.put(p, System.currentTimeMillis() + 5000);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        playerWorldTimer.remove(e.getPlayer());

        if (boards.containsKey(e.getPlayer())) {
            boards.get(e.getPlayer()).stopTasks();
            QuickBoard.instance.boards.remove(e.getPlayer());
            QuickBoard.instance.allboards.remove(this);
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
                        new PlayerBoard(player, info.get(s));
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
}
