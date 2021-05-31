package org.ypiel.invest.insurance;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class FixedInsurance implements Insurance {

    private BigDecimal value;

    @Override
    public BigDecimal compute(BigDecimal insuredAmount) {
        return value;
    }

    @Override
    public BigDecimal getParam() {
        return this.getValue();
    }


}
