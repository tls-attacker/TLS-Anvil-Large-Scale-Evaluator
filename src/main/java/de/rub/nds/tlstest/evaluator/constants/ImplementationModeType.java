/**
 * TLS-Testsuite-Large-Scale-Evaluator - A tool for executing the TLS-Testsuite against multiple targets running in Docker containers in parallel
 *
 * Copyright 2020 Ruhr University Bochum and
 * TÜV Informationstechnik GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlstest.evaluator.constants;

import de.rub.nds.tls.subject.ConnectionRole;

public enum ImplementationModeType {
    CLIENT,
    SERVER,
    BOTH;

    public static ImplementationModeType fromString(String mode) {
        if (mode.toLowerCase().equals("client")) {
            return CLIENT;
        } else if (mode.toLowerCase().equals("server")) {
            return SERVER;
        } else if (mode.toLowerCase().equals("both")) {
            return BOTH;
        }
        return null;
    }

    public ConnectionRole getConnectionRole() {
        if (this == CLIENT) {
            return ConnectionRole.CLIENT;
        } else if (this == SERVER) {
            return ConnectionRole.SERVER;
        }

        throw new RuntimeException("Cannot get connection role for both");
    }
}
