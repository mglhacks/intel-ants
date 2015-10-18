package com.ants.smartbike;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BikesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class BikesFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private Button lockButton;
    private Button unlockButton;

    public BikesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bikes, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        lockButton = (Button) getActivity().findViewById(R.id.lock_button);
        lockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).lockBike();
            }
        });
        unlockButton = (Button) getActivity().findViewById(R.id.unlock_button);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).unlockBike();
            }
        });
        boolean locked = ((MainActivity) getActivity()).currentlyLocked;
        lockButton.setVisibility(locked ? View.GONE : View.VISIBLE);
        unlockButton.setVisibility(!locked ? View.GONE : View.VISIBLE);
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
