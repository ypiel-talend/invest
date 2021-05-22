package org.ypiel.invest.loan;

import static org.ypiel.invest.Util.formatBD;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.ypiel.invest.Entry;

import lombok.Getter;

@Getter
public class LinkedLoanEntry extends Entry {

    private final Integer id;
    private BigDecimal capital;
    private BigDecimal interest;
    private final BigDecimal insurance;
    private final BigDecimal remaining;

    private LinkedLoanEntry previous;
    private LinkedLoanEntry next;

    private final Loan loan;

    private LinkedLoanEntry(final Loan loan) {
        super(loan.getStart(), loan.getMonthlyAmount(), loan.getName() + " #init");
        this.id = 0;
        this.capital = BigDecimal.ZERO;
        this.interest = BigDecimal.ZERO;
        this.insurance = BigDecimal.ZERO;
        this.remaining = loan.getAmount();
        this.loan = loan;
        this.previous = null;
    }

    public LinkedLoanEntry(final LinkedLoanEntry previous) {
        super(previous.isFirst() ? previous.getDate() : previous.getDate().plusMonths(1), previous.getLoan().getMonthlyAmount(), previous.getLoan().getName() + " #" + previous.getId() + 1);

        this.previous = previous;
        this.previous.setNext(this);
        this.loan = previous.getLoan();
        this.id = previous.getId() + 1;
        this.interest = previous.getRemaining().multiply(loan.getRate());
        this.insurance = loan.getInsurance().compute(previous.getRemaining());
        BigDecimal _capital = loan.getMonthlyAmount().subtract(interest).subtract(insurance);
        BigDecimal _remaining = previous.getRemaining().subtract(_capital);

        if (_remaining.compareTo(BigDecimal.ZERO) < 0) {
            _capital = previous.getRemaining();
            _remaining = BigDecimal.ZERO;

            super.setAmount(this.interest.add(insurance).add(_capital));
        }
        this.capital = _capital;
        this.remaining = _remaining;

        if (remaining.compareTo(previous.getRemaining()) >= 0) {
            throw new IllegalArgumentException(String.format("The remaining capital is same or increase [%s >= %s]. The loan parameter must be wrong.", formatBD(remaining), formatBD(previous.getRemaining())));
        }
    }

    public LinkedLoanEntry mergeNext(){
        if(this.getNext() == null){
            return this;
        }

        LinkedLoanEntry next = this.getNext();
        this.next = null;
        next.previous = null;

        this.interest = this.interest.add(next.getInterest());
        this.capital = this.capital.add(next.getCapital());

        super.setAmount(this.getInterest().add(this.getInsurance()).add(this.getCapital()));

        return this;
    }

    public boolean isFirst() {
        return this.previous == null;
    }

    public boolean isLast() {
        return this.next == null;
    }

    public void setNext(final LinkedLoanEntry next) {
        if (this.next != null) {
            throw new IllegalArgumentException("Next LinkedLoanEntry is already set. Can't be replaced.");
        }

        this.next = next;
    }

    public LinkedLoanEntry getFirst() {
        if (this.isFirst()) {
            return this;
        }

        return this.getPrevious().getFirst();
    }

    public LinkedLoanEntry getLast() {
        if (this.isLast()) {
            return this;
        }

        return this.getNext().getLast();
    }

    public BigDecimal totalCost(){
        if(this.isFirst()){
            return this.getInterest().add(this.getInsurance()).add(this.getLoan().getApplicationFees());
        }

        return this.getPrevious().totalCost().add(this.getInterest()).add(this.getInsurance());
    }

    /**
     *
     * @return The total number of element in the linked list.
     */
    public Integer size() {
        return this.getLast().getId() + 1;
    }

    public final static LinkedLoanEntry init(final Loan loan) {
        return new LinkedLoanEntry(loan);
    }

}
