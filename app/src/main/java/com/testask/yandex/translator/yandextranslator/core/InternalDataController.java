package com.testask.yandex.translator.yandextranslator.core;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.testask.yandex.translator.yandextranslator.MainActivity;

/**
 * Данный класс предназначен для чтения и записи в файл данных списков истории и избранного
 * и формирования их содержимого
 *
 * @version 1.0 24 Апр 2017
 * @author Миронов Максим Андреевич
 */
public final class InternalDataController extends AsyncTask <Void, Void, Void>{

    //Параметр, оперделяющий исполняемую функцию, по умолчанию - чтение
    private Boolean writeToFile = false;
    //Ссылка на клавишу, которая изменила изменила свойство избранного элемента истории
    private ImageButton caller = null;

    public InternalDataController() {
    }

    public InternalDataController(Boolean writeToFile, ImageButton imageButton) {
        this.writeToFile = writeToFile;
        this.caller = imageButton;
    }

    public InternalDataController(Boolean writeToFile) {
        this.writeToFile = writeToFile;
    }

    @Override
    protected Void doInBackground(Void... params) {
        //Если необходимо записать в файл
        if (writeToFile) {
            writeHistoryElementsToFile();
        }
        //Если необходимо прочитать из файла
        if (!writeToFile) {
            HistoryElement.historyElements = new ArrayList<>();
            readHistoryElementsFromFile();
        }
        return null;
    }

    /**
     * Записывает список истории в файл history_and_favorites.ytr
     * @return null
     */
    private synchronized String writeHistoryElementsToFile() {
        try {
            File appData = new File(MainActivity.cachePath, "history_and_favorites.ytr");
            FileOutputStream fileOutputStream =  new FileOutputStream(appData);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(HistoryElement.historyElements);
            objectOutputStream.flush();
            objectOutputStream.close();
            fileOutputStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Читает списко истории из файла history_and_favorites.ytr
     * и формирует списки истории и избранного
     * @return null
     */
    private String readHistoryElementsFromFile() {
        //Инициализация списков
        HistoryElement.historyElements = new ArrayList<>();
        HistoryElement.searchedHistoryElements = new ArrayList<>();
        HistoryElement.favoriteElements = new ArrayList<>();
        HistoryElement.searchedFavoriteElements = new ArrayList<>();
        try {
            File appData = new File(MainActivity.cachePath, "history_and_favorites.ytr");
            FileInputStream fileInputStream = new FileInputStream(appData);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            HistoryElement.historyElements = (List<HistoryElement>) objectInputStream.readObject();
            int historyElementsSize = HistoryElement.historyElements.size();
            //Формирование списков истории и избранного
            for (int i=0; i < historyElementsSize; i++) {
                HistoryElement historyElement = HistoryElement.historyElements.get(i);
                if (historyElement.isBookmarked()) {
                    HistoryElement.favoriteElements.add(historyElement);
                }
            }
            objectInputStream.close();
            fileInputStream.close();
            Log.i ("tag", "readHistoryElementsFromFile is done!");
        }
        catch (FileNotFoundException e) {
            Log.i ("tag", "FileNotFoundException");
        }
        catch (IOException e) {
            Log.i ("tag", "IOException");
        }
        catch (ClassNotFoundException e) {
            Log.i ("tag", "ClassNotFoundException");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        //Если объект был вызван клавишей, которая изменила изменила свойство избранного элемента истории,
        //то возобновить к ней доступ
        if (this.caller != null) {
            caller.setEnabled(true);
        }
    }

}
