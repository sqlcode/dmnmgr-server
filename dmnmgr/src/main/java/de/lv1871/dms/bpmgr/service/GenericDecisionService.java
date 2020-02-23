package de.lv1871.dms.bpmgr.service;

import java.util.HashMap;
import java.util.List;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;

import de.lv1871.dms.bpmgr.api.model.DecisionSimulationRequest;
import de.lv1871.dms.tester.test.dmnassert.model.DecisionSimulationResponse;
import de.lv1871.dms.tester.test.dmnassert.model.DecisionSimulationResponse.DecisionTestCaseResponseBuilder;
import de.lv1871.dms.tester.test.domain.DecisionEngine;

@Service
public class GenericDecisionService {

	@Autowired
	private VariableMapperService mapperService;

	public DecisionSimulationResponse decide(DecisionSimulationRequest decisionRequest) {
		DecisionEngine engine = deployAndCreateEngine(decisionRequest.getXml());
		return decide(engine, decisionRequest.getDmnTableId(), decisionRequest.getVariables());
	}

	public DecisionSimulationResponse decide(DecisionEngine engine, String dmnTableId, ObjectNode variablesNode) {

		try {
			HashMap<String, Object> variables = mapperService.getVariablesFromJsonAsMap(variablesNode);
			DmnDecisionTableResult decisionResult = engine.evaluateDecisionByKey(dmnTableId, variables);

			if (decisionResult.getResultList().stream().filter(result -> result.get(null) != null).count() > 0) {
				return DecisionTestCaseResponseBuilder.create()
						.withMessage("Ein oder meherere Output-Felder haben keinen Namen.").build();
			}

			List<String> resultRules = engine.getResulRules(dmnTableId);

			return DecisionTestCaseResponseBuilder.create()
					.withResultRuleIds(resultRules)
					.withResult(decisionResult.getResultList()).build();
		} catch (Exception exception) {
			exception.printStackTrace();
			if (exception.getCause() != null) {
				return DecisionTestCaseResponseBuilder.create().withMessage(exception.getCause().getMessage()).build();
			}
			return DecisionTestCaseResponseBuilder.create().withMessage(exception.getMessage()).build();
		}
	}

	public DecisionEngine deployAndCreateEngine(String dmnXml) {
		DecisionEngine engine = DecisionEngine.createEngine();
		engine.parseDecision(dmnXml);
		return engine;
	}

}
