package org.overture.ide.builders.vdmj;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.overture.ide.core.builder.AbstractVdmBuilder;
import org.overturetool.vdmj.ExitStatus;
import org.overturetool.vdmj.messages.VDMError;
import org.overturetool.vdmj.messages.VDMWarning;

public abstract class VdmjBuilder  extends AbstractVdmBuilder
{
	private List<VDMError> errors = new ArrayList<VDMError>();
	private List<VDMWarning> warnings = new ArrayList<VDMWarning>();
	protected IStatus buileModelElements(IProject project)
	{	
		
		ExitStatus typeCheckStatus = null;
		
		typeCheckStatus = typeCheck();
		if (typeCheckStatus == ExitStatus.EXIT_ERRORS)
		{
			for (VDMError error : errors)
			{
				addErrorMarker( error);
			}
		}
		if(!getProject().hasSuppressWarnings())
		for (VDMWarning warning : warnings)
		{
			addWarningMarker(project,warning);
		}

		if (typeCheckStatus == ExitStatus.EXIT_ERRORS) {
			IStatus typeChecked = new Status(IStatus.ERROR,
					IBuilderVdmjConstants.PLUGIN_ID, 0, "not typechecked",
					null);
			return typeChecked;
		} 
		else {

			IStatus typeChecked = new Status(IStatus.OK,
					IBuilderVdmjConstants.PLUGIN_ID, 0, "Type Checked",
					null);
			return typeChecked;
		}
		
	}

	private void addErrorMarker(VDMError error)
	{
		addErrorMarker(error.location.file, error.message, error.location, IBuilderVdmjConstants.PLUGIN_ID);
	}


	private void addWarningMarker(IProject project,VDMWarning error)
	{
		addWarningMarker(error.location.file, error.message, error.location, IBuilderVdmjConstants.PLUGIN_ID);
	}

	/**
	 * Handle Errors
	 * 
	 * @param list
	 *            encountered during a parse or type check
	 */
	protected void processErrors(List<VDMError> errors) {
		this.errors.addAll(errors);
	};

	/**
	 * Handle Warnings
	 * 
	 * @param errors
	 *            encountered during a parse or type check
	 */
	protected void processWarnings(List<VDMWarning> warnings) {
		this.warnings.addAll(warnings);
	};
	
	protected void processInternalError(Throwable e) {
		System.out.println(e.toString());
	};

	public abstract ExitStatus typeCheck();
}
