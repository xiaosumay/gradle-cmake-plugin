package net.freudasoft;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.util.ArrayList;
import java.util.List;

public class CMakeBuildTask extends DefaultTask {
    private final Property<String> executable;
    private final DirectoryProperty workingFolder;
    private final Property<String> buildConfig;
    private final Property<String> buildTarget;
    private final Property<Boolean> buildClean;
    private final Property<String> distribution;

    private final Property<String> jobCount;

    public CMakeBuildTask() {
        setGroup("cmake");

        executable = getProject().getObjects().property(String.class);
        workingFolder = getProject().getObjects().directoryProperty();
        buildConfig = getProject().getObjects().property(String.class);
        buildTarget = getProject().getObjects().property(String.class);
        buildClean = getProject().getObjects().property(Boolean.class);
        distribution = getProject().getObjects().property(String.class);
        jobCount = getProject().getObjects().property(String.class);
    }

    public void configureFromProject() {
        CMakePluginExtension ext = (CMakePluginExtension) getProject().getExtensions().getByName("cmake");
        executable.set(ext.getExecutable());
        workingFolder.set(ext.getWorkingFolder());
        buildConfig.set(ext.getBuildConfig());
        buildTarget.set(ext.getBuildTarget());
        buildClean.set(ext.getBuildClean());
        distribution.set(ext.getDistribution());
        jobCount.set(ext.getJobCount());
    }


    /// region getters
    @Input
    @Optional
    public Property<String> getExecutable() {
        return executable;
    }

    @InputDirectory
    public DirectoryProperty getWorkingFolder() {
        return workingFolder;
    }

    @Input
    @Optional
    public Property<String> getBuildConfig() {
        return buildConfig;
    }

    @Input
    @Optional
    public Property<String> getBuildTarget() {
        return buildTarget;
    }

    @Input
    @Optional
    public Property<Boolean> getBuildClean() {
        return buildClean;
    }

    @Input
    @Optional
    public Property<String> getDistribution() {
        return distribution;
    }

    @Input
    @Optional
    public Property<String> getJobCount() {
        return jobCount;
    }
    /// endregion

    private Boolean crossFromWin2Linux() {
        return getName().contains("linux") && System.getProperty("os.name").toLowerCase().contains("windows");
    }

    private List<String> buildCmdLine() {
        List<String> parameters = new ArrayList<>();

        if (crossFromWin2Linux()) {
            parameters.add("wsl.exe");

            if (distribution.isPresent() && !distribution.get().isEmpty()) {
                parameters.add("--distribution");
                parameters.add(distribution.get());
            }
        }

        String cmake = executable.getOrElse("cmake");

        if (crossFromWin2Linux() && cmake.indexOf(":") == 1) {
            parameters.add("$(wslpath -u '" + cmake + "')");
        } else {
            parameters.add(cmake);
        }

        parameters.add("--build");
        parameters.add("."); // working folder will be executable working dir --- workingFolder.getAsFile().get().getAbsolutePath()

        if (buildConfig.isPresent()) {
            parameters.add("--config");
            parameters.add(buildConfig.get());
        }

        if (buildTarget.isPresent()) {
            parameters.add("--target");
            parameters.add(buildTarget.get());
        }

        if (buildClean.getOrElse(Boolean.FALSE))
            parameters.add("--clean-first");

        if (jobCount.isPresent()) {
            parameters.add("--");

            int pn = Runtime.getRuntime().availableProcessors();
            if (pn > 8) pn = 8;

            parameters.add("-j" + pn);
        }

        return parameters;
    }


    @TaskAction
    public void build() {
        CMakeExecutor executor = new CMakeExecutor(getLogger(), getName());
        executor.exec(buildCmdLine(), workingFolder.getAsFile().get());
    }

}
