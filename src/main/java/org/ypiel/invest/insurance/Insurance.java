package org.ypiel.invest.insurance;

import java.math.BigDecimal;

public interface Insurance {

    BigDecimal compute(final BigDecimal insuredAmount);

    BigDecimal getParam();

}
