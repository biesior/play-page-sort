package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by dominik on 01/12/15.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "gom_event")
public abstract class EventModel extends Model {

    @Id
    public Long id;
    public Date time;

    @ManyToOne
    public ConversationModel conversation;


    public EventModel(ConversationModel conversation) {
        this.time = new Date();
        this.conversation = conversation;
    }

    public static Finder<Long, EventModel> find = new Finder<>(EventModel.class);


    public static List<EventModel> getEventsForConversationSince(ConversationModel conversation, Date date) {

        return find.where()
                .eq("conversation.id", conversation.id)
                .gt("time", date)
                .findList();
    }

}
