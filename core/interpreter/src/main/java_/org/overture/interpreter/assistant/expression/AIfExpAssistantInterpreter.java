package org.overture.interpreter.assistant.expression;

import org.overture.ast.expressions.AElseIfExp;
import org.overture.ast.expressions.AIfExp;
import org.overture.interpreter.runtime.ObjectContext;
import org.overture.interpreter.values.ValueList;
import org.overture.typechecker.assistant.expression.AIfExpAssistantTC;

public class AIfExpAssistantInterpreter extends AIfExpAssistantTC
{

	public static ValueList getValues(AIfExp exp, ObjectContext ctxt)
	{
		ValueList list = PExpAssistantInterpreter.getValues(exp.getTest(),ctxt);
		list.addAll(PExpAssistantInterpreter.getValues(exp.getThen(),ctxt));

		for (AElseIfExp elif: exp.getElseList())
		{
			list.addAll(PExpAssistantInterpreter.getValues(elif,ctxt));
		}

		if (exp.getElse() != null)
		{
			list.addAll(PExpAssistantInterpreter.getValues(exp.getElse(),ctxt));
		}

		return list;
	}

}
