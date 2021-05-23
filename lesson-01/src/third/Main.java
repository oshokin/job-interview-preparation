package third;

import java.util.ArrayList;
import java.util.List;

public class Main {

    //3. Написать пример кода, который реализует принцип полиморфизма,
    //на примере фигур — круг, квадрат, треугольник.
    public static void main(String[] args) {
        List<Shape> shapes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            shapes.add(new Circle(i));
            shapes.add(new Square(i));
            shapes.add(new Triangle(i));
        }
        shapes.forEach(Shape::draw);
    }

}
