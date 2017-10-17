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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;

//import javafx.scene.input.KeyCombination;
//import javafx.scene.control.Rating;


public class Driver extends Application{
    Scene home;

    String firstName,
            lastName;

    boolean userRider;
    Label label;
    Button button;
    TextField textField,
            startingTextField,
            endingTextField,
            minutesTextField,
            fareTextField,
            riderTextField,
            ratingTextField;

    TableView<ArrayList<String>> table;

    TableView<Transaction> transactionTable;
    BorderPane mainPane = new BorderPane();
    MenuBar menuBar;
    Menu menu;
    HBox hBox;
    VBox filterVBox;

    Label queryNum = new Label(),
          query    = new Label();

    //Rating rate = new Rating();
    Connection conn;
    private LineChart graphArea;
    private XYChart.Series dataSeries1;
    private int nQueryExec = 1;


    @SuppressWarnings("unchecked")
    public void start(Stage primaryStage) throws Exception
    {
        conn = getConnection();
        initMainScreen();

        home = new Scene(mainPane, 1000, 600);
        home.getStylesheets().add("View/Style.css");

        primaryStage.setTitle("ADVANDB - MP1");
        primaryStage.setOnCloseRequest(e -> terminateProgram());
        primaryStage.getIcons().add(new Image ("View/Images/uberLogo.png"));
        primaryStage.setScene(home);
        primaryStage.show();
    }

    public void initMainScreen()
    {
        mainPane.getChildren().remove(mainPane.getCenter());
//        mainPane.setRight(initRightVBox());
        mainPane.setCenter(initCenterVBox());
    }

    public VBox initRightVBox()
    {
        VBox vBox = new VBox();
        vBox.setId("vBoxRight");

        GridPane gridPane = new GridPane();
        gridPane.setId("rightGridPane");
        gridPane.getStyleClass().add("grid-pane");

        query.setText("");
        query.setPadding(new Insets(0,0,0,30));

        final Pane emptyPane = new Pane();
        emptyPane.setMinHeight(30);

        Button button = new Button("Repeat");
        button.setOnAction(e -> {
            nQueryExec += 1;
            String queryNumText = queryNum.getText();
            VBox tempvBox = new VBox();
            tempvBox.getChildren().add(new Label());
            table = new TableView <>();

            switch (queryNumText.charAt(queryNumText.length() - 1)){
                case '0': break;
                case '1': updateTableQuery1(tempvBox);
                          break;
                case '2': updateTableQuery2(tempvBox);
                          break;
                case '3': updateTableQuery3(tempvBox);
                          break;
                default : System.out.println("Repeating Query #" + queryNumText.charAt(queryNumText.length() - 1));
            }
        });

        GridPane.setConstraints(query    	    	, 1, 0);
        GridPane.setConstraints(emptyPane  	    	, 0, 1);
        GridPane.setConstraints(button      		, 1, 3);

        gridPane.getChildren().addAll(query, emptyPane, button);

        vBox.getChildren().addAll(gridPane);
        return vBox;
    }

