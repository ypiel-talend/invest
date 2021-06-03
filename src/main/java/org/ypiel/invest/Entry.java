package org.ypiel.invest;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
public class Entry {

    private final LocalDate date;
    private final String summary;

    private BigDecimal amount;
    private boolean isDebit;

    public Entry(final LocalDate date, final BigDecimal amount, final String summary, final boolean isDebit){
        this.date = date;
        this.summary = summary;
        this.amount = amount;
        this.isDebit = isDebit;
    }

    protected void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount(){
        return this.getAmount(false);
    }

    public BigDecimal getAmount(boolean signed){
        BigDecimal a = this.amount;

        if(signed && this.isDebit){
            a = this.amount.negate();
        }

        return a;
    }

    public BigDecimal getIncomes(){
        if(this.amount.signum() > 0){
            return this.amount;
        }

        return BigDecimal.ZERO;
    }

    public BigDecimal getOutcomes(){
        if(this.amount.signum() < 0){
            return this.amount;
        }

        return BigDecimal.ZERO;
    }

    protected void isDebit(boolean isDebit) {
        this.isDebit = isDebit;
    }
}
