/*
 * #%~
 * VDM Code Generator
 * %%
 * Copyright (C) 2008 - 2014 Overture
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #~%
 */
package org.overture.codegen.vdm2java;

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.overture.ast.types.PType;
import org.overture.codegen.cgast.INode;
import org.overture.codegen.cgast.SExpCG;
import org.overture.codegen.cgast.SStateDesignatorCG;
import org.overture.codegen.cgast.SStmCG;
import org.overture.codegen.cgast.STypeCG;
import org.overture.codegen.cgast.analysis.AnalysisException;
import org.overture.codegen.cgast.declarations.AClassDeclCG;
import org.overture.codegen.cgast.declarations.AFormalParamLocalParamCG;
import org.overture.codegen.cgast.declarations.AInterfaceDeclCG;
import org.overture.codegen.cgast.declarations.AVarLocalDeclCG;
import org.overture.codegen.cgast.expressions.AApplyExpCG;
import org.overture.codegen.cgast.expressions.ABoolLiteralExpCG;
import org.overture.codegen.cgast.expressions.ACastUnaryExpCG;
import org.overture.codegen.cgast.expressions.AEnumMapExpCG;
import org.overture.codegen.cgast.expressions.AEqualsBinaryExpCG;
import org.overture.codegen.cgast.expressions.AHeadUnaryExpCG;
import org.overture.codegen.cgast.expressions.AIsolationUnaryExpCG;
import org.overture.codegen.cgast.expressions.AMapletExpCG;
import org.overture.codegen.cgast.expressions.ANewExpCG;
import org.overture.codegen.cgast.expressions.ANotEqualsBinaryExpCG;
import org.overture.codegen.cgast.expressions.ANotUnaryExpCG;
import org.overture.codegen.cgast.expressions.AQuoteLiteralExpCG;
import org.overture.codegen.cgast.expressions.AStringLiteralExpCG;
import org.overture.codegen.cgast.expressions.SBinaryExpCG;
import org.overture.codegen.cgast.expressions.SLiteralExpCG;
import org.overture.codegen.cgast.expressions.SNumericBinaryExpCG;
import org.overture.codegen.cgast.expressions.SUnaryExpCG;
import org.overture.codegen.cgast.expressions.SVarExpCG;
import org.overture.codegen.cgast.name.ATypeNameCG;
import org.overture.codegen.cgast.statements.AApplyObjectDesignatorCG;
import org.overture.codegen.cgast.statements.AAssignmentStmCG;
import org.overture.codegen.cgast.statements.AForLoopStmCG;
import org.overture.codegen.cgast.statements.AMapSeqStateDesignatorCG;
import org.overture.codegen.cgast.types.ABoolBasicTypeCG;
import org.overture.codegen.cgast.types.ACharBasicTypeCG;
import org.overture.codegen.cgast.types.AIntBasicTypeWrappersTypeCG;
import org.overture.codegen.cgast.types.AIntNumericBasicTypeCG;
import org.overture.codegen.cgast.types.AInterfaceTypeCG;
import org.overture.codegen.cgast.types.AMethodTypeCG;
import org.overture.codegen.cgast.types.AObjectTypeCG;
import org.overture.codegen.cgast.types.ARealBasicTypeWrappersTypeCG;
import org.overture.codegen.cgast.types.ARealNumericBasicTypeCG;
import org.overture.codegen.cgast.types.ARecordTypeCG;
import org.overture.codegen.cgast.types.AStringTypeCG;
import org.overture.codegen.cgast.types.ATokenBasicTypeCG;
import org.overture.codegen.cgast.types.ATupleTypeCG;
import org.overture.codegen.cgast.types.AUnionTypeCG;
import org.overture.codegen.cgast.types.AVoidTypeCG;
import org.overture.codegen.cgast.types.SBasicTypeCG;
import org.overture.codegen.cgast.types.SMapTypeCG;
import org.overture.codegen.cgast.types.SSeqTypeCG;
import org.overture.codegen.cgast.types.SSetTypeCG;
import org.overture.codegen.ir.IRAnalysis;
import org.overture.codegen.ir.IRInfo;
import org.overture.codegen.ir.SourceNode;
import org.overture.codegen.merging.MergeVisitor;
import org.overture.codegen.merging.TemplateCallable;
import org.overture.codegen.trans.TempVarPrefixes;
import org.overture.codegen.trans.funcvalues.FunctionValueAssistant;
import org.overture.codegen.utils.GeneralUtils;
import org.overture.typechecker.assistant.type.PTypeAssistantTC;

