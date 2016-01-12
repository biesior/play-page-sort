package models;

import org.apache.commons.lang3.StringEscapeUtils;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


/**
 * Created by dominik on 26/10/15.
 */
@Entity
@DiscriminatorValue("message")
public class MessageEventModel extends EventModel {

    //@Column(columnDefinition = "BLOB CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci" )
    //@Column(columnDefinition = "TEXT CHARACTER SET 'utf8' COLLATE 'utf8_bin'")
    //@Column(columnDefinition = "BLOB")
    private String content;



    public MessageEventModel(String content, ConversationModel conversation) {

        super(conversation);

        this.content = StringEscapeUtils.escapeJava(content);
        this.conversation = conversation;
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




    @Override
    public void save() {

        // Support for Emoji if not encrypting content
        //SqlUpdate update = Ebean.createSqlUpdate("SET NAMES 'utf8mb4'");
        //update.execute();

        super.save();
    }


    public String getContent() {
        return StringEscapeUtils.unescapeJava(this.content);

    }

    public void setContent(String content) {
        this.content = content;
    }



}
