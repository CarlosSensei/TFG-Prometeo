package com.optiflow.crm.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SpanishDocumentValidator implements ConstraintValidator<ValidSpanishDocument, String> {

    private static final String LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.isBlank()) { return true; }

        String document = value.toUpperCase();

        if (document.matches("\\d{8}[A-Z]")) {
            return validateDni(document);
        }

        if (document.matches("[XYZ]\\d{7}[A-Z]")) {
            return validateNie(document);
        }

        return false;
    }

    private boolean validateDni(String document) {

        int number = Integer.parseInt(document.substring(0, 8));
        char expectedLetter = LETTERS.charAt(number % 23);
        char actualLetter = document.charAt(8);

        return expectedLetter == actualLetter;
    }

    private boolean validateNie(String document) {

        String prefix = switch (document.charAt(0)) {
            case 'X' -> "0";
            case 'Y' -> "1";
            case 'Z' -> "2";
            default -> "";
        };

        int number = Integer.parseInt(prefix + document.substring(1, 8));

        char expectedLetter = LETTERS.charAt(number % 23);
        char actualLetter = document.charAt(8);

        return expectedLetter == actualLetter;
    }
}