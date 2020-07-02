package me.tade.quickboard;

import me.clip.placeholderapi.PlaceholderAPI;
import me.tade.quickboard.events.PlayerReceiveBoardEvent;
import me.tade.quickboard.events.WhenPluginUpdateTextEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class PlayerBoard {

    private QuickBoard plugin;

    private Scoreboard board;
    private Objective score;
    private Player player;

    private List<Team> teams = new ArrayList<>();
    private HashMap<Team, String> lot = new HashMap<>();
    private List<String> list;
    private List<String> title;
    private List<String> chlist = new ArrayList<>();
    private int updateTitle;
    private int updateText;
    private int titleTask;
    private int textTask;
    private boolean ch = false;
    private ScoreboardInfo info = null;
    private HashMap<String, String> chanText = new HashMap<>();
    private HashMap<String, Integer> chanTextInt = new HashMap<>();
    private HashMap<String, String> scrollerText = new HashMap<>();
    private List<Integer> tasks = new ArrayList<>();
    private boolean ver13 = false;

    private int index = 15;
    private int titleindex = 0;

    public PlayerBoard(QuickBoard plugin, Player player, ScoreboardInfo info) {
        this.plugin = plugin;
        this.player = player;
        list = info.getText();
        title = info.getTitle();
        updateTitle = info.getTitleUpdate();
        updateText = info.getTextUpdate();

        for (String s : info.getChangeText().keySet()) {
            chanTextInt.put(s, 0);
            chanText.put(s, info.getChangeText().get(s).get(0));
        }

        this.info = info;

        PlayerReceiveBoardEvent event = new PlayerReceiveBoardEvent(getPlayer(), list, title, this);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            startSetup(event);
        }
    }

    public PlayerBoard(QuickBoard plugin, Player player, List<String> text, List<String> title, int updateTitle, int updateText) {
        this.plugin = plugin;
        this.player = player;
        this.updateTitle = updateTitle;
        this.updateText = updateText;

        this.title = title;
        this.list = text;

        PlayerReceiveBoardEvent event = new PlayerReceiveBoardEvent(getPlayer(), this.list, this.title, this);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            startSetup(event);
        }
    }

    public void startSetup(final PlayerReceiveBoardEvent event) {
        if (plugin.getBoards().containsKey(getPlayer())) {
            plugin.getBoards().get(getPlayer()).remove();
        }
        ver13 = Integer.parseInt(Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].split("_")[1]) >= 13;
        colorize();
        titleindex = event.getTitle().size();
        plugin.getBoards().put(getPlayer(), this);
        plugin.getAllboards().add(this);

        buildScoreboard(event);

        setUpText(event.getText());

        updater();
    }

    public void buildScoreboard(PlayerReceiveBoardEvent event) {
        board = Bukkit.getScoreboardManager().getNewScoreboard();
        score = board.registerNewObjective("score", "dummy");
        score.setDisplaySlot(DisplaySlot.SIDEBAR);
        if (event.getTitle().size() == 0)
            event.getTitle().add(" ");

        score.setDisplayName(event.getTitle().get(0));

        getPlayer().setScoreboard(board);
    }

    public void setUpText(final List<String> text) {
        int oi = 0;
        Iterator<String> tex = text.iterator();
        while (tex.hasNext()) {
            String s = tex.next();
            Team t = board.registerNewTeam("Team:" + index);
            String normal = s;
            String sc = chlist.get(oi);
            t.addEntry(sc);
            s = setHolders(s);
            if (s.length() < 3) {
                s = s + "§r";
            }
            String[] ts = splitString(s);
            setPrefix(t, ts[0]);
            setSuffix(t, ts[1]);
            score.getScore(sc).setScore(index);
            teams.add(t);
            lot.put(t, normal);
            index--;
            oi++;
        }
    }

    public void colorize() {
        for (ChatColor color : ChatColor.values()) {
            chlist.add(color.toString() + ChatColor.RESET.toString());
        }
    }

    public void updateText() {
        if (ch) {
            return;
        }

        Iterator<Team> teams = this.teams.iterator();

        while (teams.hasNext()) {
            Team t = teams.next();
            String s = lot.get(t);
            if (info != null) {
                for (String a : info.getChangeText().keySet()) {
                    if (s.contains("{CH_" + a + "}")) {
                        s = s.replace("{CH_" + a + "}", "");
                        s = s + chanText.get(a);
                    }
                }
                for (String a : info.getScrollerText().keySet()) {
                    if (s.contains("{SC_" + a + "}")) {
                        s = s.replace("{SC_" + a + "}", "");
                        s = s + scrollerText.get(a);
                    }
                }
            }
            s = setHolders(s);
            WhenPluginUpdateTextEvent event = new WhenPluginUpdateTextEvent(getPlayer(), s);
            Bukkit.getPluginManager().callEvent(event);

            String[] ts = splitString(event.getText());
            setPrefix(t, ts[0]);
            setSuffix(t, ts[1]);
        }
    }

    public void setPrefix(Team t, String string) {
        if (string.length() > getMaxSize()) {
            t.setPrefix(maxChars(getMaxSize(), string));
            return;
        }
        t.setPrefix(string);
    }

    public void setSuffix(Team t, String string) {
        if (string.length() > getMaxSize()) {
            t.setSuffix(maxChars(getMaxSize(), string));
            return;
        }
        t.setSuffix(string);
    }

    public String setHolders(String s) {
        s = s.replace("{PLAYER}", getPlayer().getName()).replace("{ONLINE}", Bukkit.getOnlinePlayers().size() + "")
                .replace("{TIME}", getPlayer().getWorld().getTime() + "");
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && PlaceholderAPI.containsPlaceholders(s))
            s = PlaceholderAPI.setPlaceholders(getPlayer(), s);
        if (plugin.isMVdWPlaceholderAPI())
            s = be.maximvdw.placeholderapi.PlaceholderAPI.replacePlaceholders(getPlayer(), s);

        s = ChatColor.translateAlternateColorCodes('&', s);
        return s;
    }

    public int getMaxSize() {
        if (ver13)
            return 64;
        return 16;
    }

    public void updateTitle() {
        if (ch) {
            return;
        }
        if (titleindex > (title.size() - 1)) {
            titleindex = 0;
        }
        score.setDisplayName(maxChars(ver13 ? 128 : 32, setHolders(title.get(titleindex))));
        titleindex++;
    }

    public String maxChars(int characters, String string) {
        if (ChatColor.translateAlternateColorCodes('&', string).length() > characters)
            return string.substring(0, characters);
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public void remove() {
        stopTasks();
        plugin.getBoards().remove(getPlayer());
        plugin.getAllboards().remove(this);
        getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    protected void stopTasks() {
        Bukkit.getScheduler().cancelTask(titleTask);
        Bukkit.getScheduler().cancelTask(textTask);

        for (int i : tasks)
            Bukkit.getScheduler().cancelTask(i);
    }

    public void updater() {
        titleTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                updateTitle();
            }
        }, 0, updateTitle);

        textTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
                updateText();
            }
        }, 0, updateText);

        if (info != null) {
            for (final String s : info.getChangeText().keySet()) {
                int inter = info.getChangeTextInterval().get(s);

                BukkitTask task = new BukkitRunnable() {
                    public void run() {
                        List<String> text = info.getChangeText().get(s);
                        chanTextInt.put(s, chanTextInt.get(s) + 1);

                        if (chanTextInt.get(s) >= text.size()) {
                            chanTextInt.put(s, 0);
                        }
                        int ta = chanTextInt.get(s);
                        chanText.put(s, text.get(ta));
                        updateText();
                    }
                }.runTaskTimer(plugin, 1, inter);

                tasks.add(task.getTaskId());
            }

            for (final String s : info.getScrollerText().keySet()) {
                int inter = info.getScrollerInterval().get(s);

                BukkitTask task = new BukkitRunnable() {
                    public void run() {
                        Scroller text = info.getScrollerText().get(s);
                        String txt = text.text;
                        text.setupText(setHolders(txt), text.width, text.spaceBetween, '&');
                        scrollerText.put(s, text.next());
                        updateText();
                    }
                }.runTaskTimer(plugin, 1, inter);

                tasks.add(task.getTaskId());
            }
        }
    }

    public void createNew(List<String> text, List<String> title, int updateTitle, int updateText) {
        ch = true;
        stopTasks();
        removeAll();
        colorize();
        this.list = text;
        this.title = title;
        this.updateText = updateText;
        this.updateTitle = updateTitle;
        titleindex = this.title.size();

        score = board.getObjective("score");

        score.setDisplaySlot(DisplaySlot.SIDEBAR);

        if (this.title.size() <= 0) {
            this.title.add(" ");
        }

        score.setDisplayName(this.title.get(0));

        setUpText(text);

        ch = false;
        updater();
    }

    private void removeAll() {
        chlist.clear();
        score = null;
        titleindex = 0;
        index = 15;
        lot.clear();
        teams.clear();

        for (Team t : board.getTeams()) {
            t.unregister();
        }

        for (String s : board.getEntries()) {
            board.resetScores(s);
        }
    }

    private String getResult(boolean BOLD, boolean ITALIC, boolean MAGIC, boolean STRIKETHROUGH, boolean UNDERLINE, ChatColor color) {
        return ((color != null) && (!color.equals(ChatColor.WHITE)) ? color : "") + "" + (BOLD ? ChatColor.BOLD : "") + (ITALIC ? ChatColor.ITALIC : "") + (MAGIC ? ChatColor.MAGIC : "") + (STRIKETHROUGH ? ChatColor.STRIKETHROUGH : "") + (UNDERLINE ? ChatColor.UNDERLINE : "");
    }

    private String[] splitString(String string) {
        StringBuilder prefix = new StringBuilder(string.substring(0, string.length() >= getMaxSize() ? getMaxSize() : string.length()));
        StringBuilder suffix = new StringBuilder(string.length() > getMaxSize() ? string.substring(getMaxSize()) : "");
        if (prefix.toString().length() > 1 && prefix.charAt(prefix.length() - 1) == '§') {
            prefix.deleteCharAt(prefix.length() - 1);
            suffix.insert(0, '§');
        }
        int length = prefix.length();
        boolean PASSED, UNDERLINE, STRIKETHROUGH, MAGIC, ITALIC;
        boolean BOLD = ITALIC = MAGIC = STRIKETHROUGH = UNDERLINE = PASSED = false;
        ChatColor textColor = null;
        for (int index = length - 1; index > -1; index--) {
            char section = prefix.charAt(index);
            if ((section == '§') && (index < prefix.length() - 1)) {
                char c = prefix.charAt(index + 1);
                ChatColor color = ChatColor.getByChar(c);
                if (color != null) {
                    if (color.equals(ChatColor.RESET)) {
                        break;
                    }
                    if ((textColor == null) && (color.isFormat())) {
                        if ((color.equals(ChatColor.BOLD)) && (!BOLD)) {
                            BOLD = true;
                        } else if ((color.equals(ChatColor.ITALIC)) && (!ITALIC)) {
                            ITALIC = true;
                        } else if ((color.equals(ChatColor.MAGIC)) && (!MAGIC)) {
                            MAGIC = true;
                        } else if ((color.equals(ChatColor.STRIKETHROUGH)) && (!STRIKETHROUGH)) {
                            STRIKETHROUGH = true;
                        } else if ((color.equals(ChatColor.UNDERLINE)) && (!UNDERLINE)) {
                            UNDERLINE = true;
                        }
                    } else if ((textColor == null) && (color.isColor())) {
                        textColor = color;
                    }
                }
            } else if ((index > 0) && (!PASSED)) {
                char c = prefix.charAt(index);
                char c1 = prefix.charAt(index - 1);
                if ((c != '§') && (c1 != '§') && (c != ' ')) {
                    PASSED = true;
                }
            }
            if ((!PASSED) && (prefix.charAt(index) != ' ')) {
                prefix.deleteCharAt(index);
            }
            if (textColor != null) {
                break;
            }
        }
        String result = suffix.toString().isEmpty() ? "" : getResult(BOLD, ITALIC, MAGIC, STRIKETHROUGH, UNDERLINE, textColor);
        if ((!suffix.toString().isEmpty()) && (!suffix.toString().startsWith("§"))) {
            suffix.insert(0, result);
        }
        return new String[]{prefix.toString().length() > getMaxSize() ? prefix.toString().substring(0, getMaxSize()) : prefix.toString(), suffix.toString().length() > getMaxSize() ? suffix.toString().substring(0, getMaxSize()) : suffix.toString()};
    }

    public Scoreboard getBoard() {
        return board;
    }

    public Objective getScore() {
        return score;
    }

    public Player getPlayer() {
        return player;
    }

    public List<String> getList() {
        return list;
    }

    public List<String> getTitle() {
        return title;
    }
}
