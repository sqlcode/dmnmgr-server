
package de.lv1871.dms.dmnmgr.domain;

import de.redsix.dmncheck.result.Severity;
import de.redsix.dmncheck.result.ValidationResult;
import de.redsix.dmncheck.validators.core.SimpleValidator;

import java.util.Collections;
import java.util.List;

import org.camunda.bpm.model.dmn.instance.Output;
import org.springframework.util.StringUtils;

public class OutputNameRequiredValidator extends SimpleValidator<Output> {

    @Override
    public boolean isApplicable(Output expression) {
        return true;
    }

    @Override
    public List<ValidationResult> validate(Output expression) {
        final String outputName = expression.getName();
        if(StringUtils.isEmpty(outputName)) {
            return Collections.singletonList(ValidationResult.init
                    .message(getClassUnderValidation().getSimpleName() + " has no name")
                    .severity(Severity.ERROR)
                    .element(expression)
                    .build());
        }
        return Collections.emptyList();
    }

    @Override
    public Class<Output> getClassUnderValidation() {
        return Output.class;
    }
    
}