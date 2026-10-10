package ca.gbc.comp3074.p12.restaurantguide.data

import org.junit.Assert.*
import org.junit.Test

class RestaurantValidationTest {
    @Test fun acceptsOptionalAndMultipleNumbersButRejectsMalformedEntries() {
        assertTrue(validPhoneNumbers(""))
        assertTrue(validPhoneNumbers("416-201-8280\n+1 (437) 216-6130"))
        assertTrue(validPhoneNumbers("416-201-8280; 437-216-6130"))
        assertFalse(validPhoneNumbers("call me"))
        assertFalse(validPhoneNumbers("416-201-8280\n123"))
        assertFalse(validPhoneNumbers("+1234567890123456"))
    }

    @Test fun formatsCanadianPostalCodesWithoutChangingOtherAddresses() {
        assertEquals("2324 Lake Shore Blvd W, Etobicoke, ON M8V 1B6", normalizeAddress(" 2324 Lake Shore Blvd W, Etobicoke, ON m8v1b6 "))
        assertEquals("850 Wilson Ave, North York, ON M3K 1E5", normalizeAddress("850 Wilson Ave, North York, ON M3K 1E5"))
        assertEquals("8 Main St, Boston, MA 02108", normalizeAddress("8 Main St, Boston, MA 02108"))
    }
}
