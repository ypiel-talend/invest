package org.ypiel.invest.loan;

import static org.ypiel.invest.Util.formatBD;

import java.math.BigDecimal;

import org.ypiel.invest.LinkedEntry;

import lombok.Getter;

@Getter
public class LinkedLoanEntry extends LinkedEntry {


    private BigDecimal capital;
    private BigDecimal interest;
    private final BigDecimal insurance;
    private final BigDecimal remaining;



    private final Loan loan;

    private LinkedLoanEntry(final Loan loan) {
        super(loan.getName(), loan.getStart(), loan.getMonthlyAmount(), loan.getName() + " #init");
        this.capital = BigDecimal.ZERO;
        this.interest = BigDecimal.ZERO;
        this.insurance = BigDecimal.ZERO;
        this.remaining = loan.getAmount();
        this.loan = loan;
    }

    public LinkedLoanEntry(final LinkedLoanEntry previous) {
        //super(previous.isFirst() ? previous.getDate() : previous.getDate().plusMonths(1), previous.getLoan().getMonthlyAmount(), previous.getLoan().getName());
        super(previous, previous.getAmount(), previous.getLoan().getName());

        this.loan = previous.getLoan();
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
        LinkedLoanEntry next = (LinkedLoanEntry)this.getNext();
        this.removeNext();
        next.removePrevious();

        this.interest = this.interest.add(next.getInterest());
        this.capital = this.capital.add(next.getCapital());

        super.setAmount(this.getInterest().add(this.getInsurance()).add(this.getCapital()));

        return this;
    }

    public BigDecimal totalCost(){
        if(this.isFirst()){
            return this.getInterest().add(this.getInsurance()).add(this.getLoan().getApplicationFees());
        }

        return ((LinkedLoanEntry)this.getPrevious()).totalCost().add(this.getInterest()).add(this.getInsurance());
    }


    public final static LinkedLoanEntry init(final Loan loan) {
        return new LinkedLoanEntry(loan);
    }

}
