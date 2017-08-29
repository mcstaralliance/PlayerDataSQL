package cc.bukkitPlugin.pds.command;

import cc.bukkitPlugin.commons.plugin.command.TACommandBase;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xjboss on 2017/8/30.
 */
public class CommandRunscript extends TACommandBase<PlayerDataSQL,CommandExc> {
    public CommandRunscript(CommandExc pExector){
        super(pExector,"run_script",-1);
    }

    @Override
    public ArrayList<String> onTabComplete(CommandSender pSender, String[] pArgs) {
        ArrayList<String> scripts=new ArrayList(mPlugin.getScripts().size());
        for(File f:mPlugin.getScripts()){
            scripts.add(f.getName());
        }
        return scripts;
    }

    @Override
    public boolean execute(CommandSender pSender, String pLabel, String[] pArgs) {
        return false;
    }

    @Override
    public ArrayList<String> getHelp(CommandSender pSender, String pLabel) {
        ArrayList<String> tHelps=new ArrayList<>();
        if(hasCmdPermission(pSender)){
            tHelps.add(constructCmdUsage(C("WordPlayer")));
            tHelps.add(this.mExector.getCmdUsagePrefix()+C("HelpCmdLoad"));
            tHelps.add(constructCmdUsage(C("WordFor"),C("WordFrom")));
            tHelps.add(this.mExector.getCmdUsagePrefix()+C("HelpCmdLoadForPlayer"));
        }
        return tHelps;
    }
}
