
import models.ConversationModel;
import models.MessageEventModel;
import org.jboss.netty.channel.MessageEvent;
import play.Application;
import play.GlobalSettings;
import play.Logger;

public class Global extends GlobalSettings {


    @Override
    public void onStart(Application app) {
        Logger.info("Application has started");
        populateDatabase();
    }



    private void populateDatabase() {

        ConversationModel firstConversation = new ConversationModel("First Conversation");
        ConversationModel secondConversation = new ConversationModel("Second Conversation");
        ConversationModel thirdConversation = new ConversationModel("Third Conversation");

        firstConversation.save();
        secondConversation.save();
        thirdConversation.save();

        MessageEventModel message = new MessageEventModel("first Message", firstConversation);
        message.save();

        MessageEventModel second = new MessageEventModel("second Message", secondConversation);
        second.save();

        MessageEventModel third = new MessageEventModel("third Message", thirdConversation);
        third.save();

        MessageEventModel fourth = new MessageEventModel("fourth Message", secondConversation);
        fourth.save();




    }


}