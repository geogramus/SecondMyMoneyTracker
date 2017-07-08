package com.loftscholl.mymoneytrackertwo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.loftscholl.mymoneytrackertwo.api.BalanceResult;

import java.io.IOException;

/**
 * Created by Гео on 28.06.2017.
 */
public class BalanceFragment extends Fragment {

    private TextView balance;
    private TextView expense;
    private TextView income;
    private DiagramView diagram;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.balance, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        balance = (TextView) view.findViewById(R.id.balance);
        expense = (TextView) view.findViewById(R.id.expense);
        income = (TextView) view.findViewById(R.id.income);
        diagram = (DiagramView) view.findViewById(R.id.diagram);
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && isResumed()) {
            updateData();
        }
    }

    private void updateData() {
        Log.d("BalanceFragment", "updateData");
        getLoaderManager().restartLoader(0, null, new LoaderManager.LoaderCallbacks<BalanceResult>() {
            @Override
            public Loader<BalanceResult> onCreateLoader(int id, Bundle args) {
                return new AsyncTaskLoader<BalanceResult>(getContext()) {
                    @Override
                    public BalanceResult loadInBackground() {
                        try {
                            return ((LSApp) getActivity().getApplicationContext()).api().balance().execute().body();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<BalanceResult> loader, BalanceResult data) {
                if (data != null && data.isSuccess()) {
                    balance.setText(getString(R.string.price, data.totalIncome - data.totalExpenses));
                    expense.setText(getString(R.string.price, data.totalExpenses));
                    income.setText(getString(R.string.price, data.totalIncome));
                    diagram.update(data.totalExpenses, data.totalIncome);
                } else {
                    Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onLoaderReset(Loader<BalanceResult> loader) {

            }
        }).forceLoad();
    }
}