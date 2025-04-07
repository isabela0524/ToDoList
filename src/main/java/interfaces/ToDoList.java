package interfaces;

import models.Task;
import models.Task;
import models.TaskManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.table.TableCellRenderer;

public class ToDoList {
    private static final String DATA_FILE = "tasks.data";

    private TaskManager taskManager;
    private LocalDate selectedDate;
    private LocalDate currentMonth;
    private JTable calendarTable;
    private DefaultTableModel calendarModel;
    private JLabel completedTasksLabel;

    private DefaultListModel<Task> taskListModel;
    private JList<Task> taskList;


    public ToDoList() {
        try {
            taskManager = TaskManager.loadFromFile(DATA_FILE);
        } catch (IOException | ClassNotFoundException e) {
            taskManager = new TaskManager();
        }

        currentMonth = LocalDate.now().withDayOfMonth(1);
        selectedDate = LocalDate.now();
        JFrame frame = new JFrame("To-Do List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel navigationPanel = new JPanel(new BorderLayout());
        JButton preMonthButton = new JButton("Previous Month");
        JButton nextMonthButton = new JButton("Next Month");
        JLabel monthLabel = new JLabel("", SwingConstants.CENTER);

        JButton nextDayButton = new JButton("Next Day");
        JButton prevDayButton = new JButton("Previous Day");

        nextDayButton.addActionListener(e -> {
            selectedDate = selectedDate.plusDays(1);
            updateTaskList();
            updateMonthLabel(monthLabel);
            System.out.println("Ziua urmatoare: " + selectedDate);
        });

        prevDayButton.addActionListener(e -> {
            selectedDate = selectedDate.minusDays(1);
            updateTaskList();
            updateMonthLabel(monthLabel);
            System.out.println("Ziua anterioara: " + selectedDate);
        });

        navigationPanel.add(preMonthButton, BorderLayout.WEST);
        navigationPanel.add(monthLabel, BorderLayout.CENTER);
        navigationPanel.add(nextMonthButton, BorderLayout.EAST);

        JPanel dailyPanel = new JPanel(new BorderLayout());
        dailyPanel.add(prevDayButton, BorderLayout.WEST);
        dailyPanel.add(nextDayButton, BorderLayout.EAST);
        navigationPanel.add(dailyPanel, BorderLayout.SOUTH);

        preMonthButton.addActionListener(e -> changeMonth(currentMonth.minusMonths(1), monthLabel));
        nextMonthButton.addActionListener(e -> changeMonth(currentMonth.plusMonths(1), monthLabel));
        updateMonthLabel(monthLabel);

        JPanel calendarPanel = new JPanel(new BorderLayout());
        calendarTable = new JTable();
        calendarTable.setDefaultEditor(Object.class, null);
        calendarTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        calendarModel = new DefaultTableModel(0, 7);
        calendarModel.setColumnIdentifiers(new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"});
        calendarTable.setModel(calendarModel);
        calendarTable.setRowHeight(50);
        fillCalendar(currentMonth);

        calendarTable.getSelectionModel().addListSelectionListener(e -> {
            int row = calendarTable.getSelectedRow();
            int col = calendarTable.getSelectedColumn();
            if (row >= 0 && col >= 0) {
                Object cellValue = calendarTable.getValueAt(row, col);
                if (cellValue != null) {
                    int day = Integer.parseInt(cellValue.toString());
                    LocalDate newSelectedDate = LocalDate.of(currentMonth.getYear(), currentMonth.getMonthValue(), day);

                    if (!selectedDate.equals(newSelectedDate)) {
                        selectedDate = newSelectedDate;
                        System.out.println("Ziua selectată a fost schimbată la: " + selectedDate);
                        updateTaskList();
                    }
                }
            }
        });

        calendarPanel.add(new JScrollPane(calendarTable), BorderLayout.CENTER);

        completedTasksLabel = new JLabel("Completed Tasks: 0/0", SwingConstants.LEFT);
        completedTasksLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JPanel taskPanel = new JPanel(new BorderLayout());
        taskPanel.setBackground(new Color(245, 222, 179));
        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setCellRenderer(new TaskCellRenderer());
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        taskList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = taskList.getSelectedIndex();
                if (selectedIndex >= 0) {
                    Task selectedTask = taskListModel.getElementAt(selectedIndex);

                    selectedTask.setDone(!selectedTask.isDoane());

                    System.out.println("Task-ul selectat: " + selectedTask.getDescription() +
                            " este acum " + (selectedTask.isDoane() ? "finalizat" : "nefinalizat"));

                    taskListModel.setElementAt(selectedTask, selectedIndex);
                    taskList.repaint();
                }
            }
        });

