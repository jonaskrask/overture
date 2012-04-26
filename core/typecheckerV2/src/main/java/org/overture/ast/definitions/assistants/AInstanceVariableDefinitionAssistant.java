package org.overture.ast.definitions.assistants;

import java.util.List;
import java.util.Vector;

import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.definitions.AInstanceVariableDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.types.PType;
import org.overture.ast.types.assistants.PTypeAssistant;
import org.overture.typecheck.TypeCheckException;
import org.overture.typecheck.TypeCheckInfo;
import org.overture.typecheck.TypeCheckerErrors;
import org.overturetool.vdmjV2.lex.LexNameList;
import org.overturetool.vdmjV2.lex.LexNameToken;
import org.overturetool.vdmjV2.typechecker.NameScope;

public class AInstanceVariableDefinitionAssistant {

	public static PDefinition findName(AInstanceVariableDefinition d, LexNameToken sought,
			NameScope scope) {
		
		PDefinition found = PDefinitionAssistantTC.findNameBaseCase(d, sought, scope);
		if (found != null) return found;
		return scope.matches(NameScope.OLDSTATE) &&
				d.getOldname().equals(sought) ? d : null;
	}

	public static List<PDefinition> getDefinitions(AInstanceVariableDefinition d) {
		List<PDefinition> res = new Vector<PDefinition>();
		res.add(d);
		return res;
	}
	
	public static LexNameList getVariableNames(AInstanceVariableDefinition d) {
		return new LexNameList(d.getName());
	}

	

	public static void typeResolve(AInstanceVariableDefinition d,
			QuestionAnswerAdaptor<TypeCheckInfo, PType> rootVisitor,
			TypeCheckInfo question) {
		
		try
		{
			d.setType(PTypeAssistant.typeResolve(d.getType(), null, rootVisitor, question));
		}
		catch (TypeCheckException e)
		{
			PTypeAssistant.unResolve(d.getType());
			throw e;
		}
		
	}

	public static void initializedCheck(AInstanceVariableDefinition ivd) {
		if (!ivd.getInitialized() && !PAccessSpecifierAssistantTC.isStatic(ivd.getAccess()))
		{
			TypeCheckerErrors.warning(5001, "Instance variable '" + ivd.getName() + "' is not initialized",ivd.getLocation(),ivd);
		}
		
	}

}
