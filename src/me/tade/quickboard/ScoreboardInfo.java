package me.tade.quickboard;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScoreboardInfo {

    private YamlConfiguration cfg;
    private String permission;
    private List<String> text = new ArrayList<>();
    private List<String> title = new ArrayList<>();
    private int titleUpdate = 2;
    private int textUpdate = 5;
    private List<String> enabledWorlds = new ArrayList<>();
    private HashMap<String, List<String>> changeText = new HashMap<>();
    private HashMap<String, Integer> changeTextInterval = new HashMap<>();
    private HashMap<String, Scroller> scrollerText = new HashMap<>();
    private HashMap<String, Integer> scrollerInterval = new HashMap<>();

    public ScoreboardInfo(YamlConfiguration config, String permission) {
        cfg = config;
        this.permission = permission;

        initialize();
    }

    protected void initialize() {
        text = cfg.getStringList("text");
        title = cfg.getStringList("title");
        titleUpdate = cfg.getInt("updater.title", 2);
        textUpdate = cfg.getInt("updater.text", 5);
        if (cfg.getConfigurationSection("changeableText") != null) {
            for (String s : cfg.getConfigurationSection("changeableText").getKeys(false)) {
                if (cfg.getStringList("changeableText." + s + ".text") == null) {
                    break;
                }
                changeText.put(s, cfg.getStringList("changeableText." + s + ".text"));
                changeTextInterval.put(s, cfg.getInt("changeableText." + s + ".interval", 30));
            }
        }
        if (cfg.getConfigurationSection("scroller") != null) {
            for (String s : cfg.getConfigurationSection("scroller").getKeys(false)) {
                String text = cfg.getString("scroller." + s + ".text");
                if (text == null) {
                    break;
                }
                scrollerText.put(s, new Scroller(text, cfg.getInt("scroller." + s + ".width", 26), cfg.getInt("scroller." + s + ".spaceBetween", 6), '&'));
                scrollerInterval.put(s, cfg.getInt("scroller." + s + ".update", 1));
            }
        }
        if (cfg.getStringList("enabledWorlds").isEmpty()) {
            enabledWorlds = null;
        } else {
            enabledWorlds = cfg.getStringList("enabledWorlds");
        }
    }

    public HashMap<String, List<String>> getChangeText() {
        return changeText;
    }

    public HashMap<String, Integer> getChangeTextInterval() {
        return changeTextInterval;
    }

    public HashMap<String, Scroller> getScrollerText() {
        return scrollerText;
    }

    public HashMap<String, Integer> getScrollerInterval() {
        return scrollerInterval;
    }

    public int getTitleUpdate() {
        return titleUpdate;
    }

    public int getTextUpdate() {
        return textUpdate;
    }

    public List<String> getEnabledWorlds() {
        return enabledWorlds;
    }

    public String getPermission() {
        return permission;
    }

    public List<String> getText() {
        return text;
    }

    public List<String> getTitle() {
        return title;
    }
}
