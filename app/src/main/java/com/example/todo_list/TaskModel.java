package com.example.todo_list;

import java.util.ArrayList;

public class TaskModel {

    private ArrayList<String> tasks;

    public TaskModel() {
        tasks = new ArrayList<>();
    }

    public void addTask(String task) {
        tasks.add(task);
    }

    public void removeTask(int position) {
        tasks.remove(position);
    }

    public ArrayList<String> getTasks() {
        return tasks;
    }

}
