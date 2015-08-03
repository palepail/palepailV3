package bot;

import managers.ChannelManager;
import managers.WaifuManager;
import models.Channel;
import models.Waifu;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.OpEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by palepail on 7/31/2015.
 */
public class AnimeListener extends ListenerAdapter {

    private static MessageManager messageManager = MessageManager.getInstance();
    private static WaifuManager waifuManager = new WaifuManager();
    private static ChannelManager channelManager = new ChannelManager();
    private static Boolean waifuFightOpen = false;
    private static int waifu1Votes = 0;
    private static int waifu2Votes = 0;
    private static ArrayList<String> voters = new ArrayList();


    private static String ADD_WAIFU_ID = "ADD_WAIFU";
    private static String WAIFU_ID = "WAIFU";
    private static String NOT_FOUND_IMG = "http://i.imgur.com/c5IHJC9.png";

    public static final String NAME = "ANIME";



    @Override
    public void onMessage(MessageEvent event) {
        String channelName = event.getChannel().getName();
        String userName = event.getUser().getNick();
        Channel channelEntity = channelManager.getChannelByName(channelName.substring(1));

        String message = event.getMessage();

        if (event.getMessage().equals("!waifu")) {
            if (messageManager.overLimit() || !messageManager.lock(WAIFU_ID, 2000)) {
                messageManager.delayMessage(3000);
            }
            messageManager.reduceMessages(1);
            Waifu waifu = waifuManager.getRandomFromChannel(channelEntity.getId());
            if(waifu==null){
                event.getBot().sendIRC().message(channelName, userName + "'s waifu is " + NOT_FOUND_IMG);
                return;
            }
            event.getBot().sendIRC().message(channelName, userName + "'s waifu is " + waifu.getLink());
            return;
        }

        if (message.startsWith("!waifu search ")) {
            String name = message.substring(14);
            if(name.length()<3){
                messageManager.reduceMessages(1);
                event.getBot().sendIRC().message(channelName, "Are trying to start a harem, " + userName +"?");
                return;
            }

            List<Waifu> waifu = waifuManager.getWaifuByNameFromChannel(name, channelEntity.getId());

            if(waifu.size()>5)
            {
                long seed = System.nanoTime();
                Collections.shuffle(waifu, new Random(seed));
                waifu = waifu.subList(0,5);
                messageManager.reduceMessages(1);
                event.getBot().sendIRC().message(channelName, "Some wifu got stuck in the door");
            }
            String result = "";
            if (waifu.size() == 0) {
                event.getBot().sendIRC().message(event.getChannel().getName(), NOT_FOUND_IMG);
                return;
            } else {
                for (Waifu currentWaifu : waifu) {

                    result += currentWaifu.getLink() + " ";
                }
                messageManager.reduceMessages(1);
                event.getBot().sendIRC().message(channelName, result);
            }
            return;
        }


        if (message.startsWith("!waifu add ")) {


            if (message.indexOf("(") == -1 || message.indexOf(")") == -1 || message.substring(message.indexOf(")") + 1).isEmpty()) {
                messageManager.reduceMessages(1);
                event.respond(", correct waifu syntax is !waifu add (NAME) LINK");
                return;
            }
            if (message.substring(message.indexOf(")") + 2).length() > 40) {
                event.respond(", that Link is too long to comprehend");
                return;
            }

            if (messageManager.overLimit() || !messageManager.lock(ADD_WAIFU_ID, 3000)) {

                messageManager.delayMessage(5000);
            }

            String name = message.substring(message.indexOf("(") + 1, message.indexOf(")"));
            String link = message.substring(message.indexOf(")") + 2);
            Waifu waifu = new Waifu();
            waifu.setLink(link);
            waifu.setName(name);
            waifu.setUploader(userName);
            waifu.setChannelId(channelEntity.getId());
            waifuManager.addWaifu(waifu);
            messageManager.reduceMessages(1);
            event.getBot().sendIRC().message(channelName, "waifu added");
            return;
        }

        //OP commands

        if (event.getMessage().startsWith("!waifu slap ")) {

            if (messageManager.isMod(channelName,userName)) {

                String link = message.substring(12);
                if (waifuManager.deleteWaifuByLinkFromChannel(link, channelEntity.getId())) {
                    messageManager.reduceMessages(1);
                    event.getBot().sendIRC().message(channelName, "Your waifu has left you for someone else");
                } else {
                    messageManager.reduceMessages(1);
                    event.getBot().sendIRC().message(channelName, "Your waifu was imaginary the entire time");
                }
            }
            else{
                messageManager.reduceMessages(1);
                event.getBot().sendIRC().message(channelName, userName + ", how dare you try to slap a waifu");
            }
            return;

        }

        if(event.getMessage().equals("!waifu best"))
        {
            long seed = System.nanoTime();
            List<Waifu> waifuList = waifuManager.getBest(channelEntity.getId());
            if(waifuList.size()>0)
            {
                Collections.shuffle(waifuList, new Random(seed));
                Waifu waifu = waifuList.get(0);
                messageManager.reduceMessages(1);
                event.getBot().sendIRC().message(channelName, waifu.getName() + " is the one true waifu. Gaze upon her glory. " + waifu.getLink());
            }
        }

        if(event.getMessage().equals("!waifu worst"))
        {
            long seed = System.nanoTime();
            List<Waifu> waifuList = waifuManager.getWorst(channelEntity.getId());
            if(waifuList.size()>0)
            {
            Collections.shuffle(waifuList, new Random(seed));
            Waifu waifu = waifuList.get(0);

                messageManager.reduceMessages(1);
                event.getBot().sendIRC().message(channelName, waifu.getName() + " is lower than dirt. See for yourself " + waifu.getLink());
            }
        }

        if(waifuFightOpen && event.getMessage().startsWith("1") && !voters.contains(userName))
        {
            voters.add(userName);
            waifu1Votes++;
            return;
        }
        if(waifuFightOpen && event.getMessage().startsWith("2") && !voters.contains(userName))
        {
            voters.add(userName);
            waifu2Votes++;
            return;
        }

        if(event.getMessage().equals("!waifu fight reset")){
            if (messageManager.isMod(channelName,userName)) {
                waifuManager.resetFight(channelEntity.getId());
            }
        }

        if(event.getMessage().equals("!waifu fight")){
            if (messageManager.isMod(channelName,userName)) {

                Waifu waifu1 = waifuManager.getRandomFromChannel(channelEntity.getId());

                Waifu waifu2 = waifuManager.getRandomFromChannel(channelEntity.getId());
                int loopBreaker=0;
                while(waifu1.equals(waifu2))
                {
                    waifu2 = waifuManager.getRandomFromChannel(channelEntity.getId());
                    loopBreaker++;
                    if(loopBreaker>10){
                        messageManager.reduceMessages(1);
                        event.getBot().sendIRC().message(channelName, waifu1.getName() + " stands unopposed");
                        break;
                    }
                }

                messageManager.reduceMessages(1);
                event.getBot().sendIRC().message(channelName, waifu1.getName() + " - " + waifu1.getLink() + " VS " + waifu2.getName() + " - " + waifu2.getLink());
                messageManager.reduceMessages(1);
                event.getBot().sendIRC().message(channelName, "Voting open for 30 seconds. 1: " + waifu1.getName() + " 2: "+waifu2.getName());

                waifuFightOpen = true;
                messageManager.delayMessage(30000);
                waifuFightOpen = false;

                if(waifu1Votes > waifu2Votes){
                    messageManager.reduceMessages(1);
                    event.getBot().sendIRC().message(channelName, waifu1.getName() + " wins " + waifu1Votes + " to " + waifu2Votes);
                    waifu1.setPoints(waifu1.getPoints() + 1);
                    waifuManager.updateWaifu(waifu1);
                }
                else if(waifu1Votes < waifu2Votes){
                    messageManager.reduceMessages(1);
                    event.getBot().sendIRC().message(channelName, waifu2.getName() + " wins " +waifu2Votes + " to " +waifu1Votes);
                    waifu2.setPoints(waifu2.getPoints()+1);
                    waifuManager.updateWaifu(waifu2);
                }else{

                    messageManager.reduceMessages(1);
                    event.getBot().sendIRC().message(channelName, "It's a draw. "+waifu2Votes+ " to "+waifu1Votes);
                }
                waifu1Votes=0;
                waifu2Votes =0;
                voters.clear();


            } else {
                messageManager.reduceMessages(1);
                event.getBot().sendIRC().message(channelName, userName + ", only mods may start a waifu fight");

            }


        }


    }

}
