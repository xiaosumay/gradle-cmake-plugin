package net.freudasoft;

import org.gradle.api.GradleException;
import org.gradle.api.GradleScriptException;
import org.gradle.api.logging.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import java.lang.System;

public class CMakeExecutor {
    private final Logger logger;
    private final String taskName;

    CMakeExecutor(Logger logger, String taskName) {
        this.logger = logger;
        this.taskName = taskName;
    }

    protected void exec(List<String> cmdLine, File workingFolder) throws GradleException {
        // log command line parameters
        StringBuilder sb = new StringBuilder("  CMakePlugin.task " + taskName + " - exec: ");
        for (String s : cmdLine) {
            sb.append(s).append(" ");
        }
        System.out.println(sb);

        // build process
        ProcessBuilder pb = new ProcessBuilder(cmdLine);
        pb.environment().putAll(System.getenv());
        pb.directory(workingFolder);


        try {
            // make sure working folder exists
            workingFolder.mkdirs();

            pb.redirectErrorStream(true);

            // start
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while (null != (line = reader.readLine())) {
                System.out.println(line);
            }

            int retCode = process.waitFor();
            if (retCode != 0)
                throw new GradleException("[" + taskName + "]Error: CMAKE returned " + retCode);
        } catch (IOException | InterruptedException e) {
            throw new GradleScriptException("CMakeExecutor[" + taskName + "].", e);
        }
    }
}

