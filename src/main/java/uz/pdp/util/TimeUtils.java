package uz.pdp.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
            .withZone(ZoneId.systemDefault()); // or ZoneId.of("Asia/Tashkent")

    public static String format(Instant instant) {
        return FORMATTER.format(instant);
    }
}
