package cc.bukkitPlugin.pds.command;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

import cc.bukkitPlugin.commons.plugin.command.TCommandExc;
import cc.bukkitPlugin.pds.PlayerDataSQL;

public class CommandExc extends TCommandExc<PlayerDataSQL> implements CommandExecutor,TabCompleter{

    /**
     * 必须在配置文件启用后才能调用此方法
     * 
     * @param pPlugin
     */
    public CommandExc(PlayerDataSQL pPlugin){
        super(pPlugin,"pds",true);

        this.registerSubCommand();
    }

    protected void registerSubCommand(){
        super.registerSubCommand();
        
        this.register(new CommandLoad(this));
    }

}
