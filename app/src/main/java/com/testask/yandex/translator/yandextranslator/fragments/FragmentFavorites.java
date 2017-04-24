package com.testask.yandex.translator.yandextranslator.fragments;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.testask.yandex.translator.yandextranslator.MainActivity;
import com.testask.yandex.translator.yandextranslator.R;
import com.testask.yandex.translator.yandextranslator.fragments.tabs.TabFragmentFavorites;
import com.testask.yandex.translator.yandextranslator.fragments.tabs.TabFragmentHistory;
import com.testask.yandex.translator.yandextranslator.fragments.tabs.ViewPagerAdapter;

/**
 * Данный класс предназначен для добавления вкладок и их содержимого,
 * их слушателей, BottomNavigationView activity_main
 * передачи данных для управления
 *
 * @version 1.0 24 Апр 2017
 * @author Миронов Максим Андреевич
 */
public class FragmentFavorites extends Fragment {

    //Предназначена для сохранения текущей страницы
    public static int currentPage = 0;

    //Для обращения к view-элементам
    private View view;
    //Для обращения к TabLayout
    private TabLayout tabLayout;
    //Для обращения к ViewPager
    private ViewPager viewPager;
    //Для передачи ссылки на BottomNavigationView из activity_main
    private BottomNavigationView bottomNavigationView;
    //Для передачи ссылки на действительный объект класса MainActivity
    private MainActivity mainActivity;

    public FragmentFavorites setBottomNavigationViewControllers(MainActivity activity, BottomNavigationView bottomNavigation) {
        this.bottomNavigationView = bottomNavigation;
        this.mainActivity = activity;
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_favorites, container, false);
        setViews();
        setTabsContent();
        return view;
    }
    @Override
    public void onPause() {
        super.onPause();
        currentPage = viewPager.getCurrentItem();
    }

    @Override
    public void onStop() {
        super.onStop();
        currentPage = viewPager.getCurrentItem();
    }

    /**
     * Связка переменных view с view из fragment_favorites
     */
    private void setViews() {
        tabLayout = (TabLayout)  view.findViewById(R.id.tab_layout);
        viewPager = (ViewPager) view.findViewById(R.id.view_pager);
    }

    /**
     * Создание вкладок "История" и "Избранные"
     */
    private void setTabsContent() {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this.getChildFragmentManager());
        viewPagerAdapter.addTab(new TabFragmentHistory().setBottomNavigationViewControllers(mainActivity, bottomNavigationView), getResources().getString(R.string.history));
        viewPagerAdapter.addTab(new TabFragmentFavorites().setBottomNavigationViewControllers(mainActivity, bottomNavigationView), getResources().getString(R.string.favorites));
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(currentPage, false);
    }

}
