package com.inspur.mspeech.adapter;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inspur.mspeech.R;
import com.inspur.mspeech.bean.QaBean;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author : zhangqinggong
 * date    : 2023/2/25 11:02
 * desc    :
 */
public class QaEditAdapter extends RecyclerView.Adapter<QaEditAdapter.ViewHolder> {

   private List<QaBean> list;
   private ItemClickListener mItemClickListener;
   public QaEditAdapter(List<QaBean> list){
      this.list = list;
   }

   public void setOnItemClickListener(ItemClickListener itemClickListener){
      mItemClickListener = itemClickListener;
   }
   @NonNull
   @Override
   public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.qa_edit_item,parent,false);
      return new ViewHolder(view);
   }

   @SuppressLint("RecyclerView")
   @Override
   public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      QaBean qaBean = list.get(position);
      if (!TextUtils.isEmpty(qaBean.getQuestionTxt())){
         holder.text.setText(qaBean.getQuestionTxt());
      }else if (!TextUtils.isEmpty(qaBean.getAnswerTxt())){
         holder.text.setText(qaBean.getAnswerTxt());
      }

      if (position == getItemCount()-1){
         holder.divider.setVisibility(View.GONE);
      }

      if (qaBean.getStandard() == 1){
         holder.delete.setVisibility(View.INVISIBLE);
      }
      holder.delete.setOnClickListener(view -> {
         if (mItemClickListener != null){
            mItemClickListener.onClick(position);
         }
      });

   }

   @Override
   public int getItemCount() {
      return list.size();
   }

   public class ViewHolder extends RecyclerView.ViewHolder{
      AppCompatTextView text;
      AppCompatTextView divider;
      AppCompatTextView delete;
      public ViewHolder(@NonNull View itemView) {
         super(itemView);
         text = itemView.findViewById(R.id.tv_text);
         divider = itemView.findViewById(R.id.item_divider);
         delete = itemView.findViewById(R.id.delete);
      }
   }

   public interface ItemClickListener{
      void onClick(int position);
   }
}
