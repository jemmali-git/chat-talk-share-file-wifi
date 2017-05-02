package com.jemali.nadhem.chat_and_share;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import static com.jemali.nadhem.chat_and_share.MainActivity.current_destination;
import static com.jemali.nadhem.chat_and_share.MainActivity.destination_brodcast_ip;
import static com.jemali.nadhem.chat_and_share.MainActivity.destination_ip;
import static com.jemali.nadhem.chat_and_share.MainActivity.destination_name;

import static com.jemali.nadhem.chat_and_share.MainActivity.ip_adress_list;
import static com.jemali.nadhem.chat_and_share.MainActivity.ip_brodcast_list;
import static com.jemali.nadhem.chat_and_share.MainActivity.names_list;

/**
 * Created by nadhem on 22/04/2017.
 */

public class Show_connected_people extends RecyclerView.Adapter<Show_connected_people.ViewHolder> {

    ArrayList<String> people_connected_name=new ArrayList<String>() ;
    ArrayList<Integer> alImage=new ArrayList<Integer>();
    Context context;

    public Show_connected_people(Context context, ArrayList<String> people_connected_name, ArrayList<Integer> alImage) {
        super();
        this.context = context;
        this.people_connected_name = people_connected_name;
        this.alImage = alImage;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.grid_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.tvSpecies.setText(people_connected_name.get(i));
        viewHolder.imgThumbnail.setImageResource(alImage.get(i));

        viewHolder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                destination_name=names_list.get(position);
                destination_brodcast_ip=ip_brodcast_list.get(position);
                destination_ip=ip_adress_list.get(position);
                current_destination.setText(destination_name);

            }
        });
    }

    @Override
    public int getItemCount() {
        return people_connected_name.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public ImageView imgThumbnail;
        public TextView tvSpecies;
        public ItemClickListener clickListener;

        public ViewHolder(View itemView) {
            super(itemView);
            imgThumbnail = (ImageView) itemView.findViewById(R.id.img_thumbnail);
            tvSpecies = (TextView) itemView.findViewById(R.id.tv_species);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            clickListener.onClick(view, getPosition(), false);
        }

        @Override
        public boolean onLongClick(View view) {
            clickListener.onClick(view, getPosition(), true);
            return true;
        }
    }

}

