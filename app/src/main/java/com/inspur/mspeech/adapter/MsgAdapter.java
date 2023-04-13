package com.inspur.mspeech.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.inspur.mspeech.R;
import com.inspur.mspeech.bean.Msg;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author : zhangqinggong
 * date    : 2023/2/6 16:54
 * desc    :
 */
public class MsgAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<Msg> list;
    public MsgAdapter(List<Msg> list){
        this.list = list;
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout leftLayout;
        TextView left_msg;

        LinearLayout rightLayout;
        TextView right_msg;

        public ViewHolder(View view){
            super(view);
            leftLayout = view.findViewById(R.id.left_layout);
            left_msg = view.findViewById(R.id.left_msg);

            rightLayout = view.findViewById(R.id.right_layout);
            right_msg = view.findViewById(R.id.right_msg);
        }
    }

    private class SpaceViewHolder  extends RecyclerView.ViewHolder{
        Space space;
        public SpaceViewHolder(View view){
            super(view);
            space=view.findViewById(R.id.msg_item_space);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item,parent,false);
            return new ViewHolder(view);
        }else {
            View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item_space,parent,false);
            return new SpaceViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == 0){
            ViewHolder viewHolder= (ViewHolder) holder;
            Msg msg = list.get(position);
            if(msg.getType() == Msg.TYPE_RECEIVED){
                //如果是收到的消息，则显示左边的消息布局，将右边的消息布局隐藏
                viewHolder.leftLayout.setVisibility(View.VISIBLE);
                viewHolder.left_msg.setText(msg.getContent());

                //注意此处隐藏右面的消息布局用的是 View.GONE
                viewHolder.rightLayout.setVisibility(View.GONE);
            }else if(msg.getType() == Msg.TYPE_SEND){
                //如果是发出的消息，则显示右边的消息布局，将左边的消息布局隐藏
                viewHolder.rightLayout.setVisibility(View.VISIBLE);
                viewHolder.right_msg.setText(msg.getContent());

                //同样使用View.GONE
                viewHolder.leftLayout.setVisibility(View.GONE);
            }
        }

    }


    @Override
    public int getItemCount() {
        return list.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if(position==list.size()){//最后一个Space
            return 1;
        }else{
            return 0;
        }
    }
}