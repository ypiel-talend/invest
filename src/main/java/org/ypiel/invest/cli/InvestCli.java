package org.ypiel.invest.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Collections;

import org.ypiel.invest.storage.Output;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Command(name = "inverstcli", version = "0.1", mixinStandardHelpOptions = true)
public class InvestCli implements Runnable {

    @Option(names = {"--check-storage"}, description = "Check storage")
    boolean checkStorage = false;

    @Override
    public void run() {
        if(checkStorage){
            _checkStorage();
        }
        log.info("Done.");
    }

    private void _checkStorage(){
        log.info("Check storage...");
        Output o = new Output();
        o.write(Collections.emptyList());
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new InvestCli()).execute(args);
        System.exit(exitCode);
    }
}
