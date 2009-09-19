package demo.org.powermock.examples;

import static org.junit.Assert.assertNull;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.mockpolicies.Slf4jMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

/**
 * Unit tests that assert that the {@link Slf4jMockPolicy} works.
 */
@RunWith(PowerMockRunner.class)
@MockPolicy(Slf4jMockPolicy.class)
public class Slf4jUserTest {

    @Test
    public void assertSlf4jMockPolicyWorks() throws Exception {
        final Slf4jUser tested = new Slf4jUser();

        replayAll();

        tested.getMessage();

        /*
         * Should return null since getClass() is mocked as a nice mock (because
         * the logger is mocked)
         */
        assertNull(Whitebox.getInternalState(Slf4jUser.class, Logger.class).getClass());
        verifyAll();
    }
}
