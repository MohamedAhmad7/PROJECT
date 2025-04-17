import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class Item implements Serializable {
    private static final long serialVersionUID = 1L;
    private String n;
    private String dd;
    private String p;
    private String c;
    private boolean done;

    public Item(String n, String dd, String p, String c) {
        this.n = n;
        this.dd = dd;
        this.p = p;
        this.c = c;
        this.done = false;
    }

    public String getN() { return n; }
    public String getDD() { return dd; }
    public String getP() { return p; }
    public String getC() { return c; }
    public boolean isDone() { return done; }

    public void setN(String n) { this.n = n; }
    public void setDD(String dd) { this.dd = dd; }
    public void setP(String p) { this.p = p; }
    public void setC(String c) { this.c = c; }

    public void setComplete() { this.done = true; }

    public String fetchDetails() {
        return "Item: " + n +
               "\nDue: " + dd +
               "\nPriority: " + p +
               "\nCategory: " + c +
               "\nStatus: " + (done ? "Completed" : "Pending");
    }

    @Override
    public String toString() {
        return (done ? "[✔] " : "[ ] ") + n +
               " (Due: " + dd +
               ", P: " + p +
               ", C: " + c + ")";
    }
}

class DataStore {
    private List<Item> entries;
    private static final String STORAGE_ID = "items.dat";

    public DataStore() {
        entries = retrieveData();
    }

    public void addEntry(Item entry) {
        entries.add(entry);
        persistData();
    }

    public void removeEntry(Item entry) {
        entries.remove(entry);
        persistData();
    }

    public void modifyEntry(Item oldEntry, Item updatedEntry) {
        int idx = entries.indexOf(oldEntry);
        if (idx != -1) {
            entries.set(idx, updatedEntry);
            persistData();
        }
    }

    public void flagEntryDone(Item entry) {
        entry.setComplete();
        persistData();
    }

    public List<Item> fetchAllEntries() {
        return entries;
    }

    public List<String> fetchAllCats() {
        List<String> cats = new ArrayList<>();
        cats.add("All Categories");

        entries.stream()
                .map(Item::getC)
                .distinct()
                .forEach(cats::add);

        return cats;
    }

    private void persistData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(STORAGE_ID))) {
            oos.writeObject(entries);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Item> retrieveData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(STORAGE_ID))) {
            return (List<Item>) ois.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            return new ArrayList<>();
        }
    }
}

public class SmartTaskManager extends Application {
    private DataStore store = new DataStore();
    private ObservableList<Item> obsList;
    private ListView<Item> mainList;
    private TextField fldName;
    private TextField fldDue;
    private ComboBox<String> cmbPrio;
    private ComboBox<String> cmbCat;
    private ComboBox<String> cmbFilter;
    private Item currentItem = null;
    private Button btnSubmit;
    private Button btnCancel;
    private Label lblStatus;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SmartTask Manager");

        BorderPane rootLayout = new BorderPane();
        rootLayout.setPadding(new Insets(10));

        HBox filterArea = buildFilterUI();
        rootLayout.setTop(filterArea);

        VBox centerArea = buildListUI();
        rootLayout.setCenter(centerArea);

        GridPane inputArea = buildFormUI();
        rootLayout.setBottom(inputArea);

        Scene mainScene = new Scene(rootLayout, 650, 600);
        primaryStage.setScene(mainScene);
        primaryStage.show();

