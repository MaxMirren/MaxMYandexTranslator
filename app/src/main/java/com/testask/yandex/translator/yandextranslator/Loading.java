package com.testask.yandex.translator.yandextranslator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;

import com.testask.yandex.translator.yandextranslator.core.Translator;
import com.testask.yandex.translator.yandextranslator.core.TranslatorFunctions;

/**
 * Данный класс предназначен для графической демонстрации загрузки данных
 * и запрещения взаимодействия с view, пока данные контента не загрузятся.
 *
 * @version 1.0 24 Апр 2017
 * @author Миронов Максим Андреевич
 */
public class Loading extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        setProperDimensions();
        //Создание переменной для осуществдения запроса на получение списка поддерживаемых языков
        //и передача объекта класса для будущего автозавершения
        Translator translator = new Translator(TranslatorFunctions.GET_LANGUAGE_LIST, this);
        translator.execute();
    }

    @Override
    public void onBackPressed() {
        //Запрет на самостоятельное закрытие activity
    }

    /**
     * Устанавливает размеры layout вне зависимости от размера экрана
     */
    private void setProperDimensions() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int layoutWidth = displayMetrics.widthPixels;
        final int layoutHeight = displayMetrics.heightPixels;
        getWindow().setLayout((int) (layoutWidth * 0.59), (int) (layoutHeight * 0.11));
    }

}
