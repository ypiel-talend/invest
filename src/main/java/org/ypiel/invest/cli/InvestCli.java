package org.ypiel.invest.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import org.ypiel.invest.storage.StorageEntries;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Command(name = "inverstcli", version = "0.1", mixinStandardHelpOptions = true)
public class InvestCli implements Runnable {

    @Option(names = {"--check-storage"}, description = "Check storage")
    boolean checkStorage = false;

    @Option(names = {"--keep-data"}, description = "Don't drop tables after --check-storage")
    boolean keepData = false;

    @Option(names = {"--select-only"}, description = "Only display already inserted data from this name")
    String selectOnly;

    @Override
    public void run() {
        if(checkStorage){
            _checkStorage(keepData);
        }
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

    public static void main(String[] args) {
        int exitCode = new CommandLine(new InvestCli()).execute(args);
        System.exit(exitCode);
    }
}