public class JavaFormat
{
	public static final String UTILS_FILE = "Utils";
	public static final String SEQ_UTIL_FILE = "SeqUtil";
	public static final String SET_UTIL_FILE = "SetUtil";
	public static final String MAP_UTIL_FILE = "MapUtil";

	public static final String JAVA_PUBLIC = "public";
	public static final String JAVA_INT = "int";

	private List<AClassDeclCG> classes;

	private IRInfo info;

	private FunctionValueAssistant functionValueAssistant;
	private MergeVisitor mergeVisitor;
	private JavaValueSemantics valueSemantics;

	private JavaRecordCreator recordCreator;

	public JavaFormat(TempVarPrefixes varPrefixes, IRInfo info)
	{
		this.valueSemantics = new JavaValueSemantics(this);
		this.recordCreator = new JavaRecordCreator(this);
		TemplateCallable[] templateCallables = TemplateCallableManager.constructTemplateCallables(this, IRAnalysis.class, varPrefixes, valueSemantics, recordCreator);
		this.mergeVisitor = new MergeVisitor(JavaCodeGen.JAVA_TEMPLATE_STRUCTURE, templateCallables);
		this.functionValueAssistant = null;
		this.info = info;
	}

	public String getJavaNumber()
	{
		return "Number";
	}

	public IRInfo getIrInfo()
	{
		return info;
	}

	public void setFunctionValueAssistant(
			FunctionValueAssistant functionValueAssistant)
	{
		this.functionValueAssistant = functionValueAssistant;
	}

	public void clearFunctionValueAssistant()
	{
		this.functionValueAssistant = null;
	}

	public List<AClassDeclCG> getClasses()
	{
		return classes;
	}

	public void setJavaSettings(JavaSettings javaSettings)
	{
		valueSemantics.setJavaSettings(javaSettings);
	}

	public void init()
	{
		mergeVisitor.dropMergeErrors();
	}

	public void setClasses(List<AClassDeclCG> classes)
	{
		this.classes = classes != null ? classes
				: new LinkedList<AClassDeclCG>();
	}

	public void clearClasses()
	{
		if (classes != null)
		{
			classes.clear();
		} else
		{
			classes = new LinkedList<AClassDeclCG>();
		}
	}

	public MergeVisitor getMergeVisitor()
	{
		return mergeVisitor;
	}

	public String format(AMethodTypeCG methodType) throws AnalysisException
	{
		final String OBJ = "Object";

		if (functionValueAssistant == null)
		{
			return OBJ;
		}

		AInterfaceDeclCG methodTypeInterface = functionValueAssistant.findInterface(methodType);

		if (methodTypeInterface == null)
		{
			return OBJ; // Should not happen
		}

		AInterfaceTypeCG methodClass = new AInterfaceTypeCG();
		methodClass.setName(methodTypeInterface.getName());

		LinkedList<STypeCG> params = methodType.getParams();

		for (STypeCG param : params)
		{
			methodClass.getTypes().add(param.clone());
		}

		methodClass.getTypes().add(methodType.getResult().clone());

		return methodClass != null ? format(methodClass) : OBJ;
	}

	public String format(INode node) throws AnalysisException
	{
		return format(node, false);
	}

	public String formatIgnoreContext(INode node) throws AnalysisException
	{
		return format(node, true);
	}

	private String format(INode node, boolean ignoreContext)
			throws AnalysisException
	{
		StringWriter writer = new StringWriter();
		node.apply(mergeVisitor, writer);

		return writer.toString() + getNumberDereference(node, ignoreContext);
	}

