package org.switchyard.internal;

public enum RegistrationAction {
    REGISTER("register"), UNREGISTER("unregister"),POPULATE("populate");
    private String _action;
    
    RegistrationAction(String action) {
        _action = action;
    }
    
    public String getAction() {
        return _action;
    }
    
    public static RegistrationAction fromString(String action) {
        if (REGISTER.equals(action)) {
            return REGISTER;
        }
        else if (UNREGISTER.equals(action)) {
            return UNREGISTER;
        }
        else if (POPULATE.equals(action)) {
            return POPULATE;
        }
        else {
            throw new IllegalArgumentException("Unrecognized String: " + action);
        }
    }
}
