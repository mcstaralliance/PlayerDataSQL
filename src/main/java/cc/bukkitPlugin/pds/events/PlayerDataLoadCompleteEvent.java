package cc.bukkitPlugin.pds.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerDataLoadCompleteEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    public PlayerDataLoadCompleteEvent(Player who) {
        super(who);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
