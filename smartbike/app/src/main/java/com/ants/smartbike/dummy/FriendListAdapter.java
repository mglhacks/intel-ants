package com.ants.smartbike.dummy;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ants.smartbike.FriendsFragment;
import com.ants.smartbike.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class FriendListAdapter extends ArrayAdapter<FriendsFragment.FriendItem> {

    private final Activity context;
    public List<FriendsFragment.FriendItem> friends;

    public FriendListAdapter(Activity context, List<FriendsFragment.FriendItem> friends) {
        super(context, 0, friends);

        this.context = context;
        this.friends = friends;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.fragment_friend_list, parent, false)   ;

        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView name = (TextView) rowView.findViewById(R.id.item);

        name.setText(friends.get(position).name);
        String url = "https://graph.facebook.com/" + friends.get(position).id + "/picture";
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(url, imageView);
        return rowView;
    }
}
