package me.tade.quickboard.api;

import me.tade.quickboard.PlayerBoard;
import me.tade.quickboard.QuickBoard;
import org.bukkit.entity.Player;

import java.util.List;

public class QuickBoardAPI {

    private static QuickBoard plugin;

    public QuickBoardAPI(QuickBoard plugin) {
        this.plugin = plugin;
    }

    public static PlayerBoard createBoard(Player player, String name) {
        if (plugin.getInfo().containsKey(name)) {
            return new PlayerBoard(plugin, player, plugin.getInfo().get(name));
        }
        return null;
    }

    public static PlayerBoard createBoard(Player player, List<String> text, List<String> title, int updateTitle, int updateText) {
        return new PlayerBoard(plugin, player, text, title, updateTitle, updateText);
    }

    public static List<PlayerBoard> getBoards() {
        return plugin.getAllboards();
    }

    public static void removeBoard(Player player) {
        if (plugin.getBoards().containsKey(player)) {
            plugin.getBoards().get(player).remove();
        }
    }

    public static void updateText(Player player) {
        if (plugin.getBoards().containsKey(player)) {
            plugin.getBoards().get(player).updateText();
        }
    }

    public static void updateTitle(Player player) {
        if (plugin.getBoards().containsKey(player)) {
            plugin.getBoards().get(player).updateTitle();
        }
    }

    public static void updateAll(Player player) {
        if (plugin.getBoards().containsKey(player)) {
            plugin.getBoards().get(player).updateText();
            plugin.getBoards().get(player).updateTitle();
        }
    }
}
