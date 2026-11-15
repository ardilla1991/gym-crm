package com.epam.training.gym.model.validation;

/**
 * Maximum lengths and formats of text fields. The same numbers are used by the validation (@Size)
 * and by the database columns (@Column(length = ...)), so a valid value always fits its column.
 */
public final class FieldLimits {

    /**
     * First or last name. The username is FirstName.LastName plus a serial number: at most 50 + 1 + 50 characters
     * and a few digits, so it always fits the default 255-character username column without its own check.
     */
    public static final int NAME_MAX = 50;

    /**
     * Latin letters, spaces, hyphens and apostrophes: the name becomes the username, which must be plain ASCII
     * to work in HTTP Basic from any client (browsers encode Basic credentials only as Latin-1) and in URL paths.
     * See PersonName for why other characters are not allowed.
     */
    public static final String NAME_PATTERN = "[A-Za-z '\\-]*";

    public static final int ADDRESS_MAX = 255;

    public static final int TRAINING_NAME_MAX = 100;

    private FieldLimits() {
    }
}
