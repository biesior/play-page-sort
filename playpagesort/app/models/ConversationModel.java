package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.Model;
import com.avaje.ebean.PagedList;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by dominik on 26/10/15.
 */
@Entity
@Table(name = "gom_conversation")
public class ConversationModel extends Model {

    @Id
    public Long id;


    private String name;

    @OneToMany(mappedBy="conversation", cascade= CascadeType.ALL)
    @OrderBy("time ASC")
    public List<EventModel> events;


    public ConversationModel(String name) {

        this.name = StringEscapeUtils.escapeJava(name.trim());
    }

    public Date getLastEventTime() {

        ListIterator<EventModel> li = this.events.listIterator(this.events.size());
        //Iterator<EventModel> li = this.events.iterator();

        Date lastDate = null;
        while(li.hasPrevious()) {
            EventModel event = li.previous();
            if(event instanceof MessageEventModel) {
                lastDate = event.time;
                break;
            }
        }

        return lastDate;
    }



    public static Finder<Long, ConversationModel> find = new Finder<>(ConversationModel.class);


    public static Integer getNumberOfConversations() {
        return ConversationModel.find.findRowCount();
    }


    public static ConversationModel retrieve(Long id) {
        return ConversationModel.find.where().eq("id", id).findUnique();
    }

    public static List<ConversationModel> getConversations() {

        return find.all();
    }



    public List<EventModel> getLatestEvents(int offset, int count) {

        if(offset < 0) {
            offset = 0;
        } else if(offset > this.events.size()) {
            offset= this.events.size();
        }


        int toIndex = (this.events.size()) - offset;
        int fromIndex = toIndex - count;

        if(fromIndex < 0 ) {
            fromIndex = 0;
        }

        return this.events.subList(fromIndex, toIndex);
    }




    /**
     * Return a page of conversations
     *
     * @param page Page to display
     * @param pageSize Number of computers per page
     * @param sortBy Computer property used for sorting
     * @param order Sort order (either or asc or desc)
     * @param filter Filter applied on the name column
     */
    public static PagedList<ConversationModel> page(int page, int pageSize, String sortBy, String order, String filter) {



//            return Ebean
//                    .createQuery(
//                            ConversationModel.class,
//                            "WHERE lower(name) like :search GROUP BY id"
//                    )
//                    .setParameter("search","%"+filter+"%")
//                    .order("events.time DESC")
//                    .findPagedList(page, pageSize);

            return find.where()
                    .disjunction()
                    .add(Expr.ilike("name", "%" + filter + "%"))
                    .add(Expr.and(
                            Expr.eq("events.dtype", "message"),
                            Expr.ilike("events.content", "%" + filter + "%"))
                    )
                    //.orderBy("events.time desc, " + sortBy + " " + order)
                    .orderBy(sortBy + " " + order)
                    //.setDistinct(true)
                    .findPagedList(page, pageSize);


    }


    public String getName() {

        if(this.name == null) {
            String name = "";
            return name;
        }

        return StringEscapeUtils.unescapeJava(this.name);
    }

    public void setName(String name) {
        this.name = name;
    }


}
