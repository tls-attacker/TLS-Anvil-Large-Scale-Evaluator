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

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import de.rub.nds.tlstest.evaluator.constants.DockerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DockerCleanupService {
    protected static final Logger LOGGER = LogManager.getLogger();
    protected static final DockerClient DOCKER = new DefaultDockerClient("unix:///var/run/docker.sock");

    private final List<EntityHolder> entitiesToCleanUp = new ArrayList<>();

    public void addEntityToCleanUp(DockerEntity entity, String id) {
        entitiesToCleanUp.add(new EntityHolder(id, entity));
    }

    public void cleanup() {
        Collections.reverse(entitiesToCleanUp);

        entitiesToCleanUp.forEach(i -> {
            try {
                switch (i.entity) {
                    case IMAGE:
                        DOCKER.removeImage(i.entityId, true, false);
                        break;
                    case NETWORK:
                        DOCKER.removeNetwork(i.entityId);
                        break;
                    case CONTAINER:
                        DOCKER.removeContainer(i.entityId, DockerClient.RemoveContainerParam.forceKill());
                        break;
                }
            } catch (Exception e) {
                LOGGER.error(e);
            }
        });
    }


    static class EntityHolder {
        private final DockerEntity entity;
        private final String entityId;

        EntityHolder(String id, DockerEntity entity) {
            this.entityId = id;
            this.entity = entity;
        }
    }
}
