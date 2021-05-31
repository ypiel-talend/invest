package org.ypiel.invest.insurance;


import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import static org.ypiel.invest.Util.annualPercent;

@Getter
@EqualsAndHashCode
public class VariableInsurance implements Insurance {

    private BigDecimal rate;

    public VariableInsurance(BigDecimal rate){
        this.rate = annualPercent(rate); //rate.divide(new BigDecimal(100), 10, RoundingMode.HALF_UP).divide(new BigDecimal(12), 10, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal compute(BigDecimal insuredAmount) {
        return insuredAmount.multiply(rate);
    }

    @Override
    public BigDecimal getParam() {
        return this.getRate();
    }
}
