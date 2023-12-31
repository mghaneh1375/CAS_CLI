/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bogen.studio.cas_client.Kavenegar.models;

import com.google.gson.JsonObject;
import bogen.studio.cas_client.Kavenegar.enums.MessageStatus;

/**
 * @author mohsen
 */
public class StatusResult {

    int messageId;
    MessageStatus status;
    String statusText;

    protected StatusResult() {

    }

    public StatusResult(JsonObject json) {
        this.messageId = (json.get("messageid").getAsInt());
        this.status = MessageStatus.valueOf(json.get("status").getAsInt());
        this.statusText = json.get("statustext").getAsString();
    }

    public int getMessageId() {
        return messageId;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public String getStatusText() {
        return statusText;
    }
}
