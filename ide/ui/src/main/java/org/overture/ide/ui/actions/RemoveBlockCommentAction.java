package org.overture.ide.ui.actions;

import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.ui.texteditor.ITextEditor;
import org.overture.ide.ui.folding.IVdmPartitions;



/**
 * Action that removes the enclosing comment marks from a Java block comment.
 * 
 * @since 3.0
 */
public class RemoveBlockCommentAction extends BlockCommentAction {

	/**
	 * Creates a new instance.
	 * 
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or 
	 *   <code>null</code> if none
	 * @param editor the text editor
	 */
	public RemoveBlockCommentAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
	}
	
	/*
	 * @see org.eclipse.jdt.internal.ui.actions.AddBlockCommentAction#runInternal(org.eclipse.jface.text.ITextSelection, org.eclipse.jface.text.IDocumentExtension3, org.eclipse.jdt.internal.ui.actions.AddBlockCommentAction.Edit.EditFactory)
	 */
	@Override
	protected void runInternal(ITextSelection selection, IDocumentExtension3 docExtension, Edit.EditFactory factory) throws BadPartitioningException, BadLocationException {
		List<Edit> edits= new LinkedList<Edit>();
		int tokenLength= getCommentStart().length();
		
		int offset= selection.getOffset();
		int endOffset= offset + selection.getLength();

		ITypedRegion partition= docExtension.getPartition(IVdmPartitions.VDM_PARTITIONING, offset, false);
		int partOffset= partition.getOffset();
		int partEndOffset= partOffset + partition.getLength();
		
		while (partEndOffset < endOffset) {
			
			if (partition.getType() == IVdmPartitions.VDM_COMMENT) {
				edits.add(factory.createEdit(partOffset, tokenLength, "")); //$NON-NLS-1$
				edits.add(factory.createEdit(partEndOffset - tokenLength, tokenLength, "")); //$NON-NLS-1$
			}
			
			partition= docExtension.getPartition(IVdmPartitions.VDM_PARTITIONING, partEndOffset, false);
			partOffset= partition.getOffset();
			partEndOffset= partOffset + partition.getLength();
		}

		if (partition.getType() == IVdmPartitions.VDM_COMMENT) {
			edits.add(factory.createEdit(partOffset, tokenLength, "")); //$NON-NLS-1$
			edits.add(factory.createEdit(partEndOffset - tokenLength, tokenLength, "")); //$NON-NLS-1$
		}

		executeEdits(edits);
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.actions.AddBlockCommentAction#validSelection(org.eclipse.jface.text.ITextSelection)
	 */
	@Override
	protected boolean isValidSelection(ITextSelection selection) {
		return selection != null && !selection.isEmpty();
	}

}

