package ru.yandex.practicum.sleeptracker;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class SleepFunctions {

    public static Function<TreeMap<LocalDateTime, SleepingSession>, Object> count = units -> {
        return units.size();
    };

    public static Function<TreeMap<LocalDateTime, SleepingSession>, Object> countBad = units -> {
        return units.values()
            .stream()
            .filter(unit -> unit.getMark() == Mark.BAD)
            .count();
    };

    public static Function<TreeMap<LocalDateTime, SleepingSession>, Object> min = units -> {
        return units.values()
            .stream()
            .mapToLong(SleepFunctions::sleepInMinutes)
            .min()
            .orElse(0L);
    };

    public static Function<TreeMap<LocalDateTime, SleepingSession>, Object> max = units -> {
        return units.values()
            .stream()
            .mapToLong(SleepFunctions::sleepInMinutes)
            .max()
            .orElse(0L);
    };

    public static Function<TreeMap<LocalDateTime, SleepingSession>, Object> avg = units -> {
        Double avg = units.values()
            .stream()
            .mapToDouble(SleepFunctions::sleepInMinutes)
            .average()
            .orElse(0.0);
        return Math.round(avg);
    };

    public static Function<TreeMap<LocalDateTime, SleepingSession>, Object> chronoType = units -> {
        LocalTime owlStart = LocalTime.of(23, 0);
        LocalTime owlFinish = LocalTime.of(9, 0);
        LocalTime larkStart = LocalTime.of(22, 0);
        LocalTime larkFinish = LocalTime.of(7, 0);

        Map<String, Long> votes = units.values().stream()
            .filter(unit -> unit.getStartDate().datesUntil(unit.getFinishDate().plusDays(1))
                .anyMatch(date -> overlapsNight(unit, date)))
            .map(unit -> {
                LocalTime start = unit.getStartTime();
                LocalTime finish = unit.getFinishTime();
                boolean afterMidnight = start.isBefore(LocalTime.NOON);

                if ((start.isAfter(owlStart) || afterMidnight) && finish.isAfter(owlFinish)) {
                    return "OWL";
                } else if (!afterMidnight && start.isBefore(larkStart) && finish.isBefore(larkFinish)) {
                    return "LARK";
                } else {
                    return "PIGEON";
                }
            })
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        long owlCount = votes.getOrDefault("OWL", 0L);
        long larkCount = votes.getOrDefault("LARK", 0L);
        long pigeonCount = votes.getOrDefault("PIGEON", 0L);

        if (owlCount == larkCount && owlCount == pigeonCount) {
            return "ГОЛУБЬ";
        }

        if (owlCount > larkCount && owlCount > pigeonCount) {
            return "СОВА";
        } else if (larkCount > owlCount && larkCount > pigeonCount) {
            return "ЖАВОРОНОК";
        } else {
            return "ГОЛУБЬ";
        }
    };

    public static Function<TreeMap<LocalDateTime, SleepingSession>, Object> countSleeplessNights = units -> {
        if (units.isEmpty()) {
            return 0L;
        }

        LocalDateTime firstSessionStart = units.firstKey();
        LocalDate firstNight = firstSessionStart.toLocalTime().isBefore(LocalTime.NOON)
            ? firstSessionStart.toLocalDate()
            : firstSessionStart.toLocalDate().plusDays(1);

        SleepingSession lastSession = units.lastEntry().getValue();
        LocalDate endExclusive = lastSession.getFinishDate()
            .plusDays(lastSession.getFinishTime().equals(LocalTime.MIDNIGHT) ? 0 : 1);

        return firstNight.datesUntil(endExclusive)
            .filter(date -> units.values().stream().noneMatch(unit -> overlapsNight(unit, date)))
            .count();
    };

    private static long sleepInMinutes(SleepingSession session) {
        LocalDateTime start = LocalDateTime.of(session.getStartDate(), session.getStartTime());
        LocalDateTime finish = LocalDateTime.of(session.getFinishDate(), session.getFinishTime());
        return Duration.between(start, finish).toMinutes();
    }

    private static boolean overlapsNight(SleepingSession unit, LocalDate date) {
        LocalDateTime start = LocalDateTime.of(unit.getStartDate(), unit.getStartTime());
        LocalDateTime finish = LocalDateTime.of(unit.getFinishDate(), unit.getFinishTime());
        return start.isBefore(date.atTime(6, 0)) && finish.isAfter(date.atStartOfDay());
    }

}
