package com.myq.interview.cleaner;

import com.myq.interview.cleaner.data.Input;
import org.junit.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class RobotDynamicInvalidTest {

    private static final ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    private File getFileResource(String resourceName) { //todo code duplicate
        return new File(classLoader.getResource(resourceName).getFile());
    }

    private Path getResourcePath(String resourceName) {
        try {
            return Paths.get(ClassLoader.getSystemResource(resourceName).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @TempDir
    static File tempFolder;

    static class InvalidRobotTest implements Executable {

        private final File inputFile;
        private final String expectedException;

        InvalidRobotTest(File inputFile, String expectedException) {
            this.inputFile = inputFile;
            this.expectedException = expectedException;
        }

        @Override
        public void execute() throws Throwable {
            try {
                InputOutputParser parser = new InputOutputParser(inputFile, new File("output.json"));
                Input input = parser.getInput();
                Assert.fail(String.format("Parser should have failed; returned %s instead", input));
            } catch (IOException e) {
                if (expectedException == null) {
                    Assert.fail(String.format("The verification file is missing for exception: %s", e.getMessage()));
                }
                Assert.assertTrue(String.format("%s does not start with %s", e.getMessage(), expectedException), e.getMessage().startsWith(expectedException));
            }
        }
    }

    public static final String EXCEPTION_PREF = ".except";

    @TestFactory
    @DisplayName("Robot test")
    Collection<DynamicTest> testRobot() throws IOException {
//        Path inputsFolder = getResourcePath("json/invalid_inputs");
        File inputsFolder = getFileResource("json/invalid_inputs");
        File[] inputs = inputsFolder.listFiles((dir, name) -> name.endsWith(".json") && !name.startsWith("x")); //skip tests starting with x

        List<DynamicTest> tests = new ArrayList<>(inputs.length);
        for (File input : inputs) {
            File exceptionFile = new File(input.getAbsolutePath() + EXCEPTION_PREF);
            String expectedException = exceptionFile.exists() ? Files.readAllLines(exceptionFile.toPath()).get(0) : null;
            tests.add(dynamicTest(input.getName(), new InvalidRobotTest(input, expectedException)));

        }
        return tests;
    }

}
