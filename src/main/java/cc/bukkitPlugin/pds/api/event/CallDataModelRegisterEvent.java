package cc.bukkitPlugin.pds.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CallDataModelRegisterEvent extends Event{

    protected static HandlerList mHandlers=new HandlerList();

    public static HandlerList getHandlerList(){
        return CallDataModelRegisterEvent.mHandlers;
    }

    @Override
    public HandlerList getHandlers(){
        return CallDataModelRegisterEvent.mHandlers;
    }

}
