package test;

import main.TCPClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class SimpleProgramTest {
    private final InputStream systemIn = System.in;
    private final PrintStream systemOut = System.out;

    private ByteArrayInputStream testIn;
    private ByteArrayOutputStream testOut;

    @Before
    public void setUpOutput() {
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
    }

    private void provideInput(String data) {
        testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);
    }

    private String getOutput() {
        String out = testOut.toString();
        testOut.reset();
        return out;
    }

    private void startClient() {
        try {
            TCPClient.main(new String[]{"-test"});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testMessage(String input, String expectedOutput) {
        provideInput(input);
        startClient();
        assertEquals(expectedOutput + System.getProperty("line.separator"), getOutput());
    }

    private void endConnection() {
        provideInput("");
        startClient();
    }

    @After
    public void restoreSystemInputOutput() {
        System.setIn(systemIn);
        System.setOut(systemOut);
    }

    @Test
    public void testEmpty() {
        testMessage("", "+hwar042 SFTP Service");
        endConnection();
    }

    @Test
    public void testConnection() {
        testMessage("Hello!", "+hwar042 SFTP Service");
        endConnection();
    }

    @Test
    public void testUnknownCMD() {
        testMessage("", "+hwar042 SFTP Service");
        testMessage("unkn_command", "-Unknown command, try again");
    }

    @Test
    public void testNull() {
        testMessage("", "+hwar042 SFTP Service");
        testMessage("", "-Unknown command, try again");
    }

    @Test
    public void testShort() {
        testMessage("", "+hwar042 SFTP Service");
        testMessage("q", "-Unknown command, try again");
    }

    @Test
    public void testAdmin() {
        testMessage("Hello!", "+hwar042 SFTP Service");
        testMessage("user admin", "!admin logged in");
        endConnection();
    }

    @Test
    public void testUser() {
        testMessage("Hello!", "+hwar042 SFTP Service");
        testMessage("user bob", "+User-id valid, send account and password");
        endConnection();
    }

    @Test
    public void testAcctThenPass() {
        testMessage("Hello!", "+hwar042 SFTP Service");
        testMessage("user bob", "+User-id valid, send account and password");
        testMessage("acct kobe", "+Account valid, send password");
        testMessage("pass bryant", "! Logged in");
        endConnection();
    }

    @Test
    public void testPassThenAcct() {
        testMessage("Hello!", "+hwar042 SFTP Service");
        testMessage("user bob", "+User-id valid, send account and password");
        testMessage("pass bryant", "+Send account");
        testMessage("acct kobe", "! Account valid, logged-in");
        endConnection();
    }

    @Test
    public void testBadUser() {
        testMessage("Hello!", "+hwar042 SFTP Service");
        testMessage("user casper", "-Invalid user-id, try again");
    }

    @Test
    public void testUserBadAcct() {
        testMessage("Hello!", "+hwar042 SFTP Service");
        testMessage("user bob", "+User-id valid, send account and password");
        testMessage("acct shaq", "-Invalid account, try again");
    }

    @Test
    public void testAcctNoUser() {
        testMessage("Hello!", "+hwar042 SFTP Service");
        testMessage("acct kobe", "-Invalid account, try again");
    }

    @Test
    public void testAcctBadPass() {
        testMessage("Hello!", "+hwar042 SFTP Service");
        testMessage("user bob", "+User-id valid, send account and password");
        testMessage("acct kobe", "+Account valid, send password");
        testMessage("pass jordan", "-Wrong password, try again");
    }

    @Test
    public void testPassBadAcct() {
        testMessage("Hello!", "+hwar042 SFTP Service");
        testMessage("user bob", "+User-id valid, send account and password");
        testMessage("pass bryant", "+Send account");
        testMessage("acct michael", "-Invalid account, try again");
    }

    @Test
    public void testLoginUppercase() {
        testMessage("HELLO!", "+hwar042 SFTP Service");
        testMessage("USER BOB", "+User-id valid, send account and password");
        testMessage("ACCT KOBE", "+Account valid, send password");
        testMessage("PASS bryant", "! Logged in");
        endConnection();
    }
}
