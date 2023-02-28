package com.inspur.mspeech.adapter;

import android.annotation.SuppressLint;
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
public class QaAdapter extends RecyclerView.Adapter<QaAdapter.ViewHolder> {

   private List<QaBean> list;
   private ItemClickListener mItemClickListener;
   public QaAdapter(List<QaBean> list){
      this.list = list;
   }

   public void setOnItemClickListener(ItemClickListener itemClickListener){
      mItemClickListener = itemClickListener;
   }
   @NonNull
   @Override
   public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.qa_item,parent,false);
      return new ViewHolder(view);
   }

   @SuppressLint("RecyclerView")
   @Override
   public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      QaBean qaBean = list.get(position);
      holder.question.setText("\""+qaBean.getQuestionTxt()+"\"");
      holder.answer.setText("\""+qaBean.getAnswerTxt()+"\"");
   }

   @Override
   public int getItemCount() {
      return list.size();
   }

   public class ViewHolder extends RecyclerView.ViewHolder{
      AppCompatTextView question;
      AppCompatTextView answer;
      public ViewHolder(@NonNull View itemView) {
         super(itemView);
         question = itemView.findViewById(R.id.tv_question);
         answer = itemView.findViewById(R.id.tv_answer);
      }
   }

   public interface ItemClickListener{
      void onClick(int position);
   }
}
