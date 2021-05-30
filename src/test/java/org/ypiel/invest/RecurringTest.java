package org.ypiel.invest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ypiel.invest.recurring.Recurring;
import org.ypiel.invest.recurring.Recurring.Temporal;
import org.ypiel.invest.recurring.RecurringLinkedEntry;

class RecurringTest {

    @Test
    void recurringMonthly() {

        Recurring r = new Recurring("Salary",
                LocalDate.of(2021, Month.JANUARY, 1),
                LocalDate.of(2022, Month.JANUARY, 5),
                new BigDecimal(3.0d),
                new BigDecimal(100000.0d),
                true,
                Temporal.MONTHLY
        );
        final RecurringLinkedEntry recurringLinkedEntry = r.computePaymentPlan();

        Assertions.assertNotNull(recurringLinkedEntry);
        Assertions.assertTrue(recurringLinkedEntry.size() > 1);
    }

    @Test
    void recurringAnnually() {

        Recurring r = new Recurring("Salary",
                LocalDate.of(2021, Month.JANUARY, 1),
                LocalDate.of(2022, Month.JANUARY, 5),
                new BigDecimal(3.0d),
                new BigDecimal(100000.0d),
                true,
                Temporal.ANNUALLY
        );
        final RecurringLinkedEntry recurringLinkedEntry = r.computePaymentPlan();

        Assertions.assertNotNull(recurringLinkedEntry);
        Assertions.assertTrue(recurringLinkedEntry.size() > 1);
    }

}