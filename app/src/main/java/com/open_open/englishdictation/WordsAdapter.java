package com.open_open.englishdictation;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;
import java.util.HashSet;
import java.util.List;

/**
 * Created by dellpc on 2017/6/10.
 */

public class WordsAdapter extends RecyclerView.Adapter<WordsAdapter.ViewHolder>{

    private List<Word> mWordsList;
    private SpeechUtil speechUtil;
    private Boolean isDelete = false;
    private HashSet<Integer> positionSet = new HashSet<>();

    private OnItemClickLitener mOnItemClickLitener;

    public interface OnItemClickLitener
    {
        void onItemLongClick(View v,int position);
        void allCheckedClick();
        void noAllCheckedClick();
    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener)
    {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        View itemView;
        TextView word_en;
        TextView word_ch;
        Button translate_btn;
        CheckBox checkBox;
        ImageView checkImg;

        public ViewHolder(View view){
            super(view);
            itemView = view;
            word_en = (TextView)view.findViewById(R.id.en_text);
            word_ch = (TextView)view.findViewById(R.id.ch_text);
            translate_btn = (Button)view.findViewById(R.id.listening_btn);
            checkBox = (CheckBox) view.findViewById(R.id.check_box);
            checkImg = (ImageView) view.findViewById(R.id.check_Img);
        }
    }

    public WordsAdapter(List<Word> wordsList, SpeechUtil speechUtil){
        this.mWordsList = wordsList;
        this.speechUtil = speechUtil;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.words_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder,final int position) {
        final Word word = mWordsList.get(position);

        holder.word_en.setText(word.getWord_en());
        holder.word_ch.setText(word.getWord_ch());
        if (isDelete){
            if (word.getIs_select()){
                holder.checkImg.setImageResource(R.mipmap.check_true);
                word.setIs_select(true);
            }else {
                holder.checkImg.setImageResource(R.mipmap.check_false);
                word.setIs_select(false);
            }
            holder.checkImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (word.getIs_select()){
                        holder.checkImg.setImageResource(R.mipmap.check_false);
                        positionSet.remove(position);
                        word.setIs_select(false);
                    }else {
                        holder.checkImg.setImageResource(R.mipmap.check_true);
                        positionSet.add(position);
                        word.setIs_select(true);
                    }
                    ifAllChecked();
                }
            });
        }

        holder.translate_btn.setVisibility(isDelete ? View.GONE: View.VISIBLE);
        holder.checkImg.setVisibility(isDelete ? View.VISIBLE: View.GONE);

         // 如果设置了回调，则设置点击事件
        if (mOnItemClickLitener != null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!isDelete){
                        mOnItemClickLitener.onItemLongClick(v, position);
                    }else {
                        if (word.getIs_select()){
                            holder.checkImg.setImageResource(R.mipmap.check_false);
                            positionSet.remove(position);
                            word.setIs_select(false);
                        }else {
                            holder.checkImg.setImageResource(R.mipmap.check_true);
                            positionSet.add(position);
                            word.setIs_select(true);
                        }
                    }
                    ifAllChecked();
                    return true;
                }
            });
        }

        if (word.getIs_read()){
            holder.translate_btn.setText("已 听");
            holder.translate_btn.setTextColor(Color.RED);
        }else {
            holder.translate_btn.setText("未 听");
            holder.translate_btn.setTextColor(Color.BLACK);
        }

        holder.translate_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechUtil.speak(word.getWord_en());
                mWordsList.get(position).setIs_read(true);
                holder.translate_btn.setText("已 听");
                holder.translate_btn.setTextColor(Color.RED);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mWordsList.size();
    }


    public int removeData() {
        int count = positionSet.size();
        HashSet<Word> deleteListSet = new HashSet<>();
        if (count > 0){
            for (Integer integer:positionSet){
                deleteListSet.add(mWordsList.get(integer.intValue()));
            }
            for (Word word:deleteListSet){
                mWordsList.remove(word);
                LitePal.deleteAll(Word.class, "word_en = ?", word.getWord_en());
            }
            positionSet.clear();
        }
        isDelete = false;
        notifyDataSetChanged();
        return count;
    }

    public void hideView(Boolean isDelete){
        this.isDelete = isDelete;
    }

    public boolean selectAll(){
        if ( positionSet.size() < mWordsList.size()){
            for (int i = 0; i < mWordsList.size(); i++){
                mWordsList.get(i).setIs_select(true);
                positionSet.add(i);
            }
            notifyDataSetChanged();
            return true;
        }else {
            for (int i = 0; i < mWordsList.size(); i++){
                mWordsList.get(i).setIs_select(false);
                positionSet.remove(i);
            }
            notifyDataSetChanged();
        }
        return false;
    }

    public void restartSelect(int position){
        for (int i = 0; i < mWordsList.size(); i++){
            mWordsList.get(i).setIs_select(false);
        }
        mWordsList.get(position).setIs_select(true);
        positionSet.clear();
        positionSet.add(position);
        notifyDataSetChanged();
    }

    private void ifAllChecked(){
        if (positionSet.size() == mWordsList.size()){
            mOnItemClickLitener.allCheckedClick();
        }else {
            mOnItemClickLitener.noAllCheckedClick();
        }
    }
}
