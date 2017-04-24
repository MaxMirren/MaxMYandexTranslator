package com.testask.yandex.translator.yandextranslator.fragments.tabs;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Данный класс предназначен для установки фрагментов(вкладок) для FragmentFavorites
 *
 * @version 1.0 24 Апр 2017
 * @author Миронов Максим Андреевич
 */
public final class ViewPagerAdapter extends FragmentPagerAdapter {

    //Список фрагментов к добавлению
    ArrayList<Fragment> fragments = new ArrayList<>();
    //Список названий вкладок к добавлению
    ArrayList<String> tabTitels = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitels.get(position);
    }

    /**
     * Добавление вкладки
     * @param fragments - фрагмент
     * @param titles - заголовок вкладки
     */
    public void addTab(Fragment fragments, String titles) {
        this.fragments.add(fragments);
        this.tabTitels.add(titles);
    }
}
