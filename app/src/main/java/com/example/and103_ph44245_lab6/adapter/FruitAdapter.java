package com.example.and103_ph44245_lab6.adapter;

import android.content.Context;
import android.nfc.Tag;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.and103_ph44245_lab6.R;
import com.example.and103_ph44245_lab6.databinding.ItemFruitBinding;
import com.example.and103_ph44245_lab6.model.Fruit;
import com.example.and103_ph44245_lab6.view.HomeActivity;

import java.util.ArrayList;

public class FruitAdapter  extends RecyclerView.Adapter<FruitAdapter.ViewHolder>{
    private Context context;
    private ArrayList<Fruit> list;
    private FruitClick fruitClick;

    public FruitAdapter(Context context, ArrayList<Fruit> list, FruitClick fruitClick) {
        this.context = context;
        this.list = list;
        this.fruitClick = fruitClick;
    }

    public interface FruitClick {
        void delete(Fruit fruit);
        void edit(Fruit fruit);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFruitBinding binding = ItemFruitBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Fruit fruit = list.get(position);
        holder.binding.tvName.setText("Name: " +fruit.getName());
        holder.binding.tvPriceQuantity.setText("Price: " +fruit.getPrice()+" \nQuantity: " +fruit.getQuantity());
        holder.binding.tvDes.setText("Description: " +fruit.getDescription());

        if (fruit.getImage().size()!=0){
            String url  = fruit.getImage().get(0);
            String newUrl = url.replace("localhost", "192.168.1.65");

            Glide.with(context)
                    .load(newUrl)
                    .thumbnail(Glide.with(context).load(R.drawable.baseline_broken_image_24))
                    .into(holder.binding.img);
            Log.d("321321", "onBindViewHolder: "+list.get(position).getImage().get(0));
        }
//        else {
//            Toast.makeText(context, "Khong chon anh", Toast.LENGTH_SHORT).show();
//        }



        holder.binding.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fruitClick.delete(fruit);
            }
        });

        holder.binding.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fruitClick.edit(fruit);
            }
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemFruitBinding binding;
        public ViewHolder(ItemFruitBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
