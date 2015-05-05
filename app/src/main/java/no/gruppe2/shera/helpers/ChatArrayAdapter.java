package no.gruppe2.shera.helpers;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import no.gruppe2.shera.R;
import no.gruppe2.shera.dto.Chat;

/*
This class is a custom adapter that allows us to put two TextViews inside a LinearLayout into
a ListView. The getView-method finds the correct chat-object in the list, and uses it to assign the
correct information to the two TextViews. This creates chat-bubbles.
 */

public class ChatArrayAdapter extends ArrayAdapter {

    private ArrayList<Chat> chatMessageList = new ArrayList();
    HelpMethods help = new HelpMethods();
    long userID = 0, hostID = 0;
    Chat message;


    public void add(Chat object, long host, long user) {
        message = object;
        chatMessageList.add(object);
        hostID = host;
        userID = user;
        super.add(object);
    }

    public ChatArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public int getCount() {
        return this.chatMessageList.size();
    }

    public Chat getItem(int index) {
        return this.chatMessageList.get(index);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        Chat message = getItem(position);

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.single_chat_bubble, parent, false);
        }

        TextView chatText = (TextView) row.findViewById(R.id.singleMessage);
        TextView infoText = (TextView) row.findViewById(R.id.message_datetime);

        String info = "\n" + help.leadingZeroesDate(message.getDateTime()) + " " +
                help.leadingZeroesTime(message.getDateTime()) + " " +
                message.getUserName();


        infoText.setText(info);


        String text = "\n" + message.getMessage() + "\n\n";
        if (hostID == message.getUserID()) {
            chatText.setBackground(getContext().getResources().getDrawable(R.drawable.bubble_gold));
        } else if (message.getUserID() == userID) {
            chatText.setBackground(getContext().getResources().getDrawable(R.drawable.bubble_green));
        }
        else {
            chatText.setBackground(getContext().getResources().getDrawable(R.drawable.bubble_blue));
        }
        chatText.setText(text);
        return row;
    }
}