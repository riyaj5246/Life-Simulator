package sample;

import java.util.ArrayList;
import java.util.Random;

public class Elk {
    private int x;
    private int y;
    private int age;
    private int serialNum;
    private boolean gender; //0 - male, 1 - female
    private long startTime;
    private int prevX;
    private int prevY;

    //constructor - x and y represent location, z represents serial number, gender randomly determined
    public Elk(int x,int y, int z){
        this.x = x;
        this.y = y;
        this.age = 0;
        this.serialNum = z;
        Random random = new Random();
        boolean randGender = random.nextBoolean();
        gender = randGender;
        startTime = System.nanoTime();
        prevX = x;
        prevY = y;
    }

    //method called to move the elk from one cell to another
    public void changeLoc(int[][] gameGrid, boolean[][] fireGrid){

        ArrayList<Integer> potentialCoordinatesX = new ArrayList<>();
        ArrayList<Integer> potentialCoordinatesY = new ArrayList<>();

        //collects full list of potential movement locations (accounts for out of bounds, presence of fire/other animals, and previous location
        for (int a = x - 1; a <= x + 1; a++) {
            for (int b = y - 1; b <= y + 1; b++) {
                if( a < 35 && a > -1 && b < 23 && b > -1){
                    if (gameGrid[a][b] == 0 && !fireGrid[a][b]) {
                        if(!(a == prevX && b == prevY)){
                            potentialCoordinatesX.add(a);
                            potentialCoordinatesY.add(b);
                        }
                    }
                }
            }
        }

        if(potentialCoordinatesX.size() == 0){
            //System.out.println("no movement");
            return;
        }
        else{
            //moves animal to new location
            int randIndex = (int) (Math.random() * potentialCoordinatesX.size());
            int xCoordinate = potentialCoordinatesX.get(randIndex);
            int yCoordinate = potentialCoordinatesY.get(randIndex);
            gameGrid[xCoordinate][yCoordinate]= this.getType();
            gameGrid[x][y]=0;
            prevX = x;
            prevY = y;
            x=xCoordinate;
            y=yCoordinate;
        }
    }

    //based on population size and predetermined probability of death, kills elk off
    public String killOffElk(ArrayList elk, int[][] grid){
        double probability = determineProbabilities();
        if(elk.size() < 10){
            probability += 0.1;
        }
        else if(elk.size() > 30 && age > 10){
            probability -= 0.1;
        }
        if(Math.random() > probability + 0.08){
            elk.remove(this);
            grid[this.x][this.y] = 0;
            return ("Rest in Peace Elk " + this.getSerialNum() + ". You died at " + this.age + " of natural causes.");
        }
        return "";
    }

    //checks if all preconditions for a new baby are met (elk is adult female, and male elk is nearby)
    public int[] checkForBaby(int[][] gameGrid, ArrayList elkList){
        boolean malePresent = false;
        int range = 1;
        if(elkList.size() < 15){
            range = 2;

        }
        if(this.getType() ==2){ //if elk is adult female
            for (int x = this.getX() - range; x <= (this.getX() + range); x++) {
                for (int y = this.getY() - range; y <= this.getY() + range; y++) {
                    if( x < 35 && x > -1 && y < 23 && y > -1) {
                        if (gameGrid[x][y] == 4) { //is an adult male
                            malePresent = true;
                        }
                    }
                }
            }
        }
        if(malePresent) {
            if (Math.random() < (this.determineProbabilities() + 0.2)) {
                //System.out.println("Range expanded: " + rangeLarge);
                return newBaby(gameGrid);
            }
        }
        int[] none = {100, 100}; //returned if no baby
        return none;
    }

    //called when preconditions for new baby are met, leads to birth of new baby in a cell next to mother
    private int[] newBaby(int[][] grid){
        ArrayList<Integer> potentialCoordinatesX = new ArrayList<>();
        ArrayList<Integer> potentialCoordinatesY = new ArrayList<>();

        for (int x = this.getX() - 1; x <= this.getX() + 1; x++) {
            for (int y = this.getY() - 1; y <= this.getY() + 1; y++) {
                if( x < 35 && x > -1 && y < 23 && y > -1){
                    if (grid[x][y] == 0) {
                        potentialCoordinatesX.add(x);
                        potentialCoordinatesY.add(y);
                    }
                }
            }
        }
        if (potentialCoordinatesX.size() > 0) {
            int randIndex = (int) (Math.random() * potentialCoordinatesX.size());
            int[] coordinates = {potentialCoordinatesX.get(randIndex), potentialCoordinatesY.get(randIndex)};
            return coordinates;
        }
        int[] noSpots = {100, 100};
        return noSpots;
    }

    //determines probabilities used for simulated birth/death based on age
    public double determineProbabilities(){
        if(this.age < 10){
            return 0.98;
        }
        else if(this.age  < 15){
            return 0.8;
        }
        else if(this.age  < 19){
            return 0.7;
        }
        else if(this.age  < 22){
            return 0.6;
        }
        else{
            return 0.2;
        }
    }

    //returns type of elk (gender, age)
    public int getType(){
        if(this.age <= 5 && this.gender){ //female, kid
            return 1;
        }
        else if(this.age > 5 && this.gender){ //female, adult
            return 2;
        }
        else if(this.age <= 5 && !(gender)){ //male, kid
            return 3;
        }
        else{ //male, adult
            return 4;
        }
    }

    public int getX(){
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getSerialNum() { return this.serialNum;}

    public void increaseAge(){
        this.age++;
    }

    public int getAge(){
        return this.age;
    }

    public String getGender(){
        if(this.gender){
            return "Female";
        }
        else{
            return "Male";
        }
    }

    public void resetStartTime(){
        startTime = System.nanoTime();
    }

    public long getStartTime(){
        return startTime;
    }

}

