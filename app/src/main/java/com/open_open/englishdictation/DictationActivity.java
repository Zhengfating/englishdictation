package com.open_open.englishdictation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DictationActivity extends AppCompatActivity implements View.OnClickListener{

    private Button back_btn;
    private Button title_add_btn;
    private TextView title_count_text;

    private RecyclerView recyclerView;

    private LinearLayout linearLayout_add;
    private RelativeLayout relativeLayout_del;

    private EditText bottom_editText;
    private Button bottom_add_word;
    private Button bottom_del_word;
    private Button bottom_choose_btn;

    private List<Word> wordsList;
    private WordsAdapter wordsAdapter;
    private String count = "0";
    private Word new_word;
    private String titleId;

    private static final String ENGLISH = "en";
    private static final String CHINESE = "zh";

    private SpeechUtil speechUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dictation_layout);
        //媒体音量控制
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        titleId = getIntent().getStringExtra("titleId");

        initView();

        wordsList = LitePal.where("titleId = ?", titleId).find(Word.class);
        count = String.valueOf(wordsList.size());
        title_count_text.setText("(" + count + ")");

        speechUtil = new SpeechUtil(DictationActivity.this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        wordsAdapter = new WordsAdapter(wordsList, speechUtil);
        recyclerView.setAdapter(wordsAdapter);

        wordsAdapter.setOnItemClickLitener(new WordsAdapter.OnItemClickLitener()
        {
            @Override
            public void onItemLongClick(View v,int position)
            {
                wordsAdapter.hideView(true);
                wordsAdapter.restartSelect(position);
                linearLayout_add.setVisibility(View.GONE);
                title_add_btn.setBackgroundResource(R.drawable.back_del_btn);
                relativeLayout_del.setVisibility(View.VISIBLE);
                bottom_choose_btn.setBackgroundResource(R.drawable.choose_none);
            }
            @Override
            public void allCheckedClick() {
                bottom_choose_btn.setBackgroundResource(R.drawable.choose_all);
            }

            @Override
            public void noAllCheckedClick() {
                bottom_choose_btn.setBackgroundResource(R.drawable.choose_none);
            }
        });

    }

    private void initView(){
        back_btn = (Button) findViewById(R.id.back_btn);
        title_add_btn = (Button) findViewById(R.id.add_btn);
        title_count_text = (TextView) findViewById(R.id.count_text);

        back_btn.setVisibility(View.VISIBLE);
        title_count_text.setVisibility(View.VISIBLE);

        recyclerView = (RecyclerView) findViewById(R.id.recylcer_view);

        linearLayout_add = (LinearLayout) findViewById(R.id.bottom_layout_add);
        relativeLayout_del = (RelativeLayout) findViewById(R.id.bottom_layout_del);

        bottom_editText = (EditText) findViewById(R.id.editText);
        bottom_add_word = (Button) findViewById(R.id.add_word);
        bottom_del_word = (Button) findViewById(R.id.del_word);
        bottom_choose_btn = (Button) findViewById(R.id.choose_btn);

        back_btn.setOnClickListener(this);
        title_add_btn.setOnClickListener(this);
        bottom_add_word.setOnClickListener(this);
        bottom_del_word.setOnClickListener(this);
        bottom_choose_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back_btn:
                finish();
                break;
            case R.id.add_btn:
                if (linearLayout_add.getVisibility() == View.GONE && relativeLayout_del.getVisibility() == View.GONE){
                    linearLayout_add.setVisibility(View.VISIBLE);
                    title_add_btn.setBackgroundResource(R.drawable.back_del_btn);
                }else if (linearLayout_add.getVisibility() == View.VISIBLE ){
                    linearLayout_add.setVisibility(View.GONE);
                    title_add_btn.setBackgroundResource(R.drawable.add);
                }else if (relativeLayout_del.getVisibility() == View.VISIBLE){
                    relativeLayout_del.setVisibility(View.GONE);
                    title_add_btn.setBackgroundResource(R.drawable.add);
                    wordsAdapter.hideView(false);
                    wordsAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.add_word:
                String addWord = bottom_editText.getText().toString();
                if (!"".equals(addWord)){
                    Cursor cursor = LitePal.findBySQL("select * from Word where titleId = ? and word_en = ?", titleId, addWord);
                    if (cursor.getCount() > 0){
                        Toast.makeText(DictationActivity.this, "你已添加该单词", Toast.LENGTH_SHORT).show();
                        cursor.close();
                    }else {
                        try {
                            String http = SplitAddress.getAddress(addWord, ENGLISH, CHINESE);
                            HttpUtil.sendOkHttpRequest(http, new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                }
                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    final String responseText = response.body().string();
                                    new_word = Utility.handleWord(responseText, titleId);
                                    if (new_word != null){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                            wordsList.add(new_word);
                                            wordsAdapter.notifyItemInserted(wordsList.size() - 1);
                                            recyclerView.scrollToPosition(wordsList.size() - 1);
                                            count = String.valueOf(Integer.parseInt(count) + 1);
                                            title_count_text.setText("(" + count + ")");
                                            bottom_editText.setText("");
                                            }
                                        });
                                    }
                                }
                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case R.id.del_word:
                AlertDialog.Builder dialog = new AlertDialog.Builder(DictationActivity.this);
                dialog.setTitle("警告！");
                dialog.setMessage("你要删除吗？");
                dialog.setCancelable(false);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int getcount = wordsAdapter.removeData();
                        count = String.valueOf(Integer.parseInt(count) - getcount);
                        title_count_text.setText("(" + count + ")");
                        title_add_btn.setVisibility(View.VISIBLE);
                        title_add_btn.setBackgroundResource(R.drawable.add);
                        relativeLayout_del.setVisibility(View.GONE);
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                dialog.show();
                break;
            case R.id.choose_btn:
                if (wordsAdapter.selectAll()){
                    bottom_choose_btn.setBackgroundResource(R.drawable.choose_all);
                }else {
                    bottom_choose_btn.setBackgroundResource(R.drawable.choose_none);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        speechUtil.release();
        super.onDestroy();
    }

}
