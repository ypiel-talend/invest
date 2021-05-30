package org.ypiel.invest.storage;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.ypiel.invest.BigFlatEntry;
import org.ypiel.invest.Entry;
import org.ypiel.invest.LinkedEntry;
import org.ypiel.invest.loan.LoanLinkedEntry;

public class EntryORB {

    public void addBatch(PreparedStatement ps, Entry e) throws SQLException {
        ps.clearParameters();
        ps.setDate(1, Date.valueOf(e.getDate()));
        ps.setBigDecimal(2, e.getAmount(true));
        ps.setString(3, e.getSummary());

        if(e instanceof LinkedEntry){
            linkedEntryToPs(ps, (LinkedEntry)e);
        }

        if(e instanceof LoanLinkedEntry){
            loanLinkedEntryToPs(ps, (LoanLinkedEntry)e);
        }

        ps.addBatch();
    }

    private void linkedEntryToPs(PreparedStatement ps, LinkedEntry e) throws SQLException {
        ps.setString(4, e.getName());
        ps.setInt(5, e.getId());
    }

    private void loanLinkedEntryToPs(PreparedStatement ps, LoanLinkedEntry e) throws SQLException {
        ps.setBigDecimal(6, e.getCapital());
        ps.setBigDecimal(7, e.getInterest());
        ps.setBigDecimal(8, e.getInsurance());
        ps.setBigDecimal(9, e.getRemaining());
        ps.setBigDecimal(10, e.getPrepayment());
    }

    public BigFlatEntry createEntity(ResultSet res) throws SQLException {
        BigFlatEntry bfe = new BigFlatEntry();
        bfe.setId(res.getInt("id"));
        bfe.setDate(res.getDate("date").toLocalDate());
        bfe.setAmount(res.getBigDecimal("amount"));
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
}