        refreshList();
    }

    private HBox buildFilterUI() {
        cmbFilter = new ComboBox<>();
        syncFilterCombo();
        cmbFilter.setPromptText("Filter by Category");

        Button applyBtn = new Button("Apply Filter");
        applyBtn.setOnAction(e -> refreshList());

        Button resetBtn = new Button("Show All");
        resetBtn.setOnAction(e -> {
            cmbFilter.setValue("All Categories");
            refreshList();
        });

        HBox container = new HBox(10, new Label("Filter by Category:"),
                cmbFilter, applyBtn, resetBtn);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(0, 0, 10, 0));

        return container;
    }

    private VBox buildListUI() {
        obsList = FXCollections.observableArrayList();
        mainList = new ListView<>(obsList);
        mainList.setPrefHeight(300);
        mainList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                onListSelect(newVal);
            }
        });

        TextArea detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setPrefHeight(100);

        mainList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                detailsArea.setText(newVal.fetchDetails());
            } else {
                detailsArea.clear();
            }
        });

        VBox container = new VBox(10);
        container.setPadding(new Insets(0, 0, 10, 0));
        container.getChildren().addAll(
            new Label("Tasks:"),
            mainList,
            new Label("Task Details:"),
            detailsArea
        );

        return container;
    }

    private GridPane buildFormUI() {
        GridPane formPane = new GridPane();
        formPane.setHgap(10);
        formPane.setVgap(10);
        formPane.setPadding(new Insets(10));

        fldName = new TextField();
        fldName.setPromptText("Task Name");
        fldName.setPrefWidth(200);

        fldDue = new TextField();
        fldDue.setPromptText("Due Date (YYYY-MM-DD)");

        cmbPrio = new ComboBox<>();
        cmbPrio.getItems().addAll("Low", "Medium", "High");
        cmbPrio.setValue("Medium");

        cmbCat = new ComboBox<>();
        cmbCat.setEditable(true);
        cmbCat.setPromptText("Category");
        syncCategoryCombo();

        btnSubmit = new Button("Add Task");
        btnSubmit.setOnAction(e -> processAction());

        btnCancel = new Button("Cancel");
        btnCancel.setVisible(false);
        btnCancel.setOnAction(e -> clearFields());

        Button delBtn = new Button("Delete Task");
        delBtn.setOnAction(e -> processDelete());

        Button doneBtn = new Button("Mark Completed");
        doneBtn.setOnAction(e -> processMarkDone());

        lblStatus = new Label("");
        lblStatus.setStyle("-fx-text-fill: red;");

        formPane.add(new Label("Task Name:"), 0, 0);
        formPane.add(fldName, 1, 0);

        formPane.add(new Label("Due Date:"), 0, 1);
        formPane.add(fldDue, 1, 1);

        formPane.add(new Label("Priority:"), 0, 2);
        formPane.add(cmbPrio, 1, 2);

        formPane.add(new Label("Category:"), 0, 3);
        formPane.add(cmbCat, 1, 3);

        formPane.add(lblStatus, 0, 4, 2, 1);

        HBox btnBar = new HBox(10, btnSubmit, btnCancel, doneBtn, delBtn);
        btnBar.setAlignment(Pos.CENTER);
        formPane.add(btnBar, 0, 5, 2, 1);

        return formPane;
    }

    private void processAction() {
        if (checkForm()) {
            String nameVal = fldName.getText().trim();
            String dueVal = fldDue.getText().trim();
            String prioVal = cmbPrio.getValue();
            String catVal = fetchCatValue();

            if (currentItem == null) {
                Item newItem = new Item(nameVal, dueVal, prioVal, catVal);
                store.addEntry(newItem);
                lblStatus.setText("Task added successfully");
            } else {
                Item updatedItem = new Item(nameVal, dueVal, prioVal, catVal);
                if (currentItem.isDone()) {
                    updatedItem.setComplete();
                }
                store.modifyEntry(currentItem, updatedItem);
                lblStatus.setText("Task updated successfully");
            }

            refreshList();
            syncFilterCombo();
            syncCategoryCombo();
            clearFields();
        }
    }

    private void processDelete() {
        Item itemToDelete = mainList.getSelectionModel().getSelectedItem();
        if (itemToDelete != null) {
            store.removeEntry(itemToDelete);
            refreshList();
            syncFilterCombo();
            syncCategoryCombo();
            clearFields();
            lblStatus.setText("Task deleted successfully");
        } else {
            lblStatus.setText("Please select a task to delete");
        }
    }

    private void processMarkDone() {
        Item itemToMark = mainList.getSelectionModel().getSelectedItem();
        if (itemToMark != null) {
            store.flagEntryDone(itemToMark);
            refreshList();
            clearFields(); 
            mainList.getSelectionModel().clearSelection(); 
            mainList.getSelectionModel().select(itemToMark); 
                                                        
            clearFields();
            lblStatus.setText("Task marked as completed");
        } else {
            lblStatus.setText("Please select a task to mark as completed");
        }
    }

    private void onListSelect(Item item) {
        currentItem = item;
        fldName.setText(item.getN());
        fldDue.setText(item.getDD());
        cmbPrio.setValue(item.getP());

        if (!cmbCat.getItems().contains(item.getC())) {
            cmbCat.getItems().add(item.getC());
        }
        cmbCat.setValue(item.getC());

        btnSubmit.setText("Update Task");
        btnCancel.setVisible(true);
    }

    private void clearFields() {
        fldName.clear();
        fldDue.clear();
        cmbPrio.setValue("Medium");
        cmbCat.setValue(null); 
        currentItem = null;
        mainList.getSelectionModel().clearSelection();
        btnSubmit.setText("Add Task");
        btnCancel.setVisible(false);
        lblStatus.setText("");
    }

    private void refreshList() {
        String filterVal = cmbFilter.getValue();
        List<Item> filtered = store.fetchAllEntries();

        if (filterVal != null && !filterVal.equals("All Categories")) {
            filtered = filtered.stream()
                    .filter(item -> item.getC().equals(filterVal))
                    .collect(Collectors.toList());
        }

        obsList.setAll(filtered);
    }

    private void syncFilterCombo() {
        String currentSel = cmbFilter.getValue();
        cmbFilter.getItems().setAll(store.fetchAllCats());

        if (currentSel != null && cmbFilter.getItems().contains(currentSel)) {
            cmbFilter.setValue(currentSel);
        } else {
            cmbFilter.setValue("All Categories");
        }
    }

    private void syncCategoryCombo() {
        String currentVal = cmbCat.getEditor().getText(); 
        List<String> distinctCats = store.fetchAllEntries().stream()
                                        .map(Item::getC)
                                        .distinct()
                                        .filter(c -> c != null && !c.isEmpty())
                                        .collect(Collectors.toList());
        cmbCat.getItems().setAll(distinctCats);

        if (currentVal != null && !currentVal.isEmpty()) {
             cmbCat.getEditor().setText(currentVal); 
             if (distinctCats.contains(currentVal)) {
                 cmbCat.setValue(currentVal);
             }
        } else {
             cmbCat.setValue(null);
        }
    }


    private String fetchCatValue() {
        String category = cmbCat.getEditor().getText(); 
        return (category != null && !category.trim().isEmpty()) ? category.trim() : "General";
    }

    private boolean checkForm() {
        StringBuilder issues = new StringBuilder();

        if (fldName.getText().trim().isEmpty()) {
            issues.append("Task name cannot be empty\n");
        }

        String dateTxt = fldDue.getText().trim();
        if (dateTxt.isEmpty()) {
            issues.append("Due date cannot be empty\n");
        } else {
            try {
                LocalDate.parse(dateTxt, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException ex) {
                issues.append("Invalid date format. Use YYYY-MM-DD\n");
            }
        }

        if (issues.length() > 0) {
            lblStatus.setText(issues.toString().trim());
            return false;
        }

        return true;
    }
}
