package lab01;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UserTest {
    @Test
    public void testChangeEmail() {
        User user = new User();
        assertNull(user.getEmail());
        user.setEmail("john@mail.com");
        assertEquals("john@mail.com", user.getEmail());
    }
}
