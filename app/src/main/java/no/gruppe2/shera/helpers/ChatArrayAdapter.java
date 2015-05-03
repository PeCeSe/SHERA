package no.gruppe2.shera.helpers;

import android.content.Context;
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


    public void add(Chat object) {
        chatMessageList.add(object);
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

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.single_chat_bubble, parent, false);
        }

        Chat message = getItem(position);

        TextView chatText = (TextView) row.findViewById(R.id.singleMessage);
        TextView infoText = (TextView) row.findViewById(R.id.message_datetime);

        String info = "\n" + help.leadingZeroesDate(message.getDateTime()) + " " +
                help.leadingZeroesTime(message.getDateTime()) + " " +
                message.getUserName();


        infoText.setText(info);


        String text = "\n" + message.getMessage() + "\n\n";

        chatText.setText(text);
        chatText.setTextColor(getContext().getResources().getColor(R.color.white));
        return row;
    }

}