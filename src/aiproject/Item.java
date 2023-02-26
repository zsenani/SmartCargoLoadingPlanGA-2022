package aiproject;

public class Item {

    int itemNumber;
    double itemWeight;

    public Item(int itemNumber, double itemWeight) {
        this.itemNumber = itemNumber;
        this.itemWeight = itemWeight;
    }//End constructor

    public void print() {
        System.out.println("{" + "ItemNumber= " + itemNumber + ",  ItemWeight= " + itemWeight + '}');
    }//End print
}//End class
