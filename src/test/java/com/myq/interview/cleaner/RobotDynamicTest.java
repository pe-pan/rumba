package com.myq.interview.cleaner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class RobotDynamicTest {

    private static final ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    private File getFileResource(String resourceName) { //todo code duplicate
        return new File(classLoader.getResource(resourceName).getFile());
    }

    @TempDir
    static File tempFolder;

    static class RobotTest implements Executable {

        private final File inputFile;
        private final File expectedOutputFile;

        RobotTest(File inputFile, File expectedOutputFile) {
            this.inputFile = inputFile;
            this.expectedOutputFile = expectedOutputFile;
        }

        @Override
        public void execute() throws Throwable {
            File outputFile = Paths.get(tempFolder.getAbsolutePath(), expectedOutputFile.getName()).toFile();
            Main.main(new String[]{inputFile.getAbsolutePath(), outputFile.getAbsolutePath()});
            ObjectMapper mapper = new ObjectMapper();
            JsonNode expected = mapper.readTree(expectedOutputFile);
            JsonNode actual = mapper.readTree(outputFile);
            Assert.assertEquals(expected, actual);
        }
    }

    public static final String RESULT_PREF = "result_";

    @TestFactory
    @DisplayName("Robot test")
    Collection<DynamicTest> testRobot() {
        File inputsFolder = getFileResource("json/inputs");
        File outputsFolder = getFileResource("json/outputs");
        File[] inputs = inputsFolder.listFiles((dir, name) -> name.endsWith(".json") && !name.startsWith("x")); //skip tests starting with x

        List<DynamicTest> tests = new ArrayList<>(inputs.length);
        for (File input : inputs) {
            tests.add(dynamicTest(input.getName(), new RobotTest(input, Paths.get(outputsFolder.getAbsolutePath(), RESULT_PREF + input.getName()).toFile())));
        }
        return tests;
    }

}
