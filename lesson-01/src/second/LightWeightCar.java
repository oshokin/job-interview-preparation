package second;

public class LightWeightCar extends Car implements Moveable {

    @Override
    public void open() {
        System.out.println("LightWeightCar is open");
    }

    @Override
    public void move() {
        System.out.println("LightWeightCar is moving");
    }

}