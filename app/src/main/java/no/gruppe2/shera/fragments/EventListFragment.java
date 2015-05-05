package no.gruppe2.shera.fragments;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import no.gruppe2.shera.R;
import no.gruppe2.shera.dto.Event;
import no.gruppe2.shera.view.ChatView;
import no.gruppe2.shera.view.EventView;


/*
This class contains a ListFragment that displays either Events or Chats available, and sends the
user to the corresponding EventView or ChatView when clicked.
 */
public class EventListFragment extends ListFragment {

    ArrayAdapter<String> adapter;
    private ArrayList<Event> events;
    private boolean chat;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            events = intent.getParcelableArrayListExtra(getResources()
                    .getString(R.string.intent_parcelable_key));
        } else {
            events = new ArrayList<>();
        }

        assert intent != null;
        chat = intent.getBooleanExtra("Chat", false);

        List<String> eventsName = new LinkedList<>();

        if (events != null) {
            for (int i = 0; i < events.size(); i++) {
                eventsName.add(events.get(i).getName());
            }
        }

        adapter = new ArrayAdapter<>(getActivity().getBaseContext(),
                android.R.layout.simple_list_item_1, eventsName);

        ListView l = (ListView) getActivity().findViewById(R.id.listview);
        l.setAdapter(adapter);
        l.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (chat) {
                    Intent i = new Intent(getActivity().getBaseContext(), ChatView.class);
                    i.putExtra(getResources().getString(R.string.intent_parcelable_key),
                            events.get(arg2));
                    startActivity(i);
                } else {
                    Intent intent1 = new Intent(getActivity().getBaseContext(), EventView.class);
                    intent1.putExtra(getResources()
                            .getString(R.string.intent_parcelable_key), events.get(arg2));
                    startActivity(intent1);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}