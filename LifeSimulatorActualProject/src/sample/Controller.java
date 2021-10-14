package sample;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import javax.print.attribute.IntegerSyntax;
import java.sql.SQLOutput;
import java.util.ArrayList;

public class Controller {
    private int x = 35;
    private int y = 23;
    private Button[][] btn = new Button[x][y];
    private int[][] gameGrid = new int[x][y];

    private boolean[][] isFire = new boolean[x][y];
    private boolean [][] isTornado = new boolean[x][y];
    private int[] tornadoDirection = new int[2];

    private Grass[][] grassGrid = new Grass[x][y];
    private ArrayList<Elk> elkList = new ArrayList<>();
    private ArrayList<Wolf> wolfList = new ArrayList<>();
    private ArrayList<Disaster> disasterList = new ArrayList<>();

    private ArrayList<Integer> elkPopulationData = new ArrayList<>();
    private ArrayList<Integer> wolfPopulationData = new ArrayList<>();
    private long populationTimer = System.nanoTime();
    private boolean enableDataCollection = false;

    private int elkSerialCounter = 0;
    private int wolfSerialCounter = 0;

    private long elkReproductionTimer = System.nanoTime();
    private long elkDeathTimer = System.nanoTime();

    private long wolfReproductionTimer = System.nanoTime();
    private long wolfDeathTimer = System.nanoTime();

    private long disastersTimer = System.nanoTime();
    private int disastersCounter = 0;

    private double grassGrowthDuration = 1500000000.0;

    Image key = new Image("Resources/key.JPG");

    @FXML
    LineChart lChart;

    @FXML
    private ImageView keyImageView;

    @FXML
    private ListView updatesList, gridInfo;

    @FXML
    private GridPane gPane;

    @FXML
    private Button terminateDisasterBtn, clearGridBtn, addElkBtn, defaultPopBtn, addWolfBtn, startBtn, natDisasterBtn, workingPopBtn, removeElkBtn, removeWolfBtn;

