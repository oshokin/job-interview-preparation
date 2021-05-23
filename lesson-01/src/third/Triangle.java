package third;

public class Triangle extends Shape {

    private double side;

    public Triangle(double side) {
        this.side = side;
    }

    @Override
    public double getArea() {
        return side*3 / 2;
    }

    @Override
    public void draw() {
        System.out.println("Triangle was drawn! My area is " + getArea());
    }

}
