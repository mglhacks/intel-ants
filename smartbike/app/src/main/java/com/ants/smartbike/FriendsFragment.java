package com.ants.smartbike;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.AccessToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A fragment representing a list of Items.
 * <p>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class FriendsFragment extends Fragment implements AbsListView.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private ListView listView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private FriendListAdapter friendListAdapter;
    private SharedPreferences settings;
    private Map<String, Boolean> approvedCache;

    private List<FriendItem> friends = new ArrayList<>();

    // TODO: Rename and change types of parameters
    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FriendsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        friendListAdapter = new FriendListAdapter(this.getActivity(), friends);

        approvedCache = new HashMap<>();
        settings = getActivity().getPreferences(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        // Set the adapter
        listView = (ListView) view.findViewById(R.id.friends_list);
        listView.setAdapter(friendListAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        listView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private Map<String, String> buildPostData(String userFbid) {
        Map<String, String> data = new HashMap<>();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        data.put("ownerFbid", accessToken.getUserId());
        data.put("userFbid", userFbid);
        data.put("bikeUUID", "98:4F:EE:04:51:21");
        return data;
    }

    private boolean getApproved(String friendFbid) {
        if (approvedCache.containsKey(friendFbid)) {
            return approvedCache.get(friendFbid);
        } else {
            return settings.getBoolean(friendFbid, false);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        final String friendFbid = friends.get(position).id;
        String friendName = friends.get(position).name;
        boolean approved = getApproved(friendFbid);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (!approved) {
            builder.setTitle("Approve " + friendName + " to use your bike?");
            builder.setPositiveButton(R.string.approve, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    NetworkHelper.sendPostRequest("approve", getContext(), buildPostData(friendFbid));
                    approvedCache.put(friendFbid, true);
                }
            });
        } else {
            builder.setTitle("Disable " + friendName + " from using your bike?");
            builder.setPositiveButton(R.string.disapprove, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    NetworkHelper.sendPostRequest("disapprove", getContext(), buildPostData(friendFbid));
                    approvedCache.put(friendFbid, false);
                }
            });
        }

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(friends.get(position).id);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = settings.edit();
        for (Map.Entry<String, Boolean> entry: approvedCache.entrySet()) {
            editor.putBoolean(entry.getKey(), entry.getValue());
        }

        // Commit the edits!
        editor.commit();
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = listView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    public void addItem(FriendItem friendItem) {
        friendListAdapter.add(friendItem);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String id);
    }

    public static class FriendItem {
        public String id;
        public String name;

        public FriendItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return "[" +  id + ", " + name + "]";
        }
    }
}
