package aiproject;

public class Fittest {

    int ID;
    double fitnessScore;

    public Fittest(int ID, double fitnessScore) {
        this.ID = ID;
        this.fitnessScore = fitnessScore;
    }//End constructor


    public void print() {
        System.out.println("Individual#" + (ID + 1) + ", Fitness Score = " + fitnessScore);
    }
}//End class
