package org.ypiel.invest;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Util {

    public final static String formatBD(final BigDecimal bd) {
        return NumberFormat.getCurrencyInstance().format(bd);
    }

    public final static BigDecimal annualPercent(final BigDecimal bd) {
        return bd.divide(new BigDecimal(100), 10, RoundingMode.HALF_UP).divide(new BigDecimal(12), 10, RoundingMode.HALF_UP);
    }

    public final static BigDecimal percent(final BigDecimal bd) {
        return bd.divide(new BigDecimal(100), 10, RoundingMode.HALF_UP);
    }

    public static String loadFileFromResources(String fileName) {
        ClassLoader classLoader = Util.class.getClassLoader();
        final InputStream resourceAsStream = classLoader.getResourceAsStream(fileName);

        StringBuilder content = new StringBuilder();
        try (BufferedInputStream read = new BufferedInputStream(resourceAsStream);
             BufferedReader br = new BufferedReader(new InputStreamReader(read));) {
            content.append(br.lines().collect(Collectors.joining("\n")));
        } catch (IOException e) {
            log.error(String.format("An error occurred when scanning file %s.", fileName), e);
            System.exit(1);
        }

        return content.toString();
    }

    public static void displayBigFlatEntries(PrintStream ps, final BigFlatEntry bfe) {
        ps.println("---------------------------------------------------------------------------------------------------------");
        ps.printf(BigFlatEntry.FORMAT, "ID", "DATE", "AMOUNT", "SUMMARY", "LINKEDENTRY_NAME", "LINKEDENTRY_ID", "LINKEDENTRY_CAPITAL", "LINKEDENTRY_INTEREST", "LINKEDENTRY_INSURANCE", "LINKEDENTRY_REMAINING", "LINKEDENTRY_PREPAYMENT");
        ps.println("---------------------------------------------------------------------------------------------------------");

        if (bfe == null) {
            return;
        }

        BigFlatEntry current = bfe.getFirst();
        boolean first = true;
        do {
            if (!first) {
                current = current.getNext();
            }
            current.display(ps);
            first = false;
        } while (!current.isLast());
    }

}
