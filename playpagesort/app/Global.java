
import models.ConversationModel;
import models.MessageEventModel;
import play.Application;
import play.GlobalSettings;
import play.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Global extends GlobalSettings {


    @Override
    public void onStart(Application app) {
        Logger.info("Application has started");
        if (ConversationModel.find.findRowCount() == 0) populateDatabase();
    }


    private void populateDatabase() {

        ConversationModel firstConversation = new ConversationModel("Foo");
        ConversationModel secondConversation = new ConversationModel("Zee");
        ConversationModel thirdConversation = new ConversationModel("Bar");

        firstConversation.save();
        secondConversation.save();
        thirdConversation.save();

        MessageEventModel message = new MessageEventModel("1 Message", firstConversation);
        message.time = createDate(2016, 1, 1);

        message.save();

        MessageEventModel second = new MessageEventModel("2 Message", secondConversation);
        second.time = createDate(2016, 1, 2);
        second.save();

        MessageEventModel third = new MessageEventModel("3 Message", thirdConversation);
        third.time = createDate(2016, 1, 3);
        third.save();

        MessageEventModel fourth = new MessageEventModel("4 Message", secondConversation);
        fourth.time = createDate(2016, 1, 4);
        fourth.save();


    }

    private Date createDate(int year, int month, int day) {
        java.util.Date utilDate = null;

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
            utilDate = formatter.parse(year + "/" + month + "/" + day);
        } catch (ParseException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        return utilDate;

    }


}