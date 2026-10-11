package com.crowdcheck;

import com.crowdcheck.enums.UserRole;
import com.crowdcheck.service.AuthenticationService;
import com.crowdcheck.ui.InputValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for InputValidator and Authentication hashing logic.
 */
public class InputValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "admin.support@campus.edu",
            "first_last@sub.domain.org"
    })
    @DisplayName("Valid email formats pass validation")
    void testValidEmails(String email) {
        assertTrue(InputValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid-email",
            "@missinguser.com",
            "missingdomain@",
            "",
            "   "
    })
    @DisplayName("Invalid email formats fail validation")
    void testInvalidEmails(String email) {
        assertFalse(InputValidator.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "+1-555-0101",
            "9876543210",
            "+91 98765 43210"
    })
    @DisplayName("Valid phone formats pass validation")
    void testValidPhones(String phone) {
        assertTrue(InputValidator.isValidPhone(phone));
    }

    @Test
    @DisplayName("Deterministic SHA-256 password hashing")
    void testPasswordHashing() {
        String hash1 = AuthenticationService.hashPassword("Admin@123");
        String hash2 = AuthenticationService.hashPassword("Admin@123");
        assertNotNull(hash1);
        assertEquals(hash1, hash2);
        assertEquals("e86f78a8a3caf0b60d8e74e5942aa6d86dc150cd3c03338aef25b7d2d7e3acc7", hash1);
    }

    @Test
    @DisplayName("Role RBAC permission verification")
    void testRolePermissions() {
        // ADMIN can do everything
        assertTrue(UserRole.ADMIN.canManageLocations());
        assertTrue(UserRole.ADMIN.canUpdateCrowd());
        assertTrue(UserRole.ADMIN.canManageAlerts());
        assertTrue(UserRole.ADMIN.canManageUsers());

        // ORGANIZER can manage facilities and update crowd, but not users
        assertTrue(UserRole.ORGANIZER.canManageLocations());
        assertTrue(UserRole.ORGANIZER.canUpdateCrowd());
        assertFalse(UserRole.ORGANIZER.canManageUsers());

        // STAFF can update crowd and manage alerts, but not locations or users
        assertFalse(UserRole.STAFF.canManageLocations());
        assertTrue(UserRole.STAFF.canUpdateCrowd());
        assertTrue(UserRole.STAFF.canManageAlerts());
        assertFalse(UserRole.STAFF.canManageUsers());

        // VISITOR is strictly read-only
        assertFalse(UserRole.VISITOR.canManageLocations());
        assertFalse(UserRole.VISITOR.canUpdateCrowd());
        assertFalse(UserRole.VISITOR.canManageAlerts());
        assertFalse(UserRole.VISITOR.canManageUsers());
    }
}
