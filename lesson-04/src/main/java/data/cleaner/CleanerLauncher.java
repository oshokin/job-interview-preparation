package data.cleaner;

import java.io.File;

public class CleanerLauncher {
    public static void main(String[] args) {
        Cleaner cleaner = new Cleaner();
        try {
            cleaner.saveTitlesToFileByLanguage(
                    new File("D:\\Work\\Наборы данных\\2021-06-02\\data.tsv"),
                    "russian titles.txt",
                    "title",
                    "RU");
        } catch (Exception e) {
            System.out.println("Something went wrong!");
            e.printStackTrace();
        }
    }

}
