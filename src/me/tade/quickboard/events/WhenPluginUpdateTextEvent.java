package me.tade.quickboard.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WhenPluginUpdateTextEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Player p;
    private String text;

    public WhenPluginUpdateTextEvent(Player p, String text) {
        this.p = p;
        this.text = text;
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

    public String getText() {
        return text;
    }

    public String setText(String text) {
        this.text = text;
        return text;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}

