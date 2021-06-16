package org.ypiel.invest.storage;

import java.io.IOException;
import java.io.PrintStream;
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
import java.util.Scanner;
import java.util.UUID;

import org.ypiel.invest.BigFlatEntry;
import org.ypiel.invest.Entry;
import org.ypiel.invest.Util;
import org.ypiel.invest.insurance.VariableInsurance;
import org.ypiel.invest.loan.Loan;
import org.ypiel.invest.loan.LoanLinkedEntry;
import org.ypiel.invest.recurring.Recurring;
import org.ypiel.invest.recurring.Recurring.Temporal;
import org.ypiel.invest.recurring.RecurringLinkedEntry;

import lombok.extern.slf4j.Slf4j;

import static org.ypiel.invest.Util.displayBigFlatEntries;
import static org.ypiel.invest.Util.displayLoans;
import static org.ypiel.invest.Util.loadFileFromResources;


@Slf4j
public class StorageEntries implements AutoCloseable {

    private final static String table_name_placeholder = "<table_name>";

    private final static String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private final static String protocol = "jdbc:derby:";
    private final static String url = protocol + "/home/ypiel/db/derbyDB;create=true;user=SIMU;password=SIMU"; //shutdown=true";

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

    public void writeLoans(final String tableName, List<Loan> loans) {
        _checkConn();
        _write(tableName, "sql/insert_loans.sql", new ArrayList<>(loans));
    }

    private void _checkConn() {
        if (this.conn == null) {
            throw new RuntimeException("You are not connected.");
        }
    }

    public void interactive(boolean drop, PrintStream out) {
        _checkConn();
        String name = null;
        System.out.print("Nom de la simu : ");
        Scanner scan = new Scanner(System.in);
        name = scan.next();

        String entities_names = name + "_entities";
        String loans_name = name + "_loans";

        try {

            if (drop) {
                log.info(String.format("Drop '%s' table...", entities_names));
                final Statement dropEntries = conn.createStatement();
                dropEntries.execute("drop table " + entities_names);

                log.info(String.format("Drop '%s' table...", loans_name));
                final Statement dropLoans = conn.createStatement();
                dropLoans.execute("drop table " + loans_name);
            }

            _createTable(entities_names, loans_name);

            List<Entry> entries = new ArrayList<>();

            System.out.print("Prix du bien :");
            BigDecimal prix = scan.nextBigDecimal();
            System.out.print("Frais d'agence :");
            BigDecimal agence = scan.nextBigDecimal();
            BigDecimal notaire_rate = Util.percent(new BigDecimal("7.36"));
            BigDecimal notaire = prix.multiply(notaire_rate);

            System.out.println("Notaire : " + notaire);

            prix = prix.add(agence);

            System.out.println("Prêt immobilier");
            System.out.print("Apport : ");
            BigDecimal apport = scan.nextBigDecimal();
            System.out.print("Frais bancaire : ");
            BigDecimal fees = scan.nextBigDecimal();
            System.out.print("Taux prêt : ");
            BigDecimal rate = scan.nextBigDecimal();
            System.out.print("Taux assurance : ");
            BigDecimal rate_insurance = scan.nextBigDecimal();
            System.out.print("Mensualité : ");
            BigDecimal monthly_amount = scan.nextBigDecimal();
            BigDecimal amount_loan = prix.add(notaire).add(fees).subtract(apport);

            System.out.println("Montant du prêt : " + amount_loan);

            System.out.println("Location");
            System.out.print("Loyer : ");
            BigDecimal loyer = scan.nextBigDecimal();
            System.out.print("Charges : ");
            BigDecimal charges = scan.nextBigDecimal();

            System.out.println("Impôts supplémentaires");
            System.out.print("Impôts : ");
            BigDecimal impots = scan.nextBigDecimal();

            entries.add(new Entry(LocalDate.now(), notaire, "Notaire", true));

            Loan l = new Loan("Prêt Immo", fees, LocalDate.now(), monthly_amount, rate, amount_loan, new VariableInsurance(rate_insurance));
            final LoanLinkedEntry loanLinkedEntry = l.computePaymentPlan(new BigDecimal(50.0d));
            entries.addAll(loanLinkedEntry.asList());

            Recurring rloyer = new Recurring("Loyer", LocalDate.now(), LocalDate.now().plusMonths(loanLinkedEntry.size()), BigDecimal.ZERO, loyer, false, Temporal.MONTHLY);
            final RecurringLinkedEntry recurringLinkedEntryLoyer = rloyer.computePaymentPlan();
            entries.addAll(recurringLinkedEntryLoyer.asList());

            Recurring rcharges = new Recurring("Charge", LocalDate.now(), LocalDate.now().plusMonths(loanLinkedEntry.size()), BigDecimal.ZERO, charges, true, Temporal.MONTHLY);
            final RecurringLinkedEntry recurringLinkedEntryCharge = rcharges.computePaymentPlan();
            entries.addAll(recurringLinkedEntryCharge.asList());

            Recurring rimpots = new Recurring("Impots", LocalDate.now(), LocalDate.now().plusMonths(loanLinkedEntry.size()), BigDecimal.ZERO, impots, true, Temporal.ANNUALLY);
            final RecurringLinkedEntry recurringLinkedEntryImpots = rimpots.computePaymentPlan();
            entries.addAll(recurringLinkedEntryImpots.asList());

            this.writeLoans(loans_name, Arrays.asList(l));
            this.writeEntries(entities_names, entries);

            final BigFlatEntry first = _selectEntries(entities_names);
            displayBigFlatEntries(out, first);

            log.info(String.format("Durée du prêt : %d mois / %d années & %d mois.", loanLinkedEntry.size(), (loanLinkedEntry.size() / 12), (loanLinkedEntry.size() % 12)));
            final BigDecimal cout_pret_brut = ((LoanLinkedEntry) loanLinkedEntry.getLast()).asList().stream().map(e -> e.getAmount()).reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
            log.info(String.format("Cout total du prêt brut : %f", cout_pret_brut));
            final BigDecimal cout_pret = ((LoanLinkedEntry) loanLinkedEntry.getLast()).totalCost();
            log.info(String.format("Cout total du prêt net après revente : %f", cout_pret));
            final BigDecimal cout_impots = ((RecurringLinkedEntry)recurringLinkedEntryImpots.getLast()).total();
            log.info(String.format("Cout total impots : %f", cout_impots));
            final BigDecimal cout_charges = ((RecurringLinkedEntry)recurringLinkedEntryCharge.getLast()).total();
            log.info(String.format("Cout total charges : %f", cout_charges));
            final BigDecimal apport_loyer = ((RecurringLinkedEntry) recurringLinkedEntryLoyer.getLast()).total();
            log.info(String.format("Apport loyer : %f", apport_loyer));


        } catch (SQLException e) {
            log.error(String.format("Checking database has failed."), e);
        }
    }


