package org.ypiel.invest.recurring;

import java.math.BigDecimal;

import org.ypiel.invest.LinkedEntry;
import org.ypiel.invest.loan.Loan;
import org.ypiel.invest.loan.LoanLinkedEntry;
import org.ypiel.invest.recurring.Recurring.Temporal;

import lombok.Getter;

@Getter
public class RecurringLinkedEntry extends LinkedEntry {

    private BigDecimal deltaFromPrevious;
    private Recurring recurring;

    private RecurringLinkedEntry(final Recurring recurring, final boolean isDebit) {
        super(recurring.getName(), recurring.getStart(), recurring.getAmount(), recurring.getName() + " #init", isDebit, recurring.getTemporal() == Temporal.MONTHLY ? 1 : 12);
        this.deltaFromPrevious = BigDecimal.ZERO;
        this.recurring = recurring;
    }

    public RecurringLinkedEntry(final RecurringLinkedEntry previous) {
        super(previous, previous.isFirst() ? previous.getAmount() : previous.getAmount().add(previous.getAmount(false).multiply(previous.getRecurring().getRate())), previous.getRecurring().getName(), previous.getRecurring().isDebit());

        this.deltaFromPrevious = super.getAmount().subtract(previous.getAmount());
        this.recurring = previous.getRecurring();
    }

    public final static RecurringLinkedEntry init(final Recurring recurring) {
        return new RecurringLinkedEntry(recurring, recurring.isDebit());
    }

    public BigDecimal total(){
        if(this.isFirst()){
            return this.getAmount();
        }

        return ((RecurringLinkedEntry)this.getPrevious()).total().add(this.getAmount());
    }

}
