package com.location.philippweiher.test.fragments;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.location.philippweiher.test.MapsActivity;
import com.location.philippweiher.test.R;
import com.location.philippweiher.test.StoredAddress;

import java.util.List;
import java.util.Map;

public class  MyListFragment extends ListFragment {

    MyListFragmentInterface mCallback;
    public Activity activity;



    public interface MyListFragmentInterface {

        public List<StoredAddress> getStoredAddresses ();

        public void onMapLongClick(LatLng point);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (MyListFragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement MyListFragmentInterface");
        }
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        List<StoredAddress> storedAddresses = mCallback.getStoredAddresses();
        String[] addresses = new String[storedAddresses.size()];

        int i = 0;
        for (StoredAddress s : storedAddresses){
            addresses[i] = s.toString();
            i ++;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.database_entries, addresses);
        setListAdapter(adapter);
    }
    @Override
    public void onListItemClick (ListView l, View v, int position, long id){

        StoredAddress storedAddress = mCallback.getStoredAddresses().get(position);

        LatLng latlng = new LatLng(storedAddress.getLatitude(), storedAddress.getLongitude());

        mCallback.onMapLongClick(latlng);

        getActivity().getFragmentManager().popBackStack();
    }

}

