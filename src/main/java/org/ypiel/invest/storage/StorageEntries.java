package org.ypiel.invest.storage;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.ypiel.invest.BigFlatEntry;
import org.ypiel.invest.Entry;
import org.ypiel.invest.insurance.FixedInsurance;
import org.ypiel.invest.insurance.VariableInsurance;
import org.ypiel.invest.loan.Loan;
import org.ypiel.invest.loan.LoanLinkedEntry;
import org.ypiel.invest.recurring.Recurring;
import org.ypiel.invest.recurring.Recurring.Temporal;
import org.ypiel.invest.recurring.RecurringLinkedEntry;

import lombok.extern.slf4j.Slf4j;

import static org.ypiel.invest.Util.displayBigFlatEntries;
import static org.ypiel.invest.Util.loadFileFromResources;

@Slf4j
public class StorageEntries implements AutoCloseable {

    private final static String table_name_placeholder = "<table_name>";

    private final static String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private final static String protocol = "jdbc:derby:";
    private final static String url = protocol + "/tmp/derbyDB;create=true;"; //shutdown=true";

    private Connection conn = null;

    public StorageEntries() {
        this._connect();
    }

    private void _connect() {
        log.info("Load jdbc driver : " + driver);
        log.info("jdbc url : " + url);
        try {
            Class.forName(driver);
            this.conn = DriverManager.getConnection(url);
        } catch (ClassNotFoundException | SQLException e) {
            log.error("Can't connect.", e);
        }
    }


    @Override
    public void close() {
        _checkConn();

        try {
            this.conn.close();
        } catch (SQLException e) {
            log.error("Can't close connection.", e);
        }

        log.info("Connection closed.");
    }


    public void writeEntries(final String tableName, List<Entry> entries) {
        _checkConn();
        _write(tableName, "sql/insert_entries.sql", new ArrayList<>(entries));
    }

    public void writeLoans(final String tableName, List<Loan> loans){
        _checkConn();
        _write(tableName, "sql/insert_loans.sql", new ArrayList<>(loans));
    }

    private void _checkConn() {
        if (this.conn == null) {
            throw new RuntimeException("You are not connected.");
        }
    }

    public void checkStorage() {
        _checkConn();


        final UUID uuid = UUID.randomUUID();
        String name = uuid.toString();
        if (name.length() > 7) {
            name = name.substring(0, 7);
        }
        name = "invest_" + name;

        String entities_names = name + "entities";
        String loans_name = name + "_loans";

        try {
            _createTable(entities_names, loans_name);

            List<Entry> entries = new ArrayList<>();

            entries.add(new Entry(LocalDate.now(), new BigDecimal(100.1234d), "This is a 100.1234 test.", false));
            entries.add(new Entry(LocalDate.now(), new BigDecimal(200.1234d), "This is a -200.1234 test.", true));

            Loan l = new Loan("Loan", new BigDecimal(500.0d), LocalDate.now(), new BigDecimal(700.0d), new BigDecimal(1.5d), new BigDecimal(50000.0d), new VariableInsurance(new BigDecimal(0.33d)));
            final LoanLinkedEntry loanLinkedEntry = l.computePaymentPlan(new BigDecimal(50.0d));
            entries.addAll(loanLinkedEntry.asList());
            this.writeLoans(loans_name, Arrays.asList(l));

            Recurring r = new Recurring("Recurring", LocalDate.now(), LocalDate.now().plusMonths(5), BigDecimal.ZERO, new BigDecimal(35.50d), true, Temporal.MONTHLY);
            final RecurringLinkedEntry recurringLinkedEntry = r.computePaymentPlan();
            entries.addAll(recurringLinkedEntry.asList());

            Recurring rtaxes = new Recurring("Taxes", LocalDate.now(), LocalDate.now().plusYears(4), new BigDecimal(2.0d), new BigDecimal(800.0d), true, Temporal.ANNUALLY);
            final RecurringLinkedEntry taxes = rtaxes.computePaymentPlan();
            entries.addAll(taxes.asList());

            Recurring rrent = new Recurring("Rent", LocalDate.now(), LocalDate.now().plusYears(10), new BigDecimal(1.7d), new BigDecimal(750.0d), false, Temporal.MONTHLY);
            final RecurringLinkedEntry rent = rrent.computePaymentPlan();
            entries.addAll(rent.asList());

            this.writeEntries(entities_names, entries);

            final BigFlatEntry first = _select(entities_names);
            displayBigFlatEntries(System.out, first);

            log.info(String.format("Drop '%s' table...", entities_names));
            final Statement drop = conn.createStatement();
            drop.execute("drop table " + entities_names);

        } catch (SQLException e) {
            log.error(String.format("Checking database has failed."), e);
        }

    }

    private BigFlatEntry _select(final String name) throws SQLException {
        final Statement select = conn.createStatement();
        log.info(String.format("Select entries from '%s'...", name));
        final ResultSet res = select.executeQuery("select * from " + name + " order by date");

        EntryORB orb = new EntryORB();
        BigFlatEntry current = null;
        //res.beforeFirst();
        while (res.next()) {
            BigFlatEntry bfe = orb.createEntity(res);
            if (current != null) {
                bfe.setPrevious(current);
                current.setNext(bfe);
            }
            current = bfe;
        }

        res.close();
        select.close();

        return current == null ? null : current.getFirst();
    }

    private void _createTable(final String entities, final String loans) {
        _create(entities, "sql/create_entries.sql");
        _create(loans, "sql/create_loans.sql");
    }

    private void _create(final String tableName, final String file) {
        final String create = loadFileFromResources(file);
        final String replace = create.replace(table_name_placeholder, tableName);
        try {
            final Statement st = conn.createStatement();
            log.debug("Create test table:\n" + replace);
            st.executeUpdate(replace);
            st.close();
        } catch (SQLException e) {
            throw new RuntimeException(String.format("Can't create table '%s'.", tableName), e);
        }
    }

    private void prepareStatement(final Object o, EntryORB orb, final PreparedStatement ps) throws SQLException {
        if(o instanceof Entry){
            orb.addBatch(ps, (Entry)o);
        }
        else if(o instanceof Loan){
            orb.addBatch(ps, (Loan)o);
        }
    }

    private void _write(final String tableName, final String file, final List<Object> list) {
        log.info(String.format("Store %s elements in '%s'.", list.size(), tableName));
        int i = 0;
        int n = 100;
        try {
            EntryORB orb = new EntryORB();
            String sql = loadFileFromResources(file);
            sql = sql.replace(table_name_placeholder, tableName);
            PreparedStatement ps = this.conn.prepareStatement(sql);
            for (Object o : list) {
                prepareStatement(o, orb, ps);
                i++;
                if (i % n == 0) {
                    ps.executeBatch();
                    ps = this.conn.prepareStatement(sql);
                    this.conn.commit();
                    i = 0;
                }
            }
            if (i > 0) {
                ps.executeBatch();
                this.conn.commit();
            }
            ps.close();
        } catch (SQLException e) {
            log.error("Can't write entries.", e);
        }
    }

}
