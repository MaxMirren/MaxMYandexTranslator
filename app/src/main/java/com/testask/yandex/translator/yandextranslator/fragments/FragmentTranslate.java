package com.testask.yandex.translator.yandextranslator.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.testask.yandex.translator.yandextranslator.R;
import com.testask.yandex.translator.yandextranslator.core.HistoryElement;
import com.testask.yandex.translator.yandextranslator.core.TranslatorFunctions;
import com.testask.yandex.translator.yandextranslator.core.Translator;

import java.util.Map;

/**
 * Данный класс предназначен для назначения действий для view из fragment_translate,
 * назначение данным view слушателей,
 * инициализации перевода
 *
 * @version 1.0 24 Апр 2017
 * @author Миронов Максим Андреевич
 */
public class FragmentTranslate extends Fragment {

    //Ключ значения исходного языка для savedState
    public static final String SOURCE_LANGUAGE = "SOURCE_LANGUAGE";
    //Ключ значения языка перевода для savedState
    public static final String TARGET_LANGUAGE = "TARGET_LANGUAGE";
    //Ключ значения результата перевода для savedState
    public static final String RESULT = "RESULT";
    //Ключ значения введенного текста для savedState
    public static final String TYPED = "TYPED";
    //Ключ значения видимости клавиши для очистки поля ввода для savedState
    public static final String CROSS_VISIBILITY = "VISIBLE";

    //Переданный элемент для отображения из списков истории или избранного
    public static HistoryElement historyElement = null;
    //Для сохранения данных к view-элементов
    public static Bundle savedState = null;
    //Свойство автораспознавания
    public static Boolean autoDetection = true;

    //Для обращения к view-элементам
    private View view;

