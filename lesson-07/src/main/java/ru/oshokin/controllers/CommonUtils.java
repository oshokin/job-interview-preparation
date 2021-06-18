package ru.oshokin.controllers;

import java.math.BigDecimal;

public class CommonUtils {

    public static int getIntegerOrDefault(String value, int defaultValue) {
        int funcResult;
        try {
            funcResult = Integer.parseInt(value);
        } catch(NumberFormatException e) {
            funcResult = defaultValue;
        }
        return funcResult;
    }

    public static Short castShort(String value) {
        Short funcResult = null;
        if ((value != null) && !value.isEmpty()) {
            try {
                funcResult = Short.parseShort(value);
            } catch (NumberFormatException e) {
                funcResult = null;
            }
        }
        return funcResult;
    }

}
