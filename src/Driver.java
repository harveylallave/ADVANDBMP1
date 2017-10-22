import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;

//import javafx.scene.input.KeyCombination;

public class Driver extends Application {
    Scene home;

    Button button;
    TableView<ArrayList<String>> table;

    BorderPane mainPane = new BorderPane();

    Label queryNum = new Label(),
            query = new Label();
    Button optimizeButton;
    ChoiceBox<String> queryChoiceBox;

    Connection conn;
    private LineChart graphArea;
    private XYChart.Series dataSeries1;
    private int nQueryExec = 1;
    private TextField input,
                      input_2;
    private ToggleGroup queryTypeToggleGroup;

    @SuppressWarnings("unchecked")
    public void start(Stage primaryStage) throws Exception {
        conn = getConnection();
        deleteAllIndex();
        initMainScreen();

        home = new Scene(mainPane, 1000, 600);
        home.getStylesheets().add("View/Style.css");

        primaryStage.setTitle("ADVANDB - MP1");
        primaryStage.setOnCloseRequest(e -> terminateProgram());
        primaryStage.setScene(home);
        primaryStage.show();
    }

    public void initMainScreen() {
        mainPane.getChildren().remove(mainPane.getCenter());
        mainPane.setTop(initTopBar());
        mainPane.setCenter(initCenterVBox());
        mainPane.setRight(initRightVBox());
    }

