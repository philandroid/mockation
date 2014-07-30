package com.location.philippweiher.test;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by philippweiher on 25/07/14.
 */
public class AddressAdapter extends BaseAdapter{
    private final LayoutInflater inflater;
    private List<Address> matches;

    public AddressAdapter(List<Address> matches, Context context) {
        this.matches = matches;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return matches.size();
    }

    @Override
    public Object getItem(int i) {
        return matches.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        Address addressToDisplay = matches.get(position);
        String address = "";
        for (int i = 0; i < addressToDisplay.getMaxAddressLineIndex(); i++)
            address += addressToDisplay.getAddressLine(i);

        View v = convertView;

        Holder holder = new Holder();

        // First let's verify the convertView is not null
        if (convertView == null) {
            // This a new view we inflate the new layout
            v = inflater.inflate(R.layout.address_item, null);
            // Now we can fill the layout with the right values
            TextView tv = (TextView) v.findViewById(R.id.addressDetails);

            holder.addressDetails = tv;

            v.setTag(holder);
        }
        else
            holder = (Holder) v.getTag();

        holder.addressDetails.setText(address);

        return v;
    }

    public class Holder {
        public TextView addressDetails;
    }
}
