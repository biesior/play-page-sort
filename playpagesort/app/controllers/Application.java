package controllers;

import com.avaje.ebean.PagedList;
import models.ConversationModel;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import java.util.Arrays;
import java.util.List;

public class Application extends Controller {

    public Result index(String sortBy, String order, String filter) {

        List<String> allowedFields = Arrays.asList("conversation.id", "conversation.name", "events.time", "events.id", "events.content");
        List<String> allowedOrders = Arrays.asList("ASC", "DESC");

        if (!allowedFields.contains(sortBy)) sortBy = "events.time";
        if (!allowedOrders.contains(order)) order = "DESC";

        PagedList<ConversationModel> page = ConversationModel.page(0, 10, sortBy, order, filter);

        return ok(index.render(page, sortBy, order));
    }


}
