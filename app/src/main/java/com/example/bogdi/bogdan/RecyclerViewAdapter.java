package com.example.bogdi.bogdan;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    Context context;
    View view;
    ViewHolder viewHolder;
    ArrayList<String> jsonObjects;

    public RecyclerViewAdapter(Context context, ArrayList<String> jsonObjects) {
        this.context = context;
        this.jsonObjects=jsonObjects;
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view= LayoutInflater.from(context).inflate(R.layout.show_card,parent,false);
        viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.tv_cardNumber.setText(getCardNumber(jsonObjects.get(position)));
        holder.tv_cardName.setText(getCardName(jsonObjects.get(position)));
        holder.tv_expDate.setText(getCardExpDate(jsonObjects.get(position)));

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, final int position, boolean isLongClick) {
                if(isLongClick){
                    AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(context);
                    deleteBuilder.setMessage("Do you really want to delete your Credit Card?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PayActivity.getpayActivity().setPosition(position);
                                    PayActivity.getpayActivity().authenticationFingerprint();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .create()
                            .show();
                }else {
                    PayActivity.getpayActivity().chooseCards(position,false);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return jsonObjects.size();
    }

    public String getCardName(String data){
        try {
            JSONObject jsonObject=new JSONObject(data);
            return jsonObject.get("card_name").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getCardNumber(String data){
        try {
            JSONObject jsonObject=new JSONObject(data);
            return jsonObject.get("CARD_NUMBER").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getCardExpDate(String data){
        try {
            JSONObject jsonObject=new JSONObject(data);
            return jsonObject.get("EXP_DATE").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    public  static class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {

        TextView tv_cardNumber;
        TextView tv_cardName;
        TextView tv_expDate;
        private ItemClickListener itemClickListener;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_cardNumber=(TextView) itemView.findViewById(R.id.tv_cardNumber);
            tv_cardName=(TextView) itemView.findViewById(R.id.tv_cardName);
            tv_expDate=(TextView) itemView.findViewById(R.id.tv_cardExpDate);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public  void setItemClickListener(ItemClickListener itemClickListener){
            this.itemClickListener=itemClickListener;
        }

        @Override
        public boolean onLongClick(View v) {
            itemClickListener.onClick(v,getAdapterPosition(),true);
            return true;
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v,getAdapterPosition(),false);
        }
    }
}