	private String findNumberDereferenceCall(STypeCG type)
	{
		if (type == null)
		{
			return "";
		}

		final String DOUBLE_VALUE = ".doubleValue()";
		final String LONG_VALUE = ".longValue()";

		if (type instanceof ARealNumericBasicTypeCG
				|| type instanceof ARealBasicTypeWrappersTypeCG)
		{
			return DOUBLE_VALUE;
		} else if (type instanceof AIntNumericBasicTypeCG
				|| type instanceof AIntBasicTypeWrappersTypeCG)
		{
			return LONG_VALUE;
		} else
		{
			PTypeAssistantTC typeAssistant = info.getTcFactory().createPTypeAssistant();
			SourceNode sourceNode = type.getSourceNode();

			if (sourceNode != null
					&& !(sourceNode.getVdmNode() instanceof PType))
			{
				PType vdmType = (PType) sourceNode.getVdmNode();

				if (typeAssistant.isNumeric(vdmType))
				{
					return DOUBLE_VALUE;
				}
			}

			return "";
		}
	}

	public static boolean isMapSeq(SStateDesignatorCG stateDesignator)
	{
		return stateDesignator instanceof AMapSeqStateDesignatorCG;
	}

	public String formatMapSeqStateDesignator(AMapSeqStateDesignatorCG mapSeq)
			throws AnalysisException
	{
		INode parent = mapSeq.parent();

		SStateDesignatorCG stateDesignator = mapSeq.getMapseq();
		SExpCG domValue = mapSeq.getExp();

		String stateDesignatorStr = format(stateDesignator);
		String domValStr = format(domValue);

		if (parent instanceof AAssignmentStmCG)
		{
			AAssignmentStmCG assignment = (AAssignmentStmCG) parent;
			SExpCG rngValue = assignment.getExp();
			String rngValStr = format(rngValue);

			// e.g. counters.put("c1", 4);
			return stateDesignatorStr + ".put(" + domValStr + ", " + rngValStr
					+ ")";
		} else
		{
			STypeCG type = mapSeq.getType();
			String typeStr = format(type);

			// e.g. ((Rec) m(true)).field := 2;
			return "( (" + typeStr + ")" + format(mapSeq.getMapseq()) + ".get("
					+ domValStr + "))";
		}
	}

	private String getNumberDereference(INode node, boolean ignoreContext)
	{
		if (ignoreContext && node instanceof SExpCG)
		{
			SExpCG exp = (SExpCG) node;
			STypeCG type = exp.getType();

			if (isNumberDereferenceCandidate(exp))
			{
				return findNumberDereferenceCall(type);
			}
		}

		INode parent = node.parent();

		if (parent instanceof SNumericBinaryExpCG
				|| parent instanceof AEqualsBinaryExpCG
				|| parent instanceof ANotEqualsBinaryExpCG)
		{
			SExpCG exp = (SExpCG) node;
			STypeCG type = exp.getType();

			if (isNumberDereferenceCandidate(exp))
			{
				return findNumberDereferenceCall(type);
			}
		}

		// No dereference is needed
		return "";
	}

	private static boolean isNumberDereferenceCandidate(SExpCG node)
	{
		boolean fitsCategory = !(node instanceof SNumericBinaryExpCG)
				&& !(node instanceof SLiteralExpCG)
				&& !(node instanceof AIsolationUnaryExpCG)
				&& !(node instanceof SUnaryExpCG);

		boolean isException = node instanceof AHeadUnaryExpCG
				|| node instanceof AQuoteLiteralExpCG
				|| node instanceof ACastUnaryExpCG;

		return fitsCategory || isException;
	}

	public String formatName(INode node) throws AnalysisException
	{
		if (node instanceof ANewExpCG)
		{
			ANewExpCG newExp = (ANewExpCG) node;

			return formatTypeName(node, newExp.getName());
		} else if (node instanceof ARecordTypeCG)
		{
			ARecordTypeCG record = (ARecordTypeCG) node;
			ATypeNameCG typeName = record.getName();

			return formatTypeName(node, typeName);
		}

		throw new AnalysisException("Unexpected node in formatName: "
				+ node.getClass().getName());
	}

	public String formatTypeName(INode node, ATypeNameCG typeName)
	{
		AClassDeclCG classDef = node.getAncestor(AClassDeclCG.class);

		String definingClass = typeName.getDefiningClass() != null
				&& classDef != null
				&& !classDef.getName().equals(typeName.getDefiningClass()) ? typeName.getDefiningClass()
				+ "."
				: "";

		String name = typeName.getName();

		return definingClass + name;
	}

