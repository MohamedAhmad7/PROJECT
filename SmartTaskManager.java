import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

// Task Class
class Task implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String dueDate;
    private String priority;
    private boolean isCompleted;

    public Task(String name, String dueDate, String priority) {
        this.name = name;
        this.dueDate = dueDate;
        this.priority = priority;
        this.isCompleted = false;
    }

    public String getName() { return name; }
    public String getDueDate() { return dueDate; }
    public String getPriority() { return priority; }
    public boolean isCompleted() { return isCompleted; }
    public void markCompleted() { this.isCompleted = true; }
    public void setName(String name) { this.name = name; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public void setPriority(String priority) { this.priority = priority; }

    @Override
    public String toString() {
        return (isCompleted ? "[✔] " : "[ ] ") + name + " (Due: " + dueDate + ", Priority: " + priority + ")";
    }
}

// TaskManager Class
class TaskManager {
    private List<Task> tasks;
    private static final String FILE_NAME = "tasks.dat";

    public TaskManager() {
        tasks = loadTasks();
    }

    public void addTask(Task task) {
        tasks.add(task);
        saveTasks();
    }

    public void deleteTask(Task task) {
        tasks.remove(task);
        saveTasks();
    }

    public void markTaskCompleted(Task task) {
        task.markCompleted();
        saveTasks();
    }

    public List<Task> getTasks() { return tasks; }

    private void saveTasks() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(tasks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Task> loadTasks() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            return (List<Task>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }
}

// JavaFX GUI
public class SmartTaskManager extends Application {
    private TaskManager taskManager = new TaskManager();
    private ObservableList<Task> taskList;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SmartTask Manager");

        taskList = FXCollections.observableArrayList(taskManager.getTasks());
        ListView<Task> listView = new ListView<>(taskList);

        TextField taskNameField = new TextField();
        taskNameField.setPromptText("Task Name");

        TextField dueDateField = new TextField();
        dueDateField.setPromptText("Due Date (YYYY-MM-DD)");

        ComboBox<String> priorityBox = new ComboBox<>();
        priorityBox.getItems().addAll("Low", "Medium", "High");
        priorityBox.setValue("Medium");

        Button addButton = new Button("Add Task");
        addButton.setOnAction(e -> {
            String name = taskNameField.getText();
            String dueDate = dueDateField.getText();
            String priority = priorityBox.getValue();
            if (!name.isEmpty() && !dueDate.isEmpty()) {
                Task newTask = new Task(name, dueDate, priority);
                taskManager.addTask(newTask);
                taskList.setAll(taskManager.getTasks());
                taskNameField.clear();
                dueDateField.clear();
            }
        });

        Button deleteButton = new Button("Delete Task");
        deleteButton.setOnAction(e -> {
            Task selectedTask = listView.getSelectionModel().getSelectedItem();
            if (selectedTask != null) {
                taskManager.deleteTask(selectedTask);
                taskList.setAll(taskManager.getTasks());
            }
        });

        Button markDoneButton = new Button("Mark Completed");
        markDoneButton.setOnAction(e -> {
            Task selectedTask = listView.getSelectionModel().getSelectedItem();
            if (selectedTask != null) {
                taskManager.markTaskCompleted(selectedTask);
                taskList.setAll(taskManager.getTasks());
            }
        });

        HBox inputBox = new HBox(10, taskNameField, dueDateField, priorityBox, addButton);
        inputBox.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(10, markDoneButton, deleteButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10, listView, inputBox, buttonBox);
        layout.setAlignment(Pos.CENTER);

        primaryStage.setScene(new Scene(layout, 500, 400));
        primaryStage.show();
    }
}
