package org.ypiel.invest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;

public class Util {

    public final static String formatBD(final BigDecimal bd){
        return NumberFormat.getCurrencyInstance().format(bd);
    }

    public final static BigDecimal annualPercent(final BigDecimal bd){
        return bd.divide(new BigDecimal(100), 10, RoundingMode.HALF_UP).divide(new BigDecimal(12), 10, RoundingMode.HALF_UP);
    }

    public final static BigDecimal percent(final BigDecimal bd){
        return bd.divide(new BigDecimal(100), 10, RoundingMode.HALF_UP);
    }

}
