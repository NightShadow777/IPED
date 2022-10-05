package iped.parsers.mail.win10.entries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import iped.parsers.mail.win10.tables.AttachmentTable;

public class MessageEntry extends AbstractEntry {
    private long conversationId;
    private long parentFolderId;
    private long messageSize;
    private int noOfAttachments;
    private String msgAbstract;
    private String subject;
    private String senderName;
    private String senderEmailAddress;
    private Date msgDeliveryTime;
    private Date lastModifiedTime;

    private String contentHtml;
    public final char categoryNumber = '3';

    public MessageEntry(int rowId) {
        super(rowId);
    }

    public long getConversationId() {
        return this.conversationId;
    }

    public void setConversationId(long conversationId) {
        this.conversationId = conversationId;
    }

    public long getParentFolderId() {
        return this.parentFolderId;
    }

    public void setParentFolderId(long parentFolderId) {
        this.parentFolderId = parentFolderId;
    }


    public int getNoOfAttachments() {
        return this.noOfAttachments;
    }

    public void setNoOfAttachments(int noOfAttachments) {
        this.noOfAttachments = noOfAttachments;
    }

    public long getMessageSize() {
        return this.messageSize;
    }

    public void setMessageSize(long messageSize) {
        this.messageSize = messageSize;
    }

    public String getMsgAbstract() {
        return this.msgAbstract;
    }

    public void setMsgAbstract(String msgAbstract) {
        this.msgAbstract = msgAbstract != null ? msgAbstract : "";
    }

    public String getSubject() {
        return this.subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getSenderName() {
        return this.senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderEmailAddress() {
        return this.senderEmailAddress;
    }

    public void setSenderEmailAddress(String senderAddress) {
        this.senderEmailAddress = senderAddress;
    }
    
    public Date getMsgDeliveryTime() {
        return this.msgDeliveryTime;
    }
    
    public String getMsgDeliveryTimeStr() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(msgDeliveryTime);
    }

    public void setMsgDeliveryTime(Date msgDeliveryTime) {
        this.msgDeliveryTime = msgDeliveryTime;
    }

    public Date getLastModifiedTime() {
        return this.lastModifiedTime;
    }
    
    public String getLastModifiedTimeStr() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(lastModifiedTime);
    }

    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public ArrayList<RecipientEntry> getRecipients() {
        return RecipientTable.getMessageRecipients(rowId);
    }

    public ArrayList<AttachmentEntry> getAttachments() {
        return AttachmentTable.getMessageAttachments(rowId);
    }
    
    public String getContentHtml() {
        return this.contentHtml;
    }

    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }
}
