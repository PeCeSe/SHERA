package no.gruppe2.shera;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
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

/**
 * Created by pernille.sethre on 20.02.2015.
 */
public class EventList extends ListFragment{


    private List<String> events;
    ArrayAdapter<String> adapter;
    private ListView l;
    private ArrayList<EventObject> eventObjects;

    @Override
    public View onCreateView(LayoutInflater inflater,	ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_list,	container,	false);
    }

    @Override
    public void	onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            eventObjects = intent.getParcelableArrayListExtra("EventObjects");
        } else
            eventObjects = new ArrayList<>();


        events = new LinkedList<>();

        if (eventObjects != null) {
            for (int i = 0; i < eventObjects.size(); i++) {
                events.add(eventObjects.get(i).getName());
            }
        }


        adapter = new ArrayAdapter<>(getActivity().getBaseContext(),
                android.R.layout.simple_list_item_1, events);

        l = (ListView) getActivity().findViewById(R.id.listview);
        l.setAdapter(adapter);

        l.setOnItemClickListener(new OnItemClickListener(){
            public void onItemClick(AdapterView<?> arg0,View arg1,int arg2,	long arg3) {
                Intent intent1 = new Intent(getActivity().getBaseContext(), Event.class);
                intent1.putExtra("EventObject", eventObjects.get(arg2));
                startActivity(intent1);
            }
        });
    }
}
