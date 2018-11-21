package com.bnadev.tourism.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bnadev.tourism.R;
import com.bnadev.tourism.model.TourismList;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

public class TourismAdapter extends RecyclerView.Adapter<TourismAdapter.TourismViewHolder> {

    private List<TourismList> tourismLists;
    private int rowLayout;
    private Context context;

    class TourismViewHolder extends RecyclerView.ViewHolder{

        LinearLayout lyTourism;
        ImageView ivImage;
        TextView tvPlaceName;
        TextView tvPlaceAddress;

        TourismViewHolder(View v) {
            super(v);

            lyTourism = (LinearLayout) v.findViewById(R.id.tourism_layout);
            ivImage = (ImageView) v.findViewById(R.id.ivImage);
            tvPlaceName = (TextView) v.findViewById(R.id.tvPlaceName);
            tvPlaceAddress = (TextView) v.findViewById(R.id.tvPlaceAddress);
        }
    }

    public TourismAdapter(Context context, List<TourismList> tourismLists, int rowLayout ){

        this.context = context;
        this.tourismLists = tourismLists;
        this.rowLayout = rowLayout;
    }

    @NonNull
    @Override
    public TourismAdapter.TourismViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(rowLayout, parent, false);
        return new TourismViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TourismAdapter.TourismViewHolder holder, int position) {

        Picasso.with(context).load(tourismLists.get(position).getGambarPariwisata()).into(holder.ivImage);
        holder.tvPlaceName.setText(tourismLists.get(position).getNamaPariwisata());
        holder.tvPlaceAddress.setText(tourismLists.get(position).getAlamatPariwisata());

    }

    @Override
    public int getItemCount() {
        return tourismLists.size();
    }
}
