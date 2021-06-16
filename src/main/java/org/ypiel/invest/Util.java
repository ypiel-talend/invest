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
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.ypiel.invest.loan.Loan;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Util {

    public final static String ENTRY_FORMAT = "%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s\n";
    public final static String LOAN_FORMAT = "%5s %10s %10s %10s %5s %10s %5s %5s\n";

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
        ps.printf(ENTRY_FORMAT, "ID", "DATE", "AMOUNT", "INCOMES", "OUTCOMES", "SUMMARY", "LINKEDENTRY_NAME", "LINKEDENTRY_ID", "LINKEDENTRY_CAPITAL", "LINKEDENTRY_INTEREST", "LINKEDENTRY_INSURANCE", "LINKEDENTRY_REMAINING", "LINKEDENTRY_PREPAYMENT");

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

    public static void displayLoans(PrintStream ps, final List<Loan> loans) {
        ps.println("---------------------------------------------------------------------------------------------------------");
        ps.printf(LOAN_FORMAT, "NAME", "APPLICATIONFEES", "START", "MONTHLYAMOUNT", "RATE", "AMOUNT", "INSURANCE", "INSURANCE_TYPE");
        ps.println("---------------------------------------------------------------------------------------------------------");

        if (loans == null || loans.isEmpty()) {
            return;
        }

        loans.stream().forEach(l -> l.display(ps));
    }

    public static String toString(BigDecimal bd){
        return NumberFormat.getCurrencyInstance(Locale.FRANCE).format(bd);
    }

}
