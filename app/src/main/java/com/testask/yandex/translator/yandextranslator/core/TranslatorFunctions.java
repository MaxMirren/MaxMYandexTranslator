package com.testask.yandex.translator.yandextranslator.core;

/**
 * Данный перечисление создано для определения выполняемой функции
 * для объекта класса Translator
 *
 * @version 1.0 24 Апр 2017
 * @author Миронов Максим Андреевич
 */
public enum TranslatorFunctions {
    //Запрос списка поддерживаемых языков
    GET_LANGUAGE_LIST,
    //Перевод с автоопределением
    TRANSLATE_WITH_AUTO_DETECTION,
    //Перевод без автоопределения
    TRANSLATE_WITHOUT_AUTO_DETECTION,
}
