package no.gruppe2.shera.fragments;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

/**
 * Created by pernille.sethre on 20.02.2015.
 */
public class EventListFragment extends ListFragment {

    private List<String> eventsName;
    ArrayAdapter<String> adapter;
    private ListView l;
    private ArrayList<Event> events;
    private boolean chat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        chat = intent.getBooleanExtra("Chat", false);

        eventsName = new LinkedList<>();

        if (events != null) {
            for (int i = 0; i < events.size(); i++) {
                eventsName.add(events.get(i).getName());
            }
        }

        adapter = new ArrayAdapter<>(getActivity().getBaseContext(),
                android.R.layout.simple_list_item_1, eventsName);

        l = (ListView) getActivity().findViewById(R.id.listview);
        l.setAdapter(adapter);

        l.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (chat) {
                    Intent i = new Intent(getActivity().getBaseContext(), ChatView.class);
                    i.putExtra(getResources().getString(R.string.intent_parcelable_key), events.get(arg2));
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
        Log.d("Fragment.onDestroy", "Destroyed");
    }
}