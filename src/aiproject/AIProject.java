package aiproject;

import java.util.*;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class AIProject extends Application {

    //mutationOperator = 0, then there is no mutation
    //withCrossover = false, then there is no crossover
    static int NoContainer, choice, NoItems, PopulationSize = 10, parentCount = 0, crossoverPoint, mutationOperator = 5, binarySize;
    static int fitnessEvaluations = PopulationSize, seed = 1, maxSeed = (int) Math.random() * (10 - 5 + 1) + 5, countGeneration = 0;
    static int averageFitnessScoreCount = 0;
    static String parent1, parent2, child1, child2;
    static Item[] SearchSpace;
    static Individual[] initial_population;
    static Individual newChild1, newChild2;
    static Fittest worst;
    static double[][] totalWeight;
    static Fittest[] sortFittest = new Fittest[PopulationSize];
    static Generation[] generation = new Generation[5000 * maxSeed];
    static double minFitnessScore, maxFitnessScore;
    static boolean withCrossover = false;

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);

        do {

            System.out.println("Please enter number of containers: ");
            NoContainer = in.nextInt();

            if (NoContainer <= 0) {
                System.out.println("The container should be greater than 0 ");
            }
        } while (NoContainer <= 0);
        binarySize = Integer.toBinaryString(NoContainer).length();

        do {
            System.out.println("Please enter number of items: ");
            NoItems = in.nextInt();
            if (NoItems <= 0) {
                System.out.println("The items should be greater than 0 ");
            }
        } while (NoItems <= 0);

        SearchSpace = new Item[NoItems];

        do {
            System.out.println("Choose one of the following options to set the weights of the items:");
            System.out.println("Option 1: Number of item/2.");
            System.out.println("Option 2: Number of item^2/2.");
            choice = in.nextInt();

            if (choice != 1 && choice != 2) {
                System.out.println("Choose option number 1 or 2 ");
            }

        } while (choice != 1 && choice != 2);
        System.out.println("\nSearch Space:");

        //Create serach space and set weight for each item
        switch (choice) {
            case 1:
                for (int i = 0; i < NoItems; i++) {
                    SearchSpace[i] = new Item(i + 1, (i + 1.0) / 2);
                    SearchSpace[i].print();
                }
                break;
            case 2:
                for (int i = 0; i < NoItems; i++) {
                    SearchSpace[i] = new Item(i + 1, (i + 1.0) * (i + 1.0) / 2);
                    SearchSpace[i].print();
                }
                break;
        }
        System.out.println();

        Create_Initial_Population();

        Calculate_Container_Weight();

        //Check of we reached the target # of fitness evaluations
        while (fitnessEvaluations < 10000) {
            if (fitnessEvaluations == PopulationSize) {
                System.out.println("\n##############################################################################################");
                System.out.println("Trial #" + seed);
                System.out.println("##############################################################################################\n");
            }
            if (withCrossover) {
                Single_Point_Crossover();
            } else {
                Mutation();
            }

        }

        //Draw a graph which will start in start() method
        launch(args);

        System.out.println("Total Trials = " + seed);
        System.out.println("The End of The Experiment. ");
    }//End main

    public static void Create_Initial_Population() {

        initial_population = new Individual[PopulationSize];

        System.out.println("Intial population:");
        for (int i = 0; i < PopulationSize; i++) {

            //Initialize each individual
            initial_population[i] = new Individual(NoContainer, NoItems);

            //Assign each item in the individual to container randomly
            initial_population[i].Distribute_Items();
            initial_population[i].print();
            System.out.println();

        }//End for
        System.out.println();

    }//End Create_Initial_Population

    //Fitness Methods
    //1.
    public static void Calculate_Container_Weight() {

        //Calculate total weight for each container in individual for all the population
        totalWeight = new double[PopulationSize][NoContainer];

        for (int i = 0; i < PopulationSize; i++) {
            for (int j = 0; j < NoItems; j++) {
                for (int k = 0; k < NoContainer; k++) {
                    if (initial_population[i].individual[j] == k + 1) {
                        totalWeight[i][k] += SearchSpace[j].itemWeight;
                    }
                }
            }
        }//End outer loop
        Prepare_Individual();
    }//End Calculate_Container_Weight

    //2.
    public static void Prepare_Individual() {

        //Construct temp array for each individual to sort weights
        double[] temp;
        System.out.println("Weights for each container in individual:\n");

        for (int index = 0; index < PopulationSize; index++) {

            temp = new double[NoContainer];
            System.out.println("Individual #" + (index + 1) + ":");

            for (int k = 0; k < NoContainer; k++) {

                //Print total weight for each container before sorting
                System.out.print("c" + (k + 1) + " = " + totalWeight[index][k] + ", ");
                temp[k] = totalWeight[index][k];

            }//End inner loop

            Fitness_Score(temp, index);
            System.out.println();

        }//End outer loop
        Fittest_Individual();
        Randomly_Choose_Parent();
        System.out.println("\n##############################################################################################");
        System.out.println("Trial #1");
        System.out.println("##############################################################################################\n");
        if (withCrossover) {
            Single_Point_Crossover();
        } else {
            Mutation();
        }

    }//End Prepare_Individual

    //3.
    public static void Fitness_Score(double[] temp, int index) {

        //Sort weight for each individual and compute fitness(diffrience)
        Arrays.sort(temp);

        initial_population[index].difference += temp[NoContainer - 1];

        for (int i = NoContainer - 2; i >= 0; i--) {
            initial_population[index].difference -= temp[i];
        }//End for
        System.out.println("\nFitness Score = " + initial_population[index].difference); //The lower the better 

    }//End Fitness_Score

    //4.
    public static void Fittest_Individual() {

        //Sort individuals according to the fittest
        //Assign to each individual an ID and fitness score
        for (int i = 0; i < PopulationSize; i++) {
            sortFittest[i] = new Fittest(i, initial_population[i].difference);
        }

        //Sort
        int i, j, position;
        Fittest temp = new Fittest(0, 0);
        double min;

        for (i = 0; i < PopulationSize - 1; i++) //For each sublist
        {
            min = sortFittest[i].fitnessScore;
            position = i;

            for (j = i + 1; j < PopulationSize; j++) //Find minimum
            {
                if (sortFittest[j].fitnessScore < min) {
                    min = sortFittest[j].fitnessScore;
                    position = j;
                }
            }//End inner for
            temp.ID = sortFittest[position].ID;
            temp.fitnessScore = sortFittest[position].fitnessScore; //Swap
            sortFittest[position].fitnessScore = sortFittest[i].fitnessScore;
            sortFittest[position].ID = sortFittest[i].ID;
            sortFittest[i].fitnessScore = temp.fitnessScore;
            sortFittest[i].ID = temp.ID;
        }//End outer for

        //Calculate total fitness score for the first generation.
        int sum = 0;
        System.out.println("Ordered by fittest individual: \n");
        for (int k = 0; k < PopulationSize; k++) {
            sortFittest[k].print();
            if (countGeneration == 0) {
                sum += sortFittest[k].fitnessScore;
            }

        }
        //Initialize the first generation (ID, average fitness score)
        if (countGeneration == 0) {
            generation[countGeneration] = new Generation(countGeneration + 1, sum / PopulationSize);
            countGeneration++;
        }

        System.out.println();
        worst = sortFittest[PopulationSize - 1];

    }//End Fittest_Individual

    public static void Randomly_Choose_Parent() {
        int a, b;

        //If a and b chosen correctly stop the method
        if (parentCount == 2) {
            return;
        }
        //Choose individual a and b randomly, such that a and b should not be same individual
        a = (int) Math.round(Math.random() * (PopulationSize - 1));
        do {
            b = (int) Math.round(Math.random() * (PopulationSize - 1));
        } while (a == b);

        //For parent #1 choose the best from a and b
        if (parentCount == 0) {
            if (initial_population[a].difference <= initial_population[b].difference) {
                parent1 = initial_population[a].binaryRepresentation;

            } else {
                parent1 = initial_population[b].binaryRepresentation;

            }
            parentCount++;
            System.out.println("Parent #1: " + parent1);

            //After choosing parent #1 call the method again to choose parent #2
            Randomly_Choose_Parent();
        }

        //For parent #2 choose the best from a and b
        if (parentCount == 1) {
            if (initial_population[a].difference <= initial_population[b].difference) {
                parent2 = initial_population[a].binaryRepresentation;

            } else {
                parent2 = initial_population[b].binaryRepresentation;

            }

            //parent #1 and parent #2 should not be equal, if they are equal call the method again to change parent #2
            if (parent1.equals(parent2)) {
                Randomly_Choose_Parent();
            } else {
                parentCount++;
                System.out.println("Parent #2: " + parent2);
            }
        }
    }//End Randomly_Choose_Parent

    public static void Single_Point_Crossover() {
        boolean flag = false;

        do {
            flag = false;
            //Choose crossover point randomly
            crossoverPoint = (int) Math.round(Math.random() + (NoItems * binarySize - 2));

            //Apply single point crossover to the choosen parents
            child1 = parent1.substring(0, crossoverPoint + 1) + parent2.substring(crossoverPoint + 1);
            child2 = parent2.substring(0, crossoverPoint + 1) + parent1.substring(crossoverPoint + 1);
            for (int i = 0, j = 0; i < child2.length(); i += binarySize, j++) {
                //Check if the generated containers within the accepted range 
                if ((Integer.parseInt(child1.substring(i, i + binarySize), 2) == 0 || Integer.parseInt(child1.substring(i, i + binarySize), 2) > NoContainer)
                        || (Integer.parseInt(child2.substring(i, i + binarySize), 2) == 0 || Integer.parseInt(child2.substring(i, i + binarySize), 2) > NoContainer)) {
                    flag = true;
                    break;
                }

            }
        } while (flag);
        System.out.println("Child #1 after crossover on point (" + crossoverPoint + "): " + child1);
        System.out.println("Child #2 after crossover on point (" + crossoverPoint + "): " + child2);

        Mutation();
    }//End Single_Point_Crossover

    static boolean flag = false;

    public static void Mutation() {

        newChild1 = new Individual(NoContainer, NoItems);
        newChild2 = new Individual(NoContainer, NoItems);

        if (!withCrossover) {
            child1 = parent1;
            child2 = parent2;
        }

        //Apply mutation to child #1
        //mutationOperator: # times to change random gene 
        do {
            flag = false;
            for (int i = 0; i < mutationOperator; i++) {

                //Choose gene randomly
                int randomLocus = (int) Math.round(Math.random() + (NoItems * binarySize - 2));

                //Change the gene  
                if (child1.charAt(randomLocus) == '1') {

                    child1 = child1.substring(0, randomLocus) + '0'
                            + child1.substring(randomLocus + 1);

                } else {
                    child1 = child1.substring(0, randomLocus) + '1'
                            + child1.substring(randomLocus + 1);

                }
            }//End for

            //Check if the generated containers within the accepted range 
            for (int i = 0, j = 0; i < child1.length(); i += binarySize, j++) {
                if (Integer.parseInt(child1.substring(i, i + binarySize), 2) == 0 || Integer.parseInt(child1.substring(i, i + binarySize), 2) > NoContainer) {
                    flag = true;
                    break;
                }
            }
        } while (flag);

        //Convert the binary representation to numeric to be able to calculate fitness score
        for (int i = 0, j = 0; i < child1.length(); i += binarySize, j++) {
            newChild1.individual[j] = Integer.parseInt(child1.substring(i, i + binarySize), 2);
        }

        System.out.println("Child #1 after mutation (binary): " + child1);

        System.out.print("Child #1 after mutation (numeric): ");
        for (int i = 0; i < newChild1.individual.length; i++) {
            System.out.print(newChild1.individual[i] + ", ");
        }

        //Calculate fitness function for child #1
        Mutation_Evaluate_Fitness(1);

        //Apply mutation to child #2
        do {
            flag = false;
            for (int i = 0; i < mutationOperator; i++) {

                //Choose gene randomly
                int randomLocus = (int) Math.round(Math.random() + (NoItems * binarySize - 2));

                //Change the gene 
                if (child2.charAt(randomLocus) == '1') {

                    child2 = child2.substring(0, randomLocus) + '0'
                            + child2.substring(randomLocus + 1);

                } else {
                    child2 = child2.substring(0, randomLocus) + '1'
                            + child2.substring(randomLocus + 1);

                }
            }
            //Check if the generated containers within the accepted range 
            for (int i = 0, j = 0; i < child2.length(); i += binarySize, j++) {
                if (Integer.parseInt(child2.substring(i, i + binarySize), 2) == 0 || Integer.parseInt(child2.substring(i, i + binarySize), 2) > NoContainer) {
                    flag = true;
                    break;
                }
            }
        } while (flag);

        //Convert the binary representation to numeric to be able to calculate fitness score
        for (int i = 0, j = 0; i < child2.length(); i += binarySize, j++) {
            newChild2.individual[j] = Integer.parseInt(child2.substring(i, i + binarySize), 2);
        }

        System.out.println("Child #2 after mutation (binary): " + child2);
        System.out.print("Child #2 after mutation (numeric): ");
        for (int i = 0; i < newChild2.individual.length; i++) {
            System.out.print(newChild2.individual[i] + ", ");

        }

        //Calculate fitness function for child #2
        Mutation_Evaluate_Fitness(2);
    }

    //count: makes sure that no generation created before finishing its children 
    static int count = 0;

    //The flag indicate wheather the generation improves the previous generation or not 
    static boolean flagChild1 = false, flagChild2 = false;

    public static void Mutation_Evaluate_Fitness(int childNo) {

        fitnessEvaluations++;

        //Child #1
        if (childNo == 1) {
            count++;
            //Calculate total weight for each container
            for (int i = 0; i < NoItems; i++) {
                for (int k = 0; k < NoContainer; k++) {
                    if (newChild1.individual[i] == k + 1) {
                        newChild1.fitnessScore[k] += SearchSpace[i].itemWeight;

                    }
                }
            }

            //Calculate fitness score
            Arrays.sort(newChild1.fitnessScore);
            newChild1.difference += newChild1.fitnessScore[NoContainer - 1];
            for (int i = NoContainer - 2; i >= 0; i--) {
                newChild1.difference -= newChild1.fitnessScore[i];
            }//End for

            System.out.println("\nFitness Score = " + newChild1.difference); //The lower the better

            //Check if the new solution is  fitter than the worst in the population, then replace it with the worst in the population
            if (worst.fitnessScore > newChild1.difference) {
                initial_population[worst.ID] = newChild1;
                Fittest_Individual();
            } else {
                System.out.println("The new solution is not fitter than the worst in the population");
                flagChild1 = true;
            }
        }
        System.out.println();

        //Child #2
        if (childNo == 2) {
            count++;
            //Calculate total weight for each container
            for (int i = 0; i < NoItems; i++) {
                for (int k = 0; k < NoContainer; k++) {
                    if (newChild2.individual[i] == k + 1) {
                        newChild2.fitnessScore[k] += SearchSpace[i].itemWeight;

                    }
                }

            }

            //Calculate fitness score
            Arrays.sort(newChild2.fitnessScore);
            newChild2.difference += newChild2.fitnessScore[NoContainer - 1];
            for (int i = NoContainer - 2; i >= 0; i--) {
                newChild2.difference -= newChild2.fitnessScore[i];
            }//End for

            System.out.println("\nFitness Score = " + newChild2.difference); //The lower the better

            //Check if the new solution is  fitter than the worst in the population, then replace it with the worst in the population
            if (worst.fitnessScore > newChild2.difference) {
                initial_population[worst.ID] = newChild2;
                Fittest_Individual();
            } else {
                System.out.println("The new solution is not fitter than the worst in the population");
                flagChild2 = true;
            }

        }

        //Create a new generation
        if (count == 2) {
            if (flagChild1 && flagChild2) {
                //If the same average fitness score repeated 100 times do not show the rest in the graph
                if (averageFitnessScoreCount < 100) {
                    generation[countGeneration] = new Generation(generation[countGeneration - 1].generationNo + 1, generation[countGeneration - 1].averageFitnessScore);
                    countGeneration++;
                    averageFitnessScoreCount++;
                }
            } else {
                int sum = 0;
                for (int k = 0; k < PopulationSize; k++) {
                    sum += sortFittest[k].fitnessScore;
                }
                generation[countGeneration] = new Generation(countGeneration + 1, sum / PopulationSize);
                countGeneration++;
                averageFitnessScoreCount = 0;

            }
            count = 0;
        }

        //Start new trial
        if (fitnessEvaluations == 10000 && seed != maxSeed) {
            fitnessEvaluations = PopulationSize;
            seed++;
        }
    }//End Mutation_Evaluate_Fitness

    @Override
    public void start(Stage primaryStage) throws Exception {

        Min_Max_FitnessScore();

        //Defining Axis   
        final NumberAxis xaxis = new NumberAxis(0, countGeneration, 10);
        final NumberAxis yaxis = new NumberAxis(minFitnessScore, maxFitnessScore, 100);

        //Defining Label for Axis   
        xaxis.setLabel("Generation");
        yaxis.setLabel("Fitness Score");

        //Creating the instance of linechart with the specified axis  
        LineChart linechart = new LineChart(xaxis, yaxis);

        //creating the series   
        XYChart.Series series = new XYChart.Series();

        //setting name and the date to the series   
        series.setName("Average Fitness Score");
        for (int i = 0; i < generation.length; i++) {
            if (generation[i] != null) {
                series.getData().add(new XYChart.Data(generation[i].generationNo, generation[i].averageFitnessScore));
            } else {
                break;
            }

        }

        //adding series to the linechart   
        linechart.getData().add(series);

        //setting Group and Scene   
        Group root = new Group(linechart);
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("");
        primaryStage.show();
    }//End start

    void Min_Max_FitnessScore() {
        minFitnessScore = generation[0].averageFitnessScore;
        maxFitnessScore = generation[0].averageFitnessScore;

        for (int i = 1; i < generation.length; i++) {
            if (generation[i] == null) {
                break;
            } else {
                if (generation[i].averageFitnessScore < minFitnessScore) {
                    minFitnessScore = generation[i].averageFitnessScore;
                }
                if (generation[i].averageFitnessScore > maxFitnessScore) {
                    maxFitnessScore = generation[i].averageFitnessScore;
                }
            }
        }
    }//End Min_Max_FitnessScore
}//End class

