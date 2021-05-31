package org.ypiel.invest.insurance;

import java.math.BigDecimal;

public class InsuranceFactory {

    public enum Type {
        FIXED("F"),
        VARIABLE("V");

        private final String t;

        Type(String t){
            this.t = t;
        }

        public String getId(){
            return t;
        }
    }

    private InsuranceFactory(){}

    public static Insurance createInsurance(String type, BigDecimal value){
        if(type.equals(Type.FIXED.getId())){
            return new FixedInsurance(value);
        }
        else{
            return new VariableInsurance(value);
        }
    }

    public static String getType(Insurance i){
        if(i instanceof FixedInsurance){
            return Type.FIXED.getId();
        }
        else{
            return Type.VARIABLE.getId();
        }
    }
}
