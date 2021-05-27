package org.ypiel.invest.loan;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.ypiel.invest.insurance.Insurance;

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
        this.name = name;
        this.applicationFees = applicationFees;
        this.start = start;
        this.monthlyAmount = monthlyAmount;
        this.rate = annualPercent(rate); //rate.divide(new BigDecimal(100), 10, RoundingMode.HALF_UP).divide(new BigDecimal(12), 10, RoundingMode.HALF_UP);
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

        return init;
    }

}