    public MenuBar initTopBar() {
        //Initializing menu items
        Menu refreshIndex = new Menu(),
             optimization = new Menu(),
             exit         = new Menu();

        Label refreshLabelMenu = new Label("Refresh"),
              optimizationLabelMenu = new Label("Optimization");

        refreshLabelMenu.setOnMouseClicked(e -> deleteAllIndex());
        optimizationLabelMenu.setOnMouseClicked(e -> {
            Stage myDialog = new Stage();
            myDialog.initModality(Modality.WINDOW_MODAL);

            GridPane optimizePane = new GridPane();
            optimizePane.setVgap(20);
            optimizePane.setHgap(25);
            optimizePane.setAlignment(Pos.CENTER);

            Scene dialogScene = new Scene(optimizePane, 800, 400);
            ChoiceBox optimizationType = new ChoiceBox<>();

            optimizationType.getItems().addAll("Create Indexes");

            optimizePane.add(new Label("Method"), 0, 0);
            optimizePane.add(optimizationType, 1, 0);
            optimizationType.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, number2) -> {

                switch ((Integer) number2) {
                    case 0: indexOptimization(optimizePane);
                            break;
                }
            });

            myDialog.setTitle("Query Optimization");
            dialogScene.getStylesheets().add("View/Style.css");
            myDialog.setScene(dialogScene);
            myDialog.show();
        });

        refreshIndex.setGraphic(refreshLabelMenu);
        optimization.setGraphic(optimizationLabelMenu);

        optimizationLabelMenu = new Label("Exit");
        exit.setGraphic(optimizationLabelMenu);
        optimizationLabelMenu.setOnMouseClicked(e -> terminateProgram());

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(refreshIndex, optimization, exit);

        return menuBar;
    }

    public void indexOptimization(GridPane optimizePane){
        ChoiceBox tableChoiceBox = new ChoiceBox<>();
        ChoiceBox attrChoiceBox = new ChoiceBox<>();

        optimizeButton = new Button("Optimize");
        optimizeButton.setDisable(true);
        optimizeButton.setOnAction(e -> {
            Statement st = null;
            String query = "create index i" +attrChoiceBox.getSelectionModel().getSelectedItem().toString() + " on " +
                            tableChoiceBox.getSelectionModel().getSelectedItem().toString()+ "(" +
                            attrChoiceBox.getSelectionModel().getSelectedItem().toString() + ") ";
            try{
                st = conn.createStatement();
                st.executeUpdate(query);
                st.close();
                System.out.println("Added new index: i" + attrChoiceBox.getSelectionModel().getSelectedItem().toString());
            } catch (SQLException e2) {
                System.out.println("SQLException: " + e2.getMessage());
                System.out.println("SQLState: " + e2.getSQLState());
                System.out.println("VendorError: " + e2.getErrorCode());
            }
        });

        tableChoiceBox.getItems().addAll("book", "book_author", "book_loans", "borrower", "library_branch", "publisher");
        tableChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, number2) -> {

            optimizeButton.setDisable(true);
            attrChoiceBox.getItems().clear();
            switch ((Integer) number2) {
                case 0: attrChoiceBox.getItems().addAll("BookID", "Title", "PublisherName");
                        break;

                case 1: attrChoiceBox.getItems().addAll("BookID", "AuthorLastName", "AuthorFirstName");
                        break;

                case 2: attrChoiceBox.getItems().addAll("BookID", "BranchID", "CardNo", "DateOut", "DueDate", "DateReturned");
                        break;

                case 3: attrChoiceBox.getItems().addAll("CardNo", "BorrowerLName", "BorrowerFName", "Address", "Phone");
                        break;

                case 4: attrChoiceBox.getItems().addAll("BranchID", "BranchName", "BranchAddress");
                        break;

                case 5: attrChoiceBox.getItems().addAll("PublisherName", "Address", "Phone");
                        break;

            }
            attrChoiceBox.getSelectionModel().selectedIndexProperty().addListener((observableValue1,
                                                                                   oldNum,
                                                                                   newNum) ->
                                                                                   optimizeButton.setDisable(false));

        });

        optimizePane.add(new Label("Table"), 0, 1);
        optimizePane.add(tableChoiceBox, 1, 1);
        optimizePane.add(new Label("Attribute"), 0, 2);
        optimizePane.add(attrChoiceBox, 1, 2);
        optimizePane.add(optimizeButton, 1, 3);

    }

    public VBox initRightVBox() {
        VBox mainVBox = new VBox(15);

        mainVBox.setAlignment(Pos.TOP_CENTER);
        mainVBox.setPadding(new Insets(10, 0, 0, 0));

        button = new Button("Edit Query");
        button.getStyleClass().add("rightVBoxButton");
        button.setMinWidth(150);
        button.wrapTextProperty().setValue(true);

        button.setOnAction(event -> {
            Label label = new Label(queryChoiceBox.getValue().toString());
            label.getStyleClass().add("editQueryLabel");
            VBox subVBox = new VBox(10);
            subVBox.getStyleClass().add("subVBox");
            subVBox.getChildren().add(label);

            System.out.println("\nEDIT QUERY: " + queryChoiceBox.getValue());
            button = new Button("Update Query");
            button.setMinWidth(150);

            mainVBox.getChildren().add(subVBox);
            VBox editQueryVBox;

            String queryToggleString = queryTypeToggleGroup.getSelectedToggle().getUserData().toString();
            switch (queryChoiceBox.getSelectionModel().getSelectedItem().toString()) {
                case "1. All books published by Doubleday":
                    editQueryVBox = new VBox(2);
                    editQueryVBox.setPadding(new Insets(0, 10, 0, 10));
                    input = new TextField("Doubleday");
                    editQueryVBox.getChildren().addAll(new Label("SELECT Title, PublisherName \n" +
                                                                     "FROM book \n" +
                                                                     "WHERE PublisherName like \n'%"), input,
                                                       new Label("%'\n ORDER BY PublisherName;"));
                    subVBox.getChildren().addAll(editQueryVBox, button);

                    button.setOnAction(event1 -> {
                        if (((VBox)mainPane.getCenter()).getChildren().get(2) instanceof TableView)
                            ((VBox)mainPane.getCenter()).getChildren().remove(2);
                        table = new TableView<>();
                        mainVBox.getChildren().remove(3);
                        updateTableQuery1((VBox) mainPane.getCenter(), input.getText());
                    });
                    break;

                case "2. Number of books loaned in 2017":
                    editQueryVBox = new VBox(2);
                    editQueryVBox.setPadding(new Insets(0, 10, 0, 10));
                    input = new TextField("2017");
                    editQueryVBox.getChildren().addAll(new Label("SELECT COUNT(*) AS NoBooksBor\n" +
                                                                     "FROM book_loans\n" +
                                                                     "WHERE YEAR(DateOut) = "), input);
                    subVBox.getChildren().addAll(editQueryVBox, button);

                    button.setOnAction(event1 -> {
                        if (((VBox)mainPane.getCenter()).getChildren().get(2) instanceof TableView)
                            ((VBox)mainPane.getCenter()).getChildren().remove(2);
                        table = new TableView<>();
                        mainVBox.getChildren().remove(3);
                        updateTableQuery2((VBox) mainPane.getCenter(), input.getText());
                    });
                    break;

                case "3. All borrowers who have borrowed at most 2 books":
                    editQueryVBox = new VBox(2);
                    editQueryVBox.setPadding(new Insets(0, 10, 0, 10));
                    input = new TextField("0");
                    input_2 = new TextField("2");
                    editQueryVBox.getChildren().addAll(new Label(queryToggleString.equals("Normal") ?
                            "SELECT CONCAT(BO.BorrowerLName, ', ', BO.BorrowerFName) AS BorrowerName, COUNT(*) as NoBooksBor\n" +
                                    "FROM borrower BO, book_loans BL\n" +
                                    "WHERE BO.CardNo = BL.CardNo\n" +
                                    "GROUP BY BorrowerName\n" +
                                    "HAVING NoBooksBor >= ": queryToggleString.equals("Join") ?
                            "SELECT CONCAT(BO.BorrowerLName, ', ', BO.BorrowerFName) as BorrowerName , COUNT(*) as NoBooksBor\n" +
                                    "\tFROM borrower BO join book_loans BL on BO.CardNo = BL.CardNo\n" +
                                    "\tGROUP BY BorrowerName\n" +
                                    "HAVING NoBooksBor >= ": /*SubQuery*/
                            "SELECT CONCAT(BorrowerLName, \", \", BorrowerFName) as BorrowerName, BO2.NoBooksBor\n" +
                                    "\tFROM borrower, (SELECT BL.CardNo, COUNT(CardNo) as NoBooksBor\n" +
                                    "\tFROM book_loans BL\n" +
                                    "\tGROUP BY BL.CardNo\n" +
                                    "\tHAVING COUNT(CardNo) >= "), input,
                            new Label(queryToggleString.equals("Normal") ?
                                    " and NoBooksBor <= ": queryToggleString.equals("Join") ?
                                    " and NoBooksBor <= " : /*SubQuery*/
                                    " 0 and COUNT(CardNo) <= "), input_2,
                            new Label(queryToggleString.equals("Normal") ?
                                    "ORDER BY 2 DESC, 1;\n" : queryToggleString.equals("Join") ?
                                    "ORDER BY 2 DESC, 1;" : /*SubQuery*/
                                    "ORDER BY 1) as BO2\n" +
                                    "WHERE borrower.CardNo = BO2.CardNo\n" +
                                    "ORDER BY 2 DESC;"));
                    subVBox.getChildren().addAll(editQueryVBox, button);

                    button.setOnAction(event1 -> {
                        if (((VBox)mainPane.getCenter()).getChildren().get(2) instanceof TableView)
                            ((VBox)mainPane.getCenter()).getChildren().remove(2);
                        table = new TableView<>();
                        mainVBox.getChildren().remove(3);
                        updateTableQuery3((VBox) mainPane.getCenter(), input.getText(), input_2.getText());
                    });
                    break;

                case "4. All books written by Burningpeak, Loni":
                    editQueryVBox = new VBox(2);
                    editQueryVBox.setPadding(new Insets(0, 10, 0, 10));
                    input = new TextField("Burningpeak");
                    input_2 = new TextField("Loni");
                    editQueryVBox.getChildren().addAll(new Label(queryToggleString.equals("Normal") ?
                            "SELECT B.Title, B.PublisherName, CONCAT(BA.AuthorLastName, '. ', BA.AuthorFirstName) as Author\n" +
                                    "FROM book B, (SELECT * \n" +
                                    "      FROM book_authors\n" +
                                    "      WHERE AuthorLastName LIKE  '%" : queryToggleString.equals("Join") ?
                            "SELECT B.Title, B.PublisherName, CONCAT(BA.AuthorLastName, '. ', BA.AuthorFirstName) as Author\n" +
                                    "FROM book B join (SELECT * \n" +
                                    "                   FROM book_authors\n" +
                                    "                   WHERE AuthorLastName LIKE  '%" : /*Subquery*/
                            "SELECT B.Title, B.PublisherName, \n" +
                                    "\t\tCONCAT(BA.AuthorLastName, \", \", BA.AuthorFirstName) as Author\n" +
                                    "\t\tFROM book B, (SELECT * \n" +
                                    "\t\t      FROM book_authors\n" +
                                    "\t\t      WHERE AuthorLastName LIKE '%"), input,
                            new Label(queryToggleString.equals("Normal") ?
                                    "%' and AuthorFirstName LIKE '%" : queryToggleString.equals("Join") ?
                                    "%' and AuthorFirstName LIKE '%" : "%' and AuthorFirstName LIKE '%"), input_2,
                            new Label(queryToggleString.equals("Normal") ?
                                    "%') as BA\n" +
                                    "WHERE BA.BookID = B.BookID\n" +
                                    "ORDER BY 1;\n" : queryToggleString.equals("Join") ?
                                        "%' ) as BA on BA.BookID = B.BookID\n" +
                                        "ORDER BY 1;"   : /*Subquery*/
                                    "%' ) as BA\n" +
                                    "WHERE BA.BookID = B.BookID\n" +
                                    "ORDER BY 1;"));
                    subVBox.getChildren().addAll(editQueryVBox, button);

                    button.setOnAction(event1 -> {
                        if (((VBox)mainPane.getCenter()).getChildren().get(2) instanceof TableView)
                            ((VBox)mainPane.getCenter()).getChildren().remove(2);
                        table = new TableView<>();
                        mainVBox.getChildren().remove(3);
                        updateTableQuery4((VBox) mainPane.getCenter(), input.getText(), input_2.getText());
                    });
                    break;

                case "5. All books which were never loaned out (nobody borrowed them)":
                    editQueryVBox = new VBox(2);
                    editQueryVBox.setPadding(new Insets(0, 10, 0, 10));
                    editQueryVBox.getChildren().addAll(new Label(queryToggleString.equals("Normal") ?
                                    "SELECT B.BookID, B.Title, CONCAT(BA.AuthorLName, \", \", " +
                                            "BA.AuthorFName) as AuthorName, B.PublisherName\n" +
                                            "FROM book B, book_authors BA\n" +
                                            "WHERE B.BookID NOT IN (SELECT BookID\n" +
                                            "                       FROM book_loans)\n" +
                                            "   and B.BookID = BA.BookID" +
                                            "GROUP BY B.BookID\n" +
                                            "ORDER BY 3, 2;\n" : queryToggleString.equals("Join") ?
                                    "SELECT B.BookID, B.Title, CONCAT(BA.AuthorLastName, \", \", BA.AuthorFirstName) as AuthorName, B.PublisherName\n" +
                                            "\tFROM book B inner join book_authors BA on \n" +
                                            "\tB.BookID NOT IN (SELECT BookID\n" +
                                            "\tFROM book_loans) and B.BookID = BA.BookID" : /*Subquery*/
                                    "SELECT B.BookID, B.Title, CONCAT(BA.AuthorLastName, \", \", BA.AuthorFirstName) as AuthorName, B.PublisherName\n" +
                                            "\t\tFROM book B inner join book_authors BA on \n" +
                                            "\t\tB.BookID NOT IN (SELECT BookID\n" +
                                            "\tFROM book_loans) and B.BookID = BA.BookID;"));
                    subVBox.getChildren().addAll(editQueryVBox, button);

                    button.setOnAction(event1 -> {
                        if (((VBox)mainPane.getCenter()).getChildren().get(2) instanceof TableView)
                            ((VBox)mainPane.getCenter()).getChildren().remove(2);
                        table = new TableView<>();
                        mainVBox.getChildren().remove(3);
                        updateTableQuery5((VBox) mainPane.getCenter());
                    });
                    break;

                case "6. All borrowers who have loaned books in their own branch":
                    editQueryVBox = new VBox(2);
                    editQueryVBox.setPadding(new Insets(0, 10, 0, 10));
                    input = new TextField("Burningpeak");
                    input_2 = new TextField("Loni");
                    editQueryVBox.getChildren().addAll(new Label(queryToggleString.equals("Normal") ?
                                    "SELECT BO.CardNo, CONCAT(BO.BorrowerLName, ', ', BO.BorrowerFName) as " +
                                            "BorrowerName, LB.BranchID, LB.BranchName, LB.BranchAddress\n" +
                                            "FROM borrower BO, book_loans BL, library_branch LB\n" +
                                            "WHERE BO.CardNo IN (SELECT CardNo \n" +
                                            "      FROM book_loans) AND BO.Address = LB.BranchAddress AND BL.BranchID = LB.BranchID\n" +
                                            "GROUP BY BorrowerName\n" +
                                            "ORDER BY 2;\n" : queryToggleString.equals("Join") ?
                                    "SELECT BO.CardNo, CONCAT(BO.BorrowerLName, \", \", BO.BorrowerFName) " +
                                            "as BorrowerName, LB.BranchID, LB.BranchName, LB.BranchAddress\n" +
                                            "FROM borrower BO join book_loans BL on BO.CardNo IN (SELECT CardNo \n" +
                                            "FROM book_loans) join library_branch LB on BO.Address = " +
                                            "LB.BranchAddress and BL.BranchID = LB.BranchID\n" : /*Subquery*/
                                    "SELECT BO.CardNo, CONCAT(BO.BorrowerLName, ', ', BO.BorrowerFName) " +
                                            "as BorrowerName, LB.BranchID, LB.BranchName, LB.BranchAddress\n" +
                                            "FROM borrower BO, book_loans BL, library_branch LB\n" +
                                            "WHERE BO.CardNo IN (SELECT CardNo \n" +
                                            "     FROM book_loans) AND BO.Address = LB.BranchAddress AND BL.BranchID = LB.BranchID"));
                    subVBox.getChildren().addAll(editQueryVBox, button);

                    button.setOnAction(event1 -> {
                        if (((VBox)mainPane.getCenter()).getChildren().get(2) instanceof TableView)
                            ((VBox)mainPane.getCenter()).getChildren().remove(2);
                        table = new TableView<>();
                        mainVBox.getChildren().remove(3);
                        updateTableQuery6((VBox) mainPane.getCenter());
                    });
                    break;

                case "7. First 100 book loans that were returned exactly on their due date":
                    editQueryVBox = new VBox(2);
                    editQueryVBox.setPadding(new Insets(0, 10, 0, 10));
                    input = new TextField("100");
                    editQueryVBox.getChildren().addAll(new Label(queryToggleString.equals("Normal") ?
                                    "SELECT CONCAT(BO.BorrowerLName, ', ', BO.BorrowerFName) AS BorrowerName, " +
                                            "BL.BookID, B.Title, CONCAT(BA.AuthorLastName, ', ', " +
                                            "BA.AuthorFirstName) as AuthorName, BL.DueDate, BL.DateReturned\n" +
                                            "FROM book B, book_authors BA, book_loans BL, borrower BO\n" +
                                            "WHERE B.BookID = BA.BookID AND BA.BookID = BL.BookID AND " +
                                            "BL.CardNo AND BL.DueDate = BL.DateReturned\n" +
                                            "LIMIT 0, " : queryToggleString.equals("Join") ?
                                    "SELECT CONCAT(BO.BorrowerFName, \", \" , BO.BorrowerLName) AS " +
                                            "BorrowerName, BL.BookID, B.Title, CONCAT(BA.AuthorLastName, \", \", " +
                                            "BA.AuthorFirstName) as AuthorName, BL.DueDate, BL.DateReturned\n" +
                                            "FROM book B join book_authors BA on B.BookID = BA.BookID join book_loans " +
                                            "BL on BA.BookID = BL.BookID join borrower BO on BL.DueDate = BL.DateReturned\n" +
                                            "LIMIT 0, " : /*Subquery*/
                                    "SELECT O.BorrowerName, A.AuthorName, D.BookID, D.DueDate, D.DateReturned\n" +
                                            "FROM (SELECT BL.BookID, BL.DateReturned, BL.DueDate, BL.CardNo\n" +
                                            "   FROM book_loans BL\n" +
                                            "   WHERE BL.DateReturned = BL.DueDate) AS D, (SELECT BA.BookID, " +
                                            "       CONCAT(BA.AuthorLastName, \", \", BA.AuthorFirstName) as AuthorName\n" +
                                            "       FROM book_authors BA) as A, (SELECT B.BookID, B.Title\n" +
                                            "       FROM book B) as B, (SELECT BO.CardNo, " +
                                            "       CONCAT(BO.BorrowerFName, \", \" , BO.BorrowerLName) AS BorrowerName\n" +
                                            "       FROM borrower BO) as O\n" +
                                            "WHERE D.BookID = A.BookID AND B.BookID = D.BookID AND O.CardNo = D.CardNo \n" +
                                            "LIMIT 0, "), input);
                    subVBox.getChildren().addAll(editQueryVBox, button);

                    button.setOnAction(event1 -> {
                        if (((VBox)mainPane.getCenter()).getChildren().get(2) instanceof TableView)
                            ((VBox)mainPane.getCenter()).getChildren().remove(2);
                        table = new TableView<>();
                        mainVBox.getChildren().remove(3);
                        updateTableQuery7((VBox) mainPane.getCenter(), input.getText());
                    });
                    break;

                case "8. Most popular title (most loaned out title) for each branch":
                    editQueryVBox = new VBox(2);
                    editQueryVBox.setPadding(new Insets(0, 10, 0, 10));
                    editQueryVBox.getChildren().addAll(new Label(queryToggleString.equals("Join") ?
                            "SELECT BL.BranchID, LB.BranchName, BL.BookID, BL.NoTimesLoaned, " +
                                    "B.Title, CONCAT(BA.AuthorLastName, \", \", BA.AuthorFirstName) as AuthorName, " +
                                    "P.PublisherName, P.Address AS PublisherAddress\n" +
                                    "FROM book  B join book_authors BA on B.BookID = BA.BookID join library_branch " +
                                    "LB join publisher P on B.PublisherName = P.PublisherName join " +
                                    "(SELECT BranchID, BookID, COUNT(*) AS NoTimesLoaned FROM book_loans " +
                                    "GROUP BY BranchID, BookID) AS BL on BL.BranchID = LB.BranchID and BL.BookID = B.BookID\n" +
                                    "join (SELECT TEMP.BranchID, MAX(TEMP.NoTimesLoaned) AS NoTimesLoaned FROM\n" +
                                    "(SELECT BranchID, BookID, COUNT(*) AS NoTimesLoaned FROM BOOK_LOANS GROUP BY " +
                                    "BranchID, BookID) AS TEMP  GROUP BY TEMP.BranchID) AS C on BL.BranchID = " +
                                    "C.BranchID AND BL.NoTimesLoaned = C.NoTimesLoaned\n" +
                                    "GROUP BY BL.BranchID\n" +
                                    "ORDER BY 2, 5;" : /*Normal & subquery*/
                            "SELECT BL.BranchID, LB.BranchName, BL.BookID, BL.NoTimesLoaned, " +
                                    "B.Title, CONCAT(BA.AuthorLastName, ', ', BA.AuthorFirstName) as " +
                                    "AuthorName, P.PublisherName, P.Address AS PublisherAddress\n" +
                                    "FROM book  B, book_authors BA, library_branch LB, publisher P, " +
                                    "(SELECT BranchID, BookID, COUNT(*) AS NoTimesLoaned\n" +
                                    "FROM book_loans\n" +
                                    "GROUP BY BranchID, BookID) AS BL,\n" +
                                    "(SELECT TEMP.BranchID,\n" +
                                    "MAX(TEMP.NoTimesLoaned)\n" +
                                    "AS NoTimesLoaned\n" +
                                    "FROM\n" +
                                    "(SELECT BranchID, BookID,\n" +
                                    "COUNT(*) AS NoTimesLoaned\n" +
                                    "FROM BOOK_LOANS\n" +
                                    "GROUP BY BranchID, BookID) AS TEMP\n" +
                                    "GROUP BY TEMP.BranchID) AS C\n" +
                                    "WHERE\n" +
                                    "BL.BranchID = C.BranchID AND\n" +
                                    "BL.NoTimesLoaned = C.NoTimesLoaned AND\n" +
                                    "BL.BranchID = LB.BranchID AND\n" +
                                    "BL.BookID = B.BookID AND\n" +
                                    "B.BookID = BA.BookID AND\n" +
                                    "B.PublisherName = P.PublisherName\n" +
                                    "GROUP BY BL.BranchID\n" +
                                    "ORDER BY 2, 5;\n"));
                    subVBox.getChildren().addAll(editQueryVBox, button);

                    button.setOnAction(event1 -> {
                        if (((VBox)mainPane.getCenter()).getChildren().get(2) instanceof TableView)
                            ((VBox)mainPane.getCenter()).getChildren().remove(2);
                        table = new TableView<>();
                        mainVBox.getChildren().remove(3);
                        updateTableQuery8((VBox) mainPane.getCenter());
                    });
                    break;
            }


        });

        queryTypeToggleGroup = new ToggleGroup();

        RadioButton normal = new RadioButton("Normal");
        normal.setToggleGroup(queryTypeToggleGroup);
        normal.setUserData("Normal");
        normal.setSelected(true);

        RadioButton join = new RadioButton("Join");
        join.setToggleGroup(queryTypeToggleGroup);
        join.setUserData("Join");

        RadioButton subquery = new RadioButton("Subquery");
        subquery.setToggleGroup(queryTypeToggleGroup);
        subquery.setUserData("Subquery");

        queryTypeToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                System.out.println(new_toggle.getUserData().toString());
                switch (new_toggle.getUserData().toString()) {
                    case "Normal": /* normal queries */
                        break;

                    case "Join":
                        break;

                    case "Subquery":
                        break;
                }
            }

        });

        VBox radioButtonVBox = new VBox(5);
        radioButtonVBox.setPadding(new Insets(0, 10, 0, 10));
        radioButtonVBox.getChildren().addAll(normal, join, subquery);

        mainVBox.getChildren().addAll(radioButtonVBox, new Separator(Orientation.HORIZONTAL), button);

        mainVBox.setMinWidth(200);
        mainVBox.setMaxWidth(500);
        return mainVBox;
    }

    public VBox initCenterVBox() {
        VBox vBox = new VBox();
        HBox hBox;

        vBox.getStyleClass().add("vBoxCenter");

        Pane field = new Pane();
        field.setId("centerPane");

        if (conn == null) {
            vBox.getChildren().add(new Label("Unable to connect to the database, check getConnection()"));
        } else {
            ImageView queryIcon = new ImageView("View/Images/queryIcon.png");
            queryIcon.setFitHeight(25);
            queryIcon.setFitWidth(25);
            queryIcon.setPreserveRatio(true);

            queryChoiceBox = new ChoiceBox<>();
            queryChoiceBox.getItems().addAll("",
                    "1. All books published by Doubleday",
                    "2. Number of books loaned in 2017",
                    "3. All borrowers who have borrowed at most 2 books",
                    "4. All books written by Burningpeak, Loni",
                    "5. All books which were never loaned out (nobody borrowed them)",
                    "6. All borrowers who have loaned books in their own branch",
                    "7. First 100 book loans that were returned exactly on their due date",
                    "8. Most popular title (most loaned out title) for each branch");
            queryChoiceBox.setValue("");

            queryChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                    restartProfiling();
                    queryNum.setText("Query #" + number2);
                    query.setText(queryChoiceBox.getItems().get((Integer) number2));
                    graphArea.getData().clear();
                    dataSeries1 = new XYChart.Series();
                    graphArea.getData().add(dataSeries1);
                    nQueryExec = 1;

                    if (vBox.getChildren().get(2) instanceof TableView)
                        vBox.getChildren().remove(2);

                    table = new TableView<>();
                    switch ((Integer) number2) {
                        case 0:
                            break;
                        case 1:
                            updateTableQuery1(vBox, "Doubleday");
                            break;
                        case 2:
                            updateTableQuery2(vBox, "2017");
                            break;
                        case 3:
                            updateTableQuery3(vBox, "0", "2");
                            break;
                        case 4:
                            updateTableQuery4(vBox, "Burningpeak", "Loni");
                            break;
                        case 5:
                            updateTableQuery5(vBox);
                            break;
                        case 6:
                            updateTableQuery6(vBox);
                            break;
                        case 7:
                            updateTableQuery7(vBox, "100");
                            break;
                        case 8:
                            updateTableQuery8(vBox);
                            break;
                        default:
                            System.out.println("Query choice box selection model not in the range");
                    }

                    System.out.println("\n" + queryChoiceBox.getItems().get((Integer) number2));
                }
            });

            queryNum.setText("Query #" + 0);
            queryNum.getStyleClass().add("headerText");
            queryNum.setAlignment(Pos.BASELINE_LEFT);

            hBox = new HBox(10);
            hBox.setAlignment(Pos.CENTER);

            Button button = new Button("Run");

            hBox.getChildren().addAll(queryIcon, queryChoiceBox, button);
            vBox.getChildren().add(0, queryNum);
            vBox.getChildren().add(1, hBox);

            button.setOnAction(e -> {
                refreshTable();
                nQueryExec += 1;
                String queryNumText = queryNum.getText();
                VBox tempvBox = new VBox();
                tempvBox.getChildren().addAll(new Label());
                tempvBox.getChildren().addAll(new Label());
                table = new TableView<>();

                switch (queryNumText.charAt(queryNumText.length() - 1)) {
                    case '0':
                        break;
                    case '1':
                        updateTableQuery1(tempvBox, "Doubleday");
                        break;
                    case '2':
                        updateTableQuery2(tempvBox, "2017");
                        break;
                    case '3':
                        updateTableQuery3(tempvBox, "0", "2");
                        break;
                    case '4':
                        updateTableQuery4(tempvBox, "Burningpeak", "Loni");
                        break;
                    case '5':
                        updateTableQuery5(tempvBox);
                        break;
                    case '6':
                        updateTableQuery6(tempvBox);
                        break;
                    case '7':
                        updateTableQuery7(tempvBox, "100");
                        break;
                    case '8':
                        updateTableQuery8(tempvBox);
                        break;
                    default:
                        System.out.println("Repeating Query #" + queryNumText.charAt(queryNumText.length() - 1));
                }
            });

            NumberAxis xAxis = new NumberAxis();
            xAxis.setLabel("Execution Number");

            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Time Processed");

            graphArea = new LineChart(xAxis, yAxis);
            graphArea.setId("graphArea");
            graphArea.setLegendVisible(false);
            vBox.getChildren().add(graphArea);
        }

        return vBox;
    }

    // Irrelevant query to refresh stored table
    private void refreshTable() {

        Statement st = null;
        ResultSet rs = null;
        String query = "SELECT * FROM Book B";
        System.out.println("\n\n ... \n\n");
        nQueryExec += 1;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(query);
            st.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }// catch (ClassNotFoundException e){
    }

    private void restartProfiling() {

        Statement st = null;
        try {

            st = conn.createStatement();
            st.executeQuery("    SET @@profiling = 0;");
            st.executeQuery("SET @@profiling_history_size = 0;");
            st.executeQuery("SET @@profiling_history_size = 100;");
            st.executeQuery("SET @@profiling = 1;");

            st.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }
    }

    private BigDecimal getQueryProcessTime(int i) {

        Statement st = null;
        ResultSet rs = null;

        BigDecimal time = BigDecimal.valueOf(0);
        try {

            st = conn.createStatement();
            rs = st.executeQuery("SHOW PROFILES ");

            int row = 1;
            while (rs.next()) {
                System.out.println("Line #: " + i + " || Row #:" + row +
                                   " || Time: " + rs.getFloat("Duration"));
                if (row == i)
                    time = rs.getBigDecimal("Duration");
                row++;
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }// catch (ClassNotFoundException e){

        return time;
    }

    // Update table queries
    private void updateTableQuery1(VBox vBox, String filterName) {
        TableColumn<ArrayList<String>, String> titleCol = new TableColumn<>("Title");

        titleCol.setMinWidth(100);
        titleCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(0));
        });

        TableColumn<ArrayList<String>, String> pubNameCol = new TableColumn<>("PublisherName");
        pubNameCol.setMinWidth(100);
        pubNameCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(1));
        });

        table.setItems(getQuery1(filterName));
        table.getColumns().addAll(titleCol, pubNameCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        vBox.getChildren().add(2, table);

        BigDecimal processTime = getQueryProcessTime(nQueryExec);
        // clear current data
        dataSeries1.getData().clear();

        // add new data
        dataSeries1.getData().add(new XYChart.Data(nQueryExec - nQueryExec/2, processTime));

    }

    private void updateTableQuery2(VBox vBox, String filterNum) {
        TableColumn<ArrayList<String>, String> noBooksBorCol = new TableColumn<>("NoBooksBor");
        noBooksBorCol.setMinWidth(100);
        noBooksBorCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(0));
        });
        table.setItems(getQuery2(filterNum));
        table.getColumns().addAll(noBooksBorCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        BigDecimal processTime = getQueryProcessTime(nQueryExec);
// clear current data
        dataSeries1.getData().clear();

        // add new data
        dataSeries1.getData().add(new XYChart.Data(nQueryExec - nQueryExec/2, processTime));

        vBox.getChildren().add(2, table);
    }

    private void updateTableQuery3(VBox vBox, String filter1, String filter2) {
        TableColumn<ArrayList<String>, String> nameCol = new TableColumn<>("BorrowerName");
        nameCol.setMinWidth(100);
        nameCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(0));
        });

        //Starting Point Column
        TableColumn<ArrayList<String>, String> noBooksBorCol = new TableColumn<>("NoBooksBor");
        noBooksBorCol.setMinWidth(100);
        noBooksBorCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(1));
        });


        table.setItems(getQuery3(filter1, filter2));
        table.getColumns().addAll(nameCol, noBooksBorCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        BigDecimal processTime = getQueryProcessTime(nQueryExec);
        // clear current data
        dataSeries1.getData().clear();

        // add new data
        dataSeries1.getData().add(new XYChart.Data(nQueryExec - nQueryExec/2, processTime));

        vBox.getChildren().add(2, table);
    }

    private void updateTableQuery4(VBox vBox, String filter1, String filter2) {
        TableColumn<ArrayList<String>, String> titleCol = new TableColumn<>("Title");
        titleCol.setMinWidth(100);
        titleCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(0));
        });

        TableColumn<ArrayList<String>, String> pubNameCol = new TableColumn<>("PublisherName");
        pubNameCol.setMinWidth(100);
        pubNameCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(1));
        });

        TableColumn<ArrayList<String>, String> authorCol = new TableColumn<>("Author");
        authorCol.setMinWidth(100);
        authorCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(2));
        });

        table.setItems(getQuery4(filter1, filter2));
        table.getColumns().addAll(titleCol, pubNameCol, authorCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        BigDecimal processTime = getQueryProcessTime(nQueryExec);
        System.out.println((nQueryExec - nQueryExec/2) + " || " + processTime);

        // clear current data
        dataSeries1.getData().clear();

        // add new data
        dataSeries1.getData().add(new XYChart.Data(nQueryExec - nQueryExec/2, processTime));
        vBox.getChildren().add(2, table);
    }

    private void updateTableQuery5(VBox vBox) {
        TableColumn<ArrayList<String>, String> bookIdCol = new TableColumn<>("BookID");
        bookIdCol.setMinWidth(100);
        bookIdCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(0));
        });

        TableColumn<ArrayList<String>, String> titleCol = new TableColumn<>("Title");
        titleCol.setMinWidth(100);
        titleCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(1));
        });

        TableColumn<ArrayList<String>, String> authorCol = new TableColumn<>("AuthorName");
        authorCol.setMinWidth(100);
        authorCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(2));
        });

        TableColumn<ArrayList<String>, String> pubCol = new TableColumn<>("PublisherName");
        pubCol.setMinWidth(100);
        pubCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(3));
        });


        table.setItems(getQuery5());
        table.getColumns().addAll(bookIdCol, titleCol, authorCol, pubCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        BigDecimal processTime = getQueryProcessTime(nQueryExec);
// clear current data
        dataSeries1.getData().clear();

        // add new data
        dataSeries1.getData().add(new XYChart.Data(nQueryExec - nQueryExec/2, processTime));

        vBox.getChildren().add(2, table);
    }

    private void updateTableQuery6(VBox vBox) {
        TableColumn<ArrayList<String>, String> cardNoCol = new TableColumn<>("CardNo");
        cardNoCol.setMinWidth(100);
        cardNoCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(0));
        });

        TableColumn<ArrayList<String>, String> borrowerNameCol = new TableColumn<>("BorrowerName");
        borrowerNameCol.setMinWidth(100);
        borrowerNameCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(1));
        });
        TableColumn<ArrayList<String>, String> branchIdCol = new TableColumn<>("BranchID");
        branchIdCol.setMinWidth(100);
        branchIdCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(2));
        });

        //Starting Point Column
        TableColumn<ArrayList<String>, String> branchNameCol = new TableColumn<>("BranchName");
        branchNameCol.setMinWidth(100);
        branchNameCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(3));
        });

        TableColumn<ArrayList<String>, String> branchAddressCol = new TableColumn<>("BranchAddress");
        branchAddressCol.setMinWidth(100);
        branchAddressCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(4));
        });


        table.setItems(getQuery6());
        table.getColumns().addAll(cardNoCol, borrowerNameCol, branchIdCol, branchNameCol, branchAddressCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        BigDecimal processTime = getQueryProcessTime(nQueryExec);
// clear current data
        dataSeries1.getData().clear();

        // add new data
        dataSeries1.getData().add(new XYChart.Data(nQueryExec - nQueryExec/2, processTime));

        vBox.getChildren().add(2, table);
    }

    private void updateTableQuery7(VBox vBox, String filter) {
        TableColumn<ArrayList<String>, String> nameCol = new TableColumn<>("BorrowerName");
        nameCol.setMinWidth(100);
        nameCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(0));
        });

        TableColumn<ArrayList<String>, String> idCol = new TableColumn<>("BookID");
        idCol.setMinWidth(100);
        idCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(1));
        });

        TableColumn<ArrayList<String>, String> titleCol = new TableColumn<>("Title");
        titleCol.setMinWidth(100);
        titleCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(2));
        });

        TableColumn<ArrayList<String>, String> authorCol = new TableColumn<>("AuthorName");
        authorCol.setMinWidth(100);
        authorCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(3));
        });

        TableColumn<ArrayList<String>, String> dueDateCol = new TableColumn<>("DueDate");
        dueDateCol.setMinWidth(100);
        dueDateCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(4));
        });

        TableColumn<ArrayList<String>, String> dateReturnedCol = new TableColumn<>("DateReturned");
        dateReturnedCol.setMinWidth(100);
        dateReturnedCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(5));
        });


        table.setItems(getQuery7(filter));
        table.getColumns().addAll(nameCol, idCol, titleCol, authorCol, dueDateCol, dateReturnedCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        BigDecimal processTime = getQueryProcessTime(nQueryExec);
// clear current data
        dataSeries1.getData().clear();

        // add new data
        dataSeries1.getData().add(new XYChart.Data(nQueryExec - nQueryExec/2, processTime));

        vBox.getChildren().add(2, table);
    }

    private void updateTableQuery8(VBox vBox) {

        TableColumn<ArrayList<String>, String> idCol = new TableColumn<>("BranchID");
        idCol.setMinWidth(100);
        idCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(0));
        });

        TableColumn<ArrayList<String>, String> branchNameCol = new TableColumn<>("BranchName");
        branchNameCol.setMinWidth(100);
        branchNameCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(1));
        });

        TableColumn<ArrayList<String>, String> bookIdCol = new TableColumn<>("BookID");
        bookIdCol.setMinWidth(100);
        bookIdCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(2));
        });

        TableColumn<ArrayList<String>, String> noTimesLoanedCol = new TableColumn<>("NoTimesLoaned");
        noTimesLoanedCol.setMinWidth(100);
        noTimesLoanedCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(3));
        });

        TableColumn<ArrayList<String>, String> titleCol = new TableColumn<>("Title");
        titleCol.setMinWidth(100);
        titleCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(4));
        });

        TableColumn<ArrayList<String>, String> authorCol = new TableColumn<>("AuthorName");
        authorCol.setMinWidth(100);
        authorCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(5));
        });

        TableColumn<ArrayList<String>, String> publisherNameCol = new TableColumn<>("PublisherName");
        publisherNameCol.setMinWidth(100);
        publisherNameCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(6));
        });
        TableColumn<ArrayList<String>, String> publisherAddressCol = new TableColumn<>("PublisherAddress");
        publisherAddressCol.setMinWidth(100);
        publisherAddressCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(7));
        });

        table.setItems(getQuery8());
        table.getColumns().addAll(idCol, branchNameCol, bookIdCol, noTimesLoanedCol, titleCol, authorCol, publisherNameCol, publisherAddressCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        BigDecimal processTime = getQueryProcessTime(nQueryExec);
// clear current data
        dataSeries1.getData().clear();

        // add new data
        dataSeries1.getData().add(new XYChart.Data(nQueryExec - nQueryExec/2, processTime));

        vBox.getChildren().add(2, table);
    }

    // Get queries

    public ObservableList<ArrayList<String>> getQuery1(String filterName) {
        //Connection conn = getConnection();	called at the start
        Statement st = null;
        ResultSet rs = null;
        String query = "SELECT Title, PublisherName\n" +
                        "FROM book\n" +
                        "WHERE  PublisherName like '%" + filterName + "%'\n" +
                        "ORDER BY PublisherName;\n";

        ObservableList<ArrayList<String>> arrayList = FXCollections.observableArrayList();

        try {

            st = conn.createStatement();
            rs = st.executeQuery(query);

            while (rs.next()) {
                ArrayList<String> rowData = new ArrayList<>();
                rowData.add(rs.getString("Title"));
                rowData.add(rs.getString("PublisherName"));
                arrayList.add(rowData);
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }// catch (ClassNotFoundException e){

        return arrayList;
    }

    public ObservableList<ArrayList<String>> getQuery2(String filterNum) {

        //Connection conn = getConnection();	called at the start
        Statement st = null;
        ResultSet rs = null;
        String query = "SELECT COUNT(*) AS NoBooksBor\n" +
                        "FROM book_loans\n" +
                        "WHERE YEAR(DateOut) = " + filterNum;

        ObservableList<ArrayList<String>> arrayList = FXCollections.observableArrayList();

        try {

            st = conn.createStatement();
            rs = st.executeQuery(query);

            while (rs.next()) {
                ArrayList<String> rowData = new ArrayList<>();
                rowData.add(rs.getString("NoBooksBor"));
                arrayList.add(rowData);
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }// catch (ClassNotFoundException e){

        return arrayList;
    }

    public ObservableList<ArrayList<String>> getQuery3(String f1, String f2) {

        //Connection conn = getConnection();	called at the start
        Statement st = null;
        ResultSet rs = null;
        String queryToggleString = queryTypeToggleGroup.getSelectedToggle().getUserData().toString();
        String query =  queryToggleString.equals("Normal") ?
                        "SELECT CONCAT(BO.BorrowerLName, ', ', BO.BorrowerFName) AS BorrowerName, COUNT(*) as NoBooksBor\n" +
                        "FROM borrower BO, book_loans BL\n" +
                        "WHERE BO.CardNo = BL.CardNo\n" +
                        "GROUP BY BorrowerName\n" +
                        "HAVING NoBooksBor >= " + f1 + " and NoBooksBor <= " + f2 + "\n" +
                        "ORDER BY 2 DESC, 1;\n" : queryToggleString.equals("Join") ?
                            "SELECT CONCAT(BO.BorrowerLName, ', ', BO.BorrowerFName) as BorrowerName , COUNT(*) as NoBooksBor\n" +
                            "\tFROM borrower BO join book_loans BL on BO.CardNo = BL.CardNo\n" +
                            "\tGROUP BY BorrowerName\n" +
                            "HAVING NoBooksBor >= " + f1 + " and NoBooksBor <= " + f2 + "\n" +
                            "\tORDER BY 2 DESC, 1;" : /*SubQuery*/
                                "SELECT CONCAT(BorrowerLName, \", \", BorrowerFName) as BorrowerName, BO2.NoBooksBor\n" +
                                "\tFROM borrower, (SELECT BL.CardNo, COUNT(CardNo) as NoBooksBor\n" +
                                "\t\t\t\t\tFROM book_loans BL\n" +
                                "\t\t\t\t\tGROUP BY BL.CardNo\n" +
                                "\t\t\t\t\tHAVING COUNT(CardNo) >= " + f1 + " and COUNT(CardNo) <= " + f2 + "\n" +
                                "\t\t\t\t\tORDER BY 1) as BO2\n" +
                                "WHERE borrower.CardNo = BO2.CardNo\n" +
                                "\tORDER BY 2 DESC;";

        ObservableList<ArrayList<String>> arrayList = FXCollections.observableArrayList();

        try {

            st = conn.createStatement();
            rs = st.executeQuery(query);

            while (rs.next()) {
                ArrayList<String> rowData = new ArrayList<>();
                rowData.add(rs.getString("BorrowerName"));
                rowData.add(rs.getString("NoBooksBor"));
                arrayList.add(rowData);
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }// catch (ClassNotFoundException e){

        return arrayList;
    }

    public ObservableList<ArrayList<String>> getQuery4(String f1, String f2) {

        Statement st = null;
        ResultSet rs = null;
        String queryToggleString = queryTypeToggleGroup.getSelectedToggle().getUserData().toString();
        String query =  queryToggleString.equals("Normal") ?
                        "SELECT B.Title, B.PublisherName, CONCAT(BA.AuthorLastName, '. ', BA.AuthorFirstName) as Author\n" +
                                "FROM book B, (SELECT * \n" +
                                "      FROM book_authors\n" +
                                "      WHERE AuthorLastName LIKE  '%" + f1 + "%' and " +
                                "      AuthorFirstName LIKE '%" + f2 + "%') as BA\n" +
                                "WHERE BA.BookID = B.BookID\n" +
                                "ORDER BY 1;\n" : queryToggleString.equals("Join") ?
                        "SELECT B.Title, B.PublisherName, CONCAT(BA.AuthorLastName, '. ', BA.AuthorFirstName) as Author\n" +
                                "FROM book B join (SELECT * \n" +
                                "                   FROM book_authors\n" +
                                "                   WHERE AuthorLastName LIKE '%" + f1 + "%' and " +
                                "                   AuthorFirstName LIKE '%" + f2 + "%' ) as BA on BA.BookID = B.BookID\n" +
                                "ORDER BY 1;"   : /*Subquery*/
                        "SELECT B.Title, B.PublisherName, \n" +
                                "\t\tCONCAT(BA.AuthorLastName, \", \", BA.AuthorFirstName) as Author\n" +
                                "\t\tFROM book B, (SELECT * \n" +
                                "\t\t      FROM book_authors\n" +
                                "\t\t      WHERE AuthorLastName LIKE '%" + f1 + "%' and AuthorFirstName LIKE '%" + f2 + "%' ) as BA\n" +
                                "\t\tWHERE BA.BookID = B.BookID\n" +
                                "\t\tORDER BY 1;";

        ObservableList<ArrayList<String>> arrayList = FXCollections.observableArrayList();

        try {

            st = conn.createStatement();
            rs = st.executeQuery(query);

            while (rs.next()) {
                ArrayList<String> rowData = new ArrayList<>();
                rowData.add(rs.getString("Title"));
                rowData.add(rs.getString("PublisherName"));
                rowData.add(rs.getString("Author"));
                arrayList.add(rowData);
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }// catch (ClassNotFoundException e){

        return arrayList;
    }

    public ObservableList<ArrayList<String>> getQuery5() {

        //Connection conn = getConnection();	called at the start
        Statement st = null;
        ResultSet rs = null;
        String queryToggleString = queryTypeToggleGroup.getSelectedToggle().getUserData().toString();
        String query =  queryToggleString.equals("Normal") ?
                        "SELECT B.BookID, B.Title, CONCAT(BA.AuthorLastName, \", \", BA.AuthorFirstName) as AuthorName, B.PublisherName\n" +
                        "FROM book B, book_authors BA\n" +
                        "WHERE B.BookID NOT IN (SELECT BookID\n" +
                        "                                              FROM book_loans) and B.BookID = BA.BookID\n" +
                        "GROUP BY B.BookID\n" +
                        "ORDER BY 3, 2;" : queryToggleString.equals("Join") ?
                            "SELECT B.BookID, B.Title, CONCAT(BA.AuthorLastName, \", \", BA.AuthorFirstName) as AuthorName, B.PublisherName\n" +
                            "\tFROM book B inner join book_authors BA on \n" +
                            "\tB.BookID NOT IN (SELECT BookID\n" +
                            "\tFROM book_loans) and B.BookID = BA.BookID" : /*Subquery*/
                                "SELECT B.BookID, B.Title, CONCAT(BA.AuthorLastName, \", \", BA.AuthorFirstName) as AuthorName, B.PublisherName\n" +
                                "\t\tFROM book B inner join book_authors BA on \n" +
                                "\t\tB.BookID NOT IN (SELECT BookID\n" +
                                "\tFROM book_loans) and B.BookID = BA.BookID;";

        ObservableList<ArrayList<String>> arrayList = FXCollections.observableArrayList();

        try {

            st = conn.createStatement();
            rs = st.executeQuery(query);

            while (rs.next()) {
                ArrayList<String> rowData = new ArrayList<>();
                rowData.add(rs.getInt("BookID") + "");
                rowData.add(rs.getString("Title"));
                rowData.add(rs.getString("AuthorName"));
                rowData.add(rs.getString("PublisherName"));
                arrayList.add(rowData);
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }// catch (ClassNotFoundException e){

        return arrayList;
    }

    public ObservableList<ArrayList<String>> getQuery6() {

        //Connection conn = getConnection();	called at the start
        Statement st = null;
        ResultSet rs = null;
        String queryToggleString = queryTypeToggleGroup.getSelectedToggle().getUserData().toString();
        String query =  queryToggleString.equals("Normal") ?
                        "SELECT BO.CardNo, CONCAT(BO.BorrowerLName, ', ', BO.BorrowerFName) as " +
                        "BorrowerName, LB.BranchID, LB.BranchName, LB.BranchAddress\n" +
                        "FROM borrower BO, book_loans BL, library_branch LB\n" +
                        "WHERE BO.CardNo IN (SELECT CardNo \n" +
                        "      FROM book_loans) AND BO.Address = LB.BranchAddress AND BL.BranchID = LB.BranchID\n" +
                        "GROUP BY BorrowerName\n" +
                        "ORDER BY 2;\n" : queryToggleString.equals("Join") ?
                            "SELECT BO.CardNo, CONCAT(BO.BorrowerLName, \", \", BO.BorrowerFName) " +
                            "as BorrowerName, LB.BranchID, LB.BranchName, LB.BranchAddress\n" +
                            "FROM borrower BO join book_loans BL on BO.CardNo IN (SELECT CardNo \n" +
                            "FROM book_loans) join library_branch LB on BO.Address = " +
                            "LB.BranchAddress and BL.BranchID = LB.BranchID\n" : /*Subquery*/
                                "SELECT BO.CardNo, CONCAT(BO.BorrowerLName, ', ', BO.BorrowerFName) " +
                                "as BorrowerName, LB.BranchID, LB.BranchName, LB.BranchAddress\n" +
                                "FROM borrower BO, book_loans BL, library_branch LB\n" +
                                "WHERE BO.CardNo IN (SELECT CardNo \n" +
                                "     FROM book_loans) AND BO.Address = LB.BranchAddress AND BL.BranchID = LB.BranchID";

        ObservableList<ArrayList<String>> arrayList = FXCollections.observableArrayList();

        try {

            st = conn.createStatement();
            rs = st.executeQuery(query);

            while (rs.next()) {
                ArrayList<String> rowData = new ArrayList<>();
                rowData.add(rs.getInt("CardNo") + "");
                rowData.add(rs.getString("BorrowerName"));
                rowData.add(rs.getInt("BranchID") + "");
                rowData.add(rs.getString("BranchName"));
                rowData.add(rs.getString("BranchAddress"));
                arrayList.add(rowData);
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }// catch (ClassNotFoundException e){

        return arrayList;
    }

    public ObservableList<ArrayList<String>> getQuery7(String filter) {

        //Connection conn = getConnection();	called at the start
        Statement st = null;
        ResultSet rs = null;
        String queryToggleString = queryTypeToggleGroup.getSelectedToggle().getUserData().toString();
        String query =  queryToggleString.equals("Normal") ?
                        "SELECT CONCAT(BO.BorrowerLName, ', ', BO.BorrowerFName) AS BorrowerName, " +
                        "BL.BookID, B.Title, CONCAT(BA.AuthorLastName, ', ', " +
                        "BA.AuthorFirstName) as AuthorName, BL.DueDate, BL.DateReturned\n" +
                        "FROM book B, book_authors BA, book_loans BL, borrower BO\n" +
                        "WHERE B.BookID = BA.BookID AND BA.BookID = BL.BookID AND " +
                        "BL.CardNo AND BL.DueDate = BL.DateReturned\n" +
                        "LIMIT 0, " + filter : queryToggleString.equals("Join") ?
                            "SELECT CONCAT(BO.BorrowerFName, \", \" , BO.BorrowerLName) AS " +
                            "BorrowerName, BL.BookID, B.Title, CONCAT(BA.AuthorLastName, \", \", " +
                            "BA.AuthorFirstName) as AuthorName, BL.DueDate, BL.DateReturned\n" +
                            "FROM book B join book_authors BA on B.BookID = BA.BookID join book_loans " +
                            "BL on BA.BookID = BL.BookID join borrower BO on BL.DueDate = BL.DateReturned\n" +
                            "LIMIT 0, " + filter: /*Subquery*/
                                "SELECT O.BorrowerName, A.AuthorName, D.BookID, D.DueDate, D.DateReturned\n" +
                                "FROM (SELECT BL.BookID, BL.DateReturned, BL.DueDate, BL.CardNo\n" +
                                "   FROM book_loans BL\n" +
                                "   WHERE BL.DateReturned = BL.DueDate) AS D, (SELECT BA.BookID, " +
                                "       CONCAT(BA.AuthorLastName, \", \", BA.AuthorFirstName) as AuthorName\n" +
                                "       FROM book_authors BA) as A, (SELECT B.BookID, B.Title\n" +
                                "       FROM book B) as B, (SELECT BO.CardNo, " +
                                "       CONCAT(BO.BorrowerFName, \", \" , BO.BorrowerLName) AS BorrowerName\n" +
                                "       FROM borrower BO) as O\n" +
                                "WHERE D.BookID = A.BookID AND B.BookID = D.BookID AND O.CardNo = D.CardNo \n" +
                                "LIMIT 0, " + filter;

        ObservableList<ArrayList<String>> arrayList = FXCollections.observableArrayList();

        try {

            st = conn.createStatement();
            rs = st.executeQuery(query);

            while (rs.next()) {
                ArrayList<String> rowData = new ArrayList<>();
                rowData.add(rs.getString("BorrowerName"));
                rowData.add(rs.getInt("BookID") + "");
                rowData.add(rs.getString("Title"));
                rowData.add(rs.getString("AuthorName"));
                rowData.add(rs.getString("DueDate"));
                rowData.add(rs.getString("DateReturned"));
                arrayList.add(rowData);
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }// catch (ClassNotFoundException e){

        return arrayList;
    }

    public ObservableList<ArrayList<String>> getQuery8() {
        //Connection conn = getConnection();	called at the start
        Statement st = null;
        ResultSet rs = null;
        String queryToggleString = queryTypeToggleGroup.getSelectedToggle().getUserData().toString();
        String query =  queryToggleString.equals("Join") ?
                        "SELECT BL.BranchID, LB.BranchName, BL.BookID, BL.NoTimesLoaned, " +
                        "B.Title, CONCAT(BA.AuthorLastName, \", \", BA.AuthorFirstName) as AuthorName, " +
                        "P.PublisherName, P.Address AS PublisherAddress\n" +
                        "FROM book  B join book_authors BA on B.BookID = BA.BookID join library_branch " +
                        "LB join publisher P on B.PublisherName = P.PublisherName join " +
                        "(SELECT BranchID, BookID, COUNT(*) AS NoTimesLoaned FROM book_loans " +
                        "GROUP BY BranchID, BookID) AS BL on BL.BranchID = LB.BranchID and BL.BookID = B.BookID\n" +
                        "join (SELECT TEMP.BranchID, MAX(TEMP.NoTimesLoaned) AS NoTimesLoaned FROM\n" +
                        "(SELECT BranchID, BookID, COUNT(*) AS NoTimesLoaned FROM BOOK_LOANS GROUP BY " +
                        "BranchID, BookID) AS TEMP  GROUP BY TEMP.BranchID) AS C on BL.BranchID = " +
                        "C.BranchID AND BL.NoTimesLoaned = C.NoTimesLoaned\n" +
                        "GROUP BY BL.BranchID\n" +
                        "ORDER BY 2, 5;" : /*Normal & subquery*/
                           "SELECT BL.BranchID, LB.BranchName, BL.BookID, BL.NoTimesLoaned, " +
                            "B.Title, CONCAT(BA.AuthorLastName, ', ', BA.AuthorFirstName) as " +
                            "AuthorName, P.PublisherName, P.Address AS PublisherAddress\n" +
                            "FROM book  B, book_authors BA, library_branch LB, publisher P, " +
                            "(SELECT BranchID, BookID, COUNT(*) AS NoTimesLoaned\n" +
                            "FROM book_loans\n" +
                            "GROUP BY BranchID, BookID) AS BL,\n" +
                            "(SELECT TEMP.BranchID,\n" +
                            "MAX(TEMP.NoTimesLoaned)\n" +
                            "AS NoTimesLoaned\n" +
                            "FROM\n" +
                            "(SELECT BranchID, BookID,\n" +
                            "COUNT(*) AS NoTimesLoaned\n" +
                            "FROM BOOK_LOANS\n" +
                            "GROUP BY BranchID, BookID) AS TEMP\n" +
                            "GROUP BY TEMP.BranchID) AS C\n" +
                            "WHERE\n" +
                            "BL.BranchID = C.BranchID AND\n" +
                            "BL.NoTimesLoaned = C.NoTimesLoaned AND\n" +
                            "BL.BranchID = LB.BranchID AND\n" +
                            "BL.BookID = B.BookID AND\n" +
                            "B.BookID = BA.BookID AND\n" +
                            "B.PublisherName = P.PublisherName\n" +
                            "GROUP BY BL.BranchID\n" +
                            "ORDER BY 2, 5;\n";

        ObservableList<ArrayList<String>> arrayList = FXCollections.observableArrayList();

        try {

            st = conn.createStatement();
            rs = st.executeQuery(query);

            while (rs.next()) {
                ArrayList<String> rowData = new ArrayList<>();
                rowData.add(rs.getInt("BranchID") + "");
                rowData.add(rs.getString("BranchName"));
                rowData.add(rs.getInt("BookID") + "");
                rowData.add(rs.getInt("NoTimesLoaned") + "");
                rowData.add(rs.getString("Title"));
                rowData.add(rs.getString("AuthorName"));
                rowData.add(rs.getString("PublisherName"));
                rowData.add(rs.getString("PublisherAddress"));
                arrayList.add(rowData);

            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }// catch (ClassNotFoundException e){

        return arrayList;
    }


    private void deleteAllIndex() {
        //Connection conn = getConnection();	called at the start
        Statement st = null;
        Statement st2 = null;
        ResultSet rs = null;
        String query = "SELECT DISTINCT\n" +
                        "    TABLE_NAME,\n" +
                        "    INDEX_NAME\n" +
                        "FROM INFORMATION_SCHEMA.STATISTICS\n" +
                        "WHERE TABLE_SCHEMA = 'mco1_db' AND INDEX_NAME != 'PRIMARY';";

        try {

            st = conn.createStatement();
            st2 = conn.createStatement();
            rs = st.executeQuery(query);
            while (rs.next()) {
                query = "DROP INDEX " + rs.getString("INDEX_NAME") + " ON " + rs.getString("TABLE_NAME");
                st2.executeUpdate(query);
                System.out.println("Deleted index: " + rs.getString("INDEX_NAME"));
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }// catch (ClassNotFoundException e){
    }

    private void terminateProgram() {
        if (conn != null)
            try {
                deleteAllIndex();
                conn.close();
            } catch (SQLException e) {
                System.out.println("SQLException: " + e.getMessage());
            }
        System.out.println("\nProgram has been terminated.");
        Platform.exit();
        System.exit(0);
    }

    public Connection getConnection() {
        Connection tempConn = null;

        String driver = "com.mysql.jdbc.Driver";
        String db = "mco1_db";
        String url = "jdbc:mysql://localhost/" + db + "?useSSL=false";
        String user = "root";
        String pass = "1234";

        try {
            Class.forName(driver);
            tempConn = DriverManager.getConnection(url, user, pass);
            System.out.println("Connected to database : " + db);
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException");
        }

        return tempConn;
    }

    /* Filters the table *************************************/
//    public EventHandler<KeyEvent> filterRoutesTable() {
//        return new EventHandler<KeyEvent>() {
//            @Override
//            @SuppressWarnings("unchecked")
//            public void handle(KeyEvent e) {
//                ObservableList<Route> routes = FXCollections.observableArrayList();
//
//                //Connection conn = getConnection();	called at the start
//                Statement st    = null;
//                ResultSet rs    = null;
//                String query = new String(" ");
//                TextField text = (TextField) e.getSource();
//                if(userRider){
//                    query = "SELECT l.landmarkName as `Starting Point`, l2.landmarkName as `End Point`, r.mins as `Minutes`, r.fare as `Fare` " +
//                            "FROM landmarks l, landmarks l2, route r " +
//                            "WHERE `r`.pickupLoc = l.landmarkID AND r.dropoffLoc = l2.landmarkId " +
//                            "AND l.landmarkName LIKE '%" + startingTextField.getText() + "%'" +
//                            "AND l2.landmarkName LIKE '%" + endingTextField.getText() + "%'" +
//                            "AND r.mins LIKE '%" + minutesTextField.getText() + "%'" +
//                            "AND r.fare LIKE '%" + fareTextField.getText() + "%';";
//                }
//                else
//                    query = "SELECT l.landmarkName as `Starting Point`, l2.landmarkName as `End Point`, r.mins as `Minutes`, r.fare as `Fare` " +
//                            ", CONCAT(ri.firstName, \" \", ri.lastName) AS `Rider Name`, ri.rating AS `Rating` " +
//                            "FROM landmarks l, landmarks l2, route r, riderInfo ri, transaction t " +
//                            "WHERE r.pickupLoc = l.landmarkID AND r.dropoffLoc = l2.landmarkId AND " +
//                            "t.routeId = r.routeId AND ri.riderId = t.riderId AND t.driverId = 0 "+
//                            "AND l.landmarkName LIKE '%" + startingTextField.getText() + "%'" +
//                            "AND l2.landmarkName LIKE '%" + endingTextField.getText() + "%'" +
//                            "AND r.mins LIKE '%" + minutesTextField.getText() + "%'" +
//                            "AND r.fare LIKE '%" + fareTextField.getText() + "%'" +
//                            "AND CONCAT(ri.firstName, \" \", ri.lastName) LIKE '%" + riderTextField.getText() + "%'" +
//                            "AND ri.rating LIKE '%" + ratingTextField.getText() + "%'";
//
//                try {
//
//                    st = conn.createStatement();
//                    rs = st.executeQuery(query);
//
//                    while(rs.next()){
//                        Route r = new Route(rs.getString("Starting Point"), rs.getString("End Point"),
//                                rs.getInt("Minutes"), rs.getInt("Fare"));
//                        if(!userRider){
//                            r.setRider(rs.getString("Rider Name"));
//                            r.setRating(rs.getString("Rating"));
//                        }
//
//                        routes.add(r);
//                    }
//                    st.close();
//                    rs.close();
//                } catch (SQLException e2) {
//                    System.out.println("SQLException: " + e2.getMessage());
//                    System.out.println("SQLState: "		+ e2.getSQLState());
//                    System.out.println("VendorError: "  + e2.getErrorCode());
//                }
//
//                //Starting Point Column
//                TableColumn<Route, String> startingColumn= new TableColumn<>("Starting Point");
//                startingColumn.setMinWidth(100);
//                startingColumn.setCellValueFactory(new PropertyValueFactory<>("start"));
//
//                //End Point Column
//                TableColumn<Route, String> endColumn= new TableColumn<>("End Point");
//                endColumn.setMinWidth(100);
//                endColumn.setCellValueFactory(new PropertyValueFactory<>("end"));
//
//                //Minutes Column
//                TableColumn<Route, String> minutesColumn= new TableColumn<>("Minutes");
//                minutesColumn.setMinWidth(75);
//                minutesColumn.setCellValueFactory(new PropertyValueFactory<>("Minutes"));
//
//                //Fare Column
//                TableColumn<Route, String> fareColumn= new TableColumn<>("Fare");
//                fareColumn.setMinWidth(75);
//                fareColumn.setCellValueFactory(new PropertyValueFactory<>("fare"));
//
//                if(userRider){
//                    filterVBox.getChildren().remove(routesTable);
//                    routesTable = new TableView<Route>();
//                    routesTable.setItems(routes);
//                    routesTable.getColumns().addAll(startingColumn, endColumn, minutesColumn, fareColumn);
//                    routesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
//                    filterVBox.getChildren().add(0, routesTable);
//                }
//
//                else {
//                    //Rider Info Column
//                    TableColumn<Route, String> riderInfoColumn= new TableColumn<>("Rider Name");
//                    riderInfoColumn.setMinWidth(75);
//                    riderInfoColumn.setCellValueFactory(new PropertyValueFactory<>("rider"));
//
//                    //Rating Column
//                    TableColumn<Route, String> ratingColumn= new TableColumn<>("Rating");
//                    ratingColumn.setMinWidth(75);
//                    ratingColumn.setCellValueFactory(new PropertyValueFactory<>("Rating"));
//
//                    filterVBox.getChildren().remove(driverTable);
//                    driverTable = new TableView<Route>();
//                    driverTable.setItems(routes);
//                    driverTable.getColumns().addAll(startingColumn, endColumn, minutesColumn, fareColumn, riderInfoColumn, ratingColumn);
//                    driverTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
//                    filterVBox.getChildren().add(0, driverTable);
//                }
//            }
//        };
//    }
    /*****************************************************************************************/

	/* Numeric Validation Limit the  characters to maxLength AND to ONLY DigitS *************************************/
//    public EventHandler<KeyEvent> numeric_Validation(final Integer max_Length) {
//        return new EventHandler<KeyEvent>() {
//            @Override
//            public void handle(KeyEvent e) {
//                TextField txt_TextField = (TextField) e.getSource();
//                if (max_Length > 0 && txt_TextField.getText().length() >= max_Length) {
//                    e.consume();
//                }
//                if(e.getCharacter().matches("[0-9.]")){
//                    if(txt_TextField.getText().contains(".") && e.getCharacter().matches("[.]")){
//                        e.consume();
//                    }else if(txt_TextField.getText().length() == 0 && e.getCharacter().matches("[.]")){
//                        e.consume();
//                    }
//                }else{
//                    e.consume();
//                }
//            }
//        };
//    }
    /*****************************************************************************************/

	 /* Letters Validation Limit the  characters to maxLength AND to ONLY Letters *************************************/
//    public EventHandler<KeyEvent> numOfLetters_Validation(final Integer max_Length) {
//        return new EventHandler<KeyEvent>() {
//            @Override
//            public void handle(KeyEvent e) {
//                TextField txt_TextField = (TextField) e.getSource();
//                if (max_Length > 0 && txt_TextField.getText().length() >= max_Length) {
//                    e.consume();
//                }
//                if(!(e.getCharacter().matches("[A-Za-z]")))
//                    e.consume();
//            }
//        };
//    }
    /*****************************************************************************************/

}