package com.open_open.englishdictation;

import org.litepal.crud.DataSupport;
import org.litepal.crud.LitePalSupport;

/**
 * Created by dellpc on 2017/6/22.
 */

public class Lesson extends LitePalSupport {

    String lessonName;

    public String getLessonName() {
        return lessonName;
    }

    public void setLessonName(String lessonName) {
        this.lessonName = lessonName;
    }
}