	public String format(SExpCG exp, boolean leftChild)
			throws AnalysisException
	{
		String formattedExp = format(exp);

		JavaPrecedence precedence = new JavaPrecedence();

		INode parent = exp.parent();

		if (!(parent instanceof SExpCG))
		{
			return formattedExp;
		}

		boolean isolate = precedence.mustIsolate((SExpCG) parent, exp, leftChild);

		return isolate ? "(" + formattedExp + ")" : formattedExp;
	}

	public String formatUnary(SExpCG exp) throws AnalysisException
	{
		return format(exp, false);
	}

	public String formatNotUnary(SExpCG exp) throws AnalysisException
	{
		String formattedExp = format(exp, false);

		boolean doNotWrap = exp instanceof ABoolLiteralExpCG
				|| formattedExp.startsWith("(") && formattedExp.endsWith(")");

		return doNotWrap ? "!" + formattedExp : "!(" + formattedExp + ")";
	}

	public String formatTemplateTypes(List<STypeCG> types)
			throws AnalysisException
	{
		if (types.isEmpty())
		{
			return "";
		}

		return "<" + formattedTypes(types, "") + ">";
	}

	private String formattedTypes(List<STypeCG> types, String typePostFix)
			throws AnalysisException
	{
		STypeCG firstType = types.get(0);

		if (info.getAssistantManager().getTypeAssistant().isBasicType(firstType))
		{
			firstType = info.getAssistantManager().getTypeAssistant().getWrapperType((SBasicTypeCG) firstType);
		}

		StringWriter writer = new StringWriter();
		writer.append(format(firstType) + typePostFix);

		for (int i = 1; i < types.size(); i++)
		{
			STypeCG currentType = types.get(i);

			if (info.getAssistantManager().getTypeAssistant().isBasicType(currentType))
			{
				currentType = info.getAssistantManager().getTypeAssistant().getWrapperType((SBasicTypeCG) currentType);
			}

			writer.append(", " + format(currentType) + typePostFix);
		}

		String result = writer.toString();
		return result;
	}

	public String formatTypeArgs(List<STypeCG> types) throws AnalysisException
	{
		if (types.isEmpty())
		{
			return "";
		}

		return formattedTypes(types, ".class");
	}

	public String formatEqualsBinaryExp(AEqualsBinaryExpCG node)
			throws AnalysisException
	{
		STypeCG leftNodeType = node.getLeft().getType();

		if (isTupleOrRecord(leftNodeType)
				|| leftNodeType instanceof AStringTypeCG
				|| leftNodeType instanceof ATokenBasicTypeCG
				|| leftNodeType instanceof AUnionTypeCG
				|| leftNodeType instanceof AObjectTypeCG)
		{
			return handleEquals(node);
		} else if (leftNodeType instanceof SSeqTypeCG)
		{
			return handleSeqComparison(node);
		} else if (leftNodeType instanceof SSetTypeCG)
		{
			return handleSetComparison(node);
		} else if (leftNodeType instanceof SMapTypeCG)
		{
			return handleMapComparison(node);
		}

		return format(node.getLeft()) + " == " + format(node.getRight());
	}

	public String formatNotEqualsBinaryExp(ANotEqualsBinaryExpCG node)
			throws AnalysisException
	{
		STypeCG leftNodeType = node.getLeft().getType();

		if (isTupleOrRecord(leftNodeType)
				|| leftNodeType instanceof AStringTypeCG
				|| leftNodeType instanceof ATokenBasicTypeCG
				|| leftNodeType instanceof SSeqTypeCG
				|| leftNodeType instanceof SSetTypeCG
				|| leftNodeType instanceof SMapTypeCG)
		{
			ANotUnaryExpCG transformed = transNotEquals(node);
			return formatNotUnary(transformed.getExp());
		}

		return format(node.getLeft()) + " != " + format(node.getRight());
	}

	private static boolean isTupleOrRecord(STypeCG type)
	{
		return type instanceof ARecordTypeCG || type instanceof ATupleTypeCG;
	}

