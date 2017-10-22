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

    Label label;
    Button button;
    TableView<ArrayList<String>> table;

    BorderPane mainPane = new BorderPane();
    MenuBar menuBar;

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
        Menu optimization = new Menu();

        Menu exit = new Menu();

        Label menuLabel = new Label("Optimization");
        menuLabel.setOnMouseClicked(e -> {
            Stage myDialog = new Stage();
            myDialog.initModality(Modality.WINDOW_MODAL);

            GridPane optimizePane = new GridPane();
//            optimizePane.setPadding(new Insets(10, 10, 10, 10));
            optimizePane.setVgap(20);
            optimizePane.setHgap(25);
            optimizePane.setAlignment(Pos.CENTER);

            Scene dialogScene = new Scene(optimizePane, 800, 400);
            ChoiceBox optimizationType = new ChoiceBox<>();

            optimizationType.getItems().addAll("Create Indexes", "Create Views", "Using JOIN statements");

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

        optimization.setGraphic(menuLabel);

        menuLabel = new Label("Exit");
        exit.setGraphic(menuLabel);
        menuLabel.setOnMouseClicked(e -> terminateProgram());

        menuBar = new MenuBar();

        menuBar.getMenus().addAll(optimization, exit);

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
//            TextField input;

            System.out.println(queryChoiceBox.getSelectionModel().getSelectedItem().toString());

            switch (queryChoiceBox.getSelectionModel().getSelectedItem().toString()) {
                case "All publishers located in Los Angeles":
                    editQueryVBox = new VBox(2);
                    editQueryVBox.setPadding(new Insets(0, 10, 0, 10));
                    label = new Label("SELECT PublisherName, Address\n" +
                            "FROM publisher\n" +
                            "WHERE Address like '%\n");
                    input = new TextField("Los Angeles");
                    editQueryVBox.getChildren().addAll(label, input);
                    label = new Label("%'\nORDER BY PublisherName;");
                    editQueryVBox.getChildren().add(label);

                    subVBox.getChildren().addAll(editQueryVBox, button);

                    button.setOnAction(event1 -> {
                        mainVBox.getChildren().remove(3);
                        String x = input.getText();

                        Statement st = null;
                        ResultSet rs = null;

                        String query = "SELECT PublisherName AS 'Publisher', Address\n" +
                                "FROM publisher\n" +
                                "WHERE Address like '%" + x + "%'\n" +
                                "ORDER BY PublisherName;";

                        ObservableList<ArrayList<String>> arrayList = FXCollections.observableArrayList();

                        try {

                            st = conn.createStatement();
                            rs = st.executeQuery(query);

                            while (rs.next()) {
                                ArrayList<String> rowData = new ArrayList<>();
                                rowData.add(rs.getString("Publisher"));
                                rowData.add(rs.getString("Address"));
                                arrayList.add(rowData);
                                System.out.println("NEW DATA");
                            }
                            st.close();
                            rs.close();

                            table.setItems(arrayList);
                        } catch (SQLException e) {
                            System.out.println("SQLException: " + e.getMessage());
                            System.out.println("SQLState: " + e.getSQLState());
                            System.out.println("VendorError: " + e.getErrorCode());
                        } // catch (ClassNotFoundException e){

                    });
                    break;

                case "All borrowers living in Manila":
                    editQueryVBox = new VBox(2);
                    editQueryVBox.setPadding(new Insets(0, 10, 0, 10));
                    label = new Label("SELECT BorrowerLName, BorrowerFName, Address\n" +
                            "FROM borrower\n" +
                            "WHERE Address LIKE ‘%\n");
                    input = new TextField("Manila");
                    editQueryVBox.getChildren().addAll(label, input);
                    label = new Label("%';");
                    editQueryVBox.getChildren().add(label);

                    subVBox.getChildren().addAll(editQueryVBox, button);

                    button.setOnAction(event1 -> {
                        mainVBox.getChildren().remove(3);
                        String x = input.getText();

                        Statement st = null;
                        ResultSet rs = null;
                        String query = "SELECT BorrowerLName, BorrowerFName, Address\n" +
                                "FROM borrower\n" +
                                "WHERE Address LIKE '%" + x + "'\n";

                        ObservableList<ArrayList<String>> arrayList = FXCollections.observableArrayList();

                        try {

                            st = conn.createStatement();
                            rs = st.executeQuery(query);

                            while (rs.next()) {
                                ArrayList<String> rowData = new ArrayList<>();
                                rowData.add(rs.getString("BorrowerLName"));
                                rowData.add(rs.getString("BorrowerFName"));
                                rowData.add(rs.getString("Address"));
                                arrayList.add(rowData);
                            }
                            st.close();
                            rs.close();

                            table.setItems(arrayList);
                        } catch (SQLException e) {
                            System.out.println("SQLException: " + e.getMessage());
                            System.out.println("SQLState: " + e.getSQLState());
                            System.out.println("VendorError: " + e.getErrorCode());
                        }// catch (ClassNotFoundException e){

                    });
                    break;

                case "All borrowers who have borrowed at most 2 books":
                    editQueryVBox = new VBox(2);
                    editQueryVBox.setPadding(new Insets(0, 10, 0, 10));
                    label = new Label("SELECT CONCAT(BO.BorrowerLName, ', ', BO.BorrowerFName) as BorrowerName , COUNT(*) as NoBooksBor\n" +
                            "FROM borrower BO, book_loans BL\n" +
                            "WHERE BO.CardNo = BL.CardNo\n" +
                            "GROUP BY BorrowerName\n" +
                            "HAVING NoBooksBor \n");
                    input = new TextField(">= 0");
                    editQueryVBox.getChildren().addAll(label, input);
                    label = new Label("and NoBooksBor");
                    input_2 = new TextField("<= 2");
                    editQueryVBox.getChildren().addAll(label, input_2);
                    label = new Label("ORDER BY 2 DESC, 1;");
                    editQueryVBox.getChildren().add(label);

                    subVBox.getChildren().addAll(editQueryVBox, button);

                    button.setOnAction(event1 -> {
                        mainVBox.getChildren().remove(3);
                        String x = input.getText(), y = input_2.getText();

                        Statement st = null;
                        ResultSet rs = null;
                        String query = "SELECT CONCAT(BO.BorrowerLName, ', ', BO.BorrowerFName) AS BorrowerName, COUNT(*) as NoBooksBor\n" +
                                "FROM borrower BO, book_loans BL\n" +
                                "WHERE BO.CardNo = BL.CardNo\n" +
                                "GROUP BY BorrowerName\n" +
                                "HAVING NoBooksBor" + x + " and " + y + "\n" +
                                "ORDER BY 2 DESC, 1;\n";

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
                    });
                    break;

                case "All books written by Burningpeak, Loni":
                    editQueryVBox = new VBox(2);
                    editQueryVBox.setPadding(new Insets(0, 10, 0, 10));
                    label = new Label("SELECT B.Title, B.PublisherName, CONCAT(BA.AuthorLastName, '. ', BA.AuthorFirstName) as Author\n" +
                            "FROM book B, (SELECT * \n" +
                            "\t\tFROM book_authors\n" +
                            "\t\tWHERE AuthorLastName =  ‘\n");
                    input = new TextField("Burningpeak");
                    editQueryVBox.getChildren().addAll(label, input);
                    label = new Label("’ and AuthorFirstName = ‘");
                    input_2 = new TextField("Loni");
                    editQueryVBox.getChildren().addAll(label, input_2);
                    label = new Label("’ ) as BA\n" +
                            "WHERE BA.BookID = B.BookID\n" +
                            "ORDER BY 1;\n");
                    editQueryVBox.getChildren().add(label);

                    subVBox.getChildren().addAll(editQueryVBox, button);

                    button.setOnAction(event1 -> {
                        mainVBox.getChildren().remove(3);

                        String x = input.getText(), y = input_2.getText();

                        Statement st = null;
                        ResultSet rs = null;
                        String query = "SELECT B.Title, B.PublisherName, CONCAT(BA.AuthorLastName, '. ', BA.AuthorFirstName) as Author\n" +
                                "FROM book B, (SELECT * \n" +
                                "      FROM book_authors\n" +
                                "      WHERE AuthorLastName =  '" + x + "' and AuthorFirstName = '" + y + "') as BA\n" +
                                "WHERE BA.BookID = B.BookID\n" +
                                "ORDER BY 1;\n";

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
                            table.setItems(arrayList);
                        } catch (SQLException e) {
                            System.out.println("SQLException: " + e.getMessage());
                            System.out.println("SQLState: " + e.getSQLState());
                            System.out.println("VendorError: " + e.getErrorCode());
                        }// catch (ClassNotFoundException e){
                    });
                    break;

                case "All books which were never loaned out (nobody borrowed them)":
                    editQueryVBox = new VBox(2);
                    editQueryVBox.setPadding(new Insets(0, 10, 0, 10));
                    label = new Label("SELECT BO.CardNo, CONCAT(BO.BorrowerLName, ', ', BO.BorrowerFName) as " +
                            "BorrowerName, LB.BranchID, LB.BranchName, LB.BranchAddress\n" +
                            "FROM borrower BO, book_loans BL, library_branch LB\n" +
                            "WHERE BO.CardNo NOT IN (SELECT CardNo \n" +
                            "\t\tFROM book_loans) AND BO.Address = LB.BranchAddress AND BL.BranchID = LB.BranchID\n" +
                            "GROUP BY BorrowerName\n" +
                            "ORDER BY 2;\n");

                    editQueryVBox.getChildren().add(label);
                    subVBox.getChildren().addAll(editQueryVBox, button);

                    button.setOnAction(event1 -> {
                        mainVBox.getChildren().remove(3);

                        Statement st = null;
                        ResultSet rs = null;
                        String query = "SELECT BO.CardNo, CONCAT(BO.BorrowerLName, ', ', BO.BorrowerFName) as " +
                                "BorrowerName, LB.BranchID, LB.BranchName, LB.BranchAddress\n" +
                                "FROM borrower BO, book_loans BL, library_branch LB\n" +
                                "WHERE BO.CardNo NOT IN (SELECT CardNo \n" +
                                "      FROM book_loans) AND BO.Address = LB.BranchAddress AND BL.BranchID = LB.BranchID\n" +
                                "GROUP BY BorrowerName\n" +
                                "ORDER BY 2;\n";

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
                            table.setItems(arrayList);
                        } catch (SQLException e) {
                            System.out.println("SQLException: " + e.getMessage());
                            System.out.println("SQLState: " + e.getSQLState());
                            System.out.println("VendorError: " + e.getErrorCode());
                        }// catch (ClassNotFoundException e){

                    });
                    break;


                case "All borrowers who have loaned books in their own branch":
                    editQueryVBox = new VBox(2);
                    editQueryVBox.setPadding(new Insets(0, 10, 0, 10));
                    label = new Label("SELECT B.BookID, B.Title, CONCAT(BA.AuthorLName, \", \", " +
                            "BA.AuthorFName) as AuthorName, B.PublisherName\n" +
                            "FROM book B, book_authors BA\n" +
                            "WHERE B.BookID NOT IN (SELECT BookID\n" +
                            "\t\t\tFROM book_loans)\n" +
                            "GROUP BY B.BookID\n" +
                            "ORDER BY 3, 2;\n");

                    editQueryVBox.getChildren().add(label);
                    subVBox.getChildren().addAll(editQueryVBox, button);

                    button.setOnAction(e->{
                        mainVBox.getChildren().remove(3);

                        Statement st = null;
                        ResultSet rs = null;
                        String query = "SELECT B.BookID, B.Title, CONCAT(BA.AuthorLName, \", \", " +
                                "BA.AuthorFName) as AuthorName, B.PublisherName\n" +
                                "FROM book B, book_authors BA\n" +
                                "WHERE B.BookID NOT IN (SELECT BookID\n" +
                                "                                                FROM book_loans)\n" +
                                "GROUP BY B.BookID\n" +
                                "ORDER BY 3, 2;\n";

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
                            table.setItems(arrayList);
                        } catch (SQLException ev) {
                            System.out.println("SQLException: " + ev.getMessage());
                            System.out.println("SQLState: " + ev.getSQLState());
                            System.out.println("VendorError: " + ev.getErrorCode());
                        }// catch (ClassNotFoundException e){

                    });
                    break;

                case "All book loans that were returned exactly on their due date":
                    editQueryVBox = new VBox(2);
                    editQueryVBox.setPadding(new Insets(0, 10, 0, 10));
                    label = new Label("SELECT CONCAT(BO.BorrowerFName, \", \" , BO.BorrowerLName) AS BorrowerName, BL.BookID, B.Title, CONCAT(BA.AuthorLastName, \", \", BA.AuthorFirstName) as AuthorName, BL.DueDate, BL.DateReturned\n" +
                            "FROM book B, book_authors BA, book_loans BL, borrower BO\n" +
                            "WHERE B.BookID = BA.BookID AND BA.BookID = BL.BookID AND BL.CardNo AND BL.DueDate = BL.DateReturned\n" +
                            "ORDER BY 1, 3;\n");

                    editQueryVBox.getChildren().add(label);
                    subVBox.getChildren().addAll(editQueryVBox, button);

                    button.setOnAction(event1 -> {
                        mainVBox.getChildren().remove(3);

                        Statement st = null;
                        ResultSet rs = null;
                        String query = "SELECT CONCAT(BO.BorrowerLName, ', ', BO.BorrowerFName) AS BorrowerName, " +
                                "BL.BookID, B.Title, CONCAT(BA.AuthorLastName, ', ', " +
                                "BA.AuthorFirstName) as AuthorName, BL.DueDate, BL.DateReturned\n" +
                                "FROM book B, book_authors BA, book_loans BL, borrower BO\n" +
                                "WHERE B.BookID = BA.BookID AND BA.BookID = BL.BookID AND " +
                                "BL.CardNo AND BL.DueDate = BL.DateReturned\n" +
                                "ORDER BY 1, 3;\n";

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
                            table.setItems(arrayList);
                        } catch (SQLException e) {
                            System.out.println("SQLException: " + e.getMessage());
                            System.out.println("SQLState: " + e.getSQLState());
                            System.out.println("VendorError: " + e.getErrorCode());
                        }// catch (ClassNotFoundException e){


                    });
                    break;

                case "Most popular title (most loaned out title) for each branch":
                    editQueryVBox = new VBox(2);
                    editQueryVBox.setPadding(new Insets(0, 10, 0, 10));
                    label = new Label("\n" +
                            "SELECT BL.BranchID, LB.BranchName, BL.BookID, BL.NoTimesLoaned, B.Title, CONCAT(BA.AuthorLastName, \", \", BA.AuthorFirstName) as AuthorName\n, P.PublisherName, P.Address AS PublisherAddress\n" +
                            "FROM book  B, book_authors BA, library_branch LB, publisher P, \n(SELECT BranchID, BookID, COUNT(*) AS NoTimesLoaned FROM book_loans GROUP BY BranchID, BookID) AS BL, (SELECT TEMP.BranchID, MAX(TEMP.NoTimesLoaned) AS NoTimesLoaned \nFROM" +
                            "(SELECT BranchID, BookID, COUNT(*) AS NoTimesLoaned FROM BOOK_LOANS GROUP BY BranchID, BookID) AS TEMP  \nGROUP BY TEMP.BranchID) AS C\n" +
                            "WHERE BL.BranchID = C.BranchID AND BL.NoTimesLoaned = C.NoTimesLoaned AND BL.BranchID = LB.BranchID AND BL.BookID = B.BookID AND B.BookID = BA.BookID AND B.PublisherName = P.PublisherName\n" +
                            "GROUP BY BL.BranchID\n" +
                            "ORDER BY 2, 5;\n");

                    editQueryVBox.getChildren().add(label);
                    subVBox.getChildren().addAll(editQueryVBox, button);

                    button.setOnAction(event1 -> {
                        mainVBox.getChildren().remove(3);

                        Statement st = null;
                        ResultSet rs = null;
                        String query = "SELECT BL.BranchID, LB.BranchName, BL.BookID, BL.NoTimesLoaned, " +
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
                            table.setItems(arrayList);
                        } catch (SQLException e) {
                            System.out.println("SQLException: " + e.getMessage());
                            System.out.println("SQLState: " + e.getSQLState());
                            System.out.println("VendorError: " + e.getErrorCode());
                        }// catch (ClassNotFoundException e){

                    });
                    break;

            }


        });

        ToggleGroup radioButtonGroup = new ToggleGroup();

        RadioButton normal = new RadioButton("Normal");
        normal.setToggleGroup(radioButtonGroup);
        normal.setUserData("Normal");
        normal.setSelected(true);

        RadioButton union = new RadioButton("Union");
        union.setToggleGroup(radioButtonGroup);
        union.setUserData("Union");

        RadioButton join = new RadioButton("Join");
        join.setToggleGroup(radioButtonGroup);
        join.setUserData("Join");

        RadioButton subquery = new RadioButton("Subquery");
        subquery.setToggleGroup(radioButtonGroup);
        subquery.setUserData("Subquery");

        radioButtonGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                System.out.println(new_toggle.getUserData().toString());
                switch (new_toggle.getUserData().toString()) {
                    case "Normal": /* normal queries */
                        break;

                    case "Union":
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
        radioButtonVBox.getChildren().addAll(normal, union, join, subquery);

        mainVBox.getChildren().addAll(radioButtonVBox, new Separator(Orientation.HORIZONTAL), button);

        mainVBox.setMinWidth(200);
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
                    "All publishers located in Los Angeles",
                    "All borrowers living in Manila",
                    "All borrowers who have borrowed at most 2 books",
                    "All books written by Burningpeak, Loni",
                    "All books which were never loaned out (nobody borrowed them)",
                    "All borrowers who have loaned books in their own branch",
                    "All book loans that were returned exactly on their due date",
                    "Most popular title (most loaned out title) for each branch");
            queryChoiceBox.setValue("");

            queryChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                    restartProfiling();
                    queryNum.setText("Query #" + number2);
                    query.setText(queryChoiceBox.getItems().get((Integer) number2));
                    graphArea.getData().remove(dataSeries1);
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
                            updateTableQuery1(vBox);
                            break;
                        case 2:
                            updateTableQuery2(vBox);
                            break;
                        case 3:
                            updateTableQuery3(vBox);
                            break;
                        case 4:
                            updateTableQuery4(vBox);
                            break;
                        case 5:
                            updateTableQuery5(vBox);
                            break;
                        case 6:
                            updateTableQuery6(vBox);
                            break;
                        case 7:
                            updateTableQuery7(vBox);
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
                        updateTableQuery1(tempvBox);
                        break;
                    case '2':
                        updateTableQuery2(tempvBox);
                        break;
                    case '3':
                        updateTableQuery3(tempvBox);
                        break;
                    case '4':
                        updateTableQuery4(tempvBox);
                        break;
                    case '5':
                        updateTableQuery5(tempvBox);
                        break;
                    case '6':
                        updateTableQuery6(tempvBox);
                        break;
                    case '7':
                        updateTableQuery7(tempvBox);
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
                System.out.println(rs.getInt("Query_ID") + " || " +
                        rs.getFloat("Duration") + " || " +
                        rs.getString("Query") + " ::::::: " + i + " || " + row);
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
    private void updateTableQuery1(VBox vBox) {
        TableColumn<ArrayList<String>, String> pubColumn = new TableColumn<>("Publisher");

        pubColumn.setMinWidth(100);
        pubColumn.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(0));
        });

        TableColumn<ArrayList<String>, String> addressColumn = new TableColumn<>("Address");
        addressColumn.setMinWidth(100);
        addressColumn.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(1));
        });

        table.setItems(getQuery1());
        table.getColumns().addAll(pubColumn, addressColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        vBox.getChildren().add(2, table);

        BigDecimal processTime = getQueryProcessTime(nQueryExec);
        System.out.println("GOTTEN PROCESS TIME == " + processTime + " || " + nQueryExec);
        dataSeries1.getData().add(new XYChart.Data(nQueryExec, processTime));

    }

    private void updateTableQuery2(VBox vBox) {
        TableColumn<ArrayList<String>, String> borrowerLNameCol = new TableColumn<>("BorrowerLName");
        borrowerLNameCol.setMinWidth(100);
        borrowerLNameCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(0));
        });

        //Starting Point Column
        TableColumn<ArrayList<String>, String> borrowerFNameCol = new TableColumn<>("BorrowerFName");
        borrowerFNameCol.setMinWidth(100);
        borrowerFNameCol.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(1));
        });

        //End Point Column
        TableColumn<ArrayList<String>, String> addressColumn = new TableColumn<>("Address");
        addressColumn.setMinWidth(100);
        addressColumn.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(2));
        });

        table.setItems(getQuery2());
        table.getColumns().addAll(borrowerLNameCol, borrowerFNameCol, addressColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        BigDecimal processTime = getQueryProcessTime(nQueryExec);
        dataSeries1.getData().add(new XYChart.Data(nQueryExec, processTime));

        vBox.getChildren().add(2, table);
    }

    private void updateTableQuery3(VBox vBox) {
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


        table.setItems(getQuery3());
        table.getColumns().addAll(nameCol, noBooksBorCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        BigDecimal processTime = getQueryProcessTime(nQueryExec);
        dataSeries1.getData().add(new XYChart.Data(nQueryExec, processTime));

        vBox.getChildren().add(2, table);
    }

    private void updateTableQuery4(VBox vBox) {
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

        table.setItems(getQuery4());
        table.getColumns().addAll(titleCol, pubNameCol, authorCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        BigDecimal processTime = getQueryProcessTime(nQueryExec);
        dataSeries1.getData().add(new XYChart.Data(nQueryExec, processTime));

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
        dataSeries1.getData().add(new XYChart.Data(nQueryExec, processTime));

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
        dataSeries1.getData().add(new XYChart.Data(nQueryExec, processTime));

        vBox.getChildren().add(2, table);
    }

    private void updateTableQuery7(VBox vBox) {
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


        table.setItems(getQuery7());
        table.getColumns().addAll(nameCol, idCol, titleCol, authorCol, dueDateCol, dateReturnedCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        BigDecimal processTime = getQueryProcessTime(nQueryExec);
        dataSeries1.getData().add(new XYChart.Data(nQueryExec, processTime));

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
        dataSeries1.getData().add(new XYChart.Data(nQueryExec, processTime));

        vBox.getChildren().add(2, table);
    }

    // Get queries

    public ObservableList<ArrayList<String>> getQuery1() {
        //Connection conn = getConnection();	called at the start
        Statement st = null;
        ResultSet rs = null;
        String query = "SELECT PublisherName AS 'Publisher', Address\n" +
                        "FROM publisher\n" +
                        "WHERE Address like '%Los Angeles%'\n" +
                        "ORDER BY PublisherName;";

        ObservableList<ArrayList<String>> arrayList = FXCollections.observableArrayList();

        try {

            st = conn.createStatement();
            rs = st.executeQuery(query);

            while (rs.next()) {
                ArrayList<String> rowData = new ArrayList<>();
                rowData.add(rs.getString("Publisher"));
                rowData.add(rs.getString("Address"));
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

    public ObservableList<ArrayList<String>> getQuery2() {

        //Connection conn = getConnection();	called at the start
        Statement st = null;
        ResultSet rs = null;
        String query = "SELECT BorrowerLName, BorrowerFName, Address\n" +
                "FROM borrower\n" +
                "WHERE Address LIKE '%Manila%'\n";

        ObservableList<ArrayList<String>> arrayList = FXCollections.observableArrayList();

        try {

            st = conn.createStatement();
            rs = st.executeQuery(query);

            while (rs.next()) {
                ArrayList<String> rowData = new ArrayList<>();
                rowData.add(rs.getString("BorrowerLName"));
                rowData.add(rs.getString("BorrowerFName"));
                rowData.add(rs.getString("Address"));
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

    public ObservableList<ArrayList<String>> getQuery3() {

        //Connection conn = getConnection();	called at the start
        Statement st = null;
        ResultSet rs = null;
        String query = "SELECT CONCAT(BO.BorrowerLName, ', ', BO.BorrowerFName) AS BorrowerName, COUNT(*) as NoBooksBor\n" +
                "FROM borrower BO, book_loans BL\n" +
                "WHERE BO.CardNo = BL.CardNo\n" +
                "GROUP BY BorrowerName\n" +
                "HAVING NoBooksBor >= 0 and NoBooksBor <=2\n" +
                "ORDER BY 2 DESC, 1;\n";

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

    public ObservableList<ArrayList<String>> getQuery4() {

        Statement st = null;
        ResultSet rs = null;
        String query = "SELECT B.Title, B.PublisherName, CONCAT(BA.AuthorLastName, '. ', BA.AuthorFirstName) as Author\n" +
                        "FROM book B, (SELECT * \n" +
                        "      FROM book_authors\n" +
                        "      WHERE AuthorLastName =  'Burningpeak' and AuthorFirstName = 'Loni') as BA\n" +
                        "WHERE BA.BookID = B.BookID\n" +
                        "ORDER BY 1;\n";

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
        String query = "SELECT B.BookID, B.Title, CONCAT(BA.AuthorLName, \", \", " +
                        "BA.AuthorFName) as AuthorName, B.PublisherName\n" +
                        "FROM book B, book_authors BA\n" +
                        "WHERE B.BookID NOT IN (SELECT BookID\n" +
                        "                                                FROM book_loans)\n" +
                        "GROUP BY B.BookID\n" +
                        "ORDER BY 3, 2;\n";

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
        String query = "SELECT BO.CardNo, CONCAT(BO.BorrowerLName, ', ', BO.BorrowerFName) as " +
                        "BorrowerName, LB.BranchID, LB.BranchName, LB.BranchAddress\n" +
                        "FROM borrower BO, book_loans BL, library_branch LB\n" +
                        "WHERE BO.CardNo IN (SELECT CardNo \n" +
                        "      FROM book_loans) AND BO.Address = LB.BranchAddress AND BL.BranchID = LB.BranchID\n" +
                        "GROUP BY BorrowerName\n" +
                        "ORDER BY 2;\n";

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

    public ObservableList<ArrayList<String>> getQuery7() {

        //Connection conn = getConnection();	called at the start
        Statement st = null;
        ResultSet rs = null;
        String query = "SELECT CONCAT(BO.BorrowerLName, ', ', BO.BorrowerFName) AS BorrowerName, " +
                        "BL.BookID, B.Title, CONCAT(BA.AuthorLastName, ', ', " +
                        "BA.AuthorFirstName) as AuthorName, BL.DueDate, BL.DateReturned\n" +
                        "FROM book B, book_authors BA, book_loans BL, borrower BO\n" +
                        "WHERE B.BookID = BA.BookID AND BA.BookID = BL.BookID AND " +
                        "BL.CardNo AND BL.DueDate = BL.DateReturned\n" +
                        "ORDER BY 1, 3;\n";

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
    } // TODO update query

    public ObservableList<ArrayList<String>> getQuery8() {

        //Connection conn = getConnection();	called at the start
        Statement st = null;
        ResultSet rs = null;
        String query = "SELECT BL.BranchID, LB.BranchName, BL.BookID, BL.NoTimesLoaned, " +
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