package org.ypiel.invest;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class Entry {

    private final LocalDate date;
    private BigDecimal amount;
    private final String summary;

    private boolean isDebit = false;

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

    protected void isDebit(boolean isDebit) {
        this.isDebit = isDebit;
    }
}
