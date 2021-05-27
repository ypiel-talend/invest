package org.ypiel.invest.recurring;

import static org.ypiel.invest.Util.annualPercent;
import static org.ypiel.invest.Util.percent;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.ypiel.invest.LinkedEntry;
import org.ypiel.invest.loan.LoanLinkedEntry;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Recurring {

    public enum  Temporal{
        MONTHLY,
        ANNUALLY
    }

    private final String name;
    private final LocalDate start;
    private final LocalDate stop;
    private final BigDecimal rate;
    private final BigDecimal amount;
    private final boolean isDebit;
    private final Temporal temporal;


    public Recurring(String name, LocalDate start, LocalDate stop, BigDecimal rate, BigDecimal amount, boolean isDebit, Temporal temporal) {
        this.name = name;
        this.start = start;
        this.stop = stop;
        this.rate = temporal == Temporal.MONTHLY ? annualPercent(rate) : percent(rate);
        this.amount = amount;
        this.isDebit = isDebit;
        this.temporal = temporal;
    }

    public RecurringLinkedEntry computePaymentPlan(final boolean isDebit){
        RecurringLinkedEntry init = RecurringLinkedEntry.init(this);

        RecurringLinkedEntry current = init;
        int diff = 0;
        do{
            current = new RecurringLinkedEntry(current);
            diff = current.getDate().compareTo(this.stop);
        }while(diff < 0);

        // remove init
        init = (RecurringLinkedEntry)current.getFirst().getNext().removePrevious();

        // remove last if after stop date
        if(diff > 0){
            ((RecurringLinkedEntry)current.getLast().getPrevious()).removeNext();
        }

        return init;
    }

}
