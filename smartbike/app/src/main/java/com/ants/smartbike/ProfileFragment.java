package com.ants.smartbike;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.nostra13.universalimageloader.core.ImageLoader;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ProfileFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private boolean setupDone = false;

    public ProfileFragment() {
        // Required empty public constructor
    }

    private ImageView profileIcon;
    private TextView profileName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setupDone = false;
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!setupDone) {
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            profileIcon = (ImageView) getActivity().findViewById(R.id.profile_icon);
            profileName = (TextView) getActivity().findViewById(R.id.profile_name);
            profileName.setText("Your profile");

            String url = "https://graph.facebook.com/" + accessToken.getUserId() + "/picture";
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(url, profileIcon);

            setupDone = true;
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
