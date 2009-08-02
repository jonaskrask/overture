package org.overturetool.proofsupport.external_tools;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.overturetool.proofsupport.test.TestSettings;
import org.overturetool.proofsupport.external_tools.Console;
import org.overturetool.proofsupport.external_tools.Utilities;

public class ConsoleTest extends TestCase {

	protected static final String INVALID_FILE_NAME = "invalid_file_name";
	private static final String MOSML_DIR = TestSettings.getMosmlDir();
	private static final String HOL_DIR = TestSettings.getHolDir();
	private static final String THIS_IS_A_TEST = "this is a test";

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testWriteReadLine() throws Exception {
		ArrayList<String> command = new ArrayList<String>();
		command.add(TestSettings.getCatProgram());
		Console console = new Console(command);
		console.writeLine(THIS_IS_A_TEST);
		String actual = console.readLine();

		assertEquals(THIS_IS_A_TEST, actual);
	}

	public void testWriteReadLineMosml() throws Exception {
		ArrayList<String> command = new ArrayList<String>();
		command.add(MOSML_DIR + "/bin/mosml");
		Console console = new Console(command);

		console.writeLine("quit();");

		console.waitFor();
	}

//	public void testWriteReadLineHol() throws Exception {
//		ArrayList<String> command = new ArrayList<String>();
//		command.add(HOL_DIR + "/bin/" + TestSettings.getHolBin());
//		Console console = new Console(command);
//
//		String quit = "quit();";
//		console.writeLine(quit);
//
//		Thread.sleep(3000);
//	}

	public void testWriteReadLineUnquote() throws Exception {
		ArrayList<String> command = new ArrayList<String>();
		command.add(HOL_DIR + "/bin/unquote");
		Console console = new Console(command);

		String expected = "(Parse.Term [QUOTE \" (*#loc 1 4*)this is a test\"])";
		console.writeLine("``" + THIS_IS_A_TEST + "``");
		String actual = console.readLine();
		console.destroy();

		assertEquals(expected, actual);
	}

	// TODO make commented test compatible with windows (it doesn't have env command!)
//	public void testEnvironmentVariables() throws Exception {
//		HolEnvironmentBuilder holEnvBuilder = new HolEnvironmentBuilder(
//				MOSML_DIR, "lib", "bin");
//		Map<String, String> holEnv = holEnvBuilder.getEnvironment();
//
//		ArrayList<String> command = new ArrayList<String>();
//		command.add("env");
//		Console console = new Console(command, holEnv);
//		String actual = console.readAllLines();
//
//		assertTrue(actual.contains("MOSMLHOME=" + MOSML_DIR));
//		assertTrue(actual.contains("DYLD_LIBRARY_PATH=" + MOSML_DIR));
//		assertTrue(actual.contains("PATH=" + MOSML_DIR));
//	}

	public void testExitValueHasExited() throws Exception {
		Console console = new Console(TestSettings.getNopProgramCommand());
		Thread.sleep(3000);
		int actual = console.exitValue();

		assertEquals(0, actual);
	}

	public void testExitValueHasNotExited() throws Exception {
		ArrayList<String> command = new ArrayList<String>();
		command.add(TestSettings.getCatProgram());
		Console console = new Console(command);
		try {
			console.exitValue();
		} catch (Exception e) {
			assertEquals(IllegalThreadStateException.class, e.getClass());
		}
	}

	public void testHasTerminated() throws Exception {
		Console console = new Console(TestSettings.getNopProgramCommand());
		Thread.sleep(3000);

		assertTrue(console.hasTerminated());
	}

	public void testHasNotYetTerminated() throws Exception {
		ArrayList<String> command = new ArrayList<String>();
		command.add(TestSettings.getCatProgram());
		Console console = new Console(command);

		assertFalse(console.hasTerminated());
	}

	public void testReadErrorLineSomeError() throws Exception {
		ArrayList<String> command = new ArrayList<String>();
		command.add(TestSettings.getCatProgram());
		command.add(INVALID_FILE_NAME);
		Console console = new Console(command);
		String actual = console.readErrorLine();

		assertTrue(actual.contains(INVALID_FILE_NAME));
	}

	// TODO fix: test method not compatible with windows
//	public void testReadErrorLineNoError() throws Exception {
//		ArrayList<String> command = new ArrayList<String>();
//		command.add("java");
//		command.add("-help");
//		Console console = new Console(command);
//		console.closeInput();
//		// help take a bit more to print all output.
//		Thread.sleep(5000);
//		String actual = console.readErrorLine();
//
//		System.err.println(actual);
//		assertNull(actual);
//	}

	public void testCloseInput() throws Exception {
		ArrayList<String> command = new ArrayList<String>();
		command.add(TestSettings.getCatProgram());
		Console console = new Console(command);
		console.closeInput();

		try {
			console.writeLine();
		} catch (Exception e) {
			assertEquals(IllegalThreadStateException.class, e.getClass());
		}
	}
	
	public void testWriteAndReadLine() throws Exception {
		ArrayList<String> command = new ArrayList<String>();
		command.add(TestSettings.getCatProgram());
		Console console = new Console(command);
		
		String actual = console.writeAndReadLine(THIS_IS_A_TEST);
		
		assertEquals(THIS_IS_A_TEST, actual);
	}
	
	public void testWriteAndReadLines() throws Exception {
		ArrayList<String> command = new ArrayList<String>();
		command.add(TestSettings.getCatProgram());
		Console console = new Console(command);
		
		console.writeLines(new String[]{THIS_IS_A_TEST, THIS_IS_A_TEST});
		console.closeInput();
		String actual = console.readAllLines();
		
		assertEquals(THIS_IS_A_TEST + Utilities.LINE_SEPARATOR + THIS_IS_A_TEST, actual.trim());
	}
	
	
//	public void testHasOutput() throws Exception {
//		Console console = new Console(TestSettings.getNopProgramCommand());
//		console.waitForSomeOutput(2000);
//		
//		assertTrue(console.hasOutput());
//	}
	
	public void testHasNoOutput() throws Exception {
		ArrayList<String> command = new ArrayList<String>();
		command.add(TestSettings.getCatProgram());
		Console console = new Console(command);

		assertFalse(console.hasOutput());
	}
	
	public void testWaitForSomeOuput() throws Exception {
		ArrayList<String> command = new ArrayList<String>();
		command.add("java");
		Console console = new Console(command);
		console.waitForSomeOutput(5000);
		
		assertTrue(console.hasOutput());
	}
	
	public void testBuildCommandList() throws Exception {
		List<String> expected = new ArrayList<String>();
		expected.add(THIS_IS_A_TEST);
		List<String> actual = Console.buildCommandList(THIS_IS_A_TEST);
		
		assertEquals(expected.size(), actual.size());
		for(int i = 0; i < expected.size(); i++)
			assertEquals(expected.get(i), actual.get(i));
	}
	
	public void testPrintCommandList() throws Exception {
		List<String> command = new ArrayList<String>();
		command.add(THIS_IS_A_TEST);
		Console.printCommand(command);
		
		assertTrue(true);
	}
}

