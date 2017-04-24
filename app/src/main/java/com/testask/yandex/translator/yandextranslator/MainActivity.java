/*
* %W% %E% Миронов Максим Андреевич
* Москва, 2017
 */

package com.testask.yandex.translator.yandextranslator;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.Locale;

import com.testask.yandex.translator.yandextranslator.core.InternalDataController;
import com.testask.yandex.translator.yandextranslator.fragments.FragmentFavorites;
import com.testask.yandex.translator.yandextranslator.fragments.FragmentAbout;
import com.testask.yandex.translator.yandextranslator.fragments.FragmentTranslate;

/**
 * Данный класс предназначен для добавления навигации по всем layout,
 * управления view во вкладке Переводчик, выполнения методов для формирования контента
 * при первой загрузке приложения.
 *
 * @version 1.0 24 Апр 2017
 * @author Миронов Максим Андреевич
 */
public class MainActivity extends AppCompatActivity {

    //Содержит путь до папки cache, где будет храниться файл с историей и избранным
    public static String cachePath;

    //Необходима для проверки, при первом старте приложения
    private static Boolean firstStart = true;

    //Необходима для связи с BottomNavigationView в activity_main
    private BottomNavigationView bottomNavigationView;
    //Необходимы для связи с переключателями исходного и языка перевода соответственно во fragment_translate
    private Button sourceLanguageChanger, targetLanguageChanger;
    //Необходима для связи с полем ввода во fragment_translate
    private EditText editField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setViews();
        setBottomNavigationListener();
        //Выполнение необходимых действий при первом старте
        if (firstStart) {
            requestSupportedLanguages();
            cachePath = getCacheDir().getPath();
            new InternalDataController().execute();
            firstStart = false;
        }
        //Выполнение необходимых действий при последующих стартах
        else {
            Bundle bundle = getIntent().getExtras();
           //При наличии сообщения от PopUpLanguages выполнить следующие действия
            try {
               String returnedLanguage;
               returnedLanguage = bundle.getString("Language");
                //В случае, если пришели данные исходного языка, то добавить данные для соответствующей клавиши
                // и записать сохраненное состояние для FragmentTranslate
                if (PopUpLanguages.sourceLanguage) {
                    sourceLanguageChanger.setText(returnedLanguage);
                    FragmentTranslate.savedState.putString(FragmentTranslate.SOURCE_LANGUAGE, returnedLanguage);
                }
                //В случае, если пришели данные языка перевода, то добавить данные для соответствующей клавиши
                // и записать сохраненное состояние для FragmentTranslate
                else {
                    targetLanguageChanger.setText(returnedLanguage);
                    FragmentTranslate.savedState.putString(FragmentTranslate.TARGET_LANGUAGE, returnedLanguage);
                    //Сделать поле ввода доступным
                    editField.setEnabled(true);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onBackPressed() {
        //Обеспечивает молниеносный выход из приложения
        finish();
    }

    /**
     * Связка переменных view с view из activity_main и включенных activity
     */
    private void setViews() {
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        sourceLanguageChanger = (Button) findViewById(R.id.source_language);
        targetLanguageChanger = (Button) findViewById(R.id.target_language);
        editField = (EditText) findViewById(R.id.edit_field);
    }

    /**
     * Добавление слушателя кнопкам bottomNavigationView для переключения между экранами(фрагментами)
     */
    private void setBottomNavigationListener() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                FragmentTransaction fragmentTransaction;
                //Назначение загрузки соответствующих фрагментов при клике по соответствующим пунктам
                switch (item.getItemId()) {
                    case R.id.item_translate:
                        fragment = new FragmentTranslate();
                        fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_in_activity_main, fragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                        break;
                    case R.id.item_favorites:
                        fragment = new FragmentFavorites().setBottomNavigationViewControllers(MainActivity.this, bottomNavigationView);
                        fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_in_activity_main, fragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                        break;
                    case R.id.item_settings:
                        fragment = new FragmentAbout();
                        fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_in_activity_main, fragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    /**
     * Определяет слушателей для кнопок смены исходного языка и языка перевода
     */
    public void languageChangersListeners(View view) {
        view.getId();
        Intent intent;
        switch (view.getId()) {
            //Исходный язык
            case R.id.source_language:
                 intent = new Intent(MainActivity.this, PopUpLanguages.class);
                //true - обозначение вызова для source_language
                intent.putExtra("SourceLanguage", true);
                startActivity(intent);
                break;
            //Язык перевода
            case R.id.target_language:
                intent = new Intent(MainActivity.this, PopUpLanguages.class);
                //false - обозначение вызова для target_language
                intent.putExtra("SourceLanguage", false);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    /**
     * Проверяет наличие интернет-соединения
     * @return результат проверки - true, в случае наличия и false в случае его отсутствия
     */
    private Boolean hasInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if ((networkInfo != null) && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Определяет, отличается ли язык системы от русского
     * @return возвражает true в случае, если язык системы русский, и false,
     * если язык системы отличен от русского
     */
    @NonNull
    public static Boolean checkIfSystemLanguageIsRussian() {
        String language = Locale.getDefault().getDisplayLanguage();
        return language.equals("русский");
    }

    /**
     * Получает список поддерживаемых языков и формирует интерфейс в виде списка
     */
    private void requestSupportedLanguages() {
        if (hasInternetConnection()) {
            Intent intent = new Intent(MainActivity.this, Loading.class);
            startActivity(intent);
        }
        else {
            makeToast("NO INTERNET CONNECTION");
        }
    }

    /**
     * Упрощает процесс создания Toast-уведомлений
     * @param string контент уведомления
     */
    public void makeToast(String string) {
        Toast.makeText(this, string, Toast.LENGTH_LONG).show();
    }
}
