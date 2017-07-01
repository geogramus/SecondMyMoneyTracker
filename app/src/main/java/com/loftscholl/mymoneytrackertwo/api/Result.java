package com.loftscholl.mymoneytrackertwo.api;

import android.text.TextUtils;

/**
 * Created by Гео on 29.06.2017.
 */

public class Result {
    String status;

    public boolean isSuccess() {
        return TextUtils.equals(status, "success");
    }

}
