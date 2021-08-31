package test;

import main.TCPClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.Assert.*;

public class SimpleProgramTest {
    private final InputStream systemIn = System.in;
    private final PrintStream systemOut = System.out;

    private ByteArrayInputStream testIn;
    private ByteArrayOutputStream testOut;

    // Taken From: https://stackoverflow.com/a/50721326
    @Before
    public void setUpOutput() {
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
    }

    // Taken From: https://stackoverflow.com/a/50721326
    private void provideInput(String data) {
        testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);
    }

    // Taken From: https://stackoverflow.com/a/50721326
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

    private void login() {
        testMessage("Hello!", "+hwar042 SFTP Service");
        testMessage("user admin", "!admin logged in");
    }

    // Taken From: https://stackoverflow.com/a/50721326
    @After
    public void restoreSystemInputOutput() {
        System.setIn(systemIn);
        System.setOut(systemOut);
    }

    @After
    public void restoreUploadsDownloads() {
        deleteFilesInFolder(new File("tests/uploads"));
        deleteFilesInFolder(new File("tests/downloads"));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void createDirectories() {
        new File("tests/downloads").mkdir();
        new File("tests/testKill").mkdir();
        new File("tests/uploads").mkdir();
    }

    // Taken from:
    // https://stackoverflow.com/a/13195870
    public void deleteFilesInFolder(File dir) {
        for (File file: Objects.requireNonNull(dir.listFiles())) {
            if (!file.isDirectory()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
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

    @Test
    public void testType() {
        login();
        testMessage("type a", "+Using ASCII mode");
        testMessage("type b", "+Using Binary mode");
        testMessage("type c", "+Using Continuous mode");
        testMessage("type d", "-Type not valid");
    }

    @Test
    public void testCdir() {
        login();
        testMessage("cdir database", "!Changed working dir to database");
        testMessage("cdir badFolder", "-Can't connect to directory because: directory does not exist");
    }

    @Test
    public void testList() {
        login();
        testMessage("cdir database", "!Changed working dir to database");
        testMessage("list f",
                "+database" + System.lineSeparator() +
                "accts.txt " + System.lineSeparator() +
                "users.txt ");
        testMessage("list t", "-invalid file listing format");
    }

    @Test
    public void testKill() throws IOException {
        File fileToDelete = new File("tests/testKill/test.test");
        assertTrue(fileToDelete.createNewFile());
        login();
        testMessage("cdir tests/testKill", "!Changed working dir to tests/testKill");
        testMessage("kill test.test", "+test.test deleted");
        testMessage("kill test.test", "-Not deleted because file does not exist");
    }

    @Test
    public void testName() {
        login();
        testMessage("cdir tests/testName", "!Changed working dir to tests/testName");
        testMessage("name test.test", "+File exists");
        testMessage("tobe rename.test", "+test.test was renamed to rename.test");
        testMessage("name rename.test", "+File exists");
        testMessage("tobe test.test", "+rename.test was renamed to test.test");
        testMessage("name test.test", "+File exists");
        testMessage("tobe test.test", "-File wasn't renamed because file with dest path already exists");
        login();
        testMessage("name random.test", "-Can't find random.test");
    }

    @Test
    public void testDone() {
        login();
        testMessage("done", "+closing connection");
    }

    @Test
    public void testRetrText() throws IOException {
        login();
        testMessage("cdir tests/testRetr", "!Changed working dir to tests/testRetr");
        testMessage("retr test.txt","41");
        testMessage("send", "+File received");
        endConnection();
        byte[] originalFile = Files.readAllBytes(Paths.get("tests/testRetr/test.txt"));
        byte[] newFile = Files.readAllBytes(Paths.get("tests/downloads/test.txt"));
        assertArrayEquals(originalFile, newFile);
    }

    @Test
    public void testRetrJPEG() throws IOException {
        login();
        testMessage("cdir tests/testRetr", "!Changed working dir to tests/testRetr");
        testMessage("retr test.jpg","64535");
        testMessage("send", "+File received");
        byte[] originalFile = Files.readAllBytes(Paths.get("tests/testRetr/test.jpg"));
        byte[] newFile = Files.readAllBytes(Paths.get("tests/downloads/test.jpg"));
        assertArrayEquals(originalFile, newFile);
        endConnection();
    }

    @Test
    public void testStorText() throws IOException {
        login();
        testMessage("cdir tests/uploads", "!Changed working dir to tests/uploads");
        testMessage("stor new test.txt", "+File does not exist, will create new file");
        testMessage("size 41", "+ok, waiting for file");
        testMessage("tests/testStor/test.txt","Enter Absolute Filepath of File to Send" + System.lineSeparator() + "+Saved test.txt");
        endConnection();
        byte[] originalFile = Files.readAllBytes(Paths.get("tests/testStor/test.txt"));
        byte[] newFile = Files.readAllBytes(Paths.get("tests/uploads/test.txt"));
        assertArrayEquals(originalFile, newFile);
    }

    @Test
    public void testStorJPG() throws IOException {
        login();
        testMessage("cdir tests/downloads", "!Changed working dir to tests/downloads");
        testMessage("stor new test.jpg", "+File does not exist, will create new file");
        testMessage("size 64535", "+ok, waiting for file");
        testMessage("tests/testStor/test.jpg","Enter Absolute Filepath of File to Send" + System.lineSeparator() + "+Saved test.jpg");
        endConnection();
        byte[] originalFile = Files.readAllBytes(Paths.get("tests/testStor/test.jpg"));
        byte[] newFile = Files.readAllBytes(Paths.get("tests/downloads/test.jpg"));
        assertArrayEquals(originalFile, newFile);
    }
}