    //Необходимы для связи с Button
    //(клавишы для выбора языков исходного и языка перевода соответственно) в fragment_translate
    private Button sourceLanguageChanger, targetLanguageChanger;
    //Необходима для связи с ImageButton (клавиша для смены язков) в fragment_translate
    private ImageButton languageChanger;
    //Указатель поворота для стрелки смены языков
    private Boolean languageChangerToRotateLeft = true;
    //Необходима для связи с рамкой вокруг EditText (введенный текст) в fragment_translate
    private SurfaceView surfaceView;
    //Необходима для связи с EditText (введенный текст) в fragment_translate
    private EditText editField;
    //Слушатель editField
    private TextWatcher editFieldListener;
    //Необходима для связи с ImageButton
    //(клавиша для очистки введенных и переведенных данных) в fragment_translate
    private ImageButton cross;
    //Необходима для связи с TextView ссылкой в fragment_translate на сервис Яндекс.Переводчик
    private TextView poweredByYandex;
    //Необходима для связи с TextView (результат перевода) в fragment_translate
    private TextView translateResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_translate, container, false);
        setViews();
        setSurfaceViewBorderChangers();
        setActionsForLanguageChanger();
        setEditFieldTextChangedListener();
        setCrossOnClickListener();
        setPoweredByYandexLink();
        //Если данный фрагмент был показан при щелчке по элементу истории,
        //то его содержимое будет отображено
        if (historyElement != null) {
            setSentData();
            historyElement = null;
        }
        return view;
    }

   @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        restoreFragmentState();
       if (Translator.targetLanguage != null) {
           editField.setEnabled(true);
       }
    }

    @Override
    public void onResume() {
        super.onResume();
        backupFragmentState();
    }

    @Override
    public void onPause() {
        super.onPause();
        backupFragmentState();
    }

    /**
     * Создает резервную копию динамических данных view для восстановления
     */
    private void backupFragmentState() {
        savedState = new Bundle();
        savedState.putString(SOURCE_LANGUAGE,sourceLanguageChanger.getText().toString());
        savedState.putString(TARGET_LANGUAGE,targetLanguageChanger.getText().toString());
        savedState.putString(RESULT,translateResult.getText().toString());
        savedState.putString(TYPED, editField.getText().toString());
        savedState.putInt(CROSS_VISIBILITY, cross.getVisibility());
    }

    /**
     * Восстанавливает резервную копию динамических данных view для восстановления
     */
    @SuppressWarnings("ResourceType")
    private void restoreFragmentState() {
        try {
            sourceLanguageChanger.setText(savedState.getString(SOURCE_LANGUAGE));
            targetLanguageChanger.setText(savedState.getString(TARGET_LANGUAGE));
            translateResult.setText(savedState.getString(RESULT));
            cross.setVisibility(savedState.getInt(CROSS_VISIBILITY));
            editField.removeTextChangedListener(editFieldListener);
            editField.setText(savedState.getString(TYPED));
            editField.addTextChangedListener(editFieldListener);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Связка переменных view с view из fragment_translate
     */
    private void setViews() {
        translateResult = (TextView) view.findViewById(R.id.translate_result);
        sourceLanguageChanger = (Button) view.findViewById(R.id.source_language);
        targetLanguageChanger = (Button) view.findViewById(R.id.target_language);
        surfaceView = (SurfaceView) view.findViewById(R.id.surfaceView);
        editField = (EditText) view.findViewById(R.id.edit_field);
        cross = (ImageButton) view.findViewById(R.id.cross_from_main);
        poweredByYandex = (TextView) view.findViewById(R.id.powered_by_yandex);
        languageChanger = (ImageButton) view.findViewById(R.id.language_changer);
    }

    /**
     * Устанавливает слушатель для изменения границ SurfaceView,
     * если в editField есть фокус
     */
    private void setSurfaceViewBorderChangers() {
        editField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //Если есть фокус, то сделать границы активными
                if (hasFocus && editField.isEnabled()) {
                    surfaceView.setBackground(getActivity().getDrawable(R.drawable.edit_text_background_active));
                }
                //Если нет фокуса, то сделать границы обычными
                else {
                    surfaceView.setBackground(getActivity().getDrawable(R.drawable.edit_text_background));
                }
            }
        });
    }

    /**
     * Устанавливает слушатель изменений в тексте editField
     */
    private void setEditFieldTextChangedListener() {
        editField.addTextChangedListener(editFieldListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String editFieldText = editField.getText().toString();
                //Если поле ввода пустое, скрыть клавишу для очистки содержимого поля ввода
                if (editFieldText.isEmpty()) {
                    cross.setVisibility(View.GONE);
                    translateResult.setText("");
                    savedState.putInt(CROSS_VISIBILITY, cross.getVisibility());
                    savedState.putString(RESULT, translateResult.getText().toString());
                }
                //Если поле ввода непустое, показать клавишу для очистки содержимого поля ввода
                else
                {
                    cross.setVisibility(View.VISIBLE);
                    int length = editFieldText.length();
                    String editFieldLastChar = editFieldText.charAt(length-1) + "";
                    //Если длина введенной строки больше или равна 2 и последний символ пробел,
                    //то выполнить проверки
                    if ((length >= 2) && (editFieldLastChar).equals(" ")) {
                        //Если исходный язык одинаков по отношению к языку перевода,
                        //то перевод не выполнять и вывести подсказку
                        if ((Translator.sourceLanguage  != null) && (Translator.sourceLanguage.equals(Translator.targetLanguage))) {
                            makeToast(getResources().getString(R.string.warning_translation));
                        }
                        //Если исходный язык и язык перевода разные, то выполнить перевод
                        else {
                            translate(editFieldText);
                        }
                    }
                //Сохранить введенный текст
                savedState.putString(TYPED, editField.getText().toString());
                }
            }
        });
    }

    /**
     * Удаляет весь текст из поля ввода, результата перевода
     * и делает невидимым клавишу для очистки содержимого
     */
    private void setCrossOnClickListener () {
        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editField.removeTextChangedListener(editFieldListener);
                editField.setText("");
                editField.addTextChangedListener(editFieldListener);
                cross.setVisibility(View.GONE);
                translateResult.setText("");
                savedState.putInt(CROSS_VISIBILITY, cross.getVisibility());
                savedState.putString(RESULT, translateResult.getText().toString());
            }
        });
    }

    /**
     * Этот метод добавляет управляющие кнопки Floating Action Buttons
     */
    private void setActionsForLanguageChanger () {
            languageChanger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Если в качестве исходного языка не выбрано автоопределение, выполнять сценарий в if
                    if (!autoDetection) {
                        final Animation languageChangerRotationLeft, languageChangerRotationRight;
                        languageChangerRotationLeft = AnimationUtils.loadAnimation(view.getContext(), R.anim.language_changer_rotator_left);
                        languageChangerRotationRight = AnimationUtils.loadAnimation(view.getContext(), R.anim.language_changer_rotator_right);
                        //Зависимость от повторной смены языка
                        if (languageChangerToRotateLeft) {
                            languageChanger.startAnimation(languageChangerRotationLeft);
                            languageChangerToRotateLeft = false;
                        } else {
                        languageChanger.startAnimation(languageChangerRotationRight);
                        languageChangerToRotateLeft = true;
                        }
                        final String newSourceLanguage = targetLanguageChanger.getText().toString();
                        final String newTargetLanguage = sourceLanguageChanger.getText().toString();
                        sourceLanguageChanger.setText(newSourceLanguage);
                        targetLanguageChanger.setText(newTargetLanguage);
                        savedState.putString(SOURCE_LANGUAGE, newSourceLanguage);
                        savedState.putString(TARGET_LANGUAGE, newTargetLanguage);
                        //Изменение данных для перевода
                        Translator.sourceLanguage = newSourceLanguage;
                        Translator.targetLanguage = newTargetLanguage;
                    }
                    //Если в качестве исходного языка выбрано автоопределение, вывести предупреждение
                    else {
                        makeToast(getResources().getString(R.string.warning_language_changer));
                    }
                }
            });
    }

    /**
     * Устанавливает ссылку на URI для подписи Powered by Yandex.Translate
     */
    private void setPoweredByYandexLink(){
        final String url = getActivity().getResources().getString(R.string.powered_by_yandex_url);
        poweredByYandex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
    }

    /**
     * Осуществялет перевод текста
     * @param editFieldText - текст из поля ввода
     */
    private void translate(String editFieldText) {
        Translator translator;
        //Если автоопределение
        if (autoDetection) {
            translator = new Translator(TranslatorFunctions.TRANSLATE_WITH_AUTO_DETECTION, editFieldText, translateResult);
        }
        //Если автоопределение
        else {
            translator = new Translator(TranslatorFunctions.TRANSLATE_WITHOUT_AUTO_DETECTION, editFieldText, translateResult);
        }
        //Проверка интернет-соединения
        if (hasInternetConnection()) {
            translator.execute();
        }
        //Предупреждение в случае отсутствия интернет-соединения
        else {
            makeToast(getResources().getString(R.string.no_internet_connection));
        }
    }

    /**
     * Проверяет наличие интернет-соединения
     * @return результат проверки
     */
    private Boolean hasInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if ((networkInfo != null) && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Устанавливает все данные view-элементов в соответствии с переданным historyElement
     */
    private void setSentData() {
        editField.removeTextChangedListener(editFieldListener);
        editField.setText(historyElement.getInputText());
        editField.addTextChangedListener(editFieldListener);
        cross.setVisibility(View.VISIBLE);
        translateResult.setText(historyElement.getOutputText());
        final String sourceLanguageShortage = historyElement.getSourceLanguage();
        final String targetLanguageShortage = historyElement.getTargetLanguage();
        String sourceLanguage = null;
        String targetLanguage = null;
        //Поиск по сокращениям полных версий языков
        for (Map.Entry<String, JsonElement> entry: Translator.entries) {
            entry.getKey();
            String currentEntryKey = entry.getKey();
            if ((sourceLanguage != null) && (targetLanguage != null)) {
                break;
            }
            if (currentEntryKey.equals(sourceLanguageShortage)) {
                sourceLanguage = entry.getValue().toString().replace("\"", "");
                continue;
            }
            if (currentEntryKey.equals(targetLanguageShortage)) {
                targetLanguage = entry.getValue().toString().replace("\"", "");
            }
        }
        autoDetection = false;
        sourceLanguageChanger.setText(sourceLanguage);
        targetLanguageChanger.setText(targetLanguage);
        Translator.sourceLanguage=sourceLanguage;
        Translator.targetLanguage=targetLanguage;
        savedState.putString(SOURCE_LANGUAGE,sourceLanguageChanger.getText().toString());
        savedState.putString(TARGET_LANGUAGE,targetLanguageChanger.getText().toString());
        savedState.putString(TYPED, editField.getText().toString());
        savedState.putString(RESULT,translateResult.getText().toString());
        savedState.putInt(CROSS_VISIBILITY, cross.getVisibility());
    }

    /**
     * Упрощает процесс создания Toast-уведомлений
     * @param string контент уведомления
     */
    public void makeToast(String string) {
        Toast.makeText(getActivity(), string, Toast.LENGTH_LONG).show();
    }

}
