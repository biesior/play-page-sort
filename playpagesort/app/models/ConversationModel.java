package models;

import com.avaje.ebean.*;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.persistence.*;
import javax.persistence.OrderBy;
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


    public String name;

    public String comment;

    public Boolean isActive;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    @OrderBy("time DESC")
    public List<MessageEventModel> events;


    public ConversationModel(String name) {

        this.name = StringEscapeUtils.escapeJava(name.trim());
    }

    public Date getLastEventTime() {

        ListIterator<MessageEventModel> li = this.events.listIterator(this.events.size());
        //Iterator<EventModel> li = this.events.iterator();

        Date lastDate = null;
        while (li.hasPrevious()) {
            EventModel event = li.previous();
            if (event instanceof MessageEventModel) {
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


    public List<MessageEventModel> getLatestEvents(int offset, int count) {

        if (offset < 0) {
            offset = 0;
        } else if (offset > this.events.size()) {
            offset = this.events.size();
        }


        int toIndex = (this.events.size()) - offset;
        int fromIndex = toIndex - count;

        if (fromIndex < 0) {
            fromIndex = 0;
        }

        return this.events.subList(fromIndex, toIndex);
    }

    /**
     * Return a page of conversations
     *
     * @param page     Page to display
     * @param pageSize Number of computers per page
     * @param sortBy   Computer property used for sorting
     * @param order    Sort order (either or asc or desc)
     * @param filter   Filter applied on the name column
     */
    public static PagedList<ConversationModel> page(int page, int pageSize, String sortBy, String order, String filter) {

        String sortField = (("ASC".equals(order)) ? "min" : "max") + "(" + sortBy + ")";

        RawSql rawSql = RawSqlBuilder.parse(
                "SELECT conversation.id , conversation.name, " + sortField + " sortingField "
                        + " FROM gom_conversation conversation left outer join gom_event events "
                        + " on events.conversation_id = conversation.id AND events.dtype = 'message' "
                        + " WHERE conversation.name like :search "
                        + " GROUP BY conversation.id "
                        + " ORDER BY " + sortField + " " + order)
                .columnMappingIgnore(sortField)
                .columnMapping("conversation.id", "id")
                .columnMapping("conversation.name", "name")
                .create();


        return Ebean.find(ConversationModel.class)
                .setRawSql(rawSql)
                .setParameter("search", "%" + filter + "%")
                .findPagedList(page, pageSize);

    }


    public String getName() {

        if (this.name == null) {
            return "";
        }

        return StringEscapeUtils.unescapeJava(this.name);
    }

    public void setName(String name) {
        this.name = name;
    }


}
