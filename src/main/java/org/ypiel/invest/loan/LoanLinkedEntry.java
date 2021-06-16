package org.ypiel.invest.loan;

import static org.ypiel.invest.Util.formatBD;
import static org.ypiel.invest.Util.percent;

import java.math.BigDecimal;

import org.ypiel.invest.LinkedEntry;

import lombok.Getter;

@Getter
public class LoanLinkedEntry extends LinkedEntry {

    public final static BigDecimal PREPAYMENT_REMAINING_PERCENT = percent(new BigDecimal(3.0d));

    private BigDecimal capital;
    private BigDecimal interest;
    private final BigDecimal insurance;
    private BigDecimal remaining;
    private BigDecimal prepayment;


    private final Loan loan;

    private LoanLinkedEntry(final Loan loan) {
        super(loan.getName(), loan.getStart(), loan.getMonthlyAmount(), loan.getName() + " #init", true, 1);
        this.capital = BigDecimal.ZERO;
        this.interest = BigDecimal.ZERO;
        this.insurance = BigDecimal.ZERO;
        this.prepayment = BigDecimal.ZERO;
        this.remaining = loan.getAmount();
        this.loan = loan;
    }

    public LoanLinkedEntry(final LoanLinkedEntry previous) {
        super(previous, previous.getAmount(), previous.getLoan().getName(), true);

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

    public LoanLinkedEntry mergeNext(){
        if(this.getNext() == null){
            return this;
        }
        LoanLinkedEntry next = (LoanLinkedEntry)this.getNext();
        this.removeNext();
        next.removePrevious();

        this.interest = this.interest.add(next.getInterest());
        this.capital = this.capital.add(next.getCapital());
        this.remaining = BigDecimal.ZERO;

        super.setAmount(this.getInterest().add(this.getInsurance()).add(this.getCapital()));

        return this;
    }

    public void computePrepayment(){
        BigDecimal next6MonthsInterest = this.getNextFurtherAsList(6).stream().map(e -> ((LoanLinkedEntry)e).getInterest()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal prepayment3percent = this.getRemaining().multiply(PREPAYMENT_REMAINING_PERCENT);

        this.prepayment = next6MonthsInterest.compareTo(prepayment3percent) < 0 ? next6MonthsInterest : prepayment3percent;
    }

    public BigDecimal totalCost(){
        if(this.isFirst()){
            return this.getInterest().add(this.getInsurance()).add(this.getLoan().getApplicationFees());
        }

        return ((LoanLinkedEntry)this.getPrevious()).totalCost().add(this.getInterest()).add(this.getInsurance());
    }

    /**
     * We add prepayment only to the current entry, the entry we sold the apartment.
     * @return
     */
    public BigDecimal totalCostWithPrepayment(){
        if(this.isFirst()){
            return this.getInterest().add(this.getInsurance()).add(this.getLoan().getApplicationFees());
        }

        return ((LoanLinkedEntry)this.getPrevious()).totalCost().add(this.getInterest()).add(this.getInsurance()).add(this.getPrepayment());
    }


    public final static LoanLinkedEntry init(final Loan loan) {
        return new LoanLinkedEntry(loan);
    }

    @Override
    public BigDecimal getIncomes(){
        return this.capital;
    }

    @Override
    public BigDecimal getOutcomes(){
        return this.insurance.add(this.interest).add(this.prepayment);
    }

}
