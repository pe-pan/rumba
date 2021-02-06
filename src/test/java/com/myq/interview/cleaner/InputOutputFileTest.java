package com.myq.interview.cleaner;

import org.junit.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class InputOutputFileTest {

    private static final PrintStream standardOut = System.out;
    private static final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private static final ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    @BeforeAll
    public static void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterAll
    public static void tearDown() {
        System.setOut(standardOut);
    }

    private InputOutputParser testTemplate(String[] inputs, String expectedResult) throws IOException {
        final String utf8 = StandardCharsets.UTF_8.name();
        InputOutputParser parser;
        try (PrintStream ps = new PrintStream(outputStreamCaptor, true, utf8)) {
            parser = InputOutputParser.parseArguments(inputs);
        }
        String data = outputStreamCaptor.toString(utf8);
        outputStreamCaptor.reset();     // do not read again what has been read so far
        Assert.assertTrue(String.format("Expected that the string (1) contains the string (2):\n<---1--->\n%s<---2--->\n%s", data, expectedResult), data.contains(expectedResult));
        return parser;
    }

    private File getFileResource(String resourceName) { //todo code duplicate
        return new File(classLoader.getResource(resourceName).getFile());
    }

    @Test
    void testNoInput() throws IOException {
        Assert.assertNull(testTemplate(new String[]{}, "Provide exactly 2 parameters"));
    }

    @Test
    void testOneInput() throws IOException {
        Assert.assertNull(testTemplate(new String[]{"input.json"}, "Provide exactly 2 parameters"));
    }

    @Test
    void testThreeInputs() throws IOException {
        Assert.assertNull(testTemplate(new String[]{"input.json", "output.json", "one-more.json"}, "Provide exactly 2 parameters"));
    }

    @Test
    void testNotExistingInput() throws IOException {
        Assert.assertNull(testTemplate(new String[]{"non-existing-input.json", "non-existing-output.json"}, "does not exist; please, provide a valid file path."));
    }

    @Test
    void testInvalidInput() throws IOException {
        File inputFile = getFileResource("json"); // folder
        Assert.assertNull(testTemplate(new String[]{inputFile.getAbsolutePath(), "output.json"}, "is not a valid file"));
    }

    @Test
    void testInvalidOutput() throws IOException {
        File inputFile = getFileResource("json/invalid_inputs/empty.json");
        File outputFile = getFileResource("json"); // folder
        Assert.assertNull(testTemplate(new String[]{inputFile.getAbsolutePath(), outputFile.getAbsolutePath()}, "already exists and can't be overwritten"));
    }

    @Test
    void testValidOutput() throws IOException {
        File inputFile = getFileResource("json/invalid_inputs/empty.json");
        Assert.assertNotNull(testTemplate(new String[]{inputFile.getAbsolutePath(), "output.json"}, "Input file:"));
    }


}
