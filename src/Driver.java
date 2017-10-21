import Model.Transaction;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
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

    TableView<Transaction> transactionTable;
    BorderPane mainPane = new BorderPane();
    MenuBar menuBar;

    Label queryNum = new Label(),
            query = new Label();

    ChoiceBox<String> queryChoiceBox;

    Connection conn;
    private LineChart graphArea;
    private XYChart.Series dataSeries1;
    private int nQueryExec = 1;


    @SuppressWarnings("unchecked")
    public void start(Stage primaryStage) throws Exception {
        conn = getConnection();
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
            optimizePane.setPadding(new Insets(10, 10, 10, 10));
            optimizePane.setVgap(20);
            optimizePane.setHgap(25);

            HBox itemRow = new HBox(10);
            itemRow.setAlignment(Pos.CENTER);
            label = new Label("Method");
            Scene dialogScene = new Scene(optimizePane, 800, 400);
            ChoiceBox optimizationType = new ChoiceBox<>();

            optimizationType.getItems().addAll("Create Indexes", "Create Views", "Using JOIN statements");

            itemRow.getChildren().addAll(label, optimizationType);
            optimizePane.add(itemRow, 1, 0);

            optimizationType.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {

                    switch ((Integer) number2) {
                        case 0:
                            indexOptimization(optimizePane);
                            break;
                    }
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
        HBox itemRow = new HBox(5);

        tableChoiceBox.getItems().addAll("book", "book_author", "book_loans", "borrower", "library_branch", "publisher");
        label = new Label("Table: ");
        itemRow.getChildren().addAll(label, tableChoiceBox);

        tableChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                label = new Label("Attribute:");
                button = new Button("Optimize");

                optimizePane.add(button, 2, 3);

                optimizePane.add(new Label(""), 1, 2);

                switch ((Integer) number2) {
                    case 0: /* book */
                        createIndexBook(optimizePane);
                        break;

                    case 1: /* book_author */
                        createIndexBookAuthor(optimizePane);
                        break;

                    case 2: /* book_loans */
                        createBookLoansIndex(optimizePane);
                        break;

                    case 3: /* borrower */
                        createBorrowerIndex(optimizePane);
                        break;

                    case 4: /* library_branch */
                        createLibraryBranchIndex(optimizePane);
                        break;

                    case 5: /* publisher */
                        createPublisherIndex(optimizePane);
                        break;

                }
            }
        });

        GridPane.setMargin(optimizePane, new Insets(10, 10, 10, 10));
        optimizePane.add(itemRow, 1, 1);
    }

    public void createIndexBook(GridPane optimizePane) {
        HBox tempHBox = new HBox(5);
        ChoiceBox attrChoiceBox = new ChoiceBox<>();

        System.out.println("book");

        if (optimizePane.getChildren().get(2) instanceof Label || optimizePane.getChildren().get(2) instanceof HBox) {
            optimizePane.getChildren().remove(2);
        }

        attrChoiceBox.getItems().addAll("BookID", "Title", "PublisherName");



        /* choose which attribute to create an index */
        attrChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                switch ((Integer) number2) {
                    case 0:
                        /* sql script for  BookID index */
                        break;
                    case 1:
                        /* sql script for  Title index */
                        break;
                    case 2:
                        /* sql script for PublisherName index */
                        break;
                }
            }
        });

        tempHBox.getChildren().addAll(label, attrChoiceBox);

        TextField indexValue = new TextField("Row to Index");

        optimizePane.add(indexValue, 1, 3);
        optimizePane.add(tempHBox, 1, 2);
    } // TODO index

    public void createIndexBookAuthor(GridPane optimizePane) {
        HBox tempHBox = new HBox(5);
        ChoiceBox attrChoiceBox = new ChoiceBox<>();

        System.out.println("book_author");

        if (optimizePane.getChildren().get(2) instanceof Label || optimizePane.getChildren().get(2) instanceof HBox) {
            optimizePane.getChildren().remove(2);
        }

        attrChoiceBox.getItems().addAll("BookID", "AuthorLastName", "AuthorFirstName");

        /* choose which attribute to create an index */
        attrChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                switch ((Integer) number2) {
                    case 0:
                            /* sql script for  BookID index */
                        break;
                    case 1:
                            /* sql script for  AuthorLastName index */
                        break;
                    case 2:
                            /* sql script for AuthorFirstName index */
                        break;
                }
            }
        });

        tempHBox.getChildren().addAll(label, attrChoiceBox);

        TextField indexValue = new TextField("Row to Index");

        optimizePane.add(indexValue, 1, 3);
        optimizePane.add(tempHBox, 1, 2);
    } // TODO index

    public void createBookLoansIndex(GridPane optimizePane) {
        HBox tempHBox = new HBox(5);
        ChoiceBox attrChoiceBox = new ChoiceBox<>();

        System.out.println("book_loans");

        if (optimizePane.getChildren().get(2) instanceof Label || optimizePane.getChildren().get(2) instanceof HBox) {
            optimizePane.getChildren().remove(2);
        }

        attrChoiceBox.getItems().addAll("BookID", "BranchID", "CardNo", "DateOut", "DueDate", "DateReturned");

        /* choose which attribute to create an index */
        attrChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                switch ((Integer) number2) {
                    case 0:
                            /* sql script for BookID index */
                        break;
                    case 1:
                            /* sql script for BranchID index */
                        break;
                    case 2:
                            /* sql script for CardNo index */
                        break;
                    case 3:
                            /* sql script for DateOut index */
                        break;
                    case 4:
                            /* sql script for DueDate index */
                        break;
                    case 5:
                            /* sql script for DateReturned index */
                        break;
                }
            }
        });

        tempHBox.getChildren().addAll(label, attrChoiceBox);
        optimizePane.add(tempHBox, 1, 2);
    } // TODO index

    public void createBorrowerIndex(GridPane optimizePane) {
        HBox tempHBox = new HBox(5);
        ChoiceBox attrChoiceBox = new ChoiceBox<>();

        System.out.println("borrower");

        if (optimizePane.getChildren().get(2) instanceof Label || optimizePane.getChildren().get(2) instanceof HBox) {
            optimizePane.getChildren().remove(2);
        }

        attrChoiceBox.getItems().addAll("CardNo", "BorrowerLName", "BorrowerFName", "Address", "Phone");

        /* choose which attribute to create an index */
        attrChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                switch ((Integer) number2) {
                    case 0:
                            /* sql script for CardNo index */
                        break;
                    case 1:
                            /* sql script for BorrowerLName index */
                        break;
                    case 2:
                            /* sql script for BorrowerFName index */
                        break;
                    case 3:
                            /* sql script for Address index */
                        break;
                    case 4:
                            /* sql script for Phone index */
                        break;
                }
            }
        });

        tempHBox.getChildren().addAll(label, attrChoiceBox);

        TextField indexValue = new TextField("Row to Index");

        optimizePane.add(indexValue, 1, 3);
        optimizePane.add(tempHBox, 1, 2);
    } // TODO index

    public void createLibraryBranchIndex(GridPane optimizePane) {
        HBox tempHBox = new HBox(5);
        ChoiceBox attrChoiceBox = new ChoiceBox<>();

        System.out.println("borrower");

        if (optimizePane.getChildren().get(2) instanceof Label || optimizePane.getChildren().get(2) instanceof HBox) {
            optimizePane.getChildren().remove(2);
        }

        attrChoiceBox.getItems().addAll("BranchID", "BranchName", "BranchAddress");

        /* choose which attribute to create an index */
        attrChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                switch ((Integer) number2) {
                    case 0:
                            /* sql script for BranchID index */
                        break;
                    case 1:
                            /* sql script for BranchName index */
                        break;
                    case 2:
                            /* sql script for BranchAddress index */
                        break;
                    case 3:
                            /* sql script for Address index */
                        break;
                }
            }
        });

        tempHBox.getChildren().addAll(label, attrChoiceBox);

        TextField indexValue = new TextField("Row to Index");

        optimizePane.add(indexValue, 1, 3);
        optimizePane.add(tempHBox, 1, 2);
    }// TODO index

    public void createPublisherIndex(GridPane optimizePane) {
        HBox tempHBox = new HBox(5);
        ChoiceBox attrChoiceBox = new ChoiceBox<>();

        System.out.println("borrower");

        if (optimizePane.getChildren().get(2) instanceof Label || optimizePane.getChildren().get(2) instanceof HBox) {
            optimizePane.getChildren().remove(2);
        }

        attrChoiceBox.getItems().addAll("PublisherName", "Address", "Phone");

        /* choose which attribute to create an index */
        attrChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                switch ((Integer) number2) {
                    case 0:
                            /* sql script for PublisherName index */
                        break;
                    case 1:
                            /* sql script for Address index */
                        break;
                    case 2:
                            /* sql script for Phone index */
                        break;
                    case 3:
                            /* sql script for Address index */
                        break;
                }
            }
        });

        tempHBox.getChildren().addAll(label, attrChoiceBox);

        TextField indexValue = new TextField("Row to Index");

        optimizePane.add(indexValue, 1, 3);
        optimizePane.add(tempHBox, 1, 2);
    }// TODO index


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

            subVBox.getChildren().add(button);
            mainVBox.getChildren().add(subVBox);

            button.setOnAction(event1 -> {
                mainVBox.getChildren().remove(1);

            });

        });

        mainVBox.getChildren().add(button);

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
                    "SELECT PublisherName AS 'Publisher', Address\n" +
                            "FROM publisher\n" +
                            "WHERE Address like '%Los Angeles%'\n" +
                            "ORDER BY PublisherName;",
                    "SELECT BorrowerLName, BorrowerFName, Address\n" +
                            "FROM borrower\n" +
                            "WHERE Address LIKE '%Manila%'\n",
                    "SELECT CONCAT(BO.BorrowerLName, ', ', BO.BorrowerFName) \nAS BorrowerName, COUNT(*) as NoBooksBor\n" +
                            "FROM borrower BO, book_loans BL\n" +
                            "WHERE BO.CardNo = BL.CardNo\n" +
                            "GROUP BY BorrowerName\n" +
                            "HAVING NoBooksBor >= 0 and NoBooksBor <=2\n" +
                            "ORDER BY 2 DESC, 1;\n");
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
//        book.setOnAction(e -> {
//            ObservableList<Route> routeSelected, allRoutes;
//
//            allRoutes = userRider ? routesTable.getItems() : driverTable.getItems();
//            routeSelected = userRider? routesTable.getSelectionModel().getSelectedItems() : driverTable.getSelectionModel().getSelectedItems();
//
//            Route r = routeSelected.get(0);
//            int riderId = 0,
//                    routeId = 0,
//                    driverId = 0;
//
//            if(r != null){
//                Statement st    = null;
//                ResultSet rs    = null;
//                int transactionId = 0;
//                String  query   = "SELECT r.riderId as `Id` " +
//                        "FROM riderInfo r " +
//                        "WHERE r.firstName = \"" + firstName + "\" AND r.lastName = \"" + lastName + "\";",
//                        query1  = "SELECT d.driverId as `Id` " +
//                                "FROM driverInfo d " +
//                                "WHERE d.firstName = \"" + firstName + "\" AND d.lastName = \"" + lastName + "\";",
//                        query2  = "SELECT r.routeId as `Id` " +
//                                "FROM route r, landmarks l, landmarks l2 " +
//                                "WHERE l.landmarkName = \"" + r.getStart() + "\" AND r.pickupLoc = l.landmarkID AND l2.landmarkName = \"" +
//                                r.getEnd() + "\" AND r.dropOffLoc = l2.landmarkID;",
//                        query3  = " ";
//
//                try {
//                    st = conn.createStatement();
//                    rs = st.executeQuery(query2);
//                    rs.next();
//                    routeId = Integer.parseInt(rs.getString("Id"));
//
//                    if(userRider){
//                        rs = st.executeQuery(query);
//                        rs.next();
//                        riderId = Integer.parseInt(rs.getString("Id"));
//
//                    }
//                    else {
//                        query   = "SELECT r.riderId as `Id` " +
//                                "FROM riderInfo r " +
//                                "WHERE r.firstName = \"" + r.getRider().split(" ")[0] + "\" AND r.lastName = \"" + r.getRider().split(" ")[1] + "\";";
//
//                        rs = st.executeQuery(query);
//                        rs.next();
//                        riderId = Integer.parseInt(rs.getString("Id"));
//
//                        query3  = "SELECT transactionId AS `transactionId` FROM transaction where riderId = " +
//                                riderId + " AND routeId = " + routeId + " AND driverId = 0;";
//                        rs = st.executeQuery(query1);
//                        rs.next();
//                        driverId = Integer.parseInt(rs.getString("Id"));
//                        rs = st.executeQuery(query3);
//                        rs.next();
//                        transactionId = Integer.parseInt(rs.getString("transactionId"));
//                    }
//                    st.close();
//                    rs.close();
//                } catch (SQLException e2) {
//                    System.out.println("SQLException: " + e2.getMessage());
//                    System.out.println("SQLState: "		+ e2.getSQLState());
//                    System.out.println("VendorError: "  + e2.getErrorCode());
//                }
//
//
//                st    = null;
//                if(userRider)
//                    query = "INSERT INTO transaction (`fare`, `uberType`, `routeId`, `driverId`, `riderId`, `date`)" +
//                            "VALUES ('" + r.getFare() + "', 'uberX', '" + routeId + "', '0', '" + riderId + "', '0000-00-00 00:00:00');";
//                else {
//                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//                    Date date = new Date();
//                    query = "UPDATE transaction SET driverId = '" + driverId + "', date = '" + dateFormat.format(date) +"' WHERE `transactionId`='" + transactionId + "'";
//                }
//                try {
//                    System.out.println("Processing...");
//                    st = conn.createStatement();
//                    st.executeUpdate(query);
//
//                    st.close();
//                    if(userRider)
//                        System.out.println("You have booked " + r.getStart() + " - " + r.getEnd());
//                    else System.out.println("You have accepted " + r.getRider() + "'s request for route " + r.getStart() + " to " + r.getEnd());
//                } catch (SQLException e2) {
//                    System.out.println("SQLException: " + e2.getMessage());
//                    System.out.println("SQLState: "		+ e2.getSQLState());
//                    System.out.println("VendorError: "  + e2.getErrorCode());
//                }
//
//                routeSelected.forEach(allRoutes :: remove);	//Deletes the row selected
//            }
//        });

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
            return new SimpleStringProperty(x.get(0));
        });

        //End Point Column
        TableColumn<ArrayList<String>, String> addressColumn = new TableColumn<>("Address");
        addressColumn.setMinWidth(100);
        addressColumn.setCellValueFactory(param -> {
            ArrayList<String> x = param.getValue();
            return new SimpleStringProperty(x.get(1));
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
            return new SimpleStringProperty(x.get(0));
        });


        table.setItems(getQuery3());
        table.getColumns().addAll(nameCol, noBooksBorCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        BigDecimal processTime = getQueryProcessTime(nQueryExec);
        dataSeries1.getData().add(new XYChart.Data(nQueryExec, processTime));

        vBox.getChildren().add(2, table);
    }

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
                System.out.println("NEW DATA");
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

    private void terminateProgram() {
        if (conn != null)
            try {
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