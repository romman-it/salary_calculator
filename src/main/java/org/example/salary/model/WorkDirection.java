package org.example.salary.model;

public enum WorkDirection {
    CITY("Город"),
    REGION("Регион");

private final String displayName;

WorkDirection(String displayName) {
    this.displayName = displayName;
}

public String getDisplayName() {
    return displayName;
}

@Override
public String toString() {
    return displayName;
}
}