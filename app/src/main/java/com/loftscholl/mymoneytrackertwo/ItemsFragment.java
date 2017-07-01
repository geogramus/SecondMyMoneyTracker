package com.loftscholl.mymoneytrackertwo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.loftscholl.mymoneytrackertwo.api.AddResult;
import com.loftscholl.mymoneytrackertwo.api.LSApi;
import com.loftscholl.mymoneytrackertwo.api.Result;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Гео on 27.06.2017.
 */

public class ItemsFragment extends Fragment {
    public static final String ARG_TYPE = "type";
    private static final int LOADER_ITEMS = 0;
    private static final int LOADER_ADD = 1;
    private static final int LOADER_REMOVE = 2;
    private String type;
    private LSApi api;
    private ItemsAdapter adapter = new ItemsAdapter();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.items, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final RecyclerView items = (RecyclerView) view.findViewById(R.id.items);
        items.setAdapter(adapter);
        type = getArguments().getString(ARG_TYPE);
        api = ((LSApp) getActivity().getApplication()).api();
        loadItems();
    }

    private void loadItems() {

        getLoaderManager().initLoader(LOADER_ITEMS, null, new LoaderManager.LoaderCallbacks<List<Item>>() {
            @Override
            public Loader<List<Item>> onCreateLoader(int id, Bundle args) {
                return new AsyncTaskLoader<List<Item>>(getContext()) {
                    @Override
                    public List<Item> loadInBackground() {
                        try {
                            Call<List<Item>> items = api.items(type);
                            Response<List<Item>> execute = items.execute();
                            List<Item> body = execute.body();
                            return body; //api.items(type).execute().body();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<List<Item>> loader, List<Item> data) {
                if (data == null) {
                    Toast.makeText(getContext(), R.string.error, Toast.LENGTH_SHORT).show();
                } else {
                    adapter.clear();
                    adapter.addAll(data);
                }
            }

            @Override
            public void onLoaderReset(Loader<List<Item>> loader) {

            }
        }).forceLoad();
    }

    private void addItem(final Item item) {
        getLoaderManager().restartLoader(LOADER_ADD, null, new
                LoaderManager.LoaderCallbacks<AddResult>() {

                    @Override
                    public Loader<AddResult> onCreateLoader(int id, Bundle args) {
                        return new AsyncTaskLoader<AddResult>(getContext()) {
                            @Override
                            public AddResult loadInBackground() {
                                try {
                                    return api.add(item.name, item.price, item.type).execute().body();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return null;
                                }

                            }
                        };
                    }

                    @Override
                    public void onLoadFinished(Loader<AddResult> loader, AddResult data) {
                        adapter.updateId(item, data.id);
                    }

                    @Override
                    public void onLoaderReset(Loader<AddResult> loader) {

                    }
                }).forceLoad();
    }

    private void removeItem(final Item item) {
        getLoaderManager().restartLoader(LOADER_REMOVE, null, new LoaderManager.LoaderCallbacks<Result>() {
            @Override
            public Loader<Result> onCreateLoader(int id, Bundle args) {
                return new AsyncTaskLoader<Result>(getContext()) {
                    @Override
                    public Result loadInBackground() {
                        try {
                            return api.remove(item.id).execute().body();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }

                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<Result> loader, Result data) {

            }

            @Override
            public void onLoaderReset(Loader<Result> loader) {

            }
        }).forceLoad();

    }
}
