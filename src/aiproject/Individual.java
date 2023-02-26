package aiproject;

import java.math.*;

public class Individual {

    int NoContainer;
    int NoItem;
    int[] individual;
    String binaryRepresentation = "";
    int binarySize;

    double[] fitnessScore;
    double difference;

    public Individual(int NoContainer, int NoItem) {
        this.NoContainer = NoContainer;
        this.NoItem = NoItem;
        individual = new int[NoItem];
        fitnessScore = new double[NoContainer];
        difference = 0;
        binarySize = Integer.toBinaryString(NoContainer).length();

    }//End constructor

    public void Distribute_Items() {
        //Assign each item in the individual to container randomly
        for (int i = 0; i < NoItem; i++) {
            individual[i] = (int) Math.round(((Math.random() * (NoContainer - 1)) + 1));
            binaryRepresentation += String.format("%" + binarySize + "s", Integer.toBinaryString(individual[i])).replace(' ', '0');
        }

    }//End Distribute_Items

    public void print() {
        for (int i = 0; i < NoItem; i++) {
            System.out.print(individual[i] + ", ");

        }
        System.out.print("\n " + binaryRepresentation);
    }//End print
}//End class
