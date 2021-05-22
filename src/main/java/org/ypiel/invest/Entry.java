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

    protected void setAmount(final BigDecimal amount){
        this.amount = amount;
    }
}
