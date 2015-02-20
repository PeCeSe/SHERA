package no.gruppe2.shera;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by pernille.sethre on 20.02.2015.
 */
public class EventList extends ListFragment{


    private List<String> fruits;
    ArrayAdapter<String> adapter;
    private ListView l;

    @Override
    public View onCreateView(LayoutInflater inflater,	ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_list,	container,	false);
    }

    @Override
    public void	onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        //Fruits is temporary
        fruits = new LinkedList<String>();

        fruits.add("apple");
        fruits.add("pear");
        fruits.add("orange");
        fruits.add("banana");
        fruits.add("pineapple");


        adapter = new ArrayAdapter<String>(getActivity().getBaseContext(),
                android.R.layout.simple_list_item_1, fruits);

        l = (ListView) getActivity().findViewById(R.id.listview);
        l.setAdapter(adapter);

        l.setOnItemClickListener(new OnItemClickListener(){
            public void onItemClick(AdapterView<?> arg0,View arg1,int arg2,	long arg3) {
                Toast.makeText(getActivity().getBaseContext(), "Du trykket " + arg2, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
