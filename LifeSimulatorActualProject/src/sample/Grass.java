package sample;

public class Grass {
    private double percentGrown;
    private long growthTimer;

    //constructor
    public Grass(){
        percentGrown = 1;
        growthTimer = System.nanoTime();
    }

    public double getPercentGrown(){
        return percentGrown;
    }

    //called when grass grows, increases in intervals of 0.25
    public void increasePercent(){
        if(percentGrown != 1){
            percentGrown += 0.25;
        }
    }

    //called when grass is eaten
    public void grassEaten(){
        if(percentGrown > 0.25){
            percentGrown -= 0.5;
        }
        else{
            destroyGrass();
        }

    }

    //growth reset to 0
    public void destroyGrass(){
        percentGrown = 0;
    }

    public long getGrowthTimer(){
        return growthTimer;
    }

    public void restartGrassTimer(){
        growthTimer = System.nanoTime();
    }
}
