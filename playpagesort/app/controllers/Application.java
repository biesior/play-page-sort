package controllers;

import com.avaje.ebean.PagedList;
import models.ConversationModel;
import play.*;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

    public Result index() {


        PagedList<ConversationModel> page = ConversationModel.page(0, 10,  "name", "asc", "");

        for(ConversationModel conversation : page.getList()) {
            Logger.debug("----------");
            Logger.debug("Conversation: " + conversation.getName() +  "; ID" + conversation.id);
            Logger.debug("Conversation Last Message: " + conversation.getLastEventTime());
            Logger.debug("----------");
        }


        return ok(index.render("Your new application is ready."));
    }

}
