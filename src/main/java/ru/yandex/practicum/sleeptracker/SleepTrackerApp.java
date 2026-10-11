package ru.yandex.practicum.sleeptracker;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;

public class SleepTrackerApp {

    private static final SleepData sleepLogData = new SleepData();
    private static final List<SleepAnalysisResult> utils = new ArrayList<>(List.of(
        new SleepAnalysisResult(
            "Общее количество сессий",
            SleepFunctions.count
        ),
        new SleepAnalysisResult(
            "Количество плохих сесий",
            SleepFunctions.countBad
        ),
        new SleepAnalysisResult(
            "Минимальная продолжительность сессии (мин.)",
            SleepFunctions.min
        ),
        new SleepAnalysisResult(
            "Максимальная продолжительность сессии (мин.)",
            SleepFunctions.max
        ),
        new SleepAnalysisResult(
            "Средняя продолжительность сессии (мин.)",
            SleepFunctions.avg
        ),
        new SleepAnalysisResult(
            "Ваш хронотип",
            SleepFunctions.chronoType
        ),
        new SleepAnalysisResult(
            "Бессонных ночей",
            SleepFunctions.countSleeplessNights
        )
    ));
    public static void main(String[] args) {

        try {
            if (args.length > 0) {
                sleepLogData.loadLog(args[0]);
            } else {
                System.out.println("Необходимо передать путь к логу");
                System.exit(0);
            }
         } catch (IOException e) {
            System.out.println("Ошибка загрузки лога");
            System.exit(0);
         }

        System.out.println("Выполняется анализ...\n");
        utils.stream().forEach(container -> {
            String message = container.message();
            Function<TreeMap<LocalDateTime, SleepingSession>, Object> func = container.func();
            Object result = func.apply(sleepLogData.units);
            
            System.out.println(message + ": " + result);
        });
    }
}