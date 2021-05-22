package org.ypiel.invest.insurance;


import java.math.BigDecimal;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class VariableInsurance implements Insurance {

    private BigDecimal rate;

    public VariableInsurance(BigDecimal rate){
        this.rate = rate.divide(new BigDecimal(100));
    }

    @Override
    public BigDecimal compute(BigDecimal insuredAmount) {
        return insuredAmount.multiply(rate);
    }
}
