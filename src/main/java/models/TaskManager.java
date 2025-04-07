package models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class TaskManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<LocalDate, List<Task>>taskByDate;

    public TaskManager() {
        taskByDate = new HashMap<>();
    }

    public List<Task> getTaskByDate(LocalDate date) {

        System.out.println("Căutăm taskuri pentru data: " + date);  // Verificăm ce dată cautăm
        List<Task> tasksForDate = taskByDate.get(date);

        if (tasksForDate == null) {
            System.out.println("Nu existe taskuri pentru aceasta data!");
            return new ArrayList<>();
        }
         return tasksForDate;
    }

    public void addTask(LocalDate date, Task task) {

        taskByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(task);
    }

    public void removeTask(LocalDate date, Task task) {
        List<Task> tasks = getTaskByDate(date);
        tasks.remove(task);
    }

    public Map<LocalDate, List<Task>> getAllTasks() {
        return taskByDate;
    }

    public void saveToFile(String fileName) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(this);
        }
    }

    public static TaskManager loadFromFile(String fileName) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            return (TaskManager) ois.readObject();
        }
    }
}
