package com.ecozap.backend.service;

import java.time.*;
import java.time.format.*;
import java.util.Locale;
import java.util.Optional;

/** Station hours are in India. Existing Mongo LocalDateTime fields use the server's zone. */
record BookingWindow(LocalDate date, LocalDateTime start, LocalDateTime end, boolean scheduled) {
    private static final ZoneId STATION_ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter TWELVE_HOUR = new DateTimeFormatterBuilder()
            .parseCaseInsensitive().appendPattern("h:mm a").toFormatter(Locale.ENGLISH);

    static BookingWindow forSlot(String hours, LocalDate requested, Instant now) {
        LocalDate today = now.atZone(STATION_ZONE).toLocalDate();
        LocalDate day = requested == null ? today : requested;
        String[] bounds = hours == null ? new String[0] : hours.split("\\s*[-–—]\\s*", -1);
        if (bounds.length == 2) {
            Optional<LocalTime> from = readTime(bounds[0]);
            Optional<LocalTime> to = readTime(bounds[1]);
            if (from.isPresent() && to.isPresent()) {
                LocalDateTime start = day.atTime(from.get());
                LocalDateTime end = day.atTime(to.get());
                if (!end.isAfter(start)) end = end.plusDays(1);
                return new BookingWindow(day, serverTime(start), serverTime(end), true);
            }
        }
        return new BookingWindow(day, LocalDateTime.ofInstant(now, ZoneId.systemDefault()), null, false);
    }

    boolean isBookable(Instant now) {
        if (scheduled) return start.atZone(ZoneId.systemDefault()).toInstant().isAfter(now);
        return date.equals(now.atZone(STATION_ZONE).toLocalDate());
    }

    String reservationKey(String slotId) {
        return slotId + "|" + (scheduled ? date.toString() : "now");
    }

    static LocalDateTime stationTime(LocalDateTime time) {
        return time == null ? null : time.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(STATION_ZONE).toLocalDateTime();
    }

    private static LocalDateTime serverTime(LocalDateTime value) {
        return value.atZone(STATION_ZONE).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    private static Optional<LocalTime> readTime(String value) {
        for (DateTimeFormatter format : new DateTimeFormatter[]{TWELVE_HOUR, DateTimeFormatter.ISO_LOCAL_TIME}) {
            try {
                return Optional.of(LocalTime.parse(value.trim(), format));
            } catch (DateTimeParseException ignored) { }
        }
        return Optional.empty();
    }
}
