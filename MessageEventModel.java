package models;

import com.avaje.ebean.*;
import com.avaje.ebean.Query;
import com.avaje.ebean.annotation.Encrypted;
import models.json.EventBean;
import models.json.MessageEventBean;
import models.types.MessageType;
import play.Play;
import play.api.libs.Crypto;
import play.api.libs.CryptoConfig;
import util.CryptoUtil;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by dominik on 26/10/15.
 */
@Entity
@DiscriminatorValue("message")
public class MessageEventModel extends EventModel {


    @Encrypted(dbEncryption=true)
    //@Column(columnDefinition = "BLOB CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci" )
    //@Column(columnDefinition = "TEXT CHARACTER SET 'utf8' COLLATE 'utf8_bin'")
    @Column(columnDefinition = "BLOB")
    private String content;

    public MessageType messageType;

    @ManyToOne
    public UserModel sender;

    public Date dateDeleted;

    @ManyToMany(cascade = CascadeType.REMOVE)
    @JoinTable(name = "gom_read_messages")
    public List<UserModel> readBy = new ArrayList<>();

    public boolean expired;

    public String syncId;


    public MessageEventModel(String content, ConversationModel conversation, UserModel sender) {

        super(conversation);

        this.content = StringEscapeUtils.escapeJava(content);
        this.conversation = conversation;
        this.sender = sender;
        this.messageType = MessageType.TEXT;
        this.expired = false;
    }

    public MessageEventModel(String content, MessageType messageType, ConversationModel conversation, UserModel sender) {

        this(content, conversation, sender);
        this.messageType = messageType;
    }


    public static Finder<Long, MessageEventModel> find = new Finder<>(MessageEventModel.class);


    public static MessageEventModel retrieve(Long id) {
        return find.where().eq("id", id).findUnique();
    }


    public static Integer getNumberOfMessages() {
        return MessageEventModel.find.findRowCount();
    }


    public static Integer getNumberOfMessagesForConversation(ConversationModel conversation) {
        return MessageEventModel.find.where()
                .eq("conversation.id", conversation.id)
                .eq("expired", false)
                .findRowCount();
    }

    public static int getUnreadMessagesCountForUser(UserModel user) {

        int readMessages = find.where()
                .eq("readBy.id", user.id)
                .eq("expired", false)
                .ne("content", null) // Ignore deleted
                .findRowCount();

        int totalMessages =  find.where()
                .ne("sender.id", user.id)
                .eq("expired", false)
                .ne("content", null) // Ignore deleted
                .disjunction()
                .add(Expr.eq("conversation.members.id", user.id))
                .add(Expr.eq("conversation.group.groupLeaders.id", user.id))
                .findRowCount();

        return totalMessages - readMessages;
    }


    public static List<MessageEventModel> getMessagesForUser(UserModel user, ConversationModel conversation) {
        return find.where()
                .eq("conversation.id", conversation.id)
                .eq("sender.id", user.id)
                .eq("expired", false)
                .findList();
    }

    public static List<MessageEventModel> getForgeinMessagesForUser(UserModel user, ConversationModel conversation) {
        return find.where()
                .eq("conversation.id", conversation.id)
                .eq("expired", false)
                .ne("sender.id", user.id)
                .findList();
    }
    public static List<MessageEventModel> getDeletedMessagesForConversationSince(ConversationModel conversation, Date since) {
        return find.where()
                .eq("conversation.id", conversation.id)
                .eq("expired", false)
                .ne("dateDeleted", null)
                .ge("dateDeleted", since)
                .findList();
    }


    @Override
    public void save() {

        // Support for Emoji if not encrypting content
        //SqlUpdate update = Ebean.createSqlUpdate("SET NAMES 'utf8mb4'");
        //update.execute();

        super.save();
    }


    public EventBean getEventBeanForUser(UserModel user) {
        return new MessageEventBean(this, user);
    }

    public String getContent() {
        return StringEscapeUtils.unescapeJava(this.content);

    }

    public void setContent(String content) {
        this.content = content;
    }



    public static void removeExpiredMessages() {


        Integer expireAfterDays = Play.application().configuration().getInt("message.expire-after-days");

        Date expiryDate = new Date(new Date().getTime() - (24 * expireAfterDays) * 60 * 60 * 1000);

        SqlUpdate tangoDown = Ebean.createSqlUpdate("UPDATE gom_event SET expired = true, content = NULL WHERE dtype = \"message\" AND time <= :expiryDate");
        tangoDown.setParameter("expiryDate", expiryDate);
        tangoDown.execute();
    }


}