    public VBox initCenterVBox()
    {
        VBox vBox = new VBox();
        HBox hBox;

        vBox.getStyleClass().add("vBoxCenter");

        Pane field = new Pane();
        field.setId("centerPane");

        if(conn == null){
            vBox.getChildren().add(new Label("Unable to connect to the database, check getConnection()"));
        } else {
            ImageView queryIcon = new ImageView("View/Images/queryIcon.png");
            queryIcon.setFitHeight(25);
            queryIcon.setFitWidth(25);
            queryIcon.setPreserveRatio(true);

            ChoiceBox<String> queryChoiceBox = new ChoiceBox<>();
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

                    if (vBox.getChildren().get(1) instanceof TableView)
                        vBox.getChildren().remove(1);

                    table = new TableView <>();
                    switch ((Integer)number2){
                        case 0: break;
                        case 1: updateTableQuery1(vBox);
                                break;
                        case 2: updateTableQuery2(vBox);
                                break;
                        case 3: updateTableQuery3(vBox);
                                break;
                        default: System.out.println("Query choice box selection model not in the range");
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

            button.setOnAction(e -> {
                nQueryExec += 1;
                String queryNumText = queryNum.getText();
                VBox tempvBox = new VBox();
                tempvBox.getChildren().add(new Label());
                table = new TableView <>();

                switch (queryNumText.charAt(queryNumText.length() - 1)){
                    case '0': break;
                    case '1': updateTableQuery1(tempvBox);
                        break;
                    case '2': updateTableQuery2(tempvBox);
                        break;
                    case '3': updateTableQuery3(tempvBox);
                        break;
                    default : System.out.println("Repeating Query #" + queryNumText.charAt(queryNumText.length() - 1));
                }
            });

            hBox.getChildren().addAll(queryIcon,queryChoiceBox,button);
            vBox.getChildren().add(0, queryNum);
            vBox.getChildren().add(1, hBox);

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

    private void restartProfiling()
    {

        Statement st    = null;
        try {

            st = conn.createStatement();
            st.executeQuery("    SET @@profiling = 0;");
            st.executeQuery("SET @@profiling_history_size = 0;");
            st.executeQuery("SET @@profiling_history_size = 100;");
            st.executeQuery("SET @@profiling = 1;");

            st.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: "		+ e.getSQLState());
            System.out.println("VendorError: "  + e.getErrorCode());
        }// catch (ClassNotFoundException e){

    }

    private BigDecimal getQueryProcessTime(int i)
    {

        Statement st    = null;
        ResultSet rs    = null;

        BigDecimal time = BigDecimal.valueOf(0);
        try {

            st = conn.createStatement();
            rs = st.executeQuery("SHOW PROFILES ");

            int row = 1;
            while(rs.next()){
                System.out.println(rs.getInt("Query_ID") + " || " +
                                   rs.getFloat("Duration") + " || " +
                                   rs.getString("Query") + " ::::::: " + i + " || " + row);
                if(row == i)
                    time = rs.getBigDecimal("Duration");
                row++;
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: "		+ e.getSQLState());
            System.out.println("VendorError: "  + e.getErrorCode());
        }// catch (ClassNotFoundException e){

        return time;
    }

    private void updateTableQuery1(VBox vBox)
    {
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
        dataSeries1.getData().add(new XYChart.Data( nQueryExec, processTime));

    }

    private void updateTableQuery2(VBox vBox)
    {
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
        dataSeries1.getData().add(new XYChart.Data( nQueryExec, processTime));

        vBox.getChildren().add(2, table);
    }


    private void updateTableQuery3(VBox vBox)
    {
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
        dataSeries1.getData().add(new XYChart.Data( nQueryExec, processTime));

        vBox.getChildren().add(2, table);
    }

    public ObservableList<ArrayList<String>> getQuery1()
    {

        //Connection conn = getConnection();	called at the start
        Statement st    = null;
        ResultSet rs    = null;
        String query    = "SELECT PublisherName AS 'Publisher', Address\n" +
                          "FROM publisher\n" +
                          "WHERE Address like '%Los Angeles%'\n" +
                          "ORDER BY PublisherName;";

        ObservableList<ArrayList<String>> arrayList = FXCollections.observableArrayList();

        try {

            st = conn.createStatement();
            rs = st.executeQuery(query);

            while(rs.next()){
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
            System.out.println("SQLState: "		+ e.getSQLState());
            System.out.println("VendorError: "  + e.getErrorCode());
        }// catch (ClassNotFoundException e){

        return arrayList;
    }


    public ObservableList<ArrayList<String>> getQuery2()
    {

        //Connection conn = getConnection();	called at the start
        Statement st    = null;
        ResultSet rs    = null;
        String query    = "SELECT BorrowerLName, BorrowerFName, Address\n" +
                "FROM borrower\n" +
                "WHERE Address LIKE '%Manila%'\n";

        ObservableList<ArrayList<String>> arrayList = FXCollections.observableArrayList();

        try {

            st = conn.createStatement();
            rs = st.executeQuery(query);

            while(rs.next()){
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
            System.out.println("SQLState: "		+ e.getSQLState());
            System.out.println("VendorError: "  + e.getErrorCode());
        }// catch (ClassNotFoundException e){

        return arrayList;
    }

    public ObservableList<ArrayList<String>> getQuery3()
    {

        //Connection conn = getConnection();	called at the start
        Statement st    = null;
        ResultSet rs    = null;
        String query    = "SELECT CONCAT(BO.BorrowerLName, ', ', BO.BorrowerFName) AS BorrowerName, COUNT(*) as NoBooksBor\n" +
                          "FROM borrower BO, book_loans BL\n" +
                          "WHERE BO.CardNo = BL.CardNo\n" +
                          "GROUP BY BorrowerName\n" +
                          "HAVING NoBooksBor >= 0 and NoBooksBor <=2\n" +
                          "ORDER BY 2 DESC, 1;\n";

        ObservableList<ArrayList<String>> arrayList = FXCollections.observableArrayList();

        try {

            st = conn.createStatement();
            rs = st.executeQuery(query);

            while(rs.next()){
                ArrayList<String> rowData = new ArrayList<>();
                rowData.add(rs.getString("BorrowerName"));
                rowData.add(rs.getString("NoBooksBor"));
                arrayList.add(rowData);
            }
            st.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: "		+ e.getSQLState());
            System.out.println("VendorError: "  + e.getErrorCode());
        }// catch (ClassNotFoundException e){

        return arrayList;
    }

    private void terminateProgram(){
        if(conn != null)
            try{
                conn.close();
            }catch(SQLException e){
                System.out.println("SQLException: " + e.getMessage());
            }
        System.out.println("\nProgram has been terminated.");
        Platform.exit();
        System.exit(0);
    }

    public Connection getConnection(){
        Connection tempConn = null;

        String driver   = "com.mysql.jdbc.Driver";
        String db       = "mco1_db";
        String url      = "jdbc:mysql://localhost/" + db + "?useSSL=false";
        String user     = "root";
        String pass     = "hopekook";

        try {
            Class.forName(driver);
            tempConn = DriverManager.getConnection(url,user,pass);
            System.out.println("Connected to database : " + db);
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: "		+ e.getSQLState());
            System.out.println("VendorError: "  + e.getErrorCode());
        } catch (ClassNotFoundException e){
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