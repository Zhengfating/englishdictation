package com.open_open.englishdictation;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class ChooseLesson extends AppCompatActivity {

    private ListView listView;
    private Button addBtn;
    private TextView titleTxt;

    private List<String> dataList = new ArrayList<>();
    private List<Lesson> lessonList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_lesson_layout);

        //媒体音量控制
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        initView();

        lessonList = DataSupport.findAll(Lesson.class);
        for (Lesson lesson : lessonList){
            dataList.add(lesson.getLessonName());
        }

        adapter = new ArrayAdapter<>(ChooseLesson.this, R.layout.listview_style, dataList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String titleId = dataList.get(position);
                Intent intent = new Intent(ChooseLesson.this, DictationActivity.class);
                intent.putExtra("titleId", titleId);
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(ChooseLesson.this);
                dialog.setTitle("警告！");
                dialog.setMessage("你要删除吗？");
                dialog.setCancelable(false);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String getTitleId = dataList.get(position);
                        dataList.remove(getTitleId);
                        adapter.notifyDataSetChanged();
                        LitePal.deleteAll(Lesson.class, "lessonName = ?", getTitleId);
                        LitePal.deleteAll(Word.class, "titleId = ?", getTitleId);
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                dialog.show();
                return true;
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //创建对话框构建器
                AlertDialog.Builder builder = new AlertDialog.Builder(ChooseLesson.this);
                // 获取布局
                View view2 = View.inflate(ChooseLesson.this, R.layout.add_lesson_layout, null);
                // 获取布局中的控件
                final EditText add_text = (EditText) view2.findViewById(R.id.add_text);
                final Button add_btn = (Button) view2.findViewById(R.id.add_title_btn);
                // 设置参数
                builder.setTitle("请写个标题").setCancelable(true)
                        .setView(view2);
                // 创建对话框
                final AlertDialog alertDialog = builder.create();

                add_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        String title_text = add_text.getText().toString().trim();
                        if (!"".equals(title_text)){
                            dataList.add(title_text);
                            adapter.notifyDataSetChanged();
                            listView.setSelection(dataList.size() - 1);
                            Lesson new_lesson = new Lesson();
                            new_lesson.setLessonName(title_text);
                            new_lesson.save();
                        }
                        alertDialog.dismiss();// 对话框消失
                    }
                });
                alertDialog.show();
            }
        });

    }

    private void initView(){
        listView = (ListView) findViewById(R.id.lesson_list);
        addBtn = (Button) findViewById(R.id.add_btn);
        titleTxt = (TextView) findViewById(R.id.title_text);
        titleTxt.setText("自定义标题");
    }
}
