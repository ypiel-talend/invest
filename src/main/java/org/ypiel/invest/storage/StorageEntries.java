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
import java.util.List;
import java.util.UUID;

import org.ypiel.invest.BigFlatEntry;
import org.ypiel.invest.Entry;

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


    public void write(final String tableName, List<Entry> entries) {
        _checkConn();
        _write(tableName, entries);
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

        try {
            _createTable(name);

            List<Entry> entries = new ArrayList<>();

            entries.add(new Entry(LocalDate.now(), new BigDecimal(100.1234d), "This is a 100.1234 test.", false));
            entries.add(new Entry(LocalDate.now(), new BigDecimal(200.1234d), "This is a -200.1234 test.", true));

            this._write(name, entries);

            final BigFlatEntry first = _select(name);
            displayBigFlatEntries(System.out, first);

            log.info(String.format("Drop '%s' table...", name));
            final Statement drop = conn.createStatement();
            drop.execute("drop table " + name);

        } catch (SQLException e) {
            log.error(String.format("Checking database has failed."), e);
        }

    }

    private BigFlatEntry _select(final String name) throws SQLException {
        final Statement select = conn.createStatement();
        log.info(String.format("Select entries from '%s'...", name));
        final ResultSet res = select.executeQuery("select * from " + name);

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

    private void _createTable(final String tableName) {
        final String create = loadFileFromResources("sql/createSimuTable.sql");
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

    private void _write(final String tableName, final List<Entry> entries) {
        log.info(String.format("Store %s entries in '%s'.", entries.size(), tableName));
        int i = 0;
        int n = 100;
        try {
            EntryORB orb = new EntryORB();
            String sql = loadFileFromResources("sql/insert.sql");
            sql = sql.replace(table_name_placeholder, tableName);
            PreparedStatement ps = this.conn.prepareStatement(sql);
            for (Entry e : entries) {
                orb.addBatch(ps, e);
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
