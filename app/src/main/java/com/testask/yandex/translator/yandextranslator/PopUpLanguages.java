package com.testask.yandex.translator.yandextranslator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.testask.yandex.translator.yandextranslator.core.Translator;
import com.testask.yandex.translator.yandextranslator.fragments.FragmentTranslate;

/**
 * Данный класс предназначен для добавление контента в pop_up_languages,
 * назначения слушателей для контента
 *
 * @version 1.0 24 Апр 2017
 * @author Миронов Максим Андреевич
 */
public class PopUpLanguages extends AppCompatActivity {

    //Необходима для хранения списка поддерживаемых языков
    public static Set<Map.Entry<String, JsonElement>> entries;

    //Необходима для идентификации объекта класса,
    // определяющего контент для исходного языка или языка перевода
    public static Boolean sourceLanguage;

    //Необходима для связи с ListView в pop_up_languages
    private ListView languageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        //Получение идентификатора, определяющего контент для исходного языка или языка перевода
        try {
            sourceLanguage = bundle.getBoolean("SourceLanguage");
            if (sourceLanguage) {
                //Установка соотвутствующего заголовка
                this.setTitle(getResources().getString(R.string.title_source_language));
            }
            else {
                //Установка соотвутствующего заголовка
                this.setTitle(getResources().getString(R.string.title_target_language));
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.pop_up_languages);
        setProperDimensions();
        setViews();
        setSupportedLanguages(entries);
        setListViewListener();
    }

    /**
     * Устанавливает размеры layout вне зависимости от размера экрана
     */
    private void setProperDimensions() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int layoutWidth = displayMetrics.widthPixels;
        final int layoutHeight = displayMetrics.heightPixels;
        getWindow().setLayout((int)(layoutWidth*0.9), (int)(layoutHeight*0.75));
    }

    /**
     * Связка переменной ListView с view из pop_up_languages
     */
    private void setViews() {
        languageList = (ListView) findViewById(R.id.language_list);
    }

    /**
     * Формирование списка languageList для выбора языков
     * @param entries - список языков и их сокращений
     */
    public void setSupportedLanguages (Set<Map.Entry<String, JsonElement>> entries) {
        String toAdd;
        ArrayList<String> listItems = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                listItems);
        languageList.setAdapter(adapter);
        //Если вызвано activity для исходного языка
        if (sourceLanguage) {
            listItems.add(getResources().getString(R.string.source_language));
        }
        for (Map.Entry<String, JsonElement> entry: entries) {
            toAdd = entry.getValue().toString().replace("\"", "");
            listItems.add(toAdd);
        }
    }

    /**
     * Задает обработку событий для ListView languageList
     */
    private void setListViewListener() {
        languageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(PopUpLanguages.this, MainActivity.class);
                //Если вызвано activity для исходного языка
                if (sourceLanguage) {
                    if (position == 0) {
                        //Автоопределение включено
                        FragmentTranslate.autoDetection = true;
                        //Получение данных выбранного элемента
                        Translator.sourceLanguage = languageList.getItemAtPosition(0).toString();
                    }
                    //Автоопределение выключено
                    FragmentTranslate.autoDetection = false;
                    //Получение данных выбранного элемента
                    Translator.sourceLanguage = languageList.getItemAtPosition(position).toString();
                }
                else {
                    Translator.targetLanguage = languageList.getItemAtPosition(position).toString();
                }
                //Передача выбранного языка в объект класса MainActivity
                intent.putExtra("Language", languageList.getItemAtPosition(position).toString());
                startActivity(intent);
            }
        });
    }
}
