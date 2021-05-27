package org.ypiel.invest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ypiel.invest.insurance.FixedInsurance;
import org.ypiel.invest.insurance.VariableInsurance;
import org.ypiel.invest.loan.LoanLinkedEntry;
import org.ypiel.invest.loan.Loan;

class LoanTest {

    private final static BigDecimal limitLast = new BigDecimal(50);

    @Test
    void computePaymentPlanWithFixedInsurance() {

        Loan l = new Loan("ImmoA", new BigDecimal(0d).add(new BigDecimal(0d)), LocalDate.of(2021, Month.JANUARY, 1), new BigDecimal(700), new BigDecimal(1.5d), new BigDecimal(20000), new FixedInsurance(new BigDecimal(70)));
        final LoanLinkedEntry linkedLoanEntry = l.computePaymentPlan(limitLast);

        Assertions.assertNotNull(linkedLoanEntry);
        Assertions.assertTrue(linkedLoanEntry.size() > 1);
    }

    @Test
    void computePaymentPlanWithVariableInsurance() {

        Loan l = new Loan("ImmoB", new BigDecimal(0d).add(new BigDecimal(0d)), LocalDate.of(2021, Month.JANUARY, 1), new BigDecimal(700), new BigDecimal(1.5d), new BigDecimal(20000), new VariableInsurance(new BigDecimal(0.35d)));
        final LoanLinkedEntry linkedLoanEntry = l.computePaymentPlan(limitLast);

        Assertions.assertNotNull(linkedLoanEntry);
        Assertions.assertTrue(linkedLoanEntry.size() > 1);
    }



    @Test
    void another() {

        Loan l = new Loan("Another", new BigDecimal(0d), LocalDate.of(2021, Month.JANUARY, 1), new BigDecimal(858.99), new BigDecimal(5d), new BigDecimal(10000), new FixedInsurance(new BigDecimal(2.92d)));
        final LoanLinkedEntry linkedLoanEntry = l.computePaymentPlan(limitLast);

        Assertions.assertNotNull(linkedLoanEntry);
        Assertions.assertTrue(linkedLoanEntry.size() > 1);
    }


}