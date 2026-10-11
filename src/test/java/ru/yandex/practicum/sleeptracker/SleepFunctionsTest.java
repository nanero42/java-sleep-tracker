package ru.yandex.practicum.sleeptracker;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SleepFunctionsTest {

    @Test
    void countReturnsZeroForEmptyData() {
        assertEquals(0, SleepFunctions.count.apply(sessions()));
    }

    @Test
    void countIncludesDaytimeAndNighttimeSessions() {
        assertEquals(4, SleepFunctions.count.apply(mixedSessions()));
    }

    @Test
    void countTreatsMultiDaySleepAsOneSession() {
        var data = sessions(session("2025-10-01T23:00", "2025-10-03T07:00"));

        assertEquals(1, SleepFunctions.count.apply(data));
    }

    @Test
    void countBadReturnsZeroForEmptyData() {
        assertEquals(0L, SleepFunctions.countBad.apply(sessions()));
    }

    @Test
    void countBadCountsOnlyBadSessions() {
        assertEquals(2L, SleepFunctions.countBad.apply(mixedSessions()));
    }

    @Test
    void countBadReturnsZeroWhenAllSessionsAreGoodOrNormal() {
        var data = sessions(
            session("2025-10-01T23:00", "2025-10-02T07:00"),
            session("2025-10-02T14:00", "2025-10-02T15:00", Mark.NORMAL)
        );

        assertEquals(0L, SleepFunctions.countBad.apply(data));
    }

    @Test
    void minReturnsZeroForEmptyData() {
        assertEquals(0L, SleepFunctions.min.apply(sessions()));
    }

    @Test
    void minFindsShortestSessionAmongDaytimeAndNighttimeSleep() {
        assertEquals(50L, SleepFunctions.min.apply(mixedSessions()));
    }

    @Test
    void minCalculatesDurationAcrossYearBoundary() {
        var data = sessions(session("2025-12-31T23:30", "2026-01-01T00:15"));

        assertEquals(45L, SleepFunctions.min.apply(data));
    }

    @Test
    void maxReturnsZeroForEmptyData() {
        assertEquals(0L, SleepFunctions.max.apply(sessions()));
    }

    @Test
    void maxFindsLongestSessionAmongDaytimeAndNighttimeSleep() {
        assertEquals(585L, SleepFunctions.max.apply(mixedSessions()));
    }

    @Test
    void maxIncludesWholeDaysInDuration() {
        var data = sessions(
            session("2025-10-01T23:00", "2025-10-03T07:00"),
            session("2025-10-03T23:00", "2025-10-04T07:00")
        );

        assertEquals(1920L, SleepFunctions.max.apply(data));
    }

    @Test
    void avgReturnsZeroForEmptyData() {
        assertEquals(0L, SleepFunctions.avg.apply(sessions()));
    }

    @Test
    void avgIncludesBothDaytimeAndNighttimeSleep() {
        var data = sessions(
            session("2025-10-01T23:00", "2025-10-02T07:00"),
            session("2025-10-02T14:00", "2025-10-02T15:00")
        );

        assertEquals(270L, SleepFunctions.avg.apply(data));
    }

    @Test
    void avgRoundsToNearestMinute() {
        var data = sessions(
            session("2025-10-01T14:00", "2025-10-01T15:00"),
            session("2025-10-02T14:00", "2025-10-02T15:01")
        );

        assertAll(
            () -> assertEquals(61L, SleepFunctions.avg.apply(data)),
            () -> assertEquals(396L, SleepFunctions.avg.apply(mixedSessions()))
        );
    }

    @Test
    void countSleeplessNightsReturnsZeroForEmptyData() {
        assertEquals(0L, SleepFunctions.countSleeplessNights.apply(sessions()));
    }

    @Test
    void countSleeplessNightsAcceptsPartialNightSleepOnEitherSideOfMidnight() {
        var data = sessions(
            session("2025-10-01T23:00", "2025-10-02T03:00"),
            session("2025-10-03T02:00", "2025-10-03T07:00")
        );

        assertEquals(0L, SleepFunctions.countSleeplessNights.apply(data));
    }

    @Test
    void countSleeplessNightsCountsGapsDespiteDaytimeSleep() {
        var data = sessions(
            session("2025-10-01T23:00", "2025-10-02T07:00"),
            session("2025-10-03T14:00", "2025-10-03T15:00"),
            session("2025-10-05T02:00", "2025-10-05T08:00")
        );

        assertEquals(2L, SleepFunctions.countSleeplessNights.apply(data));
    }

    @Test
    void countSleeplessNightsSelectsFirstNightRelativeToNoon() {
        assertAll(
            () -> assertEquals(0L, SleepFunctions.countSleeplessNights.apply(sessions(
                session("2025-10-02T02:00", "2025-10-02T07:00")))),
            () -> assertEquals(1L, SleepFunctions.countSleeplessNights.apply(sessions(
                session("2025-10-02T07:00", "2025-10-02T11:00")))),
            () -> assertEquals(0L, SleepFunctions.countSleeplessNights.apply(sessions(
                session("2025-10-02T14:00", "2025-10-02T15:00"))))
        );
    }

    @Test
    void countSleeplessNightsRequiresPositiveOverlapWithNightInterval() {
        var data = sessions(
            session("2025-10-01T23:00", "2025-10-02T00:00"),
            session("2025-10-02T06:00", "2025-10-02T10:00")
        );

        assertAll(
            () -> assertEquals(1L, SleepFunctions.countSleeplessNights.apply(data)),
            () -> assertEquals(0L, SleepFunctions.countSleeplessNights.apply(sessions(
                session("2025-10-01T23:00", "2025-10-02T00:00"))))
        );
    }

    @Test
    void chronoTypeSelectsOwlIncludingSleepStartingAfterMidnight() {
        var data = sessions(
            session("2025-10-01T23:30", "2025-10-02T10:00"),
            session("2025-10-03T02:00", "2025-10-03T10:00"),
            session("2025-10-03T22:30", "2025-10-04T08:00")
        );

        assertEquals("СОВА", SleepFunctions.chronoType.apply(data));
    }

    @Test
    void chronoTypeSelectsLarkIgnoringDaytimeSleepAndSleeplessNights() {
        var data = sessions(
            session("2025-10-01T21:00", "2025-10-02T06:00"),
            session("2025-10-02T14:00", "2025-10-02T15:00"),
            session("2025-10-03T07:00", "2025-10-03T11:00"),
            session("2025-10-04T14:00", "2025-10-04T15:00")
        );

        assertEquals("ЖАВОРОНОК", SleepFunctions.chronoType.apply(data));
    }

    @Test
    void chronoTypeCountsPostMidnightPigeonNightsWhenChoosingMajority() {
        var data = sessions(
            session("2025-10-01T23:30", "2025-10-02T10:00"),
            session("2025-10-03T02:00", "2025-10-03T08:00"),
            session("2025-10-04T02:00", "2025-10-04T08:00")
        );

        assertEquals("ГОЛУБЬ", SleepFunctions.chronoType.apply(data));
    }

    @Test
    void chronoTypeReturnsPigeonWithoutUniqueWinner() {
        var data = sessions(
            session("2025-10-01T23:30", "2025-10-02T10:00"),
            session("2025-10-02T21:00", "2025-10-03T06:00")
        );

        assertAll(
            () -> assertEquals("ГОЛУБЬ", SleepFunctions.chronoType.apply(data)),
            () -> assertEquals("ГОЛУБЬ", SleepFunctions.chronoType.apply(sessions()))
        );
    }

    @Test
    void chronoTypeUsesStrictTimeBoundariesForOwlAndLark() {
        assertAll(
            () -> assertEquals("ГОЛУБЬ", SleepFunctions.chronoType.apply(sessions(
                session("2025-10-01T23:00", "2025-10-02T10:00")))),
            () -> assertEquals("ГОЛУБЬ", SleepFunctions.chronoType.apply(sessions(
                session("2025-10-01T23:30", "2025-10-02T09:00")))),
            () -> assertEquals("ГОЛУБЬ", SleepFunctions.chronoType.apply(sessions(
                session("2025-10-01T22:00", "2025-10-02T06:00")))),
            () -> assertEquals("ГОЛУБЬ", SleepFunctions.chronoType.apply(sessions(
                session("2025-10-01T21:00", "2025-10-02T07:00")))),
            () -> assertEquals("ГОЛУБЬ", SleepFunctions.chronoType.apply(sessions(
                session("2025-10-02T02:00", "2025-10-02T06:00"))))
        );
    }

    private static TreeMap<LocalDateTime, SleepingSession> mixedSessions() {
        return sessions(
            session("2025-10-01T22:15", "2025-10-02T08:00"),
            session("2025-10-02T23:00", "2025-10-03T08:00", Mark.NORMAL),
            session("2025-10-03T14:30", "2025-10-03T15:20", Mark.BAD),
            session("2025-10-03T23:30", "2025-10-04T06:20", Mark.BAD)
        );
    }

    private static SleepingSession session(String start, String finish) {
        return session(start, finish, Mark.GOOD);
    }

    private static SleepingSession session(String start, String finish, Mark mark) {
        LocalDateTime startDateTime = LocalDateTime.parse(start);
        LocalDateTime finishDateTime = LocalDateTime.parse(finish);
        return new SleepingSession(
            startDateTime.toLocalDate(), startDateTime.toLocalTime(),
            finishDateTime.toLocalDate(), finishDateTime.toLocalTime(), mark
        );
    }

    private static TreeMap<LocalDateTime, SleepingSession> sessions(SleepingSession... values) {
        var data = new TreeMap<LocalDateTime, SleepingSession>();
        for (SleepingSession value : values) {
            data.put(LocalDateTime.of(value.getStartDate(), value.getStartTime()), value);
        }
        return data;
    }
}
