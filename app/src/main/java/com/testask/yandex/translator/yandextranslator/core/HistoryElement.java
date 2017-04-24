package com.testask.yandex.translator.yandextranslator.core;

import android.view.View;

import com.testask.yandex.translator.yandextranslator.fragments.tabs.TabFragmentFavorites;
import com.testask.yandex.translator.yandextranslator.fragments.tabs.TabFragmentHistory;

import java.io.Serializable;
import java.util.List;

/**
 * Данный класс предназначен для отображения структуры элемента истории
 * и реализации функционала, применимого для объектов данного класса
 *
 * @version 1.0 24 Апр 2017
 * @author Миронов Максим Андреевич
 */
public final class HistoryElement implements Serializable {

    //Список элементов истории
    public static List<HistoryElement> historyElements;
    //Список элементов избранного
    public static List<HistoryElement> favoriteElements;
    //Список поиска элементов истории
    public static List<HistoryElement> searchedHistoryElements;
    //Список поиска элементов избранного
    public static List<HistoryElement> searchedFavoriteElements;

    //Универсальный идентификатор сериализации
    private static final long serialVersionUID = 20170326;

    //Введенный текст
    private String inputText;
    //Переведенный текст
    private String outputText;
    //Сокращение исходного языка
    private String sourceLanguage;
    //Сокращение языка перевода
    private String targetLanguage;
    //Свойство, определяющее, добавлен ли элемент в список избранного
    private Boolean bookmarked = false;

    public HistoryElement() {
    }

    public HistoryElement(String inputText, String outputText, String sourceLanguage, String targetLanguage) {
        this.inputText = inputText;
        this.outputText = outputText;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
    }

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public String getOutputText() {
        return outputText;
    }

    public void setOutputText(String outputText) {
        this.outputText = outputText;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    /**
     * Метод для добавления данного элемента в избранное
     * @param fromSearch - содержит true, если метод был вызван из списка поиска
     */
    public void bookmark(Boolean fromSearch) {
        final HistoryElement historyElement = this;
        //Пометка избранного в historyElements
        int index = historyElements.indexOf(this);
        historyElements.get(index).bookmarked = true;
        TabFragmentHistory.baseAdapter.notifyDataSetChanged();
        //Добавление в список избранного в favoriteElements
        favoriteElements.add(this);
        TabFragmentFavorites.baseAdapter.notifyDataSetChanged();
        //Если запрос на добавление в избранное сделан из поиска,
        // то будет сделана пометка избранного в searchedHistoryElements
        if (fromSearch) {
            this.bookmarked = true;
            TabFragmentHistory.searchBaseAdapter.notifyDataSetChanged();
        }
        //Если во вкладке избранное ведется поиск, будет проверено
        // на соответствие добавленного элемента в favoriteElements критериям поиска
        // и обновлено содержимое результатов поиска в случае их обновления
        final String typedText = TabFragmentFavorites.savedState.getString(TabFragmentFavorites.SEARCH_FIELD_TEXT);
        try {
            //При наличии теста в поле ввода - ведется поиск
            if (!typedText.isEmpty()) {
                final String inputText = historyElement.getInputText().toLowerCase();
                final String outputText = historyElement.getOutputText().toLowerCase();
                if ((inputText.contains(typedText.toLowerCase())) || (outputText.contains(typedText.toLowerCase()))) {
                    searchedFavoriteElements.add(historyElement);
                    TabFragmentFavorites.searchBaseAdapter.notifyDataSetChanged();
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод для поиска и удаления элемента из списка избранных
     * @param fromHistory - содержит true, если метод был вызван из списка истории
     * @param fromSearch - содержит true, если метод был вызван из списка поиска
     */
    public void removeFromBookmarkedList(Boolean fromHistory, Boolean fromSearch) {
        //Установка пометки "неизбранный" в списке истории
        int index = historyElements.indexOf(this);
        historyElements.get(index).bookmarked = false;
        TabFragmentHistory.baseAdapter.notifyDataSetChanged();
        //Удаление из списка избранных
        index = favoriteElements.indexOf(this);
        favoriteElements.remove(index);
        TabFragmentFavorites.baseAdapter.notifyDataSetChanged();

        //Переменная, определяющая ведется ли поиск в истории
        final int visibilityHistorySearch = TabFragmentHistory.savedState.getInt(TabFragmentHistory.SEARCH_HISTORY_LIST_VIEW_VISIBILITY);
        //Переменная, определяющая ведется ли поиск в избранном
        final int visibilityFavoritesSearch = TabFragmentFavorites.savedState.getInt(TabFragmentFavorites.SEARCH_FAVORITES_LIST_VIEW_VISIBILITY);
        //Если поиск ведется, то выполнять сценарий, как если бы в метод был передам параметр fromSearch = true
        if ((visibilityHistorySearch == View.VISIBLE) || (visibilityFavoritesSearch == View.VISIBLE)) {
            fromSearch = true;
        }
        if (fromSearch) {
            //Удаление при наличии из списка избранных в поиске
            if (searchedFavoriteElements.contains(this) && (visibilityFavoritesSearch == View.VISIBLE)) {
                index = searchedFavoriteElements.indexOf(this);
                searchedFavoriteElements.remove(index);
                TabFragmentFavorites.searchBaseAdapter.notifyDataSetChanged();
            }
            //Удаление при наличии из списка истории в поиске
            if (searchedHistoryElements.contains(this) && (visibilityHistorySearch == View.VISIBLE)){
                index = searchedHistoryElements.indexOf(this);
                searchedHistoryElements.get(index).bookmarked = false;
                TabFragmentHistory.searchBaseAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Проверяет является ли данный элемент избранным
     * @return true, если является, false в обратном случае
     */
    public Boolean isBookmarked() {
        return this.bookmarked;
    }
}
