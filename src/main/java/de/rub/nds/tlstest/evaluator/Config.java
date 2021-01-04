/**
 * TLS-Testsuite-Large-Scale-Evaluator - A tool for executing the TLS-Testsuite against multiple targets running in Docker containers in parallel
 *
 * Copyright 2020 Ruhr University Bochum and
 * TÜV Informationstechnik GmbH
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
    private int strength = 4;

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
}
