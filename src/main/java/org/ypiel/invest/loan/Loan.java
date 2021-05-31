package org.ypiel.invest.loan;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.ypiel.invest.Util;
import org.ypiel.invest.insurance.Insurance;
import org.ypiel.invest.insurance.InsuranceFactory;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import static org.ypiel.invest.Util.annualPercent;
import static org.ypiel.invest.Util.percent;

@Getter
@EqualsAndHashCode
public class Loan {

    private final String name;

    private final BigDecimal applicationFees;
    private final LocalDate start;
    private final BigDecimal monthlyAmount;
    private final BigDecimal rate;
    private final BigDecimal amount;
    private final Insurance insurance;

    public Loan(String name, BigDecimal applicationFees, LocalDate start, BigDecimal monthlyAmount, BigDecimal rate, BigDecimal amount, Insurance insurance) {
        this(name, applicationFees, start, monthlyAmount, rate, amount, insurance, false);
    }

    public Loan(String name, BigDecimal applicationFees, LocalDate start, BigDecimal monthlyAmount, BigDecimal rate, BigDecimal amount, Insurance insurance, boolean rawRate) {
        this.name = name;
        this.applicationFees = applicationFees;
        this.start = start;
        this.monthlyAmount = monthlyAmount;
        this.rate = rawRate ? rate : annualPercent(rate); //rate.divide(new BigDecimal(100), 10, RoundingMode.HALF_UP).divide(new BigDecimal(12), 10, RoundingMode.HALF_UP);
        this.amount = amount;
        this.insurance = insurance;
    }

    /**
     *
     * @param mergeLastLimit Merge the two last entries if last amount is inferior to normal amount x mergeLastLimit%
     * @return
     */
    public LoanLinkedEntry computePaymentPlan(final BigDecimal mergeLastLimit){
        BigDecimal limit = percent(mergeLastLimit); // mergeLastLimit.divide(new BigDecimal(100), 10, RoundingMode.HALF_UP).divide(new BigDecimal(12), 10, RoundingMode.HALF_UP);

        LoanLinkedEntry init = LoanLinkedEntry.init(this);
        LoanLinkedEntry current = init;
        do{
            current = new LoanLinkedEntry(current);
        }while(current.getRemaining().compareTo(BigDecimal.ZERO) > 0);

        final BigDecimal min = this.getMonthlyAmount().multiply(limit);
        if(current.getLast().getAmount().compareTo(min) <= 0){
            current = ((LoanLinkedEntry)current.getLast().getPrevious()).mergeNext();
        }

        // remove #init
        init = (LoanLinkedEntry)current.getFirst().getNext().removePrevious();
        init.asList().stream().forEach(e -> ((LoanLinkedEntry)e).computePrepayment());

        return init;
    }

    public void display(PrintStream ps){
        ps.printf(Util.LOAN_FORMAT, this.getName(), this.getApplicationFees(), this.getStart(), this.getMonthlyAmount(), this.getRate(), this.getAmount(), this.getInsurance().getParam(), InsuranceFactory.getType(this.getInsurance()));
    }

}
