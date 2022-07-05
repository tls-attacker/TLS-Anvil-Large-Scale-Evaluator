/**
 * TLS-Anvil-Large-Scale-Evaluator - A tool for executing TLS-Anvil against multiple targets running in Docker containers in parallel
 *
 * Copyright 2022 Ruhr University Bochum
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlstest.evaluator;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import de.rub.nds.tls.subject.TlsImplementationType;
import de.rub.nds.tlstest.evaluator.constants.EvaluationTaskType;
import de.rub.nds.tlstest.evaluator.constants.ImplementationModeType;

import java.nio.file.FileSystems;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Config {
    private static Config instance = null;

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    private Config() {

    }

    @Parameter(names = {"-i", "--implementation"}, description = "TLS implementation that should be evaluated", variableArity = true)
    private List<TlsImplementationType> implementations = new ArrayList<>();

    @Parameter(names = {"-ei", "--excludeImplementation"}, description = "Libraries that should be excluded", variableArity = true)
    private List<TlsImplementationType> excludedImplementation = new ArrayList<>();

    @Parameter(names = {"-v", "--version"}, description = "Regex that is matched against the version label of tls implementation docker images. " +
            "Same order as specified libraries.", variableArity = true)
    private List<String> versions = new ArrayList<>();

    @Parameter(names = {"-m", "--mode"}, description = "Only test client/servers", converter = ImplementationModeTypeConverter.class)
    private ImplementationModeType mode = ImplementationModeType.BOTH;

    @Parameter(names = {"-p", "--parallel"}, description = "How many evaluation tasks should be executed in parallel")
    private int parallel = Runtime.getRuntime().availableProcessors();

    @Parameter(names = {"-e", "--evaluator"}, description = "Evaluator that should be used", required = true, converter = EvaluationTaskTypeConverter.class)
    private EvaluationTaskType evaluator = null;
    
    @Parameter(names = {"-s", "--strength"}, description = "Strength of the pairwise test to be used by each Testsuite", required = true)
    private int strength = 2;
    
    @Parameter(names = {"-r", "--restart"}, description = "Set the number of handshakes before the target server should be restarted (0 = never)")
    private int restartServerAfter = 0;
    
    @Parameter(names = "--ram", description = "Set the maximum RAM used for each Docker container in GB")
    private int containerRAM = 25;

    @Parameter(names = {"--noRampUpTime"}, description = "Does not wait between starting tasks")
    private boolean noRampUpTime = false;

    @Parameter(names = "--testsuiteImage", description = "Name of the Docker image that is used for the testsuite")
    private String testsuiteImage = "tlsanvil:latest";
    
    @Parameter(names = {"-t", "--tags"}, description = "List of specific tagged tests to be used for the evaluation")
    private String testTags = null;
    
    @Parameter(names = "--package", description = "Name of a specific test package to be used for the evaluation")
    private String testPackage = null;
    
    private Date launchDate = new Date();

    private String outputFolder = FileSystems.getDefault().getPath("./output/" + new SimpleDateFormat("dd-MM-yy'T'HHmmss").format(launchDate))
            .normalize().toAbsolutePath().toString();

    public List<TlsImplementationType> getImplementations() {
        return implementations;
    }

    public List<String> getVersions() {
        return versions;
    }

    public ImplementationModeType getMode() {
        return mode;
    }

    public int getParallel() {
        return parallel;
    }

    public EvaluationTaskType getEvaluator() {
        return evaluator;
    }

    public List<TlsImplementationType> getExcludedImplementation() {
        return excludedImplementation;
    }

    public Date getLaunchDate() {
        return launchDate;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public boolean isNoRampUpTime() {
        return noRampUpTime;
    }

    public String getTestsuiteImage() {
        return testsuiteImage;
    }

    public void setTestsuiteImage(String testsuiteImage) {
        this.testsuiteImage = testsuiteImage;
    }


    public static class EvaluationTaskTypeConverter implements IStringConverter<EvaluationTaskType> {
        @Override
        public EvaluationTaskType convert(String value) {
            EvaluationTaskType convertedValue = EvaluationTaskType.fromString(value);

            if(convertedValue == null) {
                throw new ParameterException("Value " + value + "can not be converted to EvaluationTaskType. " +
                        "Available values are: testsuite");
            }
            return convertedValue;
        }
    }

    public static class ImplementationModeTypeConverter implements IStringConverter<ImplementationModeType> {
        @Override
        public ImplementationModeType convert(String value) {
            ImplementationModeType convertedValue = ImplementationModeType.fromString(value);

            if(convertedValue == null) {
                throw new ParameterException("Value " + value + "can not be converted to EvaluationTaskType. " +
                        "Available values are: client, server, both");
            }
            return convertedValue;
        }
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getRestartServerAfter() {
        return restartServerAfter;
    }

    public void setRestartServerAfter(int restartServerAfter) {
        this.restartServerAfter = restartServerAfter;
    }

    public int getContainerRAM() {
        return containerRAM;
    }

    public void setContainerRAM(int containerRAM) {
        this.containerRAM = containerRAM;
    }

    public String getTestTags() {
        return testTags;
    }

    public void setTestTags(String testTags) {
        this.testTags = testTags;
    }

    public String getTestPackage() {
        return testPackage;
    }

    public void setTestPackage(String testPackage) {
        this.testPackage = testPackage;
    }
}
