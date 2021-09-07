package net.freudasoft;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.lang.String;

public class CMakeConfigureTask extends DefaultTask {
    private final Property<String> executable;
    private final DirectoryProperty workingFolder;
    private final DirectoryProperty sourceFolder;
    private final Property<String> configurationTypes;
    private final Property<String> installPrefix;
    private final Property<String> generator; // for example: "Visual Studio 16 2019"
    private final Property<String> platform; // for example "x64" or "Win32" or "ARM" or "ARM64", supported on vs > 8.0
    private final Property<String> toolset; // for example "v142", supported on vs > 10.0
    private final Property<String> distribution; 
    private final Property<Boolean> buildSharedLibs;
    private final Property<Boolean> buildStaticLibs;
    private final MapProperty<String, String> options;

    public CMakeConfigureTask() {
        setGroup("cmake");

        executable = getProject().getObjects().property(String.class);
        workingFolder = getProject().getObjects().directoryProperty();
        sourceFolder = getProject().getObjects().directoryProperty();
        configurationTypes = getProject().getObjects().property(String.class);
        installPrefix = getProject().getObjects().property(String.class);
        generator = getProject().getObjects().property(String.class);
        platform = getProject().getObjects().property(String.class);
        toolset = getProject().getObjects().property(String.class);
        distribution = getProject().getObjects().property(String.class);
        buildSharedLibs = getProject().getObjects().property(Boolean.class);
        buildStaticLibs = getProject().getObjects().property(Boolean.class);
        options = getProject().getObjects().mapProperty(String.class, String.class);
    }

    public void configureFromProject() {
        CMakePluginExtension ext = (CMakePluginExtension) getProject().getExtensions().getByName("cmake");
        executable.set(ext.getExecutable());
        workingFolder.set(ext.getWorkingFolder());
        sourceFolder.set(ext.getSourceFolder());
        configurationTypes.set(ext.getConfigurationTypes());
        installPrefix.set(ext.getInstallPrefix());
        generator.set(ext.getGenerator());
        platform.set(ext.getPlatform());
        toolset.set(ext.getToolset());
        distribution.set(ext.getDistribution());
        buildSharedLibs.set(ext.getBuildSharedLibs());
        buildStaticLibs.set(ext.getBuildStaticLibs());
        options.set(ext.getOptions());
    }

    @Input
    @Optional
    public Property<String> getExecutable() {
        return executable;
    }

    @OutputDirectory
    public DirectoryProperty getWorkingFolder() {
        return workingFolder;
    }

    @InputDirectory
    public DirectoryProperty getSourceFolder() {
        return sourceFolder;
    }

    @Input
    @Optional
    public Property<String> getConfigurationTypes() {
        return configurationTypes;
    }

    @Input
    @Optional
    public Property<String> getInstallPrefix() {
        return installPrefix;
    }

    @Input
    @Optional
    public Property<String> getGenerator() {
        return generator;
    }

    @Input
    @Optional
    public Property<String> getPlatform() {
        return platform;
    }

    @Input
    @Optional
    public Property<String> getToolset() {
        return toolset;
    }

    @Input
    @Optional
    public Property<String> getDistribution() {
        return distribution;
    }

    @Input
    @Optional
    public Property<Boolean> getBuildSharedLibs() {
        return buildSharedLibs;
    }

    @Input
    @Optional
    public Property<Boolean> getBuildStaticLibs() {
        return buildStaticLibs;
    }

    @Input
    @Optional
    public MapProperty<String, String> getOptions() {
        return options;
    }
    /// endregion

    private Boolean crossFromWin2Linux() {
        return getName().contains("linux") && System.getProperty("os.name").toLowerCase().contains("windows");
    }

    private List<String> buildCmdLine() throws Exception {
        List<String> parameters = new ArrayList<>();

        if (crossFromWin2Linux()) {
            parameters.add("wsl.exe");

            if (distribution.isPresent() && !distribution.get().isEmpty()) {
                parameters.add("--distribution");
                parameters.add(distribution.get());
            }
        }

        parameters.add(executable.getOrElse("cmake"));

        if (generator.isPresent() && !generator.get().isEmpty()) {
            parameters.add("-G");
            parameters.add(generator.get());
        }

        if (platform.isPresent() && !platform.get().isEmpty()) {
            parameters.add("-A");
            parameters.add(platform.get());
        }

        if (toolset.isPresent() && !toolset.get().isEmpty()) {
            parameters.add("-T");
            parameters.add(toolset.get());
        }

        if (configurationTypes.isPresent() && !configurationTypes.get().isEmpty())
            parameters.add("-DCMAKE_CONFIGURATION_TYPES=" + configurationTypes.get());

        if (installPrefix.isPresent() && !installPrefix.get().isEmpty())
            parameters.add("-DCMAKE_INSTALL_PREFIX=" + installPrefix.get());


        if (buildSharedLibs.isPresent())
            parameters.add("-DBUILD_SHARED_LIBS=" + (buildSharedLibs.get() ? "ON" : "OFF"));

        if (buildStaticLibs.isPresent())
            parameters.add("-DBUILD_STATIC_LIBS=" + (buildStaticLibs.get() ? "ON" : "OFF"));


        if (options.isPresent()) {
            for (Map.Entry<String, String> entry : options.get().entrySet())
                parameters.add("-D" + entry.getKey() + "=" + entry.getValue());
        }

        if (crossFromWin2Linux()) {
            parameters.add("$(wslpath -u '" + sourceFolder.getAsFile().get().getAbsolutePath() + "')");
        } else {
            parameters.add(sourceFolder.getAsFile().get().getAbsolutePath());
        }

        return parameters;
    }

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    @TaskAction
    public void configure() throws Exception {
        CMakeExecutor executor = new CMakeExecutor(getLogger(), getName());

        if(deleteDirectory(workingFolder.getAsFile().get())) {
            executor.exec(buildCmdLine(), workingFolder.getAsFile().get());
        } else {
            throw new GradleException("[" + getName() + "]Error: Directory can not be cleaned!");
        }
    }

}
