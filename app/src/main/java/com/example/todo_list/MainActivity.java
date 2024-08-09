package com.example.todo_list;

import android.content.Context;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import com.example.todo_list.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private ListView list;
    private FloatingActionButton fab_add_item;
    private ArrayAdapter<String> tasksAdapter;

    private TaskModel taskModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        taskModel = new TaskModel();

        setupList();
    }

    private void setupList() {
        list = findViewById(R.id.list);
        fab_add_item = findViewById(R.id.fab);

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
                Snackbar.make(view, "Item added to list.", Snackbar.LENGTH_SHORT).show();
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

        if (!(inputText.equals(""))){
            taskModel.addTask(inputText);
            tasksAdapter.notifyDataSetChanged();
            input.setText(""); // set the edit text back to empty
        }
        else {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}