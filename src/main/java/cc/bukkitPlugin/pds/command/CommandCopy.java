package cc.bukkitPlugin.pds.command;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.plugin.command.TACommandBase;
import cc.bukkitPlugin.commons.util.BukkitUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.user.UserManager;

public class CommandCopy extends TACommandBase<PlayerDataSQL,CommandExc>{

    public CommandCopy(CommandExc pExector){
        super(pExector,"copy",2);
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        if(!hasCmdPermission(pSender))
            return noPermission(pSender,this.mLastConstructPermisson);

        if(pArgs.length==0) return help(pSender,pLabel);
        if(pArgs.length>2) return errorArgsNumber(pSender,pArgs.length);

        Player tCopyFor,tCopyFrom;
        int tCopyFromIndex;
        if(pArgs.length==1){
            tCopyFromIndex=0;
            if(!(pSender instanceof Player))
                return send(pSender,C("MsgCannotCopyDataForConsole"));

            tCopyFor=(Player)pSender;
        }else{
            tCopyFromIndex=1;
            tCopyFor=Bukkit.getPlayerExact(pArgs[0]);
            if(tCopyFor==null)
                return send(pSender,C("MsgPlayerNotOnline","%player%",pArgs[0]));
        }
        tCopyFrom=Bukkit.getPlayerExact(pArgs[tCopyFromIndex]);
        if(tCopyFrom==null)
            return send(pSender,C("MsgPlayerNotExist","%player%",pArgs[0]));

        UserManager tUserMan=this.mPlugin.getUserManager();
        tUserMan.restoreUser(tUserMan.getUserData(tCopyFrom,false),tCopyFor);

        return send(pSender,C("MsgCopyDataForPlayer",
                new String[]{"%from%","%for%"},
                new Object[]{tCopyFrom==pSender?C("WordYou"):tCopyFrom.getName(),tCopyFor==pSender?C("WordYou"):tCopyFor.getName()}));
    }
    
    @Override
    public ArrayList<String> getHelp(CommandSender pSender,String pLabel){
        ArrayList<String> tHelps=new ArrayList<>();
        if(hasCmdPermission(pSender)){
            tHelps.add(constructCmdUsage(C("WordFrom")));
            tHelps.add(this.mExector.getCmdUsagePrefix()+C("HelpCmdCopy"));
            tHelps.add(constructCmdUsage(C("WordFor"),C("WordFrom")));
            tHelps.add(this.mExector.getCmdUsagePrefix()+C("HelpCmdCopyForPlayer"));
        }
        return tHelps;
    }

    @Override
    public ArrayList<String> getTabSubCmd(CommandSender pSender,String pLabel,String[] pArgs){
        ArrayList<String> tTabs=null;
        if(hasCmdPermission(pSender)){
            tTabs=new ArrayList<>();
            if(pArgs.length<=2){
                tTabs.addAll(BukkitUtil.getOfflinePlayersName());
            }
        }

        return tTabs;
    }

}