    public void valomboisVannes(boolean drop, String selectOnly) {
        _checkConn();
        String name = "valomboisVannes";
        String entities_names = name + "_entities";
        String loans_name = name + "_loans";

        try {
            if (selectOnly == null) {

                if (drop) {
                    log.info(String.format("Drop '%s' table...", entities_names));
                    final Statement dropEntries = conn.createStatement();
                    dropEntries.execute("drop table " + entities_names);

                    log.info(String.format("Drop '%s' table...", loans_name));
                    final Statement dropLoans = conn.createStatement();
                    dropLoans.execute("drop table " + loans_name);
                }

                _createTable(entities_names, loans_name);

                List<Entry> entries = new ArrayList<>();

                entries.add(new Entry(LocalDate.now(), new BigDecimal(13000.0d), "Notaire", true));

                Loan l = new Loan("Prêt Immo", new BigDecimal(500.0d), LocalDate.now(), new BigDecimal(.0d), new BigDecimal(1.2d), new BigDecimal(199000.0d), new VariableInsurance(new BigDecimal(0.33d)));
                final LoanLinkedEntry loanLinkedEntry = l.computePaymentPlan(new BigDecimal(50.0d));
                entries.addAll(loanLinkedEntry.asList());

                Recurring r = new Recurring("Loyer", LocalDate.now(), LocalDate.now().plusMonths(35 * 12), BigDecimal.ZERO, new BigDecimal(800d), false, Temporal.MONTHLY);
                final RecurringLinkedEntry recurringLinkedEntry = r.computePaymentPlan();
                entries.addAll(recurringLinkedEntry.asList());

                this.writeLoans(loans_name, Arrays.asList(l));
                this.writeEntries(entities_names, entries);
            }

            final BigFlatEntry first = _selectEntries(entities_names);
            displayBigFlatEntries(System.out, first);

        } catch (SQLException e) {
            log.error(String.format("Checking database has failed."), e);
        }
    }

    public void checkStorage(boolean keepData, String selectOnly) {
        _checkConn();


        final UUID uuid = UUID.randomUUID();
        String name = uuid.toString();
        if (name.length() > 7) {
            name = name.substring(0, 7);
        }

        if (selectOnly != null) {
            name = selectOnly;
        }

        name = "invest_" + name;

        String entities_names = name + "_entities";
        String loans_name = name + "_loans";

        try {
            if (selectOnly == null) {

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
            }

            final List<Loan> loans = _selectLoans(loans_name);
            displayLoans(System.out, loans);

            final BigFlatEntry first = _selectEntries(entities_names);
            displayBigFlatEntries(System.out, first);

            if (!keepData) {
                log.info(String.format("Drop '%s' table...", entities_names));
                final Statement dropEntries = conn.createStatement();
                dropEntries.execute("drop table " + entities_names);

                log.info(String.format("Drop '%s' table...", loans_name));
                final Statement dropLoans = conn.createStatement();
                dropLoans.execute("drop table " + loans_name);
            }

        } catch (SQLException e) {
            log.error(String.format("Checking database has failed."), e);
        }

    }

    private List<Loan> _selectLoans(final String name) throws SQLException {
        final Statement select = conn.createStatement();
        log.info(String.format("Select loans from '%s'...", name));
        final ResultSet res = select.executeQuery("select * from " + name + " order by start");

        EntryORB orb = new EntryORB();
        List<Loan> loans = new ArrayList<>();
        while (res.next()) {
            Loan l = orb.createLoans(res);
            loans.add(l);
        }

        res.close();
        select.close();

        return loans;
    }

    private BigFlatEntry _selectEntries(final String name) throws SQLException {
        final Statement select = conn.createStatement();
        log.info(String.format("Select entries from '%s'...", name));
        final ResultSet res = select.executeQuery("select * from " + name + " order by date");

        EntryORB orb = new EntryORB();
        BigFlatEntry current = null;
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
        if (o instanceof Entry) {
            orb.addBatch(ps, (Entry) o);
        } else if (o instanceof Loan) {
            orb.addBatch(ps, (Loan) o);
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
