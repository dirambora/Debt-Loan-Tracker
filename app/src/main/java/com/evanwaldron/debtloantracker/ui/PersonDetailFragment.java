package com.evanwaldron.debtloantracker.ui;

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.evanwaldron.debtloantracker.R;

/**
 * Created by Evan on 9/24/14.
 */
public class PersonDetailFragment extends Fragment {

    public static final String ARG_PERSON_NAME = "person_name";
    public static final String ARG_PERSON_ID = "person_id";

    public static PersonDetailFragment newInstance(int id, String name){
        PersonDetailFragment fragment = new PersonDetailFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_PERSON_ID, id);
        args.putString(ARG_PERSON_NAME, name);
        fragment.setArguments(args);

        return fragment;
    }

    private String mPersonName;
    private int mPersonId;
    private PersonInfoFragment mPersonInfo;
    private ItemListFragment mItemList;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mPersonId = getArguments().getInt(ARG_PERSON_ID);
        mPersonName = getArguments().getString(ARG_PERSON_NAME);

        mPersonInfo = PersonInfoFragment.newInstance(mPersonId);
        mItemList = ItemListFragment.newInstance(mPersonId, mPersonName);

        getFragmentManager().beginTransaction()
                .replace(R.id.person_info_container, mPersonInfo)
                .replace(R.id.item_list_container, mItemList)
                .commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_person_detail, null);
    }

    public void restoreActionBar(ActionBar actionBar){
        if(mItemList != null){
            mItemList.restoreActionBar(actionBar);
        }
    }

}
