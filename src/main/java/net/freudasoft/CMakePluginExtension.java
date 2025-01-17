/**
 * Copyright 2019 Marco Freudenberger
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.freudasoft;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import java.io.File;

public class CMakePluginExtension {


    // parameters used by config and build step
    private final Property<String> executable;
    private final DirectoryProperty workingFolder;

    // parameters used by config step
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

    // parameters used on build step
    private final Property<String> buildConfig;
    private final Property<String> buildTarget;
    private final Property<Boolean> buildClean;
    private final Property<String> jobCount;


    public CMakePluginExtension(Project project) {
        executable = project.getObjects().property(String.class);
        workingFolder = project.getObjects().directoryProperty();
        sourceFolder = project.getObjects().directoryProperty();
        configurationTypes = project.getObjects().property(String.class);
        installPrefix = project.getObjects().property(String.class);
        generator = project.getObjects().property(String.class);
        platform = project.getObjects().property(String.class);
        toolset = project.getObjects().property(String.class);
        distribution = project.getObjects().property(String.class);
        buildSharedLibs = project.getObjects().property(Boolean.class);
        buildStaticLibs = project.getObjects().property(Boolean.class);
        options = project.getObjects().mapProperty(String.class, String.class);
        buildConfig = project.getObjects().property(String.class);
        buildTarget = project.getObjects().property(String.class);
        buildClean = project.getObjects().property(Boolean.class);
        jobCount = project.getObjects().property(String.class);
    }

    /// region getters
    public Property<String> getJobCount() {
        return jobCount;
    }

    public Property<String> getExecutable() {
        return executable;
    }

    public DirectoryProperty getWorkingFolder() {
        return workingFolder;
    }

    public DirectoryProperty getSourceFolder() {
        return sourceFolder;
    }

    public Property<String> getConfigurationTypes() {
        return configurationTypes;
    }

    public Property<String> getInstallPrefix() {
        return installPrefix;
    }

    public Property<String> getGenerator() {
        return generator;
    }

    public Property<String> getPlatform() {
        return platform;
    }

    public Property<String> getToolset() {
        return toolset;
    }

    public Property<String> getDistribution() {
        return distribution;
    }

    public Property<Boolean> getBuildSharedLibs() {
        return buildSharedLibs;
    }

    public Property<Boolean> getBuildStaticLibs() {
        return buildStaticLibs;
    }

    public MapProperty<String, String> getOptions() {
        return options;
    }

    public Property<String> getBuildConfig() {
        return buildConfig;
    }

    public Property<String> getBuildTarget() {
        return buildTarget;
    }

    public Property<Boolean> getBuildClean() {
        return buildClean;
    }
/// endregion getters


}