package cc.bukkitPlugin.pds.command;

import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.bukkitPlugin.commons.plugin.command.TACommandBase;
import cc.bukkitPlugin.commons.util.BukkitUtil;
import cc.bukkitPlugin.pds.PlayerDataSQL;
import cc.bukkitPlugin.pds.user.User;
import cc.bukkitPlugin.pds.user.UserManager;
import cc.bukkitPlugin.pds.util.CPlayer;

public class CommandLoad extends TACommandBase<PlayerDataSQL,CommandExc>{

    public CommandLoad(CommandExc pExector){
        super(pExector,"load",2);
    }

    @Override
    public boolean execute(CommandSender pSender,String pLabel,String[] pArgs){
        if(!hasCmdPermission(pSender))
            return noPermission(pSender,this.mLastConstructPermisson);

        if(pArgs.length==0) return help(pSender,pLabel);
        if(pArgs.length>2) return errorArgsNumber(pSender,pArgs.length);

        Player tLoadFor;
        OfflinePlayer tLoadFrom;
        int tLoadFromIndex;
        if(pArgs.length==1){
            tLoadFromIndex=0;
            if(!(pSender instanceof Player))
                return send(pSender,C("MsgCannotLoadDataForConsole"));

            tLoadFor=(Player)pSender;
        }else{
            tLoadFromIndex=1;
            tLoadFor=Bukkit.getPlayerExact(pArgs[0]);
            if(tLoadFor==null)
                return send(pSender,C("MsgPlayerNotOnline","%player%",pArgs[0]));
        }
        tLoadFrom=Bukkit.getOfflinePlayer(pArgs[tLoadFromIndex]);
        if(tLoadFrom==null)
            return send(pSender,C("MsgPlayerNotExist","%player%",pArgs[0]));

        Bukkit.getScheduler().runTaskAsynchronously(this.mPlugin,()->{
            try{
                UserManager tUserMan=this.mPlugin.getUserManager();
                GameMode tMode=tLoadFor.getGameMode();
                User tUser=tUserMan.loadUser(new CPlayer(tLoadFrom));
                if(tUser==null){
                    send(pSender,C("MsgPlayerDataNotExist","%player%",pArgs[0]));
                    return;
                }
                tUserMan.restoreUser(new CPlayer(tLoadFor),tUser,pSender);
                tLoadFor.setGameMode(tMode);
            }catch(SQLException e){
                send(pSender,C("MsgErrorOnLoadSQLData","%player%",tLoadFrom.getName())+": "+e.getLocalizedMessage());
                return;
            }

            send(pSender,C("MsgLoadDataForPlayer",
                    new String[]{"%from%","%for%"},
                    new Object[]{tLoadFrom==pSender?C("WordYou"):tLoadFrom.getName(),tLoadFor==pSender?C("WordYou"):tLoadFor.getName()}));
        });

        return true;
    }

    @Override
    public ArrayList<String> getHelp(CommandSender pSender,String pLabel){
        ArrayList<String> tHelps=new ArrayList<>();
        if(hasCmdPermission(pSender)){
            tHelps.add(constructCmdUsage(C("WordFrom")));
            tHelps.add(this.mExector.getCmdUsagePrefix()+C("HelpCmdLoad"));
            tHelps.add(constructCmdUsage(C("WordFor"),C("WordFrom")));
            tHelps.add(this.mExector.getCmdUsagePrefix()+C("HelpCmdLoadForPlayer"));
        }
        return tHelps;
    }

    @Override
    public ArrayList<String> getTabSubCmd(CommandSender pSender,String pLabel,String[] pArgs){
        ArrayList<String> tTabs=null;
        if(hasCmdPermission(pSender)){
            tTabs=new ArrayList<>();
            if(pArgs.length<=2){
                tTabs.addAll(BukkitUtil.getOnlinePlayersName());
            }
        }

        return tTabs;
    }

}
