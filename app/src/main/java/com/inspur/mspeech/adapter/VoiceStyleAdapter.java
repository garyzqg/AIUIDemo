package com.inspur.mspeech.adapter;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inspur.mspeech.R;
import com.inspur.mspeech.bean.VoiceStyleBean;
import com.inspur.mspeech.utils.PrefersTool;

import java.util.List;

/**
 * @author : zhangqinggong
 * date    : 2023/5/10 17:02
 * desc    :
 */
public class VoiceStyleAdapter extends RecyclerView.Adapter<VoiceStyleAdapter.ViewHolder> {

   private List<VoiceStyleBean> list;
   private ItemClickListener mItemClickListener;
   public VoiceStyleAdapter(List<VoiceStyleBean> list){
      this.list = list;
   }

   public void setOnItemClickListener(ItemClickListener itemClickListener){
      mItemClickListener = itemClickListener;
   }
   @NonNull
   @Override
   public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.voice_name_item,parent,false);
      return new ViewHolder(view);
   }

   @SuppressLint("RecyclerView")
   @Override
   public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      VoiceStyleBean voiceBean = list.get(position);
      holder.voiceAlias.setText(voiceBean.getVoiceStyleCnName());
      if (TextUtils.equals(PrefersTool.getVoiceStyle(),voiceBean.getVoiceStyleEnName())){
         holder.select.setImageResource(R.drawable.selected);
         holder.select.setEnabled(false);
      }else {
         holder.select.setImageResource(R.drawable.un_select);
         holder.select.setEnabled(true);
      }

      holder.select.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            PrefersTool.setVoiceStyle(voiceBean.getVoiceStyleEnName());
            if (mItemClickListener != null){
               mItemClickListener.onClick(position);
            }
         }
      });
   }

   @Override
   public int getItemCount() {
      return list.size();
   }

   public class ViewHolder extends RecyclerView.ViewHolder{
      TextView voiceAlias;
      ImageView select;
      public ViewHolder(@NonNull View itemView) {
         super(itemView);
         voiceAlias = itemView.findViewById(R.id.tv_voice_alias);
         select = itemView.findViewById(R.id.iv_select);
      }
   }

   public interface ItemClickListener{
      void onClick(int position);
   }
}
