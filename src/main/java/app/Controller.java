package app;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {

    @FXML
    private ListView<String> tools;
    @FXML
    private VBox options;
    @FXML
    private VBox middle;
    @FXML
    private VBox right;

    final String[] filterBy = {"only", "ignore", "need"};

    // tag search elements
    private ListView<String> modes;
    private Button clear;
    private TextField inputField;
    private ListView<String> suiteList;
    private Label selected;
    private Label t;
    private ListView<PieChart> pieChartListView;

    // filter elements
    private ListView<HBox> filterList;
    private Label match;
    private ListView<String> matchingTestList;
    // private Map<CheckBox, ComboBox<String>[]> flat = new HashMap<>();
    ArrayList<ArrayList<String>> activeSelectedFilter = new ArrayList<>();

    // never run elements
    private ListView<HBox> categoriesList;
    private ArrayList<String> activeCatagoriesList = new ArrayList<>();

    ArrayList<ArrayList<String>> chosenFilter = new ArrayList<>();
    ArrayList<Boolean> activeFilter = new ArrayList<>();

    ArrayList<String> availableSuites = new ArrayList<>();
    ArrayList<String> curr = new ArrayList<>();
    ArrayList<String> modeTypes = new ArrayList<>(Arrays.asList("All", "Selected", "Single"));

    ToolMode mode = ToolMode.SINGLE;

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
        initializeNeverRun();

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
                            enableTagSearch(true);  // tag
                            enableFilter(false);    // no filter
                            enableNeverRun(false);  // no never run
                        }
                        case "Filter" -> {
                            enableTagSearch(false); // no tag
                            enableFilter(true);     // filter
                            enableNeverRun(false);  // no never run
                        }
                        case "Never runs" -> {
                            enableTagSearch(false); // no tag
                            enableFilter(false);    // no filter
                            enableNeverRun(true);   // never run
                        }
                    }
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
        stylize(modes, "modes");
        setStringCellFactory(modes, "modeCell");

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
        clear.setOnMouseClicked(e -> {resetCurr(); updateCurr();});
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
            // System.out.println(" Text Changed to  " + newValue + ")\n");

            suiteList.getItems().setAll(availableSuites);
            suiteList.getItems().removeIf(e -> !e.contains(newValue));

        });

        /* stylize suiteList */
        suiteList = new ListView<>();
        suiteList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        suiteList.getItems().addAll(availableSuites);
        setStringCellFactory(suiteList, "suiteCell");

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

        pieChartListView = new ListView<>();
        stylize(pieChartListView, "PieChartList");
        setNodeCellFactory(pieChartListView, "PieChartListCell");

        right.getChildren().add(pieChartListView);


        enableTagSearch(false);

    }

    public void initializeFilter() {

        // middle part
        filterList = new ListView<>();
        stylize(filterList, "categories");

        for (String cat : Categories.getCategoryNames()) {

            HBox h = new HBox();
            h.setAlignment(Pos.CENTER_LEFT);
            stylize(h, "filterBox");

            // add listener later
            CheckBox checkBox = new CheckBox();
            checkBox.setText("");
            stylize(checkBox, "filterEnable");

            h.getChildren().add(checkBox);

            VBox v = new VBox();
            stylize(v, "filterDisplay");

            Label title = new Label();
            title.setText(cat);
            stylize(title, "filterTitle");
            v.getChildren().add(title);

            // load only/ignore/need option if they exist in category

            ComboBox<String> enable = new ComboBox<>();
            stylize(enable, "optionList");
            setStringCellFactory(enable, "optionCell");

            ComboBox<String> category = new ComboBox<>();
            stylize(category, "optionList");
            setStringCellFactory(category, "dirCell");

            TreeSet<String> cate = new TreeSet<>();

            for (int i = 0; i < 3; i++) {
                if (!p.categories.categories.get(cat).get(i).isEmpty()) {
                    enable.getItems().add(filterBy[i]);

                    cate.addAll(p.categories.categories.get(cat).get(i));
                }
            }

            category.getItems().addAll(cate);

            enable.getSelectionModel().selectFirst();
            category.getSelectionModel().selectFirst();

            /*
            chosenFilter: {
                {cat, enable, category},
                ...

            }
            */

            ArrayList<String> temp = new ArrayList<>();
            temp.add(cat);
            temp.add(enable.getSelectionModel().getSelectedItem());
            temp.add(category.getSelectionModel().getSelectedItem());
            chosenFilter.add(temp);
            activeFilter.add(false);

            checkBox.setOnAction(e -> {updateChosenFilter(cat, checkBox, enable, category);});
            enable.setOnAction(e -> {updateChosenFilter(cat, checkBox, enable, category);});
            category.setOnAction(e -> {updateChosenFilter(cat, checkBox, enable, category);});

            v.getChildren().add(enable);

            h.getChildren().add(v);
            h.getChildren().add(category);

            filterList.getItems().add(h);

        }

        setNodeCellFactory(filterList, "filterCell");

        middle.getChildren().add(filterList);

        // right part

        match = new Label();
        match.setText("Matching tests: ");
        stylize(match, "");

        right.getChildren().add(match);

        matchingTestList = new ListView<>();
        stylize(matchingTestList, "testList");
        setStringCellFactory(matchingTestList, "dirCell");

        right.getChildren().add(matchingTestList);

        for (int i = 0; i < 3; i++) activeSelectedFilter.add(new ArrayList<>());
        matchingTestList.getItems().addAll(p.givenDirs(activeSelectedFilter));

        enableFilter(false);

    }

    public void initializeNeverRun() {

        // middle part
        categoriesList = new ListView<>();
        stylize(categoriesList, "categories");
        setNodeCellFactory(categoriesList, "filterCell");

        for (String cat : Categories.getCategoryNames()) {

            HBox h = new HBox();
            h.setAlignment(Pos.CENTER_LEFT);
            stylize(h, "filterBox");

            // add listener later
            CheckBox checkBox = new CheckBox();
            checkBox.setText("");
            stylize(checkBox, "filterEnable");

            h.getChildren().add(checkBox);

            Label title = new Label();
            title.setText(cat);
            stylize(title, "filterTitle");
            h.getChildren().add(title);

            // load only/ignore/need option if they exist in category

            ComboBox<String> category = new ComboBox<>();
            stylize(category, "optionList");
            setStringCellFactory(category, "dirCell");

            TreeSet<String> cate = new TreeSet<>();

            for (int i = 0; i < 3; i++) {
                if (!p.categories.categories.get(cat).get(i).isEmpty()) {
                    cate.addAll(p.categories.categories.get(cat).get(i));
                }
            }

            category.getItems().addAll(cate);

            category.getSelectionModel().selectFirst();

            /*
            chosenFilter: {
                {cat, enable, category},
                ...

            }
            */

            ArrayList<String> temp = new ArrayList<>();
            temp.add(cat);
            temp.add(category.getSelectionModel().getSelectedItem());
            chosenFilter.add(temp);
            activeFilter.add(false);

//            checkBox.setOnAction(e -> {updateChosenFilter(cat, checkBox, enable, category);});
//            category.setOnAction(e -> {updateChosenFilter(cat, checkBox, enable, category);});

            h.getChildren().add(category);

            categoriesList.getItems().add(h);

        }

        middle.getChildren().add(categoriesList);

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
        pieChartListView.setManaged(active);
        pieChartListView.setVisible(active);

        if (active) updateCurr();
    }

    public void enableFilter(boolean active) {
        //left components

        //middle components
        filterList.setManaged(active);
        filterList.setVisible(active);

        //right components
        match.setManaged(active);
        match.setVisible(active);
        matchingTestList.setManaged(active);
        matchingTestList.setVisible(active);
    }

    public void enableNeverRun(boolean active) {
        // middle components
        categoriesList.setManaged(active);
        categoriesList.setVisible(active);
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

        updateTagSearchStats();

    }

    public void updateTagSearchStats() {

        try {

            // clear out the elements in pie chart
            pieChartListView.getItems().clear();

            Categories info = p.analyseFolders(curr);

            for (String suite : curr)   // for every currently selected suite
                for (String cat : Categories.getCategoryNames()) {  // for every category, used to generate chart data

                    // skip category if no data is present
                    if (info.getCounted().get(cat).get(0).isEmpty() &&
                            info.getCounted().get(cat).get(1).isEmpty() &&
                            info.getCounted().get(cat).get(2).isEmpty()) continue;

                    ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

                    for (int i = 0; i < 3; i++) {
                        TreeMap<String, Integer> dirCount = info.getCounted().get(cat).get(i);
                        for (String name : dirCount.keySet()) {
                            pieChartData.add(new PieChart.Data(
                                    filterBy[i]+ "-" +name + ": " + dirCount.get(name), dirCount.get(name)));
                        }
                    }




                     PieChart chart = new PieChart(pieChartData);
                     chart.setTitle(cat);

                    pieChartListView.getItems().add(chart);

                }

            // p.displayStats();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void resetCurr() {
        if (!curr.isEmpty()) curr.clear();
        if (!(mode == ToolMode.ALL)) selected.setText("[]");
    }

    public void updateChosenFilter(String cat, CheckBox checkBox, ComboBox<String> enable, ComboBox<String> category) {

        // update chosenFilter
        for (int i = 0; i < chosenFilter.size(); i++) {

            if (!chosenFilter.get(i).getFirst().equals(cat)) continue;

            // modify enable to be current
            chosenFilter.get(i).set(1, enable.getSelectionModel().getSelectedItem());

            //modify category directive
            chosenFilter.get(i).set(2, category.getSelectionModel().getSelectedItem());

            //mark as active if checked
            if (checkBox.isSelected()) {
                activeFilter.set(i, true);
                updateMatch();
            } else {
                activeFilter.set(i, false);
                updateMatch();
            }

            System.out.println(i);
            System.out.println(chosenFilter.get(i).get(1));
            System.out.println(chosenFilter.get(i).get(2));

            break;

        }



    }

    public void updateMatch() {

        matchingTestList.getItems().clear();

        activeSelectedFilter.clear();
        for (int i = 0; i < 3; i++) activeSelectedFilter.add(new ArrayList<>());

        for (int i = 0; i < chosenFilter.size(); i++) {
            if (!activeFilter.get(i)) continue;

            switch (chosenFilter.get(i).get(1)) {
                case "only" -> activeSelectedFilter.get(0).add(chosenFilter.get(i).get(2));
                case "ignore" -> activeSelectedFilter.get(1).add(chosenFilter.get(i).get(2));
                case "need" -> activeSelectedFilter.get(2).add(chosenFilter.get(i).get(2));

            }
        }

        System.out.println(activeSelectedFilter);

        ArrayList<String> matchedTest = p.givenDirs(activeSelectedFilter);

        match.setText("Matching tests: " + matchedTest.size());
        matchingTestList.getItems().addAll(matchedTest);

    }

    public void stylize(Parent e, String styleClass) {

        e.getStyleClass().add(styleClass);
        e.getStylesheets().add("styles.css");
    }

    // for listview
    public void setStringCellFactory(ListView<String> list, String styleClass) {
        list.setCellFactory(new Callback<>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        getStyleClass().add(styleClass);
                        setText(item);
                    }
                };
            }
        });
    }

    // overloaded for comboBox
    public void setStringCellFactory(ComboBox<String> list, String styleClass) {
        list.setCellFactory(new Callback<>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        getStyleClass().add(styleClass);
                        setText(item);
                    }
                };
            }
        });
    }

    public <K extends Node> void setNodeCellFactory(ListView<K> list, String styleClass) {
        list.setCellFactory(new Callback<>() {
            @Override
            public ListCell<K> call(ListView<K> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(K item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(item);
                        getStyleClass().add(styleClass);

                    }
                };
            }
        });
    }



}
