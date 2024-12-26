package app;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private ListView<String> tools;
    @FXML
    private VBox options;
    @FXML
    private VBox middle;
    @FXML
    private VBox right;

    // tag search elements
    private ListView<String> modes;
    private Button clear;
    private TextField inputField;
    private ListView<String> suiteList;
    private Label selected;
    private Label t;

    ArrayList<String> availableSuites = new ArrayList<>();
    ArrayList<String> curr = new ArrayList<>();
    ArrayList<String> modeTypes = new ArrayList<>(Arrays.asList("All", "Selected", "Single"));

    ToolMode mode = ToolMode.ALL;

    Parser p = new Parser();

    public Controller() throws IOException {
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        availableSuites = p.getSuiteNames();

        options.setManaged(true);
        options.setVisible(true);
        middle.setManaged(true);
        middle.setVisible(true);
        right.setManaged(true);
        right.setVisible(true);

        initializeTagSearch();
        initializeFilter();

        /* stylize tools*/
        tools.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tools.getItems().addAll("Tag Search", "Filter", "Never runs");
        tools.setCellFactory(new Callback<>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        getStyleClass().add("toolsCell");
                        setText(item);
                    }
                };
            }
        });     // Style Class "toolsCell"

        /* activate and deactivate nodes upon changing tools */
        tools.getSelectionModel().getSelectedItems().addListener((ListChangeListener<String>) change -> {
            while(change.next()) {
                if (change.wasAdded()) {
                    switch (change.getAddedSubList().getFirst().toString()) {
                        case "Tag Search" -> {
                            // activate all, select, single and clear nodes
                            enableTagSearch(true);
                        }
                        case "Filter" -> {
                            enableTagSearch(false);
                        }
                        case "Never runs" -> {
                            // elements for never run
                            enableTagSearch(false);
                        }
                    }
                    updateCurr();
                }
            }

        });

    }

    //ListView modes, Button clear
    public void initializeTagSearch() {

        //left side

        /* stylize list of modes */
        modes = new ListView<>();
        modes.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        modes.getItems().addAll(modeTypes);
        modes.setCellFactory(new Callback<>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        getStyleClass().add("modeCell");
                        setText(item);
                    }
                };
            }
        });     // Style Class "modeCell"
        stylize(modes, "modes");

        /* changes modes based on selected mode */
        modes.getSelectionModel().getSelectedItems().addListener((ListChangeListener<String>) change -> {
            while(change.next()) {
                if (change.wasAdded()) {
                    switch (change.getAddedSubList().getFirst().toString()) {
                        case "All" -> {
                            mode = ToolMode.ALL;
                            suiteList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                        }
                        case "Selected" -> {
                            mode = ToolMode.SELECT;
                            suiteList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                        }
                        case "Single" -> {
                            mode = ToolMode.SINGLE;
                            suiteList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                        }
                    }
                    resetCurr();
                    updateCurr();
                }
            }

        });

        clear = new Button();
        clear.setText("Clear");
        clear.setOnMouseClicked(e -> resetCurr());
        stylize(clear, "");

        options.getChildren().add(modes);
        options.getChildren().add(clear);

        //middle part

        /* make inputField*/
        inputField = new TextField();
        inputField.setPromptText("test suite...");
        stylize(inputField, "input");

        /* update when input change */
        inputField.textProperty().addListener((_, _, newValue) -> {
            System.out.println(" Text Changed to  " + newValue + ")\n");

            suiteList.getItems().setAll(availableSuites);
            suiteList.getItems().removeIf(e -> !e.contains(newValue));

        });

        /* stylize suiteList */
        suiteList = new ListView<>();
        suiteList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        suiteList.getItems().addAll(availableSuites);
        suiteList.setCellFactory(new Callback<>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        getStyleClass().add("suiteCell");
                        setText(item);
                    }
                };
            }
        });     // Style Class "suiteCell"

        suiteList.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) updateCurr();
        });

        middle.getChildren().add(inputField);
        middle.getChildren().add(suiteList);

        //right part
        t = new Label();
        t.setText("Selected:");
        stylize(t, "");

        selected = new Label();
        selected.setText("selected:");
        stylize(selected, "");

        right.getChildren().add(t);
        right.getChildren().add(selected);

        enableTagSearch(false);

    }

    public void initializeFilter() {

    }

    public void enableTagSearch(boolean active) {
        //left components
        modes.setManaged(active);
        modes.setVisible(active);
        clear.setManaged(active);
        clear.setVisible(active);

        //middle components
        inputField.setManaged(active);
        inputField.setVisible(active);
        suiteList.setManaged(active);
        suiteList.setVisible(active);

        //right components
        t.setManaged(active);
        t.setVisible(active);
        selected.setManaged(active);
        selected.setVisible(active);
    }

    public void updateCurr() {

        switch (mode) {
            case ALL -> {
                resetCurr();
                curr.addAll(availableSuites);
            }
            case SELECT -> {
                for(String item : suiteList.getSelectionModel().getSelectedItems()) {
                    if (curr.contains(item)) curr.remove(item);
                    else curr.add(item);
                }
            }
            case SINGLE -> {
                resetCurr();
                curr.add(suiteList.getSelectionModel().getSelectedItem());
            }
        }

        selected.setText(curr.toString());

        updateStats();

    }

    public void updateStats() {

        try {

            p.displayStats();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void resetCurr() {
        if (!curr.isEmpty()) curr.clear();
        if (!(mode == ToolMode.ALL)) selected.setText("[]");
    }

    public void stylize(Parent e, String styleClass) {

        e.getStyleClass().add(styleClass);
        e.getStylesheets().add("styles.css");
    }
}
