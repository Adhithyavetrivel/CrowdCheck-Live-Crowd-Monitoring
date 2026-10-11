package com.crowdcheck.ui;

import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Validates console inputs for robustness, preventing crashes and bad data entries.
 */
public class InputValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[0-9\\s-]{7,20}$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                return 0;
            }
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("  [!] Please enter a number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid number format. Please enter an integer.");
            }
        }
    }

    public static String readString(Scanner scanner, String prompt, boolean allowEmpty) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                return "";
            }
            String input = scanner.nextLine().trim();
            if (!input.isEmpty() || allowEmpty) {
                return input;
            }
            System.out.println("  [!] Input cannot be empty. Please try again.");
        }
    }

    public static String readEmail(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (isValidEmail(input)) {
                return input;
            }
            System.out.println("  [!] Invalid email format (example: user@example.com). Please try again.");
        }
    }

    public static String readPhone(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (isValidPhone(input)) {
                return input;
            }
            System.out.println("  [!] Invalid phone/contact format (example: +1-555-0199). Please try again.");
        }
    }

    public static <T extends Enum<T>> T readEnum(Scanner scanner, String prompt, Class<T> enumClass) {
        T[] constants = enumClass.getEnumConstants();
        System.out.println("  Available options:");
        for (int i = 0; i < constants.length; i++) {
            System.out.printf("    %d. %s%n", (i + 1), constants[i].name());
        }
        while (true) {
            int choice = readInt(scanner, prompt + " [1-" + constants.length + "]: ", 1, constants.length);
            return constants[choice - 1];
        }
    }

    public static boolean readBoolean(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) return true;
            if (input.equals("n") || input.equals("no")) return false;
            System.out.println("  [!] Please enter 'y' or 'n'.");
        }
    }
}
