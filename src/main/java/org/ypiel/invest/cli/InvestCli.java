package org.ypiel.invest.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.ypiel.invest.storage.StorageEntries;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Command(name = "inverstcli", version = "0.1", mixinStandardHelpOptions = true)
public class InvestCli implements Runnable {

    @Option(names = {"--check-storage"}, description = "Check storage")
    boolean checkStorage = false;

    @Option(names = {"--valombois-Vannes"}, description = "Annonce valombois/Vannes")
    boolean valomboisVannes = false;

    @Option(names = {"--keep-data"}, description = "Don't drop tables after --check-storage")
    boolean keepData = false;

    @Option(names = {"--select-only"}, description = "Only display already inserted data from this name")
    String selectOnly;

    @Option(names = {"--drop"}, description = "Drop table before computing")
    boolean drop;

    @Option(names = {"--interactive"}, description = "Read config from input")
    boolean interactive;

    @Option(names = {"--file" }, description = "Print to this file")
    File output;

    @Override
    public void run() {
        PrintStream out = System.out;
        if(output != null){
            try {
                out = new PrintStream(output);
            }
            catch (FileNotFoundException e){
                log.error("File not found.", e);
            }
        }

        if(checkStorage){
            _checkStorage(keepData);
        }
        else if(valomboisVannes){
            _valomboisVannes(drop);
        }
        else if(interactive){
            _interactive(drop, out);
        }

        out.close();
        log.info("Done.");
    }

    private void _checkStorage(boolean keepData){
        log.info("Check storage...");
        try(StorageEntries o = new StorageEntries()) {
            o.checkStorage(keepData, selectOnly);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void _interactive(boolean drop, PrintStream out){
        log.info("Interactive mode...");
        try(StorageEntries o = new StorageEntries()) {
            o.interactive(drop, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void _valomboisVannes(boolean drop){
        log.info("Annonce valombois/Vannes...");
        try(StorageEntries o = new StorageEntries()) {
            o.valomboisVannes(drop, selectOnly);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new InvestCli()).execute(args);
        System.exit(exitCode);
    }
}
