package hibernator;

import javax.persistence.EntityManagerFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Launcher {

    private static final Random randGen = new Random();

    private List<String> surnames;
    private List<String> maleNames;
    private List<String> femaleNames;
    private EntityManagerFactory sessionFactory;
    private final Repository<Student, Integer> studentsRepository;

    public static void main(String[] args) {
        Launcher launcher = new Launcher();
        launcher.runSomeTests();
        launcher.close();
    }

    public Launcher() {
        fillLists();
        try {
            sessionFactory = SessionFactory.getFactory();
        } catch (Exception e) {
            System.out.println("Something got broken definitely: " + e.getMessage());
            System.exit(1);
        }
        studentsRepository = new Repository<>(sessionFactory, Student.class);
    }

    public void close() {
        if (sessionFactory != null && sessionFactory.isOpen()) SessionFactory.shutdown();
    }

    private void runSomeTests() {
        System.out.println("First, let's see all the students!");
        showAllStudents();
        int studentsToCreateAmount = 1000;
        System.out.printf("Then, let's create %d dead souls (random students)%n", studentsToCreateAmount);
        createRandomStudents(studentsToCreateAmount);
        int studentsToUpdateAmount = 10;
        System.out.printf("Okay, let's change marks of %d students%n", studentsToUpdateAmount);
        updateRandomStudentsMarks(studentsToUpdateAmount);
        int studentToRemoveAmount = 5;
        System.out.printf("Time to erase %d motherfuckers! No man - no problem%n", studentToRemoveAmount);
        removeRandomStudents(studentToRemoveAmount);
    }

    private void showAllStudents() {
        List<Student> students = null;
        try {
            students = studentsRepository.findAll();
        } catch (Exception e) {
            System.out.println("Algo se fue muy mal");
            e.printStackTrace();
        }
        if (students != null) {
            students.forEach(System.out::println);
        }
    }

    private void createRandomStudents(int studentsAmount) {
        for (int i = 0; i < studentsAmount; i++) {
            Student student = createRandomStudent();
            try {
                studentsRepository.insert(student);
                System.out.println("Added: " + student);
            } catch (Exception e) {
                System.err.printf("Couldn't add %s, reason: %s%n", student, e.getMessage());
            }
        }
    }

    private Range getIdsRange() {
        Range funcResult;
        try {
            funcResult = new Range(studentsRepository.getFirstId(), studentsRepository.getLastId());
        } catch (Exception e) {
            funcResult = null;
            System.err.printf("Couldn't run query, reason: %s%n", e.getMessage());
        }
        return funcResult;
    }

    private void updateRandomStudentsMarks(int studentsAmount) {
        Range range = getIdsRange();
        if (range != null) {
            for (int i = 0; i < studentsAmount; i++) {
                int idToTest = getRandomInteger(range.getLow(), range.getHigh());
                byte newMark = (byte) getRandomInteger(1, 5);
                try {
                    Student student = studentsRepository.findById(idToTest);
                    System.out.printf("Victim (before update): %s, new mark: %d%n", student, newMark);
                    student.setMark(newMark);
                    Student updatedStudent = studentsRepository.update(student);
                    System.out.printf("Victim (after update): %s%n", updatedStudent);
                } catch (Exception e) {
                    System.out.println("Oops! " + e.getMessage());
                }
            }
        }
    }

    private void removeRandomStudents(int studentsAmount) {
        Range range = getIdsRange();
        if (range != null) {
            for (int i = 0; i < studentsAmount; i++) {
                int idToRemove = getRandomInteger(range.getLow(), range.getHigh());
                try {
                    Student student = studentsRepository.findById(idToRemove);
                    System.out.printf("%s, what's your last wish?%n", student);
                    studentsRepository.delete(idToRemove);
                    Student removedStudent = studentsRepository.findById(idToRemove);
                    System.out.printf("Victim (after removal): %s%n", removedStudent);
                } catch (Exception e) {
                    System.out.println("Oops! " + e.getMessage());
                }
            }
        }
    }

    private Student createRandomStudent() {
        boolean isMale = (getRandomInteger(0, 1) == 0);
        String basicLastName = surnames.get(getRandomInteger(0, surnames.size() - 1));
        StringBuilder lastName = new StringBuilder(basicLastName.length());
        String firstName;
        if (isMale) {
            firstName = maleNames.get(getRandomInteger(0, maleNames.size() - 1));
            lastName.append(basicLastName);
        } else {
            firstName = femaleNames.get(getRandomInteger(0, femaleNames.size() - 1));
            if (basicLastName.endsWith("ий")) {
                lastName.append(basicLastName, 0, basicLastName.length() - 2).append("ая");
            } else lastName.append(basicLastName).append("а");
        }

        return new Student(null, String.join(" ", firstName, lastName.toString()), (byte) getRandomInteger(1, 5));
    }

    private void fillLists() {
        surnames = tryLoadResource("surnames.txt");
        maleNames = tryLoadResource("male names.txt");
        femaleNames = tryLoadResource("female names.txt");
    }

    private List<String> tryLoadResource(String fileName) {
        List<String> funcResult;
        try {
            funcResult = Files.readAllLines(getResourcePath(fileName));
        } catch (Exception e) {
            funcResult = new ArrayList<>();
            System.out.printf("Couldn't make magic for file \"%s\" work: %s%n", fileName, e.getMessage());
        }
        return funcResult;
    }

    private Path getResourcePath(String name) throws Exception {
        return Paths.get(getClass().getClassLoader().getResource(name).toURI());
    }

    private int getRandomInteger(int begin, int end) {
        return randGen.nextInt(end - begin + 1) + begin;
    }

}