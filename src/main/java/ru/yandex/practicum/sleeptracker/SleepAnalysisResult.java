package ru.yandex.practicum.sleeptracker;

import java.time.LocalDateTime;
import java.util.TreeMap;
import java.util.function.Function;

public record SleepAnalysisResult(
    String message,
    Function<TreeMap<LocalDateTime, SleepingSession>, Object> func
) {}
