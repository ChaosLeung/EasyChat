package org.zhj.easychat.friend;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.zhj.easychat.R;

/**
 * @author Chaos
 *         2015/03/11.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView friendsView;
    private TextView noneTipsText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
        friendsView = (RecyclerView) rootView.findViewById(R.id.friends);
        friendsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        friendsView.setItemAnimator(new DefaultItemAnimator());

        noneTipsText = (TextView) rootView.findViewById(R.id.noneTips);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }
}
