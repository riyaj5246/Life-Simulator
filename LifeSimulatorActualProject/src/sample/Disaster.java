package sample;

public class Disaster {
    private int type;
    private long startTime = 0;
    private int duration;

    //constructor - t represents type (0 - fire, 1 - drought, 2 - tornado), d represents duration in seconds
    public Disaster(int t, int d){
        this.type = t; // 0 - fire, 1 - drought, 2 - tornado
        this.duration = d;
        switch (type){
            case(0):
                System.out.println("fire" + duration);
                break;
            case(1):
                System.out.println("drought" + duration);
                break;
            case(2):
                System.out.println("tornado" + duration);
                break;
        }
    }

    public int getType(){
        return type;
    }

    public int getDuration(){
        return duration;
    }

}
