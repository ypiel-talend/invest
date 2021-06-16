package org.ypiel.invest.storage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.ypiel.invest.BigFlatEntry;
import org.ypiel.invest.Entry;
import org.ypiel.invest.LinkedEntry;
import org.ypiel.invest.insurance.Insurance;
import org.ypiel.invest.insurance.InsuranceFactory;
import org.ypiel.invest.loan.Loan;
import org.ypiel.invest.loan.LoanLinkedEntry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntryORB {

    private final static int scale = 2;

    public void addBatch(PreparedStatement ps, Loan e) throws SQLException {
        log.info("Store loan...");
        ps.clearParameters();
        ps.setString(1, e.getName());
        ps.setBigDecimal(2, e.getApplicationFees().setScale(scale, RoundingMode.HALF_UP));
        ps.setDate(3, Date.valueOf(e.getStart()));
        ps.setBigDecimal(4, e.getMonthlyAmount().setScale(scale, RoundingMode.HALF_UP));
        ps.setBigDecimal(5, e.getRate().setScale(scale, RoundingMode.HALF_UP));
        ps.setBigDecimal(6, e.getAmount().setScale(scale, RoundingMode.HALF_UP));
        ps.setBigDecimal(7, e.getInsurance().getParam().setScale(scale, RoundingMode.HALF_UP));
        ps.setString(8, InsuranceFactory.getType(e.getInsurance()));

        ps.addBatch();
    }


    public void addBatch(PreparedStatement ps, Entry e) throws SQLException {
        ps.clearParameters();
        ps.setDate(1, Date.valueOf(e.getDate()));
        ps.setBigDecimal(2, e.getAmount(true).setScale(scale, RoundingMode.HALF_UP));
        ps.setBigDecimal(3, e.getIncomes().setScale(scale, RoundingMode.HALF_UP));
        ps.setBigDecimal(4, e.getOutcomes().setScale(scale, RoundingMode.HALF_UP));
        ps.setString(5, e.getSummary());

        if (e instanceof LinkedEntry) {
            linkedEntryToPs(ps, (LinkedEntry) e);
        }

        if (e instanceof LoanLinkedEntry) {
            loanLinkedEntryToPs(ps, (LoanLinkedEntry) e);
        }

        ps.addBatch();
    }

    private void linkedEntryToPs(PreparedStatement ps, LinkedEntry e) throws SQLException {
        ps.setString(6, e.getName());
        ps.setInt(7, e.getId());
    }

    private void loanLinkedEntryToPs(PreparedStatement ps, LoanLinkedEntry e) throws SQLException {
        ps.setBigDecimal(8, e.getCapital().setScale(scale, RoundingMode.HALF_UP));
        ps.setBigDecimal(9, e.getInterest().setScale(scale, RoundingMode.HALF_UP));
        ps.setBigDecimal(10, e.getInsurance().setScale(scale, RoundingMode.HALF_UP));
        ps.setBigDecimal(11, e.getRemaining().setScale(scale, RoundingMode.HALF_UP));
        ps.setBigDecimal(12, e.getPrepayment().setScale(scale, RoundingMode.HALF_UP));
    }

    public BigFlatEntry createEntity(ResultSet res) throws SQLException {
        BigFlatEntry bfe = new BigFlatEntry();
        bfe.setId(res.getInt("id"));
        bfe.setDate(res.getDate("date").toLocalDate());
        bfe.setAmount(res.getBigDecimal("amount"));
        bfe.setIncomes(res.getBigDecimal("incomes"));
        bfe.setOutcomes(res.getBigDecimal("outcomes"));
        bfe.setSummary(res.getString("summary"));
        bfe.setLinkedentry_name(res.getString("linkedentry_name"));
        bfe.setLinkedentry_id(res.getInt("linkedentry_id"));
        bfe.setLinkedentry_capital(res.getBigDecimal("linkedentry_capital"));
        bfe.setLinkedentry_interest(res.getBigDecimal("linkedentry_interest"));
        bfe.setLinkedentry_insurance(res.getBigDecimal("linkedentry_insurance"));
        bfe.setLinkedentry_remaining(res.getBigDecimal("linkedentry_remaining"));
        bfe.setLinkedentry_remaining(res.getBigDecimal("linkedentry_prepayment"));

        return bfe;
    }

    public Loan createLoans(ResultSet res) throws SQLException {
        String name = res.getString("name");
        BigDecimal applicationFees = res.getBigDecimal("applicationFees");
        LocalDate start = res.getDate("start").toLocalDate();
        BigDecimal monthlyAmount = res.getBigDecimal("monthlyAmount");
        BigDecimal rate = res.getBigDecimal("rate");
        BigDecimal amount = res.getBigDecimal("amount");

        BigDecimal insuranceVal = res.getBigDecimal("insurance");
        String insuranceType = res.getString("insurance_type");

        final Insurance insurance = InsuranceFactory.createInsurance(insuranceType, insuranceVal, true);

        return new Loan(name, applicationFees, start, monthlyAmount, rate, amount, insurance, true);
    }
}
