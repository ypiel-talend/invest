package org.ypiel.invest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ypiel.invest.insurance.FixedInsurance;
import org.ypiel.invest.insurance.VariableInsurance;
import org.ypiel.invest.loan.Loan;
import org.ypiel.invest.loan.LoanEntry;

class LoanTest {

    @Test
    void computePaymentPlanWithFixedInsurance() {

        Loan l = new Loan("ImmoA", LocalDate.of(2021, Month.JANUARY, 1), new BigDecimal(700), new BigDecimal(1.5d), new BigDecimal(20000), new FixedInsurance(new BigDecimal(70)));
        final List<LoanEntry> loanEntries = l.computePaymentPlan();

        Assertions.assertNotNull(loanEntries);
        Assertions.assertTrue(loanEntries.size() > 1);
    }

    @Test
    void computePaymentPlanWithVariableInsurance() {

        Loan l = new Loan("ImmoB", LocalDate.of(2021, Month.JANUARY, 1), new BigDecimal(700), new BigDecimal(1.5d), new BigDecimal(20000), new VariableInsurance(new BigDecimal(0.3)));
        final List<LoanEntry> loanEntries = l.computePaymentPlan();

        Assertions.assertNotNull(loanEntries);
        Assertions.assertTrue(loanEntries.size() > 1);
    }


}