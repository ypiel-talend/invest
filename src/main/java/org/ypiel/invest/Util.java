package org.ypiel.invest;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class Util {

    public final static String formatBD(final BigDecimal bd){
        return NumberFormat.getCurrencyInstance().format(bd);
    }

}
