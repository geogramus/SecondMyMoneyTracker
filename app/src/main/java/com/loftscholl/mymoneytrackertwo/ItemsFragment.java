package com.loftscholl.mymoneytrackertwo;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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

import static android.app.Activity.RESULT_OK;
import static com.loftscholl.mymoneytrackertwo.AddItemActivity.RC_ADD_ITEM;

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
    private View add;
    private ActionMode actionMode;
    private SwipeRefreshLayout refresh;
    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.items, menu);
            add.setVisibility(View.GONE);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_remove:
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.app_name)
                            .setMessage(R.string.confirm_remove)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    for (int i = adapter.getSelectedItems().size() - 1; i >= 0; i--)
                                        removeItem(adapter.remove(adapter.getSelectedItems().get(i)));
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    actionMode.finish();
                                }
                            })
                            .show();
                    return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            adapter.clearSeclections();
            add.setVisibility(View.VISIBLE);
        }
    };


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
        final GestureDetector gestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                if (actionMode != null) return;
                actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(actionModeCallback);
                adapter.toggleSelection(items.getChildLayoutPosition(items.findChildViewUnder(e.getX(), e.getY())));
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (actionMode != null)
                    adapter.toggleSelection(items.getChildLayoutPosition(items.findChildViewUnder(e.getX(), e.getY())));
                return super.onSingleTapConfirmed(e);
            }
        });
        items.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });
        refresh = view.findViewById(R.id.refresh);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadItems();
            }

        });
        add = view.findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddItemActivity.class);
                intent.putExtra(AddItemActivity.EXTRA_TYPE, type);
                startActivityForResult(intent, RC_ADD_ITEM);
            }
        });
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
                refresh.setRefreshing(false);

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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_ADD_ITEM && resultCode == RESULT_OK) {
            Item item = (Item) data.getSerializableExtra(AddItemActivity.RESULT_ITEM);
            addItem(item);
            loadItems();
            Toast toast = Toast.makeText(getContext(), item.name, Toast.LENGTH_LONG);
            toast.show();
        }
    }

}

