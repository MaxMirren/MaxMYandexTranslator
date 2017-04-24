package com.testask.yandex.translator.yandextranslator.core;

import android.os.AsyncTask;
import android.widget.TextView;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.google.gson.*;

import com.testask.yandex.translator.yandextranslator.Loading;
import com.testask.yandex.translator.yandextranslator.MainActivity;
import com.testask.yandex.translator.yandextranslator.PopUpLanguages;
import com.testask.yandex.translator.yandextranslator.fragments.FragmentTranslate;
import com.testask.yandex.translator.yandextranslator.fragments.tabs.TabFragmentFavorites;
import com.testask.yandex.translator.yandextranslator.fragments.tabs.TabFragmentHistory;

/**
 * Данный класс предназначен для получения списка поддерживаемых языков и
 * и перевода с или без автоопределения
 *
 * Для использования API Яндекс.Переводчика применяется ключ:
 * "trnsl.1.1.20170326T205710Z.bb7ad29955ea17db.00b430935e6f07c60dd1ace8669b583fdf028977"
 *
 * @version 1.0 24 Апр 2017
 * @author Миронов Максим Андреевич
 */
public final class Translator extends AsyncTask<String,String,String> {

    //Исходный язык
    public static String sourceLanguage = null;
    //Язык перевода
    public static String targetLanguage = null;
    //Необходима для хранения списка поддерживаемых языков
    public static Set<Map.Entry<String, JsonElement>> entries;

    //Указывает функцию, необходимую для выполнения для данного объекта
    private TranslatorFunctions followingFunction;
    //Введенный текст для перевода
    private String textToTranslate;

    //URL для обращения к серверу Яндекс
    private String urlStr;
    //Поля для хранения данных отправки и получения Https-запросов
    private URL urlObj = null;
    private HttpsURLConnection connection;
    private DataOutputStream dataOutputStream;
    private InputStream response;
    //Оформленный ответ сервера
    private String answer = "";

    //Необходима для связи с TextView в fragment_translate
    private TextView translationResult;
    //Необходима для связи с обектов класса Loading, который создал данный объект
    private Loading loader;

    public Translator(TranslatorFunctions followingFunction, Loading loader) {
        this.followingFunction = followingFunction;
        this.loader = loader;
    }

    public Translator(TranslatorFunctions followingFunction, String textToTranslate, TextView textView) {
        this.followingFunction = followingFunction;
        this.textToTranslate = textToTranslate;
        this.translationResult = textView;
    }

