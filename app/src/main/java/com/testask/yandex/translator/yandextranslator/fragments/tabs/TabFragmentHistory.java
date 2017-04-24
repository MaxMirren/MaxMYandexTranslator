package com.testask.yandex.translator.yandextranslator.fragments.tabs;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.testask.yandex.translator.yandextranslator.MainActivity;
import com.testask.yandex.translator.yandextranslator.R;
import com.testask.yandex.translator.yandextranslator.core.HistoryElement;
import com.testask.yandex.translator.yandextranslator.core.InternalDataController;
import com.testask.yandex.translator.yandextranslator.fragments.FragmentTranslate;

/**
 * Данный класс предназначен для организации поведения view-елементов
 * для tab_fragment_history
 *
 * @version 1.0 24 Апр 2017
 * @author Миронов Максим Андреевич
 */
public class TabFragmentHistory extends Fragment {

    //Ключ значения текста в поле поиска для savedState
    public static final String SEARCH_FIELD_TEXT = "SEARCH_FIELD_TEXT";
    //Ключ значения видимости для historyListView savedState
    public static final String SEARCH_HISTORY_LIST_VIEW_VISIBILITY = "SEARCH_HISTORY_LIST_VIEW_VISIBILITY";
    //Ключ значения видимости для searchedHistoryListView savedState
    private static final String HISTORY_LIST_VIEW_VISIBILITY = "HISTORY_LIST_VIEW_VISIBILITY";
    //Ключ значения видимости клавиши для очистки поля ввода для savedState
    private static final String CROSS_VISIBILITY = "CROSS_VISIBILITY";
    //Ключ значения видимости статуса наличия элементов для savedState
    private static final String STATUS_VISIBILITY = "STATUS_VISIBILITY";
    //Ключ значения текста статуса наличия элементов для savedState
    private static final String STATUS_TEXT = "STATUS_TEXT";

    //Для сохранения данных к view-элементов
    public static Bundle savedState = null;
    //Адаптеры для historyListView, searchedHistoryListView соответственно
    public static HistoryListViewAdapter baseAdapter, searchBaseAdapter;

    //Для обращения к view-элементам
    private View view;

    //Необходима для связи с ConstraintLayout (оболочка для элементов поиска) в tab_fragment_history
    private ConstraintLayout searchPlace;
    //Необходима для связи с TextView (статус наличия элементов) в tab_fragment_history
    private TextView status;
    //Необходима для связи с EditText (полем ввода) в tab_fragment_history
    private EditText searchField;
    //Слушатель searchField
    private TextWatcher searchFieldListener;
    //Необходимы для связи с ImageButton
    //(клавиша для очистки введенных и переведенных данных и удаления видимых элементов соответственно)
    //в tab_fragment_history
    private ImageButton cross, trashBin;
    //Необходима для связи с TextView ссылкой в tab_fragment_history на сервис Яндекс.Переводчик
    private TextView poweredByYandex;
    //Необходимы для связи с ListView истории и истории поиска в tab_fragment_history
    private ListView historyListView, searchedHistoryListView;
    //Необходима для использования методов действительного объекта класса MainActivity
    private MainActivity mainActivity;
    //Необходима для связи с BottomNavigationView в activity_main
    private BottomNavigationView bottomNavigationView;

    /**
     * Данный класс предназначен для унификации поведения адаптеров
     * для списков истории и поиска по истории
     *
     * @version 1.0 24 Апр 2017
     * @author Миронов Максим Андреевич
     */
    public class HistoryListViewAdapter extends BaseAdapter {

        //Указывает на принадлежность объекта к ListView - для поиска или истории
        Boolean forSearching;
        //Указывает на необходимость создания View-элементов в ListView
        Boolean needToSetViews;

        private HistoryListViewAdapter(Boolean forSearching) {
            this.forSearching = forSearching;
            this.needToSetViews = needToSetViews(true);
        }