	private ANotUnaryExpCG transNotEquals(ANotEqualsBinaryExpCG notEqual)
	{
		ANotUnaryExpCG notUnary = new ANotUnaryExpCG();
		notUnary.setType(new ABoolBasicTypeCG());

		AEqualsBinaryExpCG equal = new AEqualsBinaryExpCG();
		equal.setType(new ABoolBasicTypeCG());
		equal.setLeft(notEqual.getLeft().clone());
		equal.setRight(notEqual.getRight().clone());

		notUnary.setExp(equal);

		// Replace the "notEqual" expression with the transformed expression
		INode parent = notEqual.parent();

		// It may be the case that the parent is null if we execute e.g. [1] <> [1] in isolation
		if (parent != null)
		{
			parent.replaceChild(notEqual, notUnary);
			notEqual.parent(null);
		}

		return notUnary;
	}

	private String handleEquals(AEqualsBinaryExpCG valueType)
			throws AnalysisException
	{
		return format(valueType.getLeft()) + ".equals("
				+ format(valueType.getRight()) + ")";
	}

	private String handleSetComparison(AEqualsBinaryExpCG node)
			throws AnalysisException
	{
		return handleCollectionComparison(node, SET_UTIL_FILE);
	}

	private String handleSeqComparison(SBinaryExpCG node)
			throws AnalysisException
	{
		return handleCollectionComparison(node, SEQ_UTIL_FILE);
	}

	private String handleMapComparison(SBinaryExpCG node)
			throws AnalysisException
	{
		return handleCollectionComparison(node, MAP_UTIL_FILE);
	}

	private String handleCollectionComparison(SBinaryExpCG node,
			String className) throws AnalysisException
	{
		// In VDM the types of the equals are compatible when the AST passes the type check
		SExpCG leftNode = node.getLeft();
		SExpCG rightNode = node.getRight();

		final String EMPTY = ".isEmpty()";

		if (isEmptyCollection(leftNode.getType()))
		{
			return format(node.getRight()) + EMPTY;
		} else if (isEmptyCollection(rightNode.getType()))
		{
			return format(node.getLeft()) + EMPTY;
		}

		return className + ".equals(" + format(node.getLeft()) + ", "
				+ format(node.getRight()) + ")";

	}

	private boolean isEmptyCollection(STypeCG type)
	{
		if (type instanceof SSeqTypeCG)
		{
			SSeqTypeCG seq = (SSeqTypeCG) type;

			return seq.getEmpty();
		} else if (type instanceof SSetTypeCG)
		{
			SSetTypeCG set = (SSetTypeCG) type;

			return set.getEmpty();
		} else if (type instanceof SMapTypeCG)
		{
			SMapTypeCG map = (SMapTypeCG) type;

			return map.getEmpty();
		}

		return false;
	}

	public String format(List<AFormalParamLocalParamCG> params)
			throws AnalysisException
	{
		StringWriter writer = new StringWriter();

		if (params.size() <= 0)
		{
			return "";
		}

		final String finalPrefix = " final ";

		AFormalParamLocalParamCG firstParam = params.get(0);
		writer.append(finalPrefix);
		writer.append(format(firstParam));

		for (int i = 1; i < params.size(); i++)
		{
			AFormalParamLocalParamCG param = params.get(i);
			writer.append(", ");
			writer.append(finalPrefix);
			writer.append(format(param));
		}
		return writer.toString();
	}

	public String formatSuperType(AClassDeclCG classDecl)
	{
		return classDecl.getSuperName() == null ? "" : "extends "
				+ classDecl.getSuperName();
	}

	public String formatMaplets(AEnumMapExpCG mapEnum) throws AnalysisException
	{
		LinkedList<AMapletExpCG> members = mapEnum.getMembers();

		return "new Maplet[]{" + formatArgs(members) + "}";
	}

	public String formatArgs(List<? extends SExpCG> exps)
			throws AnalysisException
	{
		StringWriter writer = new StringWriter();

		if (exps.size() <= 0)
		{
			return "";
		}

		SExpCG firstExp = exps.get(0);
		writer.append(format(firstExp));

		for (int i = 1; i < exps.size(); i++)
		{
			SExpCG exp = exps.get(i);
			writer.append(", " + format(exp));
		}

		return writer.toString();
	}

	public boolean isNull(INode node)
	{
		return node == null;
	}

	public boolean isVoidType(STypeCG node)
	{
		return node instanceof AVoidTypeCG;
	}

	public String formatInitialExp(SExpCG exp) throws AnalysisException
	{
		// private int a = 2; (when exp != null)
		// private int a; (when exp == null)

		return exp == null ? "" : " = " + format(exp);
	}

