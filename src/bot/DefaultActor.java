package bot;

import managers.ChannelManager;
import managers.CustomMessageManager;
import models.Channel;
import models.CustomMessage;
import org.pircbotx.hooks.events.MessageEvent;

import java.util.List;
import java.util.Random;

/**
 * Created by palepail on 8/5/2015.
 */
public class DefaultActor {

    MessageManager messageManager;
    ChannelManager channelManager = new ChannelManager();
    CustomMessageManager customMessageManager = new CustomMessageManager();
    String commandsLink = "palepail.com/#/palebot/commands/";
    Channel channelEntity;
    String channelName;
    String message;
    String username;
    public void setValues(MessageEvent event){
        channelName = event.getChannel().getName();
        messageManager = MessageManager.getInstance(channelName);
        channelEntity = channelManager.getChannelByName(channelName.substring(1));
        message = event.getMessage();
        username = event.getUser().getNick();
    }

    private boolean isPalebotAdmin()
    {
        return username.equalsIgnoreCase("palepail");
    }


    public void palebotInfo(MessageEvent event){

        messageManager.reduceMessages(1);
        event.getBot().sendIRC().message(event.getChannel().getName(), "Hi! I'm palebot.");
        
    }

    public void palebotBan(MessageEvent event){
        messageManager.reduceMessages(1);
        if(isPalebotAdmin() || messageManager.isMod(channelName,username)) {
            String offender = message.replace("!ban ", "");
            event.getBot().sendIRC().message(event.getChannel().getName(), ".ban " + offender);
        }else {
            event.getBot().sendIRC().message(event.getChannel().getName(), username + ", you are not worthy enough to wield the ban hammer.");
        }
    }

    public void palebotUnban(MessageEvent event){
        messageManager.reduceMessages(1);
        if(isPalebotAdmin() || messageManager.isMod(channelName,username)) {
            String offender = message.replace("!unban ", "");
            event.getBot().sendIRC().message(event.getChannel().getName(), ".unban " + offender);
        }else {
            event.getBot().sendIRC().message(event.getChannel().getName(), username + ", you are not worthy enough to wield the ban hammer.");
        }
    }

    public void palebotTimeout(MessageEvent event){
        messageManager.reduceMessages(1);
        if(isPalebotAdmin() || messageManager.isMod(channelName,username)) {
            String[] list = message.split(" ");
            event.getBot().sendIRC().message(event.getChannel().getName(), ".timeout " + list[1] +" " + list[2]);
        }else {
            event.getBot().sendIRC().message(event.getChannel().getName(), username + ", you are not worthy enough to wield the ban hammer.");
        }
    }



    public void palebotCommands(MessageEvent event){

        messageManager.reduceMessages(1);
        event.getBot().sendIRC().message(event.getChannel().getName(), "Bot commands can be found here " + commandsLink+channelName.substring(1));
    }

    public void  selfTimeout(MessageEvent event)
        {
            messageManager.reduceMessages(1);
            messageManager.delayMessage(1500);
            event.getBot().sendIRC().message(event.getChannel().getName(), "/timeout " + event.getUser().getNick() + " 1");
        }

    public void deleteCustomMessage(MessageEvent event)
    {
        String message = event.getMessage();
        String regex = "\\!custom delete ?\\(\\!([a-z1-9]+)\\)";
        if (!message.matches(regex)) {
            messageManager.sendMessage(event, event.getUser().getNick()+ ", correct !custom delete syntax is !custom delete (!TRIGGER)");
            return;
        }

        String trigger = message.substring(message.indexOf("(") + 1, message.indexOf(")"));
        if(customMessageManager.deleteTriggerFromChannel(channelEntity.getId(), trigger))
        {
            messageManager.sendMessage(event,"Trigger Deleted");
        }else{
            messageManager.sendMessage(event, "Trigger Not Found" );
        }
    }

    public void saveCustomMessage(MessageEvent event)
    {
        String regex = "\\!custom ?\\(\\!([A-Za-z1-9]+)\\) ?(.{0,240})";
        if (!event.getMessage().matches(regex)) {
            messageManager.sendMessage(event, event.getUser().getNick() + ", correct new custom message syntax is !custom (!TRIGGER) MESSAGE - Max message length is 240");
            return;
        }
        String trigger = message.substring(message.indexOf("(") + 1, message.indexOf(")"));
        String customMessage = message.substring(message.indexOf(")") + 2);


        CustomMessage custom = new CustomMessage();
        if (customMessage.indexOf("-mod") != -1) {
            customMessage = customMessage.replace("-mod", "");
            custom.setRestriction(1);
        }


        custom.setMessage(customMessage);
        custom.setCustomTrigger(trigger);
        custom.setChannelId(channelEntity.getId());
        customMessageManager.addCustomMessage(custom);
        messageManager.sendMessage(event, "Custom Message Saved");

    }
    public void getAllCustomMessages(MessageEvent event){
        List<CustomMessage> messages = customMessageManager.getCustomMessagesByChannel(channelEntity.getId());
        String post ="";
        for(CustomMessage message : messages)
        {
            post+= message.getCustomTrigger() + " ";
        }

        messageManager.sendMessage(event, post);

    }

    public void rollDice(MessageEvent event)
    {

        Random rand = new Random();
        int number = rand.nextInt(5);
        number += 1;
        String flair = "";
        switch (number) {
            case 1: {
                flair = "Kappa";
                break;
            }
            case 2: {
                flair = "BibleThump";
                break;
            }
            case 3: {
                flair = "DansGame";
                break;
            }
            case 4: {
                flair = "MVGame";
                break;
            }
            case 5: {
                flair = "FrankerZ";
                break;
            }
            case 6: {
                flair = "PogChamp";
                break;
            }
        }
        messageManager.sendMessage(event, event.getUser().getNick() + " rolled a " + number + " " + flair);

    }

    public void customTrigger(MessageEvent event){
        List<CustomMessage> customMessages = customMessageManager.getCustomMessagesByChannel(channelEntity.getId());
        for (CustomMessage customMessage : customMessages) {
            if (event.getMessage().startsWith(customMessage.getCustomTrigger())) {
                messageManager.reduceMessages(1);
                event.getBot().sendIRC().message(channelName, customMessage.getMessage());
            }
        }
    }

}
