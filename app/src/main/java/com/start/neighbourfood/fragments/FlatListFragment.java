package com.start.neighbourfood.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.start.neighbourfood.R;
import com.start.neighbourfood.Utils.RecyclerTouchListener;
import com.start.neighbourfood.adapters.FlatsInfoRecyclerViewAdapter;
import com.start.neighbourfood.auth.TaskHandler;
import com.start.neighbourfood.models.FlatsInfo;
import com.start.neighbourfood.models.ServiceConstants;
import com.start.neighbourfood.pages.BaseActivity;
import com.start.neighbourfood.services.ServiceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates the use of {@link RecyclerView} with a {@link LinearLayoutManager} and a
 * {@link GridLayoutManager}.
 */
public class FlatListFragment extends BaseFragment implements TaskHandler, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = FlatListFragment.class.getSimpleName();
    private List<FlatsInfo> mDataset;
    public FlatsInfoRecyclerViewAdapter mAdapter;
    private SearchView searchView;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    public FlatListFragment() {
        this.mDataset = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        fetchFlatinfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.content_flat_list, container, false);
        RecyclerView mRecyclerView = rootView.findViewById(R.id.recyclerView);
        mAdapter = new FlatsInfoRecyclerViewAdapter(getActivity(), mDataset);
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipe_container);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        /*mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                fetchFlatinfo();
            }
        });*/

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        //Set On Click listner
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                FlatsInfo flats = mDataset.get(position);
                loadFoodItemsForFlat(flats.getSellerId());
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        return rootView;
    }

    private void fetchFlatinfo() {
        showProgressDialog();
        try {
            JSONObject userBaseInfo = new JSONObject(((BaseActivity) getActivity()).getFromSharedPreference(ServiceConstants.userDetail));
            ServiceManager.getInstance(getActivity()).fetchAvailableHoods(userBaseInfo, this);
        } catch (IllegalAccessException | JSONException e) {
            e.printStackTrace();
        }
    }


    private void loadFoodItemsForFlat(String sellerId) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        FoodListFragment fragment = new FoodListFragment();
        Slide exitSlide = new Slide(){{setSlideEdge(Gravity.LEFT);}};
        Slide enterSlide = new Slide(){{setSlideEdge(Gravity.RIGHT);}};
        fragment.setEnterTransition(enterSlide);
        fragment.setExitTransition(exitSlide);
        Bundle args = new Bundle();
        args.putString("sellerId", sellerId);
        fragment.setArguments(args);

        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home, menu);
        SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                mAdapter.getFilter().filter(query);
                return false;
            }
        });
    }

    @Override
    public void onTaskCompleted(JSONObject result) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<FlatsInfo> flatsInfos = objectMapper.readValue(result.getJSONArray("Result").toString(), new TypeReference<List<FlatsInfo>>() {
            });
            mDataset = flatsInfos;
            mAdapter.setDataSet(flatsInfos);
            mAdapter.notifyDataSetChanged();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        hideProgressDialog();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e(TAG, "onErrorResponse: Unable to load", error);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        fetchFlatinfo();
    }
}
