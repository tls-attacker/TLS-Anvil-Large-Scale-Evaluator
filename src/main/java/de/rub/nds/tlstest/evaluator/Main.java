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


import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.github.dockerjava.api.model.Image;
import de.rub.nds.tls.subject.TlsImplementationType;
import de.rub.nds.tls.subject.constants.TlsImageLabels;
import de.rub.nds.tls.subject.docker.DockerTlsManagerFactory;
import de.rub.nds.tlstest.evaluator.constants.ImplementationModeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) {
        Config config = Config.getInstance();

        try {
            JCommander.newBuilder().addObject(config).build().parse(args);
        } catch (ParameterException E) {
            LOGGER.error(E);
            JCommander.newBuilder().addObject(config).build().usage();
            return;
        }


        if (config.getVersions().size() > 0
                && config.getImplementations().size() > 0
                && config.getVersions().size() != config.getImplementations().size()) {
            LOGGER.warn("Implementations and versions differ in size");
        }
        
        if(config.getContainerRAM() > 200) {
            LOGGER.warn("Container RAM should be given in GB - limit is very high: " + config.getContainerRAM());
        }

        List<Image> images = DockerTlsManagerFactory.getAllImages().parallelStream().filter(image -> {
            TlsImplementationType implementation = TlsImplementationType.fromString(image.getLabels().get(TlsImageLabels.IMPLEMENTATION.getLabelName()));
            String version = image.getLabels().get(TlsImageLabels.VERSION.getLabelName());
            String role = image.getLabels().get(TlsImageLabels.CONNECTION_ROLE.getLabelName());

            if (config.getImplementations().size() > 0 && !config.getImplementations().contains(implementation)) {
                return false;
            }

            if (config.getExcludedImplementation().size() > 0 && config.getExcludedImplementation().contains(implementation)) {
                return false;
            }

            if (config.getVersions().size() > 0) {
                int versionRegexIndex = config.getImplementations().indexOf(implementation);
                if (versionRegexIndex > config.getVersions().size() || versionRegexIndex == -1) {
                    versionRegexIndex = config.getVersions().size() - 1;
                }
                String versionRegex = config.getVersions().get(versionRegexIndex).replaceAll("\\.", "\\\\.");
                Pattern p = Pattern.compile(versionRegex);
                Matcher m = p.matcher(version);
                if (!m.find()) {
                    return false;
                }
            }

            if (config.getMode() != ImplementationModeType.BOTH) {
                return role.equals(config.getMode().name().toLowerCase());
            }

            return true;
        }).collect(Collectors.toList());
        
        List<Image> clientImages = images.parallelStream().filter(image -> image.getLabels().get(TlsImageLabels.CONNECTION_ROLE.getLabelName()).equals("client")).collect(Collectors.toList());
        List<Image> serverImages = images.parallelStream().filter(image -> image.getLabels().get(TlsImageLabels.CONNECTION_ROLE.getLabelName()).equals("server")).collect(Collectors.toList());

        Evaluator evaluator = new Evaluator(clientImages, serverImages);
        evaluator.start();
    }
}