package org.ypiel.invest.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.ypiel.invest.Entry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Output {

    private final static String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private final static String protocol = "jdbc:derby:";
    private final static String url = protocol + "/tmp/derbyDB;create=true;shutdown=true";

    public void write(List<Entry> entries){

        try {
            Class.forName(driver);
            try(Connection conn = DriverManager.getConnection(url)){
                _checkDB(conn);
                _write(conn, entries);
            }
        } catch (ClassNotFoundException | SQLException e) {
            log.error("Can't write entries : " + e.getMessage());
            e.printStackTrace(System.err);
        }


    }

    private void _checkDB(final Connection conn){
        
    }

    private void _write(final Connection conn, final List<Entry> entries){

    }


}
