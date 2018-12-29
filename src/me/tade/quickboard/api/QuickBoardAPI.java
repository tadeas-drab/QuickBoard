package me.tade.quickboard.api;

import me.tade.quickboard.PlayerBoard;
import me.tade.quickboard.QuickBoard;
import org.bukkit.entity.Player;

import java.util.List;

public class QuickBoardAPI {

    public static PlayerBoard createBoard(Player player, String name) {
        if (QuickBoard.instance.info.containsKey(name)) {
            return new PlayerBoard(player, QuickBoard.instance.info.get(name));
        }
        return null;
    }

    public static PlayerBoard createBoard(Player player, List<String> text, List<String> title, int updateTitle, int updateText) {
        return new PlayerBoard(player, text, title, updateTitle, updateText);
    }

    public static List<PlayerBoard> getBoards() {
        return QuickBoard.instance.allboards;
    }

    public static void removeBoard(Player player) {
        if (QuickBoard.instance.boards.containsKey(player)) {
            QuickBoard.instance.boards.get(player).remove();
        }
    }

    public static void updateText(Player player) {
        if (QuickBoard.instance.boards.containsKey(player)) {
            QuickBoard.instance.boards.get(player).updateText();
        }
    }

    public static void updateTitle(Player player) {
        if (QuickBoard.instance.boards.containsKey(player)) {
            QuickBoard.instance.boards.get(player).updateTitle();
        }
    }

    public static void updateAll(Player player) {
        if (QuickBoard.instance.boards.containsKey(player)) {
            QuickBoard.instance.boards.get(player).updateText();
            QuickBoard.instance.boards.get(player).updateTitle();
        }
    }
}
