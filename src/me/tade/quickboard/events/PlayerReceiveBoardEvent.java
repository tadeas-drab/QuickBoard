package me.tade.quickboard.events;

import me.tade.quickboard.PlayerBoard;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class PlayerReceiveBoardEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private Player p;
    private List<String> text;
    private List<String> title;
    private PlayerBoard scoreboard;

    public PlayerReceiveBoardEvent(Player p, List<String> text, List<String> title, PlayerBoard scoreboard) {
        this.p = p;
        this.text = text;
        this.title = title;
        this.scoreboard = scoreboard;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public static HandlerList getHandlersList() {
        return handlers;
    }

    public Player getPlayer() {
        return p;
    }

    public List<String> getText() {
        return text;
    }

    public List<String> getTitle() {
        return title;
    }

    public PlayerBoard getBoard() {
        return scoreboard;
    }

    public List<String> setText(List<String> text) {
        this.text = text;
        return text;
    }

    public List<String> setTitle(List<String> title) {
        this.title = title;
        return title;
    }

    public PlayerBoard setBoard(PlayerBoard board) {
        this.scoreboard = board;
        return board;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
