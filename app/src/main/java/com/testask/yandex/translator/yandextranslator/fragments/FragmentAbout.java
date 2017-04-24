package com.testask.yandex.translator.yandextranslator.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.testask.yandex.translator.yandextranslator.R;

/**
 * Данный класс предназначен для связки с fragment_about
 *
 * @version 1.0 24 Апр 2017
 * @author Миронов Максим Андреевич
 */
public class FragmentAbout extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }
}