	public String formatOperationBody(SStmCG body) throws AnalysisException
	{
		String NEWLINE = "\n";
		if (body == null)
		{
			return ";";
		}

		StringWriter generatedBody = new StringWriter();

		generatedBody.append("{" + NEWLINE + NEWLINE);
		generatedBody.append(format(body));
		generatedBody.append(NEWLINE + "}");

		return generatedBody.toString();
	}

	public String formatTemplateParam(INode potentialBasicType)
			throws AnalysisException
	{
		if (potentialBasicType == null)
		{
			return "";
		}

		if (potentialBasicType instanceof AIntNumericBasicTypeCG
				|| potentialBasicType instanceof ARealNumericBasicTypeCG)
		{
			return "Number";
		} else if (potentialBasicType instanceof ABoolBasicTypeCG)
		{
			return "Boolean";
		} else if (potentialBasicType instanceof ACharBasicTypeCG)
		{
			return "Character";
		} else
		{
			return format(potentialBasicType);
		}
	}

	public boolean isStringLiteral(SExpCG exp)
	{
		return exp instanceof AStringLiteralExpCG;
	}

	public boolean isSeqType(SExpCG exp)
	{
		return info.getAssistantManager().getTypeAssistant().isSeqType(exp);
	}

	public boolean isMapType(SExpCG exp)
	{
		return info.getAssistantManager().getTypeAssistant().isMapType(exp);
	}

	public boolean isStringType(STypeCG type)
	{
		return info.getAssistantManager().getTypeAssistant().isStringType(type);
	}

	public boolean isStringType(SExpCG exp)
	{
		return info.getAssistantManager().getTypeAssistant().isStringType(exp);
	}

	public boolean isCharType(STypeCG type)
	{
		return type instanceof ACharBasicTypeCG;
	}

	public String buildString(List<SExpCG> exps) throws AnalysisException
	{
		StringBuilder sb = new StringBuilder();

		sb.append("new String(new char[]{");

		if (exps.size() > 0)
		{
			sb.append(format(exps.get(0)));

			for (int i = 1; i < exps.size(); i++)
			{
				sb.append(", " + format(exps.get(i)));
			}
		}

		sb.append("})");

		return sb.toString();
	}

	public String formatElementType(STypeCG type) throws AnalysisException
	{
		if (type instanceof SSetTypeCG)
		{
			SSetTypeCG setType = (SSetTypeCG) type;

			return format(setType.getSetOf());
		} else if (type instanceof SSeqTypeCG)
		{
			SSeqTypeCG seqType = (SSeqTypeCG) type;

			return format(seqType.getSeqOf());
		}

		throw new AnalysisException("Expected set or seq type when trying to format element type");

	}

	public String nextVarName(String prefix)
	{
		return info.getTempVarNameGen().nextVarName(prefix);
	}

	public STypeCG findElementType(AApplyObjectDesignatorCG designator)
	{
		return info.getAssistantManager().getTypeAssistant().findElementType(designator, classes, info);
	}

	public boolean isLoopVar(AVarLocalDeclCG localVar)
	{
		return localVar.parent() instanceof AForLoopStmCG;
	}

	public boolean isLambda(AApplyExpCG applyExp)
	{
		SExpCG root = applyExp.getRoot();

		if (root instanceof AApplyExpCG
				&& root.getType() instanceof AMethodTypeCG)
		{
			return true;
		}

		if (!(root instanceof SVarExpCG))
		{
			return false;
		}

		SVarExpCG varExp = (SVarExpCG) root;

		return varExp.getIsLambda() != null && varExp.getIsLambda();
	}

	public String escapeStr(String str)
	{
		String escaped = "";
		for (int i = 0; i < str.length(); i++)
		{
			char currentChar = str.charAt(i);
			escaped += GeneralUtils.isEscapeSequence(currentChar) ? StringEscapeUtils.escapeJava(currentChar
					+ "")
					: currentChar + "";
		}

		return escaped;
	}

	public String escapeChar(char c)
	{
		return GeneralUtils.isEscapeSequence(c) ? StringEscapeUtils.escapeJavaScript(c
				+ "")
				: c + "";
	}
}