    //initializes grassGrid, displays simulation screen, enables population controls
    @FXML
    private void handleStart(ActionEvent event) {
        //enables all simulation control buttons
        startBtn.setDisable(true);
        addElkBtn.setDisable(false);
        addWolfBtn.setDisable(false);
        defaultPopBtn.setDisable(false);
        natDisasterBtn.setDisable(false);
        workingPopBtn.setDisable(false);
        removeElkBtn.setDisable(false);
        removeWolfBtn.setDisable(false);
        clearGridBtn.setDisable(false);
        terminateDisasterBtn.setDisable(false);

        keyImageView.setImage(key);
        //sets up gridview so that there is a button in each cell
        for (int i = 0; i < btn.length; i++) {
            for (int j = 0; j < btn[0].length; j++) {
                //Initializing 2D buttons with values i,j
                btn[i][j] = new Button();
                btn[i][j].setStyle("-fx-background-color:#d3d3d3");
                isFire[i][j] = false;
                btn[i][j].setPrefWidth(25);
                gPane.add(btn[i][j], i, j);
                gameGrid[i][j] = 0;
            }
        }
        gPane.setGridLinesVisible(true);
        gPane.setVisible(true);

        handleAddGrass();

        //when cell in grid is clicked, information about cell properties is displayed
        EventHandler z = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                int yPos = gPane.getRowIndex(((Button) t.getSource()));
                int xPos = gPane.getColumnIndex(((Button) t.getSource()));
                gridInfo.getItems().clear();
                if(isFire[xPos][yPos]){ //in case of fire
                     gridInfo.getItems().add("Forest fire");
                     gridInfo.getItems().add("Grass: 0% (burnt)");
                     gridInfo.getItems().add("Animals: none (dead)");
                }
                else if(isTornado[xPos][yPos]){ //in case of tornado
                    gridInfo.getItems().add("Tornado");
                    gridInfo.getItems().add("Grass: normal");
                    gridInfo.getItems().add("Animals: none (dead)");
                }
                else if(gameGrid[xPos][yPos] == 0){ //in case of empty (just grass)
                    gridInfo.getItems().add("Grass");
                    gridInfo.getItems().add("Percent grown: " + 100* (grassGrid[xPos][yPos].getPercentGrown()) + "%");
                    if(grassGrowthDuration > 1500000000.0){
                        gridInfo.getItems().add("Growth rate: slowed (drought)");
                    }
                }
                else if(gameGrid[xPos][yPos] > 0 && gameGrid[xPos][yPos] < 5){ //in case of elk
                    gridInfo.getItems().add("Elk");
                    for(Elk e : elkList){
                        if(e.getX() == xPos && e.getY() == yPos){
                            gridInfo.getItems().add("Gender: " + e.getGender());
                            gridInfo.getItems().add("Age: " + e.getAge() );
                            gridInfo.getItems().add("Serial Num: " + e.getSerialNum());
                            break;
                        }
                    }
                }
                else{ //in case of wolf
                    gridInfo.getItems().add("Wolf");
                    for(Wolf w : wolfList){
                        if(w.getX() == xPos && w.getY() == yPos){
                            gridInfo.getItems().add("Gender: " + w.getGender());
                            gridInfo.getItems().add("Age: " + w.getAge() );
                            gridInfo.getItems().add("Serial Num: " + w.getSerialNum());
                            break;
                        }
                    }
                }
                gridInfo.getItems().add("X Position: " + xPos);
                gridInfo.getItems().add("Y Position: " + yPos);
            }

        };
        for (int i = 0; i < btn.length; i++) {
            for (int j = 0; j < btn[0].length; j++) {
                btn[i][j].setOnMouseClicked(z);
            }
        }
        start();
    }

    //creates an instance of the Grass class for every cell on the grid
    private void handleAddGrass() {
        for (int i = 0; i < btn.length; i++) {
            for (int j = 0; j < btn[0].length; j++) {
                grassGrid[i][j] = new Grass();
            }
        }
        updateScreen();
    }

    @FXML //called when simulation is cleared/restarted...clears animal populations, simulation data, update data
    private void handleClearGrid(){
        elkList.clear();
        wolfList.clear();
        for (int i = 0; i < btn.length; i++) {
            for (int j = 0; j < btn[0].length; j++) {
                gameGrid[i][j] = 0;
                while(grassGrid[i][j].getPercentGrown() != 1){
                    grassGrid[i][j].increasePercent();
                }
            }
        }
        wolfPopulationData.clear();
        elkPopulationData.clear();
        showData();
        enableDataCollection = false;
        updatesList.getItems().clear();
        updatesList.getItems().add(0, "Grid and all population data was cleared!");
    }

    @FXML //adds elk in random unfilled spot on grid
    private void handleAddElk() {
        enableDataCollection = true;
        int randX = (int) (Math.random() * 35);
        int randY = (int) (Math.random() * 23);

        while (gameGrid[randX][randY] != 0) {
            randX = (int) (Math.random() * 35);
            randY = (int) (Math.random() * 23);
        }

        elkList.add(new Elk(randX, randY, elkSerialCounter));
        elkSerialCounter++;
        Elk newElk = elkList.get(elkList.size() - 1);
        gameGrid[newElk.getX()][newElk.getY()] = newElk.getType();
        updateScreen();
    }

    @FXML //adds wolf in random unfilled spot on grid
    private void handleAddWolves(){
        enableDataCollection = true;
        int randX = (int) (Math.random() * 35);
        int randY = (int) (Math.random() * 23);

        while (gameGrid[randX][randY] != 0) {
            randX = (int) (Math.random() * 35);
            randY = (int) (Math.random() * 23);
        }

        wolfList.add(new Wolf(randX, randY, wolfSerialCounter));
        wolfSerialCounter++;
        Wolf newWolf = wolfList.get(wolfList.size() - 1);
        gameGrid[newWolf.getX()][newWolf.getY()] = newWolf.getType();
        updateScreen();
    }

    @FXML //kills any natural disaster, resetting grid to default
    private void handleTerminateDisaster(){
        disasterList.clear();
        disastersCounter = 0;
        natDisasterBtn.setDisable(false);
        restartDisastersTimer();
        for (int i = 0; i < btn.length; i++) {
            for (int j = 0; j < btn[0].length; j++) {
                isFire[i][j] = false;
                isTornado[i][j] = false;
            }
        }
        grassGrowthDuration = 1500000000.0;
        addToUpdatesList("Natural disaster has been terminated!");
    }

    @FXML //chooses a random elk and removes from simulation
    private void handleRemoveElk(){
        if(!(elkList.size() == 0)){
            Elk removed = elkList.get((int) (Math.random() * elkList.size()));
            gameGrid[removed.getX()][removed.getY()] = 0;
            addToUpdatesList("Elk " + removed.getSerialNum() + " on (" + removed.getX() + ", " + removed.getY() + ") was removed.");
            elkList.remove(removed);
        }
    }

    @FXML //chooses a random wolf and removes from simulation
    private void handleRemoveWolf(){
        if(!(wolfList.size() == 0)){
            Wolf removed = wolfList.get((int) (Math.random() * wolfList.size()));
            gameGrid[removed.getX()][removed.getY()] = 0;
            addToUpdatesList("Wolf " + removed.getSerialNum() + " on (" + removed.getX() + ", " + removed.getY() + ") was removed.");
            wolfList.remove(removed);
        }
    }

    @FXML //adds 20 randomly placed elk and 20 randomly placed wolves into simulation
    private void handleDefaultSetup(){
        for(int i = 0; i < 20; i++){
            handleAddElk();
        }
        for(int a = 0; a < 20; a++){
            handleAddWolves();
        }
    }

    @FXML //adds strategically placed elk and wolves into simulation
    private void handleWorkingPop(){
        enableDataCollection = true;
        int[] wolfXCoordinates = new int[]{ 20, 25, 25, 30, 20, 15, 17, 20, 20, 21, 22, 23}; //34
        int[] wolfYCoordinates = new int[]{ 10, 15, 20, 9, 9, 15, 15, 7, 6, 20, 18, 19}; //22

        for(int i = 0; i < wolfXCoordinates.length; i++){
            wolfList.add(new Wolf(wolfXCoordinates[i], wolfYCoordinates[i], wolfSerialCounter));
            wolfSerialCounter++;
            Wolf newWolf = wolfList.get(wolfList.size() - 1);
            gameGrid[newWolf.getX()][newWolf.getY()] = newWolf.getType();
        }

        int[] elkXCoordinates = new int[]{ 10, 20, 15, 23, 10, 30, 20, 6, 5, 30, 5, 8, 4, 2, 16, 21}; //34
        int[] elkYCoordinates = new int[]{ 8, 13, 13, 6, 10, 14, 15, 20, 18, 15, 17, 5, 3, 5, 15, 19}; //22

        for(int i = 0; i < elkXCoordinates.length; i++){
            elkList.add(new Elk(elkXCoordinates[i], elkYCoordinates[i], elkSerialCounter));
            elkSerialCounter++;
            Elk newElk = elkList.get(elkList.size() - 1);
            gameGrid[newElk.getX()][newElk.getY()] = newElk.getType();
        }

        //handleDefaultSetup();

    }

    @FXML //creates a new instance of disaster - type and duration randomly chosen
    private void handleDisaster(){
        natDisasterBtn.setDisable(true);
        int time = (int)(Math.random() * 5) + 6; //anywhere from 6 to 10 seconds
        int type = (int) (Math.random() * 3);
        disasterList.add(new Disaster(type, time));
        if(type == 0){
            startFire();
        }
        else if(type == 1){
            startDrought();
        }
        else{
            startTornado();
        }
    }

    //updates simulation gridview colors based on the gameGrid values of each cell
    private void updateScreen() {
        for (int i = 0; i < btn.length; i++) {
            for (int j = 0; j < btn[0].length; j++) {
                if(!isFire[i][j] && !isTornado[i][j]){
                    if (gameGrid[i][j] == 0) {
                        //btn[i][j].setStyle(grassGrid[i][j].getGrassColor());
                        btn[i][j].setStyle("-fx-background-color:#008704");
                    } else if (gameGrid[i][j] == 1) {
                        btn[i][j].setStyle("-fx-background-color:#FF9F97"); //elk-female-kid
                    } else if (gameGrid[i][j] == 2) {
                        btn[i][j].setStyle("-fx-background-color:#F26055"); //elk-female-adult
                    } else if (gameGrid[i][j] == 3) {
                        btn[i][j].setStyle("-fx-background-color:#FFE169"); //elk-male-kid
                    } else if (gameGrid[i][j] == 4) {
                        btn[i][j].setStyle("-fx-background-color:#ECA000"); //elk-male-adult
                    } else if (gameGrid[i][j] == 5) {
                        btn[i][j].setStyle("-fx-background-color:#FB96E4"); //wolf-female-kid
                    } else if (gameGrid[i][j] == 6) {
                        btn[i][j].setStyle("-fx-background-color:#FB37CE"); //wolf-female-adult
                    } else if (gameGrid[i][j] == 7) {
                        btn[i][j].setStyle("-fx-background-color:#9A9DEF"); //wolf-male-kid
                    } else if (gameGrid[i][j] == 8) {
                        btn[i][j].setStyle("-fx-background-color:#5257F2"); //wolf-male-adult
                    }
                }
            }
        }
    }

    //recursive loop with all the timer related actions
    private void start() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                //grass grows continuously (rate of growth dependent on external conditions)
                for (int i = 0; i < grassGrid.length; i++) {
                    for (int j = 0; j < grassGrid[0].length; j++) {
                        if (now - grassGrid[i][j].getGrowthTimer() > grassGrowthDuration) {
                            grassGrid[i][j].increasePercent();
                            grassGrid[i][j].restartGrassTimer();
                        }
                    }
                }
                //elk population related factors
                if(elkList.size() > 0) {
                    //each elk moves to a new location on the grid, eats grass, and ages every one second
                    for (int e = 0; e < elkList.size(); e++) {
                            if (now - elkList.get(e).getStartTime() > 1000000000.0) { //one second
                            elkList.get(e).changeLoc(gameGrid, isFire);
                            grassGrid[elkList.get(e).getX()][elkList.get(e).getY()].grassEaten();
                            elkList.get(e).increaseAge();
                            elkList.get(e).resetStartTime();
                            checkIsEnoughGrass(elkList.get(e));
                        }
                    }
                    // checks whether elk or not elk have reproduced every 1.1 seconds
                    if (now - elkReproductionTimer > 1100000000.0) {
                        for (int e = 0; e < elkList.size(); e++) {
                            int[] newCoordinates = elkList.get(e).checkForBaby(gameGrid, elkList);
                            if (newCoordinates[0] != 100) {
                                elkList.add(new Elk(newCoordinates[0], newCoordinates[1], elkSerialCounter));
                                elkSerialCounter++;
                                gameGrid[newCoordinates[0]][newCoordinates[1]] = elkList.get(elkList.size() - 1).getType();
                                addToUpdatesList("Congrats! Elk " + elkList.get(elkList.size() - 1).getSerialNum() + " was born!");
                            }
                        }
                        restartElkReproductionTimer();
                    //checks if any elk have died due to old age every 4 seconds
                    }
                    if (now - elkDeathTimer > 4000000002.0) {
                        for (int i = 0; i < elkList.size(); i++) {
                            String deathUpdate = elkList.get(i).killOffElk(elkList, gameGrid);
                            if (!deathUpdate.equals("")) {
                                addToUpdatesList(deathUpdate);
                            }
                        }
                        restartElkDeathTimer();
                    }
                }
                //wolf population related factors
                if(wolfList.size() > 0){
                    //timer allows every wolf to move/eat elk every 0.8 seconds
                    for (int w = 0; w < wolfList.size(); w++) {
                        if (now - wolfList.get(w).getStartTime() > 800000000.0) { //0.8 second
                            if (wolfList.get(w).changeLoc(gameGrid, isFire)){
                                wolfEatsElk(wolfList.get(w));
                            }
                            wolfList.get(w).resetStartTime();
                            checkWolfNutrition(wolfList.get(w));

                        }
                    }
                    //checks whether any wolves have reproduced every 1 second
                    if (now - wolfReproductionTimer > 1000000001.0){
                        for (int e = 0; e < wolfList.size(); e++) {
                            wolfList.get(e).increaseAge();
                            int[] newCoordinates = wolfList.get(e).checkForBaby(gameGrid, wolfList);
                            if (newCoordinates[0] != 100) {
                                wolfList.add(new Wolf(newCoordinates[0], newCoordinates[1], wolfSerialCounter));
                                wolfSerialCounter++;
                                gameGrid[newCoordinates[0]][newCoordinates[1]] = wolfList.get(wolfList.size() - 1).getType();
                                addToUpdatesList("Congrats! Wolf " + wolfList.get(wolfList.size() - 1).getSerialNum() + " was born!");
                            }
                        }
                        restartWolfReproductionTimer();
                    }
                    //checks if any wolves have died due to old age every 4 seconds
                    if (now - wolfDeathTimer > 4000000001.0) {
                        for (int i = 0; i < wolfList.size(); i++) {
                            String deathUpdate = wolfList.get(i).killOffWolf(wolfList, gameGrid);
                            if (!deathUpdate.equals("")) {
                                addToUpdatesList(deathUpdate);
                            }
                        }
                        restartWolfDeathTimer();
                    }
                }

                //adds population data and updates chart every two seconds
                if(now - populationTimer > 2000000007.0 && enableDataCollection){
                    elkPopulationData.add(elkList.size());
                    wolfPopulationData.add(wolfList.size());
                    showData();
                    //System.out.println(populationData);
                    restartPopulationTimer();
                }
                //only called if and when there is a natural disaster has been called
                if(disasterList.size() > 0){
                    int duration = disasterList.get(0).getDuration();
                    if(disasterList.get(0).getType() == 2){ //if tornado, duration is irrelevant - will just go off the board
                        duration += 100;
                    }
                    //checks for when disaster duration ends - at that point, kills of current disaster and enables possibility of new disaster
                        if(now - disastersTimer > 1000000002.0){
                            disastersCounter++;
                            if(disasterList.get(0).getType() == 2){
                                moveTornado2();
                            }
                            if(disastersCounter == duration){
                                disastersCounter = 0;
                                switch(disasterList.get(0).getType()){
                                    case(0):
                                        System.out.println("kill fire");
                                        killFire();
                                        break;
                                    case(1):
                                        System.out.println("kill drought");
                                        killDrought();
                                        break;

                                    case(2):
                                        System.out.println("kill tornado");
                                        killTornado();
                                        break;
                                }
                                disasterList.remove(0);
                                natDisasterBtn.setDisable(false);
                            }
                            restartDisastersTimer();
                        }
                }
                updateScreen();

            }
        }.start(); //recursive function
    }

    //determines if wolf ate any elk during its move, removes said elk from simulation, and posts updates
    private void wolfEatsElk(Wolf w){
        int x = w.getX();
        int y = w.getY();
        for(Wolf a : wolfList){
            if(a.getX() >= x - 2 && a.getX() <= x + 2 && a.getY() >= x - 2 && a.getY() <= x + 2){
                a.resetSustainability();
                addToUpdatesList("Wolf " + a.getSerialNum() + " ate today! (Thanks to Wolf " + w.getSerialNum() + ")." );
            }
        }
        for(Elk e : elkList){
            if(e.getX() == x && e.getY() == y){
                elkList.remove(e);
                addToUpdatesList("Wolf " + w.getSerialNum() + " killed Elk " + e.getSerialNum() + ". Rip.");
                return;
            }
        }
    }

    //for each wolf, makes sure that they have enough food (have recently eaten an elk) and if not, kills them
    private void checkWolfNutrition(Wolf wolf){
        if(wolf.getSustainability() > 15){
            if(Math.random() > 0.7){
                gameGrid[wolf.getX()][wolf.getY()] = 0;
                wolfList.remove(wolf);
                addToUpdatesList("Wolf " + wolf.getSerialNum() + " died due to lack of food (could not eat an elk in time).");
            }
        }
    }

    //for each elk, makes sure they have enough grass accessible to them, and if not, kills them
    private void checkIsEnoughGrass(Elk elk){
        double grassSum = 0;
        for (int x = elk.getX() - 1; x <= (elk.getX() + 1); x++) {
            for (int y = elk.getY() - 1; y <= elk.getY() + 1; y++) {
                if(x >= 0 && x <= 34 && y >= 0 && y <= 22){
                    grassSum += grassGrid[x][y].getPercentGrown();
                }
            }
        }
        boolean doesDie = false;
        if(grassSum >= 4.5){
            return;
        }
        if(grassSum > 3.5){
            if(Math.random() > 0.30){
                doesDie = true;
            }
        }
        else if(grassSum > 3){
            if(Math.random() > 0.40){
                doesDie = true;
            }
        }
        else if (grassSum > 2.5){
            if(Math.random() > 0.50){
                doesDie = true;
            }
        }
        else if (grassSum > 1){
            if(Math.random() > 0.60){
                doesDie = true;
            }
        }
        else{
            doesDie = true;
        }

        if(doesDie){
            gameGrid[elk.getX()][elk.getY()] = 0;
            elkList.remove(elk);
            addToUpdatesList("Elk " + elk.getSerialNum() + " died due to lack of sufficient grass (caused by overgrazing).");
        }
    }

    //creates a fire on a random 5x5 plot on grid and kills any animals on plot
    private void startFire(){
        int xLoc = (int)(Math.random() * (32 - 2)) + 2;
        int yLoc = (int)(Math.random() * (20 - 2)) + 2;

        ArrayList<Elk> killedElk = new ArrayList<>();
        ArrayList<Wolf> killedWolves = new ArrayList<>();

        for(int i = xLoc - 2; i <= xLoc + 2; i++){
            for(int j = yLoc - 2; j <= yLoc + 2; j++){
                isFire[i][j] = true;
                btn[i][j].setStyle("-fx-background-color:#008704;" + "-fx-background-image: url(resources/fire.png);"+ "-fx-background-size: 25px 25px;"+
                                "-fx-background-repeat: no-repeat;"+ "-fx-background-position: center;");

                for(Elk e : elkList){
                    if(e.getX() == i && e.getY() ==j){
                        killedElk.add(e);
                        gameGrid[i][j] = 0;
                    }
                }
                for(Wolf w : wolfList){
                    if(w.getX() == i && w.getY() == j){
                        killedWolves.add(w);
                        gameGrid[i][j] = 0;
                    }
                }
            }
        }

        String elksDisplayString = "";
        while(killedElk.size() > 0){
            elksDisplayString += killedElk.get(0).getSerialNum() + ", ";
            elkList.remove(killedElk.remove(0));
        }
        String wolfDisplayString = "";
        while(killedWolves.size() > 0){
            wolfDisplayString += killedWolves.get(0).getSerialNum() + ", ";
            wolfList.remove(killedWolves.remove(0));
        }
        String finalDisplayString = "There is a fire in the forest. ";
        if(!elksDisplayString.equals("")){
            elksDisplayString = elksDisplayString.substring(0, elksDisplayString.length() - 2);
            finalDisplayString += "Elks " + elksDisplayString + " died.";
        }
        if(!wolfDisplayString.equals("")){
            wolfDisplayString = wolfDisplayString.substring(0, wolfDisplayString.length() - 2);
            finalDisplayString += " Wolves " + wolfDisplayString + " died.";
        }

        addToUpdatesList(finalDisplayString);
    }

    //ends fire once duration of fire has passed or user ends disaster
    private void killFire(){
        for(int i = 0; i < 35; i++){
            for(int j = 0; j < 23; j++){
                if(isFire[i][j]){
                    isFire[i][j]= false;
                    grassGrid[i][j].destroyGrass();

                }
            }
        }
        addToUpdatesList("The fire has ended!");
    }

    //begins drought be slowing down the duration at which grass grows
    private void startDrought(){
        grassGrowthDuration = 5000000002.0;
        addToUpdatesList("There's a drought! All the grass is going to grow very slowly since there isn't enough water!");

    }

    //ends drought by returning grass growth duration back to original value
    private void killDrought(){
        addToUpdatesList("Drought ended, so grass will grow normally again!");
        grassGrowthDuration = 1500000000.0;
    }

    //generates tornado at random location and determines direction of movement based on initial position
    private void startTornado(){
        addToUpdatesList("Oh no! There's a tornado. Any animals in its path will be killed.");

        int xLoc = (int)(Math.random() * (34));
        int yLoc = (int)(Math.random() * (22));
        for(int i = xLoc; i <= xLoc + 1; i++) {
            for (int j = yLoc; j <= yLoc + 1; j++) {
                isTornado[i][j] = true;
                btn[i][j].setStyle("-fx-background-color:#008704;" + "-fx-background-image: url(resources/tornado.png);" + "-fx-background-size: 25px 25px;" +
                        "-fx-background-repeat: no-repeat;" + "-fx-background-position: center;");
            }
        }
        if(xLoc > 17){
            tornadoDirection[0] = -1;
        }
        else{
            tornadoDirection[0] = 1;
        }
        if(yLoc > 11){
            tornadoDirection[1] = -1;
        }
        else{
            tornadoDirection[1] = 1;
        }
    }

    //moves tornado based on pre-established direction and kills any animals in path
    private void moveTornado2(){
        ArrayList<Integer> newX = new ArrayList<>();
        ArrayList<Integer> newY = new ArrayList<>();
        for(int i = 0; i < isTornado.length; i++){
            for(int j = 0; j < isTornado[i].length; j++){
                if(isTornado[i][j]){
                    isTornado[i][j] = false;
                    if(i + tornadoDirection[0] > -1 && i + tornadoDirection[0] < 35 && j + tornadoDirection[1] > -1 && j + tornadoDirection[1] < 23){
                        newX.add(i + tornadoDirection[0]);
                        newY.add(j + tornadoDirection[1]);
                    }
                }
            }
        }
        String killedList = "";
        for(int x: newX){
            for(int y: newY){
                for(int i = x - 2; i <= x + 2; i++){
                    for(int j = y - 2; j <= y + 2; j++){
                        String sideKills = checkIfAnimalKilled(i,j);
                        if(!sideKills.equals("")){
                            killedList += sideKills + ", ";
                        }
                    }
                }
                isTornado[x][y] = true;
                btn[x][y].setStyle("-fx-background-color:#008704;" + "-fx-background-image: url(resources/tornado.png);" + "-fx-background-size: 25px 25px;" + "-fx-background-repeat: no-repeat;" + "-fx-background-position: center;");
            }
        }
        if(!killedList.equals("")){
            killedList = killedList.substring(0, killedList.length() - 2) + " died due to the tornado.";
            addToUpdatesList(killedList);
        }
        if(newX.size()==0){
            natDisasterBtn.setDisable(false);
            killTornado();
            disasterList.remove(0);
            addToUpdatesList("The tornado has exited the field. All the animals are now safe!.");
        }
    }

    //checks if animal is at a specific location, and if so, kills animal
    private String checkIfAnimalKilled(int xLoc, int yLoc){
        String casualty = "";
        for(int e = 0; e < elkList.size(); e++){
            if(elkList.get(e).getX() ==xLoc && elkList.get(e).getY() == yLoc){
                casualty += "Elk " + elkList.get(e).getSerialNum();
                elkList.remove(e);
                gameGrid[xLoc][yLoc] = 0;
            }
        }

        for(int w = 0; w < wolfList.size(); w++){
            if(wolfList.get(w).getX() == xLoc && wolfList.get(w).getY() == yLoc){
                casualty += "Wolf " + wolfList.get(w).getSerialNum();
                wolfList.remove(w);
                gameGrid[xLoc][yLoc] = 0;
            }
        }
        return casualty;
    }

    //kills off tornado once it is off the grid
    private void killTornado(){
        for(int i = 0; i < 35; i++){
            for(int j = 0; j < 23; j++){
                isTornado[i][j] = false;
            }
        }
    }

    //timer restarting methods (separate for each timer to avoid threading errors)
    private void restartElkReproductionTimer() {
        elkReproductionTimer = System.nanoTime();
    }
    private void restartElkDeathTimer() {
        elkDeathTimer = System.nanoTime();
    }
    private void restartWolfDeathTimer(){ wolfDeathTimer = System.nanoTime();}
    private void restartWolfReproductionTimer(){ wolfReproductionTimer = System.nanoTime(); }
    private void restartDisastersTimer(){ disastersTimer = System.nanoTime();}
    private void restartPopulationTimer(){ populationTimer = System.nanoTime(); }

    //adds given string to updates list
    private void addToUpdatesList(String x) {
        updatesList.getItems().add(0, x);
        //System.out.println(x);
    }

    //graphs most updated elk and wolf population data on line chart
    private void showData(){
        lChart.getData().clear();
        XYChart.Series elkSeries = new XYChart.Series();
        elkSeries.setName("Elk Population Size");
        elkSeries.getData().clear();

        XYChart.Series wolfSeries = new XYChart.Series();
        wolfSeries.setName("Wolf Population Size");
        wolfSeries.getData().clear();

        for (int i = determineStartingPoint(); i < elkPopulationData.size(); i++) {
            elkSeries.getData().add(new XYChart.Data(((i+1)*3) + "", elkPopulationData.get(i)));
            wolfSeries.getData().add(new XYChart.Data(((i+1)*3) + "", wolfPopulationData.get(i)));

        }
        lChart.getData().add(elkSeries);
        lChart.getData().add(wolfSeries);
    }

    //used to determine staring point of data displayed (to maintain a cap on how much data can be on the screen at once
    private int determineStartingPoint(){
        if(elkPopulationData.size() <= 15){
            return 0;
        }
        else{
            return (elkPopulationData.size() - 15);
        }
    }
}