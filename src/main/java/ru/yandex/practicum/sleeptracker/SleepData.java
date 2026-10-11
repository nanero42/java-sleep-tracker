package ru.yandex.practicum.sleeptracker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;

public final class SleepData {

    public final TreeMap<LocalDateTime, SleepingSession> units;

    public SleepData() {
        units = new TreeMap<>();
    }

    public void loadLog(String pathToLog) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(pathToLog))) {
            br.lines().forEach(line -> {
                SleepingSession unit = stringToUnit(line);
                LocalDateTime unitKey = LocalDateTime.of(unit.getStartDate(), unit.getStartTime());
                units.put(unitKey, unit);
            });
        }
    }

    private static SleepingSession stringToUnit(String str) {
        String[] parts = str.split("\\s|\\;");
        
        String[] sDate = parts[0].split("\\.");
        String[] sTime = parts[1].split("\\:");
        String[] fDate = parts[2].split("\\.");
        String[] fTime = parts[3].split("\\:");

        LocalDate startDate = parseDate(sDate);
        LocalTime startTime = parseTime(sTime);
        LocalDate finishDate = parseDate(fDate);
        LocalTime finishTime = parseTime(fTime);

        Mark mark = switch (parts[4]) {
            case "GOOD" -> Mark.GOOD;
            case "NORMAL" -> Mark.NORMAL;
            default -> Mark.BAD;
        };

        return new SleepingSession(startDate, startTime, finishDate, finishTime, mark);
    }

    private static LocalDate parseDate(String[] p) {
        return LocalDate.of(2000 + Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0]));
    }

    private static LocalTime parseTime(String[] p) {
        return LocalTime.of(Integer.parseInt(p[0]), Integer.parseInt(p[1]));
    }
}
