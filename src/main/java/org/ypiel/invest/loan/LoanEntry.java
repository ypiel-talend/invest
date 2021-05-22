package org.ypiel.invest.loan;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.ypiel.invest.Entry;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class LoanEntry extends Entry {

    private BigDecimal capital;
    private BigDecimal interest;
    private BigDecimal insurance;
    private BigDecimal remaining;

    public LoanEntry(final LocalDate date, final String summary, final BigDecimal capital, final BigDecimal interest, final BigDecimal insurance, final BigDecimal remaining) {
        super(date, capital.add(interest).add(insurance), summary);
        this.capital = capital;
        this.interest = interest;
        this.insurance = insurance;
        this.remaining = remaining;
    }
}