        JButton deleteButton = new JButton("Delete Task");
        deleteButton.addActionListener(e -> {
            int selectedIndex = taskList.getSelectedIndex();
            if (selectedIndex >= 0) {
                Task selectedTask = taskListModel.getElementAt(selectedIndex);

                taskListModel.removeElementAt(selectedIndex);

                taskManager.removeTask(selectedDate, selectedTask);

                System.out.println("Task-ul a fost șters: " + selectedTask.getDescription());

                updateTaskList();
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(completedTasksLabel, BorderLayout.EAST);
        taskPanel.add(bottomPanel, BorderLayout.SOUTH);

        taskList.setBackground(new Color(245, 222, 179));
        taskList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int index = taskList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    Task task = taskListModel.get(index);
                    System.out.println("Am selectat task-ul: " + task.getDescription());

                    Rectangle cellBounds = taskList.getCellBounds(index, index);
                    Point relativeClickPoint = new Point(e.getX() - cellBounds.x, e.getY() - cellBounds.y);

                    Component renderer = taskList.getCellRenderer().getListCellRendererComponent(taskList, task, index, false, false);
                    if (renderer instanceof JPanel panel) {
                        for (Component component : panel.getComponents()) {
                            if (component instanceof JLabel label && label.getName() != null && label.getName().startsWith("circle_")) {
                                Rectangle labelBounds = label.getBounds();

                                if (labelBounds.contains(relativeClickPoint)) {
                                    task.setDone(!task.isDoane());
                                    System.out.println("Starea task-ului a fost schimbată: " + (task.isDoane() ? "finalizat" : "nefinalizat"));

                                    taskListModel.setElementAt(task, index);

                                    taskList.repaint();
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        });

        JScrollPane taskScrollPane = new JScrollPane(taskList);
        taskPanel.add(taskScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField taskInput = new JTextField();
        JButton addButton = new JButton("Add Task");
        addButton.addActionListener(e -> {
            String newTaskDescription = taskInput.getText().trim();
            if (!newTaskDescription.isEmpty()) {
                Task newTask = new Task(newTaskDescription);
                taskManager.addTask(selectedDate, newTask);
                updateTaskList();
                taskInput.setText("");
            }
        });

        JButton testButton = new JButton("Test Click");
        testButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                System.out.println("Am dat click pe butonul de test!");
            }
        });

        inputPanel.add(taskInput, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);
        inputPanel.add(deleteButton, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, calendarPanel, taskPanel);
        splitPane.setDividerLocation(400);

        frame.add(navigationPanel, BorderLayout.NORTH);
        frame.add(splitPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    taskManager.saveToFile(DATA_FILE);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        frame.setVisible(true);
        updateTaskList();
    }

    public DefaultListModel<Task> getTaskListModel() {
        return taskListModel;
    }

    private void changeMonth(LocalDate newMonth, JLabel monthLabel) {
        currentMonth = newMonth.withDayOfMonth(1);
        fillCalendar(currentMonth);
        updateMonthLabel(monthLabel);

        if (selectedDate.getMonth() != currentMonth.getMonth()) {
            selectedDate = LocalDate.of(currentMonth.getYear(), currentMonth.getMonth(), 1);
            updateTaskList();
        }
    }

    public class CalendarCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = new JLabel(String.valueOf(value), SwingConstants.CENTER);
            label.setOpaque(true);

            if (value != null) {
                int day = Integer.parseInt(value.toString());

                int year = LocalDate.now().getYear();
                int month = LocalDate.now().getMonthValue();

                LocalDate currentDay = LocalDate.of(year, month, day);

                List<Task> tasksForDay = taskManager.getTaskByDate(currentDay);

                int completedTasksCount = 0;
                for (Task task : tasksForDay) {
                    if (task.isDoane()) {
                        completedTasksCount++;
                    }
                }

                double completionPercentage = tasksForDay.isEmpty() ? 0 : (completedTasksCount / (double) tasksForDay.size()) * 100;

                Color dayColor;
                if (tasksForDay.isEmpty()) {
                    dayColor = Color.WHITE;
                } else if (completionPercentage > 50) {
                    dayColor = Color.GREEN;
                } else if (completionPercentage > 0) {
                    dayColor = new Color(255, 165, 0);
                } else {
                    dayColor = Color.RED;
                }

                label.setBackground(dayColor);
            }

            return label;
        }
    }

    private void fillCalendar(LocalDate date) {
        YearMonth yearMonth = YearMonth.of(date.getYear(), date.getMonthValue());
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int startDayOfWeek = (firstDayOfMonth.getDayOfWeek().getValue() % 7);
        int daysInMonth = yearMonth.lengthOfMonth();

        calendarModel.setRowCount(0);
        calendarModel.setRowCount(6);

        int day = 1;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                if (i == 0 && j < startDayOfWeek) {
                    calendarModel.setValueAt(null, i, j);
                } else if (day <= daysInMonth) {
                    calendarModel.setValueAt(day, i, j);
                    day++;
                } else {
                    calendarModel.setValueAt(null, i, j);
                }
            }
        }

        for (int i = 0; i < 7; i++) {
            calendarTable.getColumnModel().getColumn(i).setCellRenderer(new CalendarCellRenderer());
        }
    }


    private void updateMonthLabel(JLabel monthLabel) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
        String formattedDate = selectedDate.format(formatter);
        monthLabel.setText(formattedDate);
    }

    public void updateTaskList() {
        System.out.println("Updating task list for date: " + selectedDate);

        taskListModel.clear();
        List<Task> tasksForDate = taskManager.getTaskByDate(selectedDate);

        if (tasksForDate.isEmpty()) {
            System.out.println("Nu există taskuri pentru ziua selectată: " + selectedDate);
        } else {
            System.out.println("Există " + tasksForDate.size() + " taskuri pentru ziua selectată.");
        }

        int completedTasksCount = 0;

        for (Task task : tasksForDate) {
            taskListModel.addElement(task);
            if (task.isDoane()) {
                completedTasksCount++;
            }
        }

        completedTasksLabel.setText("Completed Tasks: " + completedTasksCount + "/" + tasksForDate.size());

        taskList.repaint();
    }


    public class TaskCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Task task) {

                String status = task.isDoane() ? "☑ " : "☐ ";
                label.setText(status + task.getDescription());

                if (task.isDoane()) {
                    label.setBackground(Color.GREEN);
                    label.setForeground(Color.BLACK);
                } else {
                    label.setBackground(new Color(245, 222, 179));
                    label.setForeground(Color.BLACK);
                }

                label.setOpaque(true);
            }
            return label;
        }
    }
}