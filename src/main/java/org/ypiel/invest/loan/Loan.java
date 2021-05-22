package org.ypiel.invest.loan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import org.ypiel.invest.insurance.Insurance;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static org.ypiel.invest.Util.formatBD;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class Loan {

    private String name;
    private LocalDate start;
    private BigDecimal monthlyAmount;
    private BigDecimal rate;
    private BigDecimal amount;
    private Insurance insurance;

    public List<LoanEntry> computePaymentPlan(){
        LinkedList<LoanEntry> entries = new LinkedList<>();

        BigDecimal r = rate.divide(new BigDecimal(100), 5, RoundingMode.HALF_UP).divide(new BigDecimal(12), 5, RoundingMode.HALF_UP);

        LocalDate entryDate = start;
        LoanEntry init = new LoanEntry(entryDate, this.name+" #Init", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, amount);
        entries.add(init);
        while(entries.getLast().getRemaining().intValue() > 0){
            final LoanEntry last = entries.getLast();
            final BigDecimal interest = last.getRemaining().multiply(r);
            final BigDecimal insurance = this.insurance.compute(last.getRemaining());
            BigDecimal capital = monthlyAmount.subtract(interest).subtract(insurance);
            BigDecimal remaining = last.getRemaining().subtract(capital);

            if(remaining.compareTo(BigDecimal.ZERO) < 0){
                capital = last.getRemaining();
                remaining = BigDecimal.ZERO;
            }

            if(remaining.compareTo(entries.getLast().getRemaining()) >= 0){
                throw new IllegalArgumentException(String.format("The remaining capital is same or increase [%s >= %s]. The loan parameter must be wrong.", formatBD(remaining), formatBD(entries.getLast().getRemaining())));
            }

            LoanEntry le = new LoanEntry(entryDate, this.name+" #"+entries.size(), capital, interest, insurance, remaining);
            entries.add(le);
            entryDate = entryDate.plusMonths(1);

            System.out.println("=> " + entries.size());
        }

        return entries;
    }

}
