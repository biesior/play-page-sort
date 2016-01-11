package models;

import javax.persistence.*;
import javax.persistence.OrderBy;

import com.avaje.ebean.*;
import com.avaje.ebean.annotation.Encrypted;
import com.avaje.ebeaninternal.server.cluster.mcast.Message;
import models.types.ConversationStatus;
import models.types.ConversationType;
import models.types.UserRole;
import org.apache.commons.lang3.StringEscapeUtils;
import play.Logger;
import util.CryptoUtil;
import util.HashHelper;
import util.Util;

import java.util.*;

/**
 * Created by dominik on 26/10/15.
 */
@Entity
@Table(name = "gom_conversation")
public class ConversationModel extends Model {

    @Id
    public Long id;

    @Encrypted(dbEncryption=true)
    private String name;

    public ConversationType conversationType;

    public ConversationStatus conversationStatus;

    @ManyToOne
    public GroupModel group;

    @OneToMany(mappedBy="conversation", cascade= CascadeType.ALL)
    @OrderBy("time ASC")
    public List<EventModel> events;

    @ManyToMany(mappedBy="conversations", cascade = CascadeType.ALL)
    public List<UserModel> members;

    public String syncId;

    public ConversationModel(String name, ConversationType conversationType, GroupModel group) {

        this.name = StringEscapeUtils.escapeJava(name.trim());
        this.conversationType = conversationType;
        this.group = group;
        this.conversationStatus = ConversationStatus.OPEN;
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


    @Transient
    public String getGroupName() {
        return this.group != null ? this.group.getName() : "-";
    }

    public static Finder<Long, ConversationModel> find = new Finder<>(ConversationModel.class);


    public static Integer getNumberOfConversations() {
        return ConversationModel.find.findRowCount();
    }

    public static Integer getNumberOfConversations(UserModel user) {

        if(user.userRole == UserRole.ADMIN) {
            return ConversationModel.find.findRowCount();
        }
        else {
            return ConversationModel.find.where().eq("group.groupLeaders.id", user.id).findRowCount();
        }
    }

    public static ConversationModel retrieve(Long id) {
        return ConversationModel.find.where().eq("id", id).findUnique();
    }

    public static List<ConversationModel> getConversations() {

        return find.all();
    }

    public static List<ConversationModel> getConversationsForUser(UserModel user) {
        List<ConversationModel> conversations =  find.where()
                .disjunction()
                .add(Expr.eq("members.id", user.id))
                .add(Expr.eq("group.groupLeaders.id", user.id))
                .findList();

        return conversations;

    }

    public static ConversationModel getDirectConversation(UserModel user, UserModel recipient) {


        //return find.where().in("members", new Object[]{user, recipient}).findUnique();


        String sql = "SELECT id FROM gom_conversation c INNER JOIN gom_user_gom_conversation u " +
                "ON c.id = u.gom_conversation_id " +
                "WHERE c.conversation_type = :type AND " +
                "u.gom_user_id IN (:user, :recipient) HAVING COUNT(DISTINCT u.gom_user_id) = 2;";


        RawSql rawSql = RawSqlBuilder.parse(sql)
                .tableAliasMapping("id", "id")
                .create();

        ConversationModel conversation = find.setRawSql(rawSql)
                .setRawSql(rawSql)
                .setParameter("user", user.id)
                .setParameter("recipient", recipient.id)
                .setParameter("type", ConversationType.DIRECT)
                .findUnique();

        return conversation;


    }




    public List<EventModel> getLatestEvents(int offset, int count) {

        MessageEventModel.removeExpiredMessages();

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
    public static PagedList<ConversationModel> page(int page, int pageSize, String sortBy, String order, String filter, UserModel user) {


        if(user.userRole == UserRole.ADMIN) {

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

        else if(user.userRole == UserRole.CONSULTANT) {
            return find.where()
                    .eq("group.groupLeaders.id", user.id)
                    .disjunction()
                    .add(Expr.ilike("name", "%" + filter + "%"))
                    .add(Expr.and(
                            Expr.eq("events.dtype", "message"),
                            Expr.ilike("events.content", "%" + filter + "%"))
                    )
                    .orderBy(sortBy + " " + order)
                    .findPagedList(page, pageSize);
        }


        else {
            Logger.error("Invalid attempt to list conversations");
            return null;
        }

    }


    public String getName() {

        if(this.name == null && this.conversationType == ConversationType.DIRECT) {
            String name = "";
            for(int i=0; i<this.members.size(); i++) {
                UserModel user = this.members.get(i);
                if(i!=0) {
                    name += "; ";
                }
                name += user.getShortName();
            }
            return name;
        }

        return StringEscapeUtils.unescapeJava(this.name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UserModel> getGroupLeaders() {
        if(this.group != null) {
            return this.group.getGroupLeaders();
        }
        else return new ArrayList<>();
    }


}