        /**
         * Проверка на необходимость создания view-строк
         * @param calledFromConstructor - true, если метод вызван из конструктора
         * @return - при неоходимости создания view-строк true, иначе false
         */
        private Boolean needToSetViews(Boolean calledFromConstructor) {
            //Идет поиск и найдены совпадения
            if (forSearching && (HistoryElement.searchedHistoryElements.size() != 0)) {
                if (!calledFromConstructor) {
                    setNotFoundSearchResult(false);
                }
                return true;
            }
            //Идет поиск и количество совпадений 0
            if (forSearching && (HistoryElement.searchedHistoryElements.size() == 0)) {
                if (!calledFromConstructor) {
                    setNotFoundSearchResult(true);
                    hideListViews();
                }
                return false;
            }
            //Не идет поиск, но есть элементы истории
            if (!forSearching && (HistoryElement.historyElements.size() != 0)){
                if (searchPlace.getVisibility() == View.GONE) {
                    setSearchAccess(true);
                }
                return true;
            }
            //Не идет поиск и количество элементов истории 0
            if (!forSearching && (HistoryElement.historyElements.size() == 0)) {
                if (searchPlace.getVisibility() == View.VISIBLE) {
                    setSearchAccess(false);
                }
                return false;
            }
            return null;
        }

        @Override
        public void notifyDataSetChanged() {
            needToSetViews = needToSetViews(false);
            super.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if (forSearching) {
                return HistoryElement.searchedHistoryElements.size();
            }
            else {
                return HistoryElement.historyElements.size();
            }
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HistoryElement historyElement;
            //Идет поиск и найдены совпадения
            if (forSearching && needToSetViews) {
                historyElement = HistoryElement.searchedHistoryElements.get(position);
                convertView = setViewContent(historyElement, position, convertView, parent);
            }
            //Не идет поиск, но есть элементы истории
            if (!forSearching && needToSetViews) {
                historyElement = HistoryElement.historyElements.get(position);
                convertView = setViewContent(historyElement, position, convertView, parent);
            }
            return convertView;
        }