    @Override
    protected String doInBackground(String... params) {
        String result = null;
        switch (followingFunction){
            //Если надо запросить список поддерживаемых языков
            case GET_LANGUAGE_LIST:
                try {
                    result = getLanguageList();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            //Если надо выполнить перевод с автоопределением
            case TRANSLATE_WITH_AUTO_DETECTION:
                try {
                    result = translateWithAutoDetection(this.textToTranslate);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            //Если надо выполнить перевод без автоопределения
            case TRANSLATE_WITHOUT_AUTO_DETECTION:
                try {
                    result = translateWithoutAutoDetection(this.textToTranslate);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * Получает список поддерживаемых языков на русском или английском
     * @return строку с языками и их сокращениями
     * @throws IOException
     */
    private String getLanguageList () throws IOException {
        //Формирование https-запроса
        urlStr = "https://translate.yandex.net/api/v1.5/tr.json/getLangs?key=trnsl.1.1.20170326T205710Z.bb7ad29955ea17db.00b430935e6f07c60dd1ace8669b583fdf028977";
        try {
            urlObj = new URL(urlStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        connection = (HttpsURLConnection)urlObj.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        dataOutputStream = new DataOutputStream(connection.getOutputStream());
        if (MainActivity.checkIfSystemLanguageIsRussian()) {
            dataOutputStream.writeBytes("&ui=ru");
        }
        else {
            dataOutputStream.writeBytes("&ui=en");
        }
        //Получение ответа
        response = connection.getInputStream();
        answer = new Scanner(response).nextLine();
        //Формирование Set с помощью библиотеки GSON и оформление данных
        final int LANG_DESCRIPTION_STAR_INDEX = answer.lastIndexOf("{");
        final int LANG_DESCRIPTION_END_INDEX = answer.lastIndexOf("}");
        answer = answer.substring(LANG_DESCRIPTION_STAR_INDEX, LANG_DESCRIPTION_END_INDEX);
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(answer);
        JsonObject obj = element.getAsJsonObject();
        entries = obj.entrySet();
        answer = "";
        for (Map.Entry<String, JsonElement> entry: entries) {
            answer += entry.getKey() + " : " + entry.getValue().toString() + "\n";
        }
        return null;
    }

    /**
     * Выполняет перевод текста из EditText с автоопределением исходного языка
     * @return переведенную строку
     * @throws IOException
     */
    private String translateWithAutoDetection(String text) throws IOException{
        text = text.substring(0, text.length()-1);
        //Получение сокращения используем языка перевода
        // для формирования https-запроса
        String stringTargetLanguageShortage = null;
        String currentEntryValue;
        for (Map.Entry<String, JsonElement> entry: entries) {
            currentEntryValue = entry.getValue().toString().replace("\"", "");
            if (currentEntryValue.equals(targetLanguage)) {
                stringTargetLanguageShortage = entry.getKey();
                break;
            }
        }
        //Формирование https-запроса
        urlStr = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20170326T205710Z.bb7ad29955ea17db.00b430935e6f07c60dd1ace8669b583fdf028977";
        urlObj = new URL(urlStr);
        connection = (HttpsURLConnection)urlObj.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
        dataOutputStream.writeBytes("text=" + URLEncoder.encode(text, "UTF-8") + "&lang=" +
                stringTargetLanguageShortage + "&format=plain&options=1");
        //Формирование переведенной строки из ответа
        response = connection.getInputStream();
        answer = new java.util.Scanner(response).nextLine();
        int startPosition = answer.lastIndexOf("{\"lang\":\"")+9;
        int endPosition = answer.lastIndexOf("\"},\"l");
        String stringSourceLanguageShortage = answer.substring(startPosition, endPosition);
        startPosition = answer.lastIndexOf("[");
        endPosition = answer.lastIndexOf("]");
        answer = answer.substring(startPosition + 2, endPosition - 1);
        //Добавление переведенного элемента истории в список
        HistoryElement historyElement = new HistoryElement(text, answer, stringSourceLanguageShortage, stringTargetLanguageShortage);
        //Если дублируется элемент и содержится в избранном,
        // значит добавить данный элемент в конец списка и сделать избранным
        if (updatePositionIfDoubling(historyElement)) {
            HistoryElement.historyElements.add(historyElement);
            int index = HistoryElement.historyElements.indexOf(historyElement);
            HistoryElement.historyElements.get(index).bookmark(false);
        }
        //Если элемент не добавлен в список избранного
        if (!historyElement.isBookmarked()) {
            updateHistoryListIfContinue(historyElement);
            HistoryElement.historyElements.add(historyElement);
        }
        //Обновить историю поиска, если ведется поиск
        tryRefreshHistorySearch(historyElement);
        return answer;
    }

    /**
     * Выполняет перевод текста из EditText
     * @return переведенную строку
     * @throws IOException
     */
    private String translateWithoutAutoDetection(String text) throws IOException {
        text = text.substring(0, text.length()-1);
        //Получение сокращений используемых языков (исходного и языка перевода)
        // для формирования https-запроса
        String stringSourceLanguageShortage = null;
        String stringTargetLanguageShortage = null;
        String currentEntryValue;
        for (Map.Entry<String, JsonElement> entry: entries) {
            currentEntryValue = entry.getValue().toString().replace("\"", "");
            if ((stringSourceLanguageShortage != null) && (stringTargetLanguageShortage != null)) {
                break;
            }
            if (currentEntryValue.equals(sourceLanguage)) {
               stringSourceLanguageShortage = entry.getKey();
                continue;
            }
            if (currentEntryValue.equals(targetLanguage)) {
                stringTargetLanguageShortage = entry.getKey();
            }
        }
        //Формирование https-запроса
        urlStr = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20170326T205710Z.bb7ad29955ea17db.00b430935e6f07c60dd1ace8669b583fdf028977";
        urlObj = new URL(urlStr);
        connection = (HttpsURLConnection)urlObj.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
        dataOutputStream.writeBytes("text=" + URLEncoder.encode(text, "UTF-8") + "&lang=" +
                stringSourceLanguageShortage  + "-" + stringTargetLanguageShortage + "&format=plain");
        //Формирование переведенной строки из ответа
        response = connection.getInputStream();
        answer = new java.util.Scanner(response).nextLine();
        final int OPEN_BRACKET_POSITION = answer.lastIndexOf("[");
        final int CLOSE_BRACKET_POSITION = answer.lastIndexOf("]");
        answer = answer.substring(OPEN_BRACKET_POSITION + 2, CLOSE_BRACKET_POSITION - 1);
        //Добавление переведенного элемента истории в список
        HistoryElement historyElement = new HistoryElement(text, answer, stringSourceLanguageShortage, stringTargetLanguageShortage);
        //Если дублируется элемент и содержится в избранном,
        // значит добавить данный элемент в конец списка и сделать избранным
        if (updatePositionIfDoubling(historyElement)) {
            HistoryElement.historyElements.add(historyElement);
            int index = HistoryElement.historyElements.indexOf(historyElement);
            HistoryElement.historyElements.get(index).bookmark(false);
        }
        //Если элемент не добавлен в список избранного
        if (!historyElement.isBookmarked()) {
            updateHistoryListIfContinue(historyElement);
            HistoryElement.historyElements.add(historyElement);
        }
        //Обновить историю поиска, если ведется поиск
        tryRefreshHistorySearch(historyElement);
        return answer;
    }

    /**
     * Проверяет, является ли текущий перевод продолжением предыдущего и обновляется список,
     * заменяя предыдущий перевод текущим
     * @param historyElement
     */
    private void updateHistoryListIfContinue(HistoryElement historyElement) {
        final int historyElementsSize = HistoryElement.historyElements.size();
        //При количестве элементов в списке historyElements больше 0 необходимо начать поиск
        if (historyElementsSize != 0) {
            final String input = historyElement.getInputText();
            final String languages = historyElement.getSourceLanguage() + historyElement.getTargetLanguage();
            final HistoryElement lastElement = HistoryElement.historyElements.get(historyElementsSize-1);
            final String lastElementInput = lastElement.getInputText();
            final String lastElementLanguages = lastElement.getSourceLanguage() + lastElement.getTargetLanguage();
            //Если совпадают введеные данные и языки (исходный и намеченный) у последнего элемента
            // (не в списке избранное) с добавляемым, тогда выполнить поиск в списке поиска истории
            // и удалить последний элемент истории
            if (input.contains(lastElementInput) && languages.equals(lastElementLanguages) && (!lastElement.isBookmarked())) {
                try {
                    final String searchFieldText = TabFragmentHistory.savedState.getString(TabFragmentHistory.SEARCH_FIELD_TEXT);
                    //Если во вкладке история производится поиск, то необходимо обновить список,
                    //предварительно удалив найденный последний элемент списка истории
                    //и добавить в него новый элемент
                    if (HistoryElement.searchedHistoryElements.contains(lastElement) && !searchFieldText.isEmpty()) {
                        HistoryElement.searchedHistoryElements.remove(lastElement);
                        HistoryElement.searchedHistoryElements.add(historyElement);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                HistoryElement.historyElements.remove(historyElementsSize-1);
            }
        }
    }


    /**
     * Проверяет на возможное дублирование при добавлении нового элемента истории перевода и
     * переносит совпавший элемент истории перевода в конец списка
     * @param historyElement - передаваемый элемент истории перевода
     * @return true, если обновленный элемент добавлен в список избранных
     */
    private Boolean updatePositionIfDoubling(HistoryElement historyElement) {
        final int historyElementsSize = HistoryElement.historyElements.size();
        //Если списк истории не пустой, то выполнять проверку на дублирование
        if (historyElementsSize != 0) {
            final String input = historyElement.getInputText();
            final String output = historyElement.getOutputText();
            final String languages = historyElement.getSourceLanguage() + historyElement.getTargetLanguage();
            HistoryElement thisElement;
            //Начало цикла проверки элементов в списке истории
            for (int i = 0; i < historyElementsSize; i++) {
                thisElement = HistoryElement.historyElements.get(i);
                String thisInput = thisElement.getInputText();
                String thisOutput = thisElement.getOutputText();
                String thisSourceLanguage = thisElement.getSourceLanguage();
                String thisTargetLanguage = thisElement.getTargetLanguage();
                String thisLanguages = thisSourceLanguage + thisTargetLanguage;
                //Если элемент найден, то он удаляется и производятся дальнейшие проверки
                if (input.equals(thisInput) && output.equals(thisOutput) && languages.equals(thisLanguages)) {
                    HistoryElement.historyElements.remove(i);
                    String searchedText =
                            TabFragmentHistory.savedState.getString(TabFragmentHistory.SEARCH_FIELD_TEXT);
                    //Проверка на содержание элемента в списке результатов поиска
                    int index = HistoryElement.searchedHistoryElements.indexOf(thisElement);
                    try {
                        if (!searchedText.isEmpty() && (index != -1)) {
                            HistoryElement.searchedHistoryElements.remove(index);
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    if (thisElement.isBookmarked()) {
                        HistoryElement.favoriteElements.remove(thisElement);
                        searchedText = TabFragmentFavorites.savedState.getString(TabFragmentFavorites.SEARCH_FIELD_TEXT);
                        index = HistoryElement.searchedFavoriteElements.indexOf(thisElement);
                        try {
                            if (!searchedText.isEmpty() && (index != -1)) {
                                HistoryElement.searchedFavoriteElements.remove(index);
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                    else {
                        return false;
                    }
                }
            }
        }
    return false;
    }

    /**
     * Проверяет, соответствует ли добавленный элемент истории перевода выполненному
     * поисковому запросу во вкладке история, обновляет содержимое данной вкладки в случае
     * соответствия
     * @param historyElement - передаваемый элемент истории перевода
     */
    private void tryRefreshHistorySearch (HistoryElement historyElement) {
        try {
            final String typedText = TabFragmentHistory.savedState.getString(TabFragmentHistory.SEARCH_FIELD_TEXT);
            //Если поле ввода не пустое, значит ведется поиск
            if (!typedText.isEmpty()) {
                final String input = historyElement.getInputText();
                final String output = historyElement.getOutputText();
                if (input.contains(typedText) || output.contains(typedText)) {
                    HistoryElement.searchedHistoryElements.add(historyElement);
                }
            }
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(String s) {
        switch (followingFunction){
            //Если надо запросить список поддерживаемых языков
            case GET_LANGUAGE_LIST:
                //Передача списка в PopUpLanguages
                PopUpLanguages.entries = entries;
                //Графическое завершение загрузки
                loader.finish();
                break;
            //Если надо выполнить перевод с автоопределением
            case TRANSLATE_WITH_AUTO_DETECTION:
                //Установка переведенного текста и сохранение в файл
                translationResult.setText(s);
                FragmentTranslate.savedState.putString(FragmentTranslate.RESULT, translationResult.getText().toString());
                new InternalDataController(true, null).execute();
                break;
            //Если надо выполнить перевод без автоопределения
            case TRANSLATE_WITHOUT_AUTO_DETECTION:
                //Установка переведенного текста и сохранение в файл
                translationResult.setText(s);
                FragmentTranslate.savedState.putString(FragmentTranslate.RESULT, translationResult.getText().toString());
                new InternalDataController(true, null).execute();
                break;
            default:
                break;
        }
    }

}


