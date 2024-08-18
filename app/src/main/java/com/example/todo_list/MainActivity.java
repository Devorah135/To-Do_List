package com.example.todo_list;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import static com.example.todo_list.lib.Utils.showInfoDialog;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import com.example.todo_list.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ListView list;
    private Button fab_add_item;
    private ArrayAdapter<String> tasksAdapter;
    private TaskModel taskModel;
    private Snackbar mSnackBar;
    private boolean mUseAutoSave;
    private String mKEY_LIST;
    private String mKEY_AUTO_SAVE;
    private ActivityResultLauncher<Intent> settingsLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize the settings launcher here
        settingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> restoreOrSetFromPreferences_AllAppAndGameSettings());

        setSupportActionBar(binding.toolbar);

        taskModel = new TaskModel();
        setupFields();
        setupList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        restoreFromPreferences_SavedListIfAutoSaveWasSetOn();
        restoreOrSetFromPreferences_AllAppAndGameSettings();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveOrDeleteGameInSharedPrefs();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(mKEY_LIST, getJSONFromList(list));
        outState.putBoolean(mKEY_AUTO_SAVE, mUseAutoSave);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore the list view properly
        // list = getListFromJSON(savedInstanceState.getString(mKEY_LIST)); // This is incorrect
        mUseAutoSave = savedInstanceState.getBoolean(mKEY_AUTO_SAVE, true);
    }

    private void setupFields() {
        mKEY_AUTO_SAVE = getString(R.string.auto_save_key);
        mKEY_LIST = getString(R.string.list_key); // Ensure this key is set
    }

    private void setupList() {
        list = findViewById(R.id.list);
        fab_add_item = findViewById(R.id.add_button);

        // Clicking the fab adds an item by calling additem() method
        setupFAB();

        tasksAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskModel.getTasks());
        list.setAdapter(tasksAdapter);

        deleteOnLongClick();
    }

    private void setupFAB() {
        fab_add_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                additem(view);
            }
        });
    }

    private void deleteOnLongClick() {
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return remove(position);
            }
        });
    }

    private void additem(View view) {
        EditText input = findViewById(R.id.edit_text);
        String inputText = input.getText().toString();

        if (!(inputText.equals(""))) {
            taskModel.addTask(inputText);
            tasksAdapter.notifyDataSetChanged();
            input.setText(""); // set the edit text back to empty
            Snackbar.make(view, "Item added to list.", Snackbar.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Please enter text.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean remove(int position) {
        Context context = getApplicationContext();
        Toast.makeText(context, "Item Removed", Toast.LENGTH_LONG).show();
        taskModel.removeTask(position);
        tasksAdapter.notifyDataSetChanged();
        return true;
    }

    private void saveOrDeleteGameInSharedPrefs() {
        SharedPreferences defaultSharedPreferences = getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = defaultSharedPreferences.edit();

        // Save current game or remove any prior game to/from default shared preferences
        if (mUseAutoSave) {
            editor.putString(mKEY_LIST, getJSONFromCurrentList(list));
        } else {
            editor.remove(mKEY_LIST);
        }

        editor.apply();
    }

    private void restoreFromPreferences_SavedListIfAutoSaveWasSetOn() {
        SharedPreferences defaultSharedPreferences = getDefaultSharedPreferences(this);
        if (defaultSharedPreferences.getBoolean(mKEY_AUTO_SAVE, true)) {
            String gameString = defaultSharedPreferences.getString(mKEY_LIST, null);
            if (gameString != null) {
                List<String> tasks = getGameFromJSON(gameString);
                tasksAdapter.clear();  // Clear the current items in the adapter
                tasksAdapter.addAll(tasks);  // Add the restored tasks
                tasksAdapter.notifyDataSetChanged();  // Notify the adapter to update the ListView
            }
        }
    }

    private List<String> getGameFromJSON(String jsonString) {
        List<String> tasks = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                tasks.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    private void restoreOrSetFromPreferences_AllAppAndGameSettings() {
        SharedPreferences sp = getDefaultSharedPreferences(this);
        mUseAutoSave = sp.getBoolean(mKEY_AUTO_SAVE, true);
    }

    private String getJSONFromList(ListView listView) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView.getAdapter();
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < adapter.getCount(); i++) {
            jsonArray.put(adapter.getItem(i));
        }
        return jsonArray.toString();
    }

    private String getJSONFromCurrentList(ListView listView) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView.getAdapter();
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < adapter.getCount(); i++) {
            jsonArray.put(adapter.getItem(i));
        }
        return jsonArray.toString();
    }

    private ListView getListFromJSON(String jsonString) {
        ListView listView = new ListView(this);
        ArrayList<String> tasks = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                tasks.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tasks);
        listView.setAdapter(adapter);
        return listView;
    }

    private void showSettings() {
        dismissSnackbar();
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        settingsLauncher.launch(intent);
    }

    private void showAbout() {
        dismissSnackbar();
        showInfoDialog(MainActivity.this, getString(R.string.about_title),
                getString(R.string.about_message));
    }

    private void dismissSnackbar() {
        if (mSnackBar != null && mSnackBar.isShown()) {
            mSnackBar.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            showSettings();
            return true;
        } else if (id == R.id.action_about) {
            showAbout();
        }
        return false;
    }
}
