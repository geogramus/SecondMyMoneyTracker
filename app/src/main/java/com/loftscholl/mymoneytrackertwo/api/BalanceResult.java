package com.loftscholl.mymoneytrackertwo.api;

/**
 * Created by Гео on 08.07.2017.
 */

public class BalanceResult extends Result {
    public long totalExpenses;
    public long totalIncome;

    private BalanceResult(long totalExpenses, long totalIncome) {
        this.totalExpenses = totalExpenses;
        this.totalIncome = totalIncome;
        status = "success";

    }

}