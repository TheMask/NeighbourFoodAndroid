package com.start.neighbourfood.fragments;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.start.neighbourfood.R;
import com.start.neighbourfood.models.FlatsInfo;
import com.start.neighbourfood.models.FoodItemDetails;

import java.util.List;


public class FoodItemsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "FoodItemsRecyclerViewAdapter";

    private List<FoodItemDetails> mDataSet;
    private RecyclerViewClickListener mListener;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public FoodItemsRecyclerViewAdapter(RecyclerViewClickListener listener, List<FoodItemDetails> dataSet) {
        // For now, giving the dummy food items
        mDataSet = dataSet;
        //mContext = context;
        mListener = listener;

    }
    // END_INCLUDE(recyclerViewSampleViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public FoodItemRowViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        // Create a new view.
        View v = LayoutInflater.from(context)
                .inflate(R.layout.fragment_flat_info, viewGroup, false);

        return new FoodItemRowViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RowViewHolder) {
            FoodItemRowViewHolder rowHolder = (FoodItemRowViewHolder) holder;
            //set values of data here
            // Get element from your dataset at this position and replace the contents of the view
            // with that element
            rowHolder.getFoodNameView().setText(mDataSet.get(position).getItemName());
            rowHolder.getServingView().setText(mDataSet.get(position).getItemServing());
            rowHolder.getPriceView().setText(mDataSet.get(position).getItemPrice());
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

}
