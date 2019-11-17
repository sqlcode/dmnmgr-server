package de.lv1871.dms.dmnmgr.test.driver;

import static de.lv1871.dms.tester.test.function.LambdaExtension.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngine;
import org.junit.Assert;

import com.fasterxml.jackson.databind.node.ObjectNode;

import de.lv1871.dms.dmnmgr.test.driver.model.DmnTest;
import junit.framework.TestCase;
import static de.lv1871.dms.tester.test.dmnassert.DmnAssert.assertEqual;
import de.lv1871.dms.tester.test.dmnassert.model.DecisionSimulationResponse;

public class DmnTestExecutor extends TestCase {

	// @formatter:off
	private static String ASSERTION_ERROR_MESSAGE_TEMPLATE = 
			"\n--------------------------------------------\n" +
			"Expected Data:\n" +
			"%s\n\n" +
			"--------------------------------------------\n" +
			"Result:\n" +
			"%s";
	// @formatter:on

	private static VariableMapperService MAPPER = new VariableMapperService();

	private final DmnTest test;
	private final ProcessEngine engine;

	public DmnTestExecutor(DmnTest test, ProcessEngine engine) {
		super(test.getName());
		this.test = test;
		this.engine = engine;
	}

	protected void runTest() throws Throwable {
		testDecision();
	}

	public void testDecision() {
		DecisionSimulationResponse decisionSimulationResponse = DecisionRunner.decide(engine, this.test.getTableId(),
				this.test.getData());

		if (decisionSimulationResponse.getResult() == null) {
			fail(decisionSimulationResponse.getMessage());
		}

		try {

			List<Entry<String, Object>> expectedDataAssertionFailed = new ArrayList<>();

			for (ObjectNode expectedNode : this.test.getExpectedData()) {
				expectedDataAssertionFailed.addAll(testExpectation(decisionSimulationResponse, expectedNode));
			}

			Boolean testFailed = expectedDataAssertionFailed.size() > 0;

			String resultMessage = testFailed ? getFailureMessage(decisionSimulationResponse) : "";

			Assert.assertFalse(resultMessage, testFailed);

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	private String getFailureMessage(DecisionSimulationResponse decisionSimulationResponse) {
		String expectedData = MAPPER.writeJson(this.test.getExpectedData());
		String result = MAPPER.writeJson(decisionSimulationResponse.getResult());

		return decisionSimulationResponse.getMessage() == null
				? String.format(ASSERTION_ERROR_MESSAGE_TEMPLATE, expectedData, result)
				: decisionSimulationResponse.getMessage();
	}

	private List<Entry<String, Object>> testExpectation(DecisionSimulationResponse decisionSimulationResponse,
			ObjectNode expectedNode) {
		List<Entry<String, Object>> expectedDataAssertionFailed = new ArrayList<>();

		// @formatter:off
		
		Map<String, Object> expectedMapRaw = MAPPER.getVariablesFromJsonAsMap(expectedNode);

		Map<String, Object> expectedMap = expectedMapRaw
				.entrySet()
				.stream()
				.filter(notNull(Entry::getValue))
				.collect(Collectors.toMap(Entry::getKey,  Entry::getValue));
		// @formatter:on

		for (Entry<String, Object> expectedEntry : expectedMap.entrySet()) {

			boolean isEqual = assertEqual(decisionSimulationResponse, expectedEntry);

			if (!isEqual) {
				expectedDataAssertionFailed.add(expectedEntry);
			}
		}
		return expectedDataAssertionFailed;
	}

}
