package ru.yandex.practicum.sleeptracker;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public final class SleepingSession {

    private final LocalDate startDate;
    private final LocalTime startTime;
    private final LocalDate finishDate;
    private final LocalTime finishTime;
    private final Mark mark;

    public SleepingSession(
        LocalDate startDate,
        LocalTime startTime,
        LocalDate finishDate,
        LocalTime finishTime,
        Mark mark
    ) {
        this.startDate = startDate;
        this.startTime = startTime;
        this.finishDate = finishDate;
        this.finishTime = finishTime;
        this.mark = mark;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalDate getFinishDate() {
        return finishDate;
    }

    public LocalTime getFinishTime() {
        return finishTime;
    }

    public Mark getMark() {
        return mark;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || o.getClass() != getClass()) return false;
        SleepingSession unit = (SleepingSession) o;
        return Objects.equals(startDate, unit.startDate)
            && Objects.equals(startTime, unit.startTime)
            && Objects.equals(finishDate, unit.finishDate)
            && Objects.equals(finishTime, unit.finishTime)
            && Objects.equals(mark, unit.mark);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, startTime, finishDate, finishTime, mark);
    }

    @Override
    public String toString() {
        return "SleepingSession [startDate=" + startDate + ", startTime=" + startTime + ", finishDate=" + finishDate
                + ", finishTime=" + finishTime + ", mark=" + mark + "]";
    }
}
