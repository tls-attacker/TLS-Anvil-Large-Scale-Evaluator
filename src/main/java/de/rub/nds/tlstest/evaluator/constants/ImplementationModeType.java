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
