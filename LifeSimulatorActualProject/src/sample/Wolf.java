package sample;

import java.util.ArrayList;
import java.util.Random;

public class Wolf {
    private int x;
    private int y;
    private int age;
    private int serialNum;
    private boolean gender; //0 - male, 1 - female
    private long startTime;
    private int sustainability; //records the number of days wolf goes without eating

    //constructor - x and y represent location, z represents serial number, gender randomly determined
    public Wolf(int x,int y, int z){
        this.x = x;
        this.y = y;
        this.age = 0;
        this.serialNum = z;
        Random random = new Random();
        boolean randGender = random.nextBoolean();
        gender = randGender;
        startTime = System.nanoTime();
        sustainability = 0;
    }

    //method called to move the wolf from one cell to another
    public boolean changeLoc(int[][] gameGrid, boolean[][]fireGrid){

        ArrayList<Integer> potentialCoordinatesX = new ArrayList<>();
        ArrayList<Integer> potentialCoordinatesY = new ArrayList<>();

        //collects full list of potential movement locations (accounts for out of bounds and presence of fire/other wolves
        for (int a = x - 1; a <= x + 1; a++) {
            for (int b = y - 1; b <= y + 1; b++) {
                if( a < 35 && a > -1 && b < 23 && b > -1){
                    if (gameGrid[a][b] < 5 && !fireGrid[a][b]) { //empty or has elk
                        potentialCoordinatesX.add(a);
                        potentialCoordinatesY.add(b);
                    }
                }
            }
        }

        if(potentialCoordinatesX.size() == 0){
            //System.out.println("no movement");
            return false;
        }
        else{
            //moves wolf to new location, returning true if wolf ate an elk
            int xCoordinate = this.x;
            int yCoordinate = this.y;
            boolean eaten = false;
            for(int i = 0; i < potentialCoordinatesX.size(); i++){
                if (gameGrid[potentialCoordinatesX.get(i)][potentialCoordinatesY.get(i)] != 0){
                    xCoordinate = potentialCoordinatesX.get(i);
                    yCoordinate = potentialCoordinatesY.get(i);
                    eaten = true;
                    sustainability = 0;
                }
            }
            if(!eaten){
                int randIndex = (int) (Math.random() * potentialCoordinatesX.size());
                xCoordinate = potentialCoordinatesX.get(randIndex);
                yCoordinate = potentialCoordinatesY.get(randIndex);
                sustainability++;
            }

            gameGrid[xCoordinate][yCoordinate]= this.getType();
            gameGrid[x][y]=0;
            x=xCoordinate;
            y=yCoordinate;
            return eaten;
        }
    }

    //based on population size and predetermined probability of death, kills wolf off
    public String killOffWolf(ArrayList wolf, int[][] grid){
        double probability = determineProbabilities();
        if(wolf.size() < 10){
            probability += 0.1;
        }
        else if(wolf.size() > 25 && age > 7){
            probability -= 0.1;
        }
        if(Math.random() > probability){
            wolf.remove(this);
            grid[this.x][this.y] = 0;
            return ("Rest in Peace Wolf " + this.getSerialNum() + ". You died at " + this.age + " due to natural causes.");
        }
        return "";
    }

    //checks if all preconditions for a new baby are met (wolf is adult female, and male wolf is nearby)
    public int[] checkForBaby(int[][] gameGrid, ArrayList wolfList){
        boolean malePresent = false;
        int range = 1;
        if(wolfList.size() < 7){
            range = 2;
        }

        if(this.getType() == 6){ //if elk is adult female
            for (int x = this.getX() - range; x <= (this.getX() + range); x++) {
                for (int y = this.getY() - range; y <= this.getY() + range; y++) {
                    if( x < 35 && x > -1 && y < 23 && y > -1) {
                        if (gameGrid[x][y] == 8) { //is an adult male
                            malePresent = true;
                        }
                    }
                }
            }
        }
        if(malePresent) {
            if (Math.random() < (this.determineProbabilities() + 0.2)) {
                return newBaby(gameGrid);
            }
        }
        int[] none = {100, 100};
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

    //returns type of wolf (gender, age)
    public int getType(){
        if(this.age <= 5 && this.gender){ //female, kid
            return 5;
        }
        else if(this.age > 5 && this.gender){ //female, adult
            return 6;
        }
        else if(this.age <= 5 && !(gender)){ //male, kid
            return 7;
        }
        else{ //male, adult
            return 8;
        }
    }

    public int getX(){
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getSerialNum() { return this.serialNum;}

    public void resetStartTime(){
        startTime = System.nanoTime();
    }

    public long getStartTime(){
        return startTime;
    }

    public int getSustainability(){
        return sustainability;
    }

    public void resetSustainability(){
        sustainability = 0;
    }

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
}