        /**
         * Данный метод предназначен для добавления view-строки истории перевода,
         * инициализации ее элементов и назначения слушателей
          * @param historyElement - текущий элемент списка истории
         * @param position - позиция view-строки
         * @param convertView - view-строка
         * @param parent - view-родитель
         * @return готовая view-строка
         */
        private View setViewContent(final HistoryElement historyElement, final int position, View convertView, ViewGroup parent) {
            convertView = getActivity().getLayoutInflater().inflate(R.layout.translated_element, parent, false);
            //Клик по всей view-строке для отображения ее содержимого в переводчике
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTranslate.historyElement = historyElement;
                    Fragment fragment = new FragmentTranslate();
                    FragmentTransaction fragmentTransaction = mainActivity.getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_in_activity_main, fragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    bottomNavigationView.getMenu().getItem(1).setChecked(false);
                    bottomNavigationView.getMenu().getItem(0).setChecked(true);
                }
            });
            TextView inputText = (TextView) convertView.findViewById(R.id.input_1);
            TextView outputText = (TextView) convertView.findViewById(R.id.output_1);
            TextView translationForward = (TextView) convertView.findViewById(R.id.translation_forward_1);
            inputText.setText(historyElement.getInputText());
            outputText.setText(historyElement.getOutputText());
            translationForward.setText(historyElement.getSourceLanguage().toUpperCase() + "-" +
                    historyElement.getTargetLanguage().toUpperCase());
            final ImageButton bookmarkedStatus = (ImageButton) convertView.findViewById(R.id.bookmark_button_1);
            if (historyElement.isBookmarked()) {
                bookmarkedStatus.setImageDrawable(getResources().getDrawable(R.drawable.btn_bookmark, getActivity().getTheme()));
            }
            else {
                bookmarkedStatus.setImageDrawable(getResources().getDrawable(R.drawable.btn_bookmark_not_bookmarked, getActivity().getTheme()));
            }
            //Клик по клавише избранное для добавления или удаления элемента из избранного
            bookmarkedStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (historyElement.isBookmarked()) {
                        bookmarkedStatus.setImageDrawable(getResources().getDrawable(R.drawable.btn_bookmark_not_bookmarked, getActivity().getTheme()));
                        if (forSearching) {
                            HistoryElement.searchedHistoryElements.get(position).removeFromBookmarkedList(true, true);
                        } else {
                            HistoryElement.historyElements.get(position).removeFromBookmarkedList(true, false);
                        }
                    }
                    else {
                        bookmarkedStatus.setImageDrawable(getResources().getDrawable(R.drawable.btn_bookmark, getActivity().getTheme()));
                        if (forSearching) {
                            HistoryElement.searchedHistoryElements.get(position).bookmark(true);
                        } else {
                            HistoryElement.historyElements.get(position).bookmark(false);
                        }
                    }
                    v.setEnabled(false);
                    //Сохранение изменений в файле
                    new InternalDataController(true, (ImageButton) v).execute();
                }
            });
            return convertView;
        }
    }

    /**
     * Этот метод предназначен для получения activity и bottomNavigation для осуществления
     * контроля над ними
     * @param activity - действительный объект класса MainActivity
     * @param bottomNavigation - BottomNavigationView в activity_main
     * @return объект данного класса
     */
    public TabFragmentHistory setBottomNavigationViewControllers(MainActivity activity, BottomNavigationView bottomNavigation) {
        this.bottomNavigationView = bottomNavigation;
        this.mainActivity = activity;
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.tab_fragment_history, container, false);
        setViews();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        restoreFragmentState();
    }

    @Override
    public void onStart() {
        super.onStart();
        savedState = new Bundle();
        setListViews();
        setCrossAndSearchFieldListeners();
        setPoweredByYandexLink();
        setTrashBinListener();
    }

    /**
     * Восстанавливает резервную копию динамических данных view для восстановления
     */
    @SuppressWarnings("ResourceType")
    private void restoreFragmentState() {
        try {
            //searchPlace.setVisibility(savedState.getInt(SEARCH_PLACE_VISIBILITY));
            if (!savedState.getString(SEARCH_FIELD_TEXT).isEmpty()) {
                searchField.setText(savedState.getString(SEARCH_FIELD_TEXT));
                cross.setVisibility(savedState.getInt(CROSS_VISIBILITY));
            }
            if (savedState.getInt(STATUS_VISIBILITY) == View.VISIBLE) {
                status.setText(savedState.getString(STATUS_TEXT));
                status.setVisibility(savedState.getInt(STATUS_VISIBILITY));
            }
            historyListView.setVisibility(savedState.getInt(HISTORY_LIST_VIEW_VISIBILITY));
            searchedHistoryListView.setVisibility(savedState.getInt(SEARCH_HISTORY_LIST_VIEW_VISIBILITY));
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        backUpFragmentState();
    }

    @Override
    public void onStop() {
        super.onStop();
        backUpFragmentState();
    }

    /**
     * Создает резервную копию динамических данных view для восстановления
     */
    private void backUpFragmentState() {
        savedState.putInt(HISTORY_LIST_VIEW_VISIBILITY, historyListView.getVisibility());
        savedState.putInt(SEARCH_HISTORY_LIST_VIEW_VISIBILITY, searchedHistoryListView.getVisibility());
        savedState.putInt(STATUS_VISIBILITY, status.getVisibility());
        if (status.getVisibility() == View.VISIBLE) {
            savedState.putString(STATUS_TEXT, status.getText().toString());
        }
        if (!searchField.getText().toString().isEmpty()) {
            savedState.putString(SEARCH_FIELD_TEXT, searchField.getText().toString());
            savedState.getInt(CROSS_VISIBILITY, cross.getVisibility());
        }
    }

    /**
     * Связка переменных view с view из tab_fragment_history
     */
    private void setViews() {
        historyListView = (ListView) view.findViewById(R.id.history_elements_list_view);
        searchedHistoryListView = (ListView) view.findViewById(R.id.searched_history_elements_list_view);
        searchPlace = (ConstraintLayout) view.findViewById(R.id.container_history_search);
        searchField = (EditText) view.findViewById(R.id.searching_in_history);
        cross = (ImageButton) view.findViewById(R.id.cross_history_search);
        trashBin = (ImageButton) view.findViewById(R.id.trash_bin_history);
        poweredByYandex = (TextView) view.findViewById(R.id.powered_by_yandex_history);
        status = (TextView) view.findViewById(R.id.history_page_status);
    }

    /**
     * Инициализация ListView поиска и истории
     */
    private void setListViews() {
        baseAdapter = new HistoryListViewAdapter(false);
        historyListView.setAdapter(baseAdapter);
        searchBaseAdapter = new HistoryListViewAdapter(true);
        searchedHistoryListView.setAdapter(searchBaseAdapter);
        //Если ведется поиск, то обновить список поиска
        if (!searchField.getText().toString().isEmpty()) {
            searchBaseAdapter.notifyDataSetChanged();
        }
        setMainListViewItemsListener();
        setSearchListViewItemsListener();
    }

    /**
     * Установка слушателя для historyListView на удаление выбрнанного элемента перевода
     */
    private void setMainListViewItemsListener() {
        historyListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final HistoryElement elementToDelete = HistoryElement.historyElements.get(position);
                new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getResources().getString(R.string.acceptance))
                        .setMessage(getActivity().getResources().getString(R.string.delete_this_item))
                        .setNegativeButton(getActivity().getResources().getString(R.string.cancel), null)
                        .setPositiveButton(getActivity().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                HistoryElement.historyElements.remove(elementToDelete);
                                baseAdapter.notifyDataSetChanged();
                                //Если элемент добавлен в список избранного, то его тоже необходимо удалить
                                if (elementToDelete.isBookmarked()) {
                                    HistoryElement.favoriteElements.remove(elementToDelete);
                                    TabFragmentFavorites.baseAdapter.notifyDataSetChanged();
                                    final String typedText = TabFragmentFavorites.savedState.getString(TabFragmentFavorites.SEARCH_FIELD_TEXT);
                                    int index = HistoryElement.searchedFavoriteElements.indexOf(elementToDelete);
                                    try {
                                        //Проверка для поиска в избранном
                                        if (!typedText.isEmpty() && (index != -1)) {
                                            HistoryElement.searchedFavoriteElements.remove(index);
                                            TabFragmentFavorites.searchBaseAdapter.notifyDataSetChanged();
                                        }
                                    } catch (NullPointerException e) {
                                        e.printStackTrace();
                                    }
                                }
                                //Запись изменений в файл
                                new InternalDataController(true).execute();
                            }
                        }).create().show();
                return true;
            }
        });
    }

    /**
     * Установка слушателя для searchedHistoryListView на удаление выбрнанного элемента перевода
     */
    private void setSearchListViewItemsListener() {
        searchedHistoryListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final HistoryElement elementToDelete = HistoryElement.searchedHistoryElements.get(position);
                new AlertDialog.Builder(getActivity())
                        .setTitle(getActivity().getResources().getString(R.string.acceptance))
                        .setMessage(getActivity().getResources().getString(R.string.delete_this_item))
                        .setNegativeButton(getActivity().getResources().getString(R.string.cancel), null)
                        .setPositiveButton(getActivity().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                HistoryElement.searchedHistoryElements.remove(elementToDelete);
                                HistoryElement.historyElements.remove(elementToDelete);
                                baseAdapter.notifyDataSetChanged();
                                searchBaseAdapter.notifyDataSetChanged();
                                //Если элемент добавлен в список избранного, то его тоже необходимо удалить
                                if (elementToDelete.isBookmarked()) {
                                    HistoryElement.favoriteElements.remove(elementToDelete);
                                    TabFragmentFavorites.baseAdapter.notifyDataSetChanged();
                                    final String typedText = TabFragmentFavorites.savedState.getString(TabFragmentFavorites.SEARCH_FIELD_TEXT);
                                    int index = HistoryElement.searchedFavoriteElements.indexOf(elementToDelete);
                                    try {
                                        //Проверка для поиска в избранном
                                        if (!typedText.isEmpty() && (index != -1)) {
                                            HistoryElement.searchedFavoriteElements.remove(index);
                                            TabFragmentFavorites.searchBaseAdapter.notifyDataSetChanged();
                                        }
                                    } catch (NullPointerException e) {
                                        e.printStackTrace();
                                    }
                                }
                                //Запись изменений в файл
                                new InternalDataController(true).execute();
                            }
                        }).create().show();
                return true;
            }
        });
    }

    /**
     * Устанавливает слушателя для клавиши, удаляющей все значения из поля searchField,
     * устанавливает слушаетля для поля searchField
     */
    private void setCrossAndSearchFieldListeners () {
        searchField.addTextChangedListener(searchFieldListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                final String searchFieldText = searchField.getText().toString().toLowerCase();
                //Если поле ввода пустое, скрыть клавишу для очистки содержимого поля ввода
                if (searchFieldText.isEmpty()) {
                    cross.setVisibility(View.GONE);
                    savedState.putInt(CROSS_VISIBILITY, cross.getVisibility());
                    savedState.putString(SEARCH_FIELD_TEXT, searchField.getText().toString());
                    setHistoryListViewVisibility(true);
                }
                //Если поле ввода непустое, показать клавишу для очистки содержимого поля ввода
                else
                {
                    cross.setVisibility(View.VISIBLE);
                    savedState.putInt(CROSS_VISIBILITY, cross.getVisibility());
                    savedState.putString(SEARCH_FIELD_TEXT, searchField.getText().toString());

                    HistoryElement.searchedHistoryElements.clear();
                    final int historyElementsSize = HistoryElement.historyElements.size();
                    //Проверка на соответствие запросу
                    for (int i = 0; i < historyElementsSize; i++) {
                        HistoryElement historyElement = HistoryElement.historyElements.get(i);
                        String inputText = historyElement.getInputText().toLowerCase();
                        String outputText = historyElement.getOutputText().toLowerCase();
                        if ((inputText.contains(searchFieldText)) || (outputText.contains(searchFieldText))) {
                            HistoryElement.searchedHistoryElements.add(historyElement);
                        }
                    }
                    setHistoryListViewVisibility(false);
                    searchBaseAdapter.notifyDataSetChanged();
                }
            }
        });

        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchField.removeTextChangedListener(searchFieldListener);
                searchField.setText("");
                searchField.addTextChangedListener(searchFieldListener);
                cross.setVisibility(View.GONE);
                setHistoryListViewVisibility(true);
                savedState.putString(SEARCH_FIELD_TEXT, searchField.getText().toString());
                savedState.putInt(CROSS_VISIBILITY, cross.getVisibility());
            }
        });
    }

    /**
     * Устанавливает слушателя для корзину, при нажатии на которую удаляются все эдементы, в показанном ListView
     */
    private void setTrashBinListener() {
        trashBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (historyListView.getVisibility() == View.VISIBLE) {

                    new AlertDialog.Builder(getActivity())
                            .setTitle(getActivity().getResources().getString(R.string.acceptance))
                            .setMessage(getActivity().getResources().getString(R.string.delete_all_items))
                            .setNegativeButton(getActivity().getResources().getString(R.string.cancel), null)
                            .setPositiveButton(getActivity().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    HistoryElement.historyElements.clear();
                                    TabFragmentHistory.baseAdapter.notifyDataSetChanged();
                                    HistoryElement.favoriteElements.clear();
                                    TabFragmentFavorites.baseAdapter.notifyDataSetChanged();
                                    final String searchedText = TabFragmentFavorites.savedState.getString(TabFragmentFavorites.SEARCH_FIELD_TEXT);
                                    try {
                                        //Проверка в поиске избранного
                                        if (!searchedText.isEmpty()) {
                                            HistoryElement.searchedFavoriteElements.clear();
                                            TabFragmentFavorites.searchBaseAdapter.notifyDataSetChanged();
                                        }
                                    } catch (NullPointerException e) {
                                        e.printStackTrace();
                                    }
                                    new InternalDataController(true).execute();
                                }
                            }).create().show();


                }
                else {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getActivity().getResources().getString(R.string.acceptance))
                            .setMessage(getActivity().getResources().getString(R.string.delete_all_displayed_items))
                            .setNegativeButton(getActivity().getResources().getString(R.string.cancel), null)
                            .setPositiveButton(getActivity().getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (HistoryElement historyElement : HistoryElement.searchedHistoryElements) {
                                        HistoryElement.historyElements.remove(historyElement);
                                        if (historyElement.isBookmarked()) {
                                            HistoryElement.favoriteElements.remove(historyElement);
                                            TabFragmentFavorites.baseAdapter.notifyDataSetChanged();
                                            final String typedText = TabFragmentFavorites.savedState.getString(TabFragmentFavorites.SEARCH_FIELD_TEXT);
                                            int index = HistoryElement.searchedFavoriteElements.indexOf(historyElement);
                                            try {
                                                //Проверка в поиске избранного
                                                if (!typedText.isEmpty() && (index != -1)) {
                                                    HistoryElement.searchedFavoriteElements.remove(index);
                                                    TabFragmentFavorites.searchBaseAdapter.notifyDataSetChanged();
                                                }
                                            } catch (NullPointerException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    searchField.removeTextChangedListener(searchFieldListener);
                                    searchField.setText("");
                                    searchField.addTextChangedListener(searchFieldListener);
                                    cross.setVisibility(View.GONE);
                                    savedState.putString(SEARCH_FIELD_TEXT, searchField.getText().toString());
                                    savedState.putInt(CROSS_VISIBILITY, cross.getVisibility());
                                    baseAdapter.notifyDataSetChanged();
                                    setHistoryListViewVisibility(true);
                                    new InternalDataController(true).execute();
                                }
                            }).create().show();
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
     * Определяет доступность View-элементов для осуществления поиска в зависимости от наличия
     *  элементов истории и их соответствия запросу поиска
     * @param accessStatus - определяет доступ
     */
    private void setSearchAccess(Boolean accessStatus){
        if (accessStatus) {
            searchPlace.setVisibility(View.VISIBLE);
            status.setVisibility(View.GONE);
        }
        else {
            final String HISTORY_LIST_IS_EMPTY = getActivity().getResources().getText(R.string.history_list_is_empty).toString();
            searchPlace.setVisibility(View.GONE);
            status.setText(HISTORY_LIST_IS_EMPTY);
            status.setVisibility(View.VISIBLE);
            savedState.putString(STATUS_TEXT, status.getText().toString());
        }
        savedState.putInt(STATUS_VISIBILITY, status.getVisibility());
    }

    /**
     * Показывает или скрывает результат при не найденных элементах в зависимости от visibility
     * @param visibility - true - элементы не найдены, необходимо показать результат,
     *                   что элементы не найдены
     */
    private void setNotFoundSearchResult (Boolean visibility) {
        if (visibility) {
            final String NOTHING_WAS_FOUND = getActivity().getResources().getText(R.string.no_appropriate_elements_were_found).toString();
            status.setText(NOTHING_WAS_FOUND);
            status.setVisibility(View.VISIBLE);
            savedState.putString(STATUS_TEXT, status.getText().toString());
        } else {
            status.setVisibility(View.GONE);
            searchedHistoryListView.setVisibility(View.VISIBLE);
            savedState.getInt(SEARCH_HISTORY_LIST_VIEW_VISIBILITY, searchedHistoryListView.getVisibility());
        }
        savedState.putInt(STATUS_VISIBILITY, status.getVisibility());
    }

    /**
     * При значении true параметра visibility делает видимым historyListView,
     * при значении false параметра visibility делает невидимым historyListView,
     * searchedHistoryListView в зависимости от параметра visibility ведет себя
     * противоположным образом
     * @param visibility - значение видимости элемента historyListView
     */
    private void setHistoryListViewVisibility(Boolean visibility) {
        if (visibility) {
            historyListView.setVisibility(View.VISIBLE);
            searchedHistoryListView.setVisibility(View.GONE);
        }
        else {
            historyListView.setVisibility(View.GONE);
            searchedHistoryListView.setVisibility(View.VISIBLE);
        }
        status.setVisibility(View.GONE);
        savedState.putInt(STATUS_VISIBILITY, status.getVisibility());
        savedState.putInt(HISTORY_LIST_VIEW_VISIBILITY, historyListView.getVisibility());
        savedState.putInt(SEARCH_HISTORY_LIST_VIEW_VISIBILITY, searchedHistoryListView.getVisibility());
    }

    /**
     * Скрывает оба ListView
     */
    private void hideListViews() {
        historyListView.setVisibility(View.GONE);
        searchedHistoryListView.setVisibility(View.GONE);
        savedState.putInt(HISTORY_LIST_VIEW_VISIBILITY, historyListView.getVisibility());
        savedState.putInt(SEARCH_HISTORY_LIST_VIEW_VISIBILITY, searchedHistoryListView.getVisibility());
    }

}
