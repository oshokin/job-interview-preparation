package movie.theater;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DBQueriesRunner {

    private final Random randGen = new Random();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private List<String> surnames;
    private List<String> maleNames;
    private List<String> femaleNames;
    private List<Client> clients;

    public static void main(String[] args) {
        DBQueriesRunner tester = new DBQueriesRunner();
        //tester.showDummyInitializationQueries();

        //1. Ошибки в расписании (фильмы накладываются друг на друга), отсортированные по возрастанию времени.
        //Выводить надо колонки «фильм 1», «время начала», «длительность», «фильм 2», «время начала»,
        //«длительность»;

        String scheduleErrorsQuery =
        "WITH sessions_with_movies_data AS\n" +
                "  (SELECT T.id AS id,\n" +
                "          T.movie_id AS movie_id,\n" +
                "          main_movies_data.name AS movie_name,\n" +
                "          main_movies_data.duration duration,\n" +
                "          T.start_date AS start_date,\n" +
                "          DATE_ADD(T.start_date, INTERVAL main_movies_data.duration MINUTE) AS end_date\n" +
                "   FROM sessions AS T\n" +
                "   INNER JOIN movies AS main_movies_data \n" +
                "   ON T.movie_id = main_movies_data.id)\n" +
                "SELECT main_table.id AS main_session_id,\n" +
                "       main_table.movie_id AS main_movie_id,\n" +
                "       main_table.movie_name AS main_movie_name,\n" +
                "       main_table.start_date AS main_start_date,\n" +
                "       main_table.duration AS main_duration,\n" +
                "       comparation_table.id AS secondary_session_id,\n" +
                "       comparation_table.movie_id AS secondary_movie_id,\n" +
                "       comparation_table.movie_name AS secondary_movie_name,\n" +
                "       comparation_table.start_date AS secondary_start_date,\n" +
                "       comparation_table.duration AS secondary_duration\n" +
                "FROM sessions_with_movies_data AS main_table\n" +
                "INNER JOIN sessions_with_movies_data AS comparation_table \n" +
                "ON main_table.id > comparation_table.id\n" +
                "   AND main_table.movie_id <> comparation_table.movie_id\n" +
                "   AND (main_table.start_date BETWEEN comparation_table.start_date AND comparation_table.end_date\n" +
                "   OR main_table.end_date BETWEEN comparation_table.start_date AND comparation_table.end_date)\n" +
                "ORDER BY\n" +
                "   main_start_date ASC";

        //2. Перерывы 30 минут и более между фильмами — выводить по уменьшению длительности перерыва.
        //Колонки:
        // «фильм 1», «время начала», «длительность», «время начала второго фильма», «длительность перерыва»;

        String halfHourBreaksBetweenMoviesQuery =
                "WITH sessions_with_movies_data AS\n" +
                        "  (SELECT T.id AS id,\n" +
                        "          T.movie_id AS movie_id,\n" +
                        "          main_movies_data.name AS movie_name,\n" +
                        "          main_movies_data.duration duration,\n" +
                        "          T.start_date AS start_date,\n" +
                        "          DATE_ADD(T.start_date, INTERVAL main_movies_data.duration MINUTE) AS end_date\n" +
                        "   FROM sessions AS T\n" +
                        "   INNER JOIN movies AS main_movies_data \n" +
                        "   ON T.movie_id = main_movies_data.id \n" +
                        "   LIMIT 1000)\n" +
                        "SELECT main_table.id AS main_session_id,\n" +
                        "       main_table.movie_id AS main_movie_id,\n" +
                        "       main_table.movie_name AS main_movie_name,\n" +
                        "       main_table.start_date AS main_start_date,\n" +
                        "       main_table.end_date AS main_end_date,\n" +
                        "       main_table.duration AS main_duration,\n" +
                        "       comparation_table.id AS secondary_session_id,\n" +
                        "       comparation_table.movie_id AS secondary_movie_id,\n" +
                        "       comparation_table.movie_name AS secondary_movie_name,\n" +
                        "       comparation_table.start_date AS secondary_start_date,\n" +
                        "       TIMESTAMPDIFF(MINUTE, main_table.end_date, comparation_table.start_date) as break_in_minutes\n" +
                        "FROM sessions_with_movies_data AS main_table\n" +
                        "INNER JOIN sessions_with_movies_data AS comparation_table \n" +
                        "ON main_table.id > comparation_table.id\n" +
                        "   AND main_table.movie_id <> comparation_table.movie_id\n" +
                        "   AND comparation_table.start_date > main_table.end_date\n" +
                        "   AND TIMESTAMPDIFF(MINUTE, main_table.end_date, comparation_table.start_date) BETWEEN 30 AND 1440\n" +
                        "ORDER BY\n" +
                        "   break_in_minutes DESC,\n" +
                        "   main_session_id ASC";
        //3. Список фильмов, для каждого — с указанием общего числа посетителей за все время,
        //среднего числа зрителей за сеанс и общей суммы сборов по каждому фильму
        //(отсортировать по убыванию прибыли).
        //Внизу таблицы должна быть строчка «итого», содержащая данные по всем фильмам сразу;
        //Число посетителей и кассовые сборы, сгруппированные по времени начала фильма:
        //с 9 до 15, с 15 до 18, с 18 до 21, с 21 до 00:00
        //(сколько посетителей пришло с 9 до 15 часов и т.д.).
        //поскольку используется inner join, нет проверки деления на ноль, т. к. в любом случае будет хоть 1 запись
        String summaryQuery =
                "WITH sessions_with_movies_data AS\n" +
                        "  (SELECT T.id AS id,\n" +
                        "          T.movie_id AS movie_id,\n" +
                        "          main_movies_data.name AS movie_name,\n" +
                        "          T.price AS price\n" +
                        "   FROM sessions AS T\n" +
                        "   INNER JOIN movies AS main_movies_data ON T.movie_id = main_movies_data.id)\n" +
                        "SELECT t.movie_id,\n" +
                        "       t.movie_name,\n" +
                        "       COUNT(tickets.uuid) AS tickets_bought,\n" +
                        "       COUNT(tickets.uuid) / COUNT(t.id) AS tickets_per_session,\n" +
                        "       SUM(t.price) / COUNT(tickets.uuid) AS average_ticket_price,\n" +
                        "       SUM(t.price) AS total_outcome\n" +
                        "FROM sessions_with_movies_data AS t\n" +
                        "INNER JOIN tickets AS tickets ON t.id = tickets.session_id\n" +
                        "GROUP BY t.movie_id,\n" +
                        "         t.movie_name";

    }

    private void showDummyInitializationQueries() {
        int clientsCount = 100;
        int pricesCount = 10_000;
        int sessionsCount = 20_000;
        int ticketsCount = 10_000;

        fillLists();

        System.out.println(getRandomPricesQuery(pricesCount));
        System.out.println(getRandomSessionsQuery(sessionsCount));
        System.out.println(getRandomClientsQuery(clientsCount));
        System.out.println(getRandomTicketsQuery(ticketsCount, clientsCount, sessionsCount));
    }

    private void fillLists() {
        clients = new ArrayList<>();
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
            System.out.println("Couldn't make magic work");
            e.printStackTrace();
        }
        return funcResult;
    }

    private String getRandomPricesQuery(int rowsCount) {
        String statementStart = "INSERT INTO prices\n(date_time, movie_id, price)\nVALUES\n";
        StringBuilder builder = new StringBuilder(statementStart.length() + (40 * rowsCount));
        builder.append(statementStart);
        for (int i = 0; i < rowsCount; i++) {
            if (i > 0) builder.append(",\n");
            builder.append("('").
                    append(formatter.format(getRandomLocalDateTime(2020, 2021))).append("', ").
                    append(getRandomInteger(1, 300)).append(", ").
                    append(getRandomInteger(250, 500)).append(")");
        }
        builder.append(";");
        builder.trimToSize();

        return builder.toString();
    }

    private String getRandomSessionsQuery(int rowsCount) {
        String statementStart = "INSERT INTO sessions\n(movie_id, start_date, tickets_amount, price)\nVALUES\n";
        StringBuilder builder = new StringBuilder(statementStart.length() + (40 * rowsCount));
        builder.append(statementStart);
        for (int i = 0; i < rowsCount; i++) {
            if (i > 0) builder.append(",\n");
            builder.append("(").
                    append(getRandomInteger(1, 300)).
                    append(", '").
                    append(formatter.format(getRandomLocalDateTime(2020, 2021))).append("', ").
                    append(getRandomInteger(200, 300)).append(", ").
                    append(getRandomInteger(250, 500)).append(")");
        }
        builder.append(";");
        builder.trimToSize();

        return builder.toString();
    }

    private String getRandomClientsQuery(int rowsCount) {
        do {
            Client client = getRandomClient();
            if (clients.contains(client)) continue;
            clients.add(client);
        } while (clients.size() < rowsCount);
        String statementStart = "INSERT INTO clients\n(phone_number, first_name, last_name)\nVALUES\n";
        StringBuilder builder = new StringBuilder(statementStart.length() + (40 * rowsCount));
        builder.append(statementStart);
        for (int i = 0; i < clients.size(); i++) {
            Client client = clients.get(i);
            if (i > 0) builder.append(",\n");
            builder.append("(\"").append(client.getPhoneNumber()).append("\", ").
                    append("\"").append(client.getFirstName()).append("\", ").
                    append("\"").append(client.getLastName()).append("\")");
        }
        builder.append(";");
        builder.trimToSize();

        return builder.toString();
    }

    private String getRandomTicketsQuery(int rowsCount, int clientCount, int sessionsCount) {
        String statementStart = "INSERT INTO tickets\n(uuid, client_id, session_id, seat_row, seat_number)\nVALUES\n";
        StringBuilder builder = new StringBuilder(statementStart.length() + (80 * rowsCount));
        builder.append(statementStart);
        for (int i = 0; i < rowsCount; i++) {
            if (i > 0) builder.append(",\n");
            builder.append("(\"").append(UUID.randomUUID()).append("\", ").
                    append(getRandomInteger(1, clientCount)).append(", ").
                    append(getRandomInteger(1, sessionsCount)).append(", ").
                    append(getRandomInteger(1, 9)).append(", ").
                    append(getRandomInteger(1, 24)).append(")");
        }
        builder.append(";");
        builder.trimToSize();

        return builder.toString();
    }

    private Client getRandomClient() {
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
                lastName.append(basicLastName.substring(0, basicLastName.length() - 2)).append("ая");
            } else lastName.append(basicLastName).append("а");
        }
        return new Client(getRandomPhoneNumber(), firstName, lastName.toString());
    }

    private Path getResourcePath(String name) throws Exception {
        return Paths.get(getClass().getClassLoader().getResource(name).toURI());
    }

    private int getRandomInteger(int begin, int end) {
        return randGen.nextInt(end - begin + 1) + begin;
    }

    private LocalDateTime getRandomLocalDateTime(int initialYear, int finalYear) {
        int year = getRandomInteger(initialYear, finalYear);
        int month = getRandomInteger(1, 12);
        int dayOfMonth = getRandomInteger(1, YearMonth.of(year, month).lengthOfMonth());

        return LocalDateTime.of(year, month, dayOfMonth,
                getRandomInteger(0, 23),
                getRandomInteger(0, 59),
                getRandomInteger(0, 59));
    }

    private String getRandomPhoneNumber() {
        StringBuilder sb = new StringBuilder(20);
        sb.append("+7").
                append(getRandomInteger(900, 999)).
                append(getRandomInteger(100, 999)).
                append(getRandomInteger(10, 99)).
                append(getRandomInteger(10, 99));
        sb.trimToSize();

        return sb.toString();
    }

}