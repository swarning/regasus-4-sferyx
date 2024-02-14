package de.regasus.common.combo;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import com.lambdalogic.report.od.DocumentFormat;
import com.lambdalogic.report.oo.OpenOfficeHelper;
import com.lambdalogic.util.CollectionsHelper;

/**
 * This class is NOT in the core plugin because it needs the constants from the report framework.

 * @author manfred
 *
 */
public class OpenOfficeFormatCombo extends Combo {

	private DocumentFormat[] availableDocumentFormats = null;


	public OpenOfficeFormatCombo(Composite parent, int style, DocumentFormat templateFormat) {
		super(parent, style);
		setTemplateFormat(templateFormat);
	}


	public OpenOfficeFormatCombo(Composite parent, int style) {
		super(parent, style);
	}


	public void setTemplateFormat(DocumentFormat templateFormat) {
		removeAll();

		if (templateFormat != null) {
    		availableDocumentFormats = OpenOfficeHelper.getAvailableDocumentFormats(templateFormat);
    		for (DocumentFormat documentFormat : availableDocumentFormats) {
    			add(documentFormat.name);
    		}

    		// first element is the default format
    		select(0);

    		getParent().layout();
		}
	}


	public void setTemplateFormats(Collection<DocumentFormat> selectedTemplateFormats) {
		removeAll();

		// calculate the intersection of the available formats for all templates

		if (selectedTemplateFormats != null) {
			List<DocumentFormat> documentFormatIntersection = null;

			for (DocumentFormat selectedDocumentFormat : selectedTemplateFormats) {
				DocumentFormat[] availableDocumentFormats = OpenOfficeHelper.getAvailableDocumentFormats(selectedDocumentFormat);

				if (documentFormatIntersection == null) {
					// add all available formats of the first template
					documentFormatIntersection = CollectionsHelper.createArrayList();
					for (DocumentFormat availableDocumentFormat : availableDocumentFormats) {
						documentFormatIntersection.add(availableDocumentFormat);
					}
				}
				else {
					// for every more template calculate the intersection
					Set<DocumentFormat> set = CollectionsHelper.createHashSet(availableDocumentFormats);
					documentFormatIntersection.retainAll(set);
				}
			}


			// copy DocumentFormats to availableDocumentFormats
			availableDocumentFormats = new DocumentFormat[documentFormatIntersection.size()];
			availableDocumentFormats = documentFormatIntersection.toArray(availableDocumentFormats);

			if (!documentFormatIntersection.isEmpty()) {
        		for (DocumentFormat documentFormat : documentFormatIntersection) {
        			add(documentFormat.name);
        		}

        		// first element of first selected template is the default format
        		select(0);
			}

    		getParent().layout();
		}
	}


	public DocumentFormat getFormat() {
		DocumentFormat documentFormat = null;

		if (availableDocumentFormats != null) {
			documentFormat = availableDocumentFormats[getSelectionIndex()];
		}

		return documentFormat;
	}


	public void setFormat(DocumentFormat documentFormat) {
		if (availableDocumentFormats != null) {
			for (int i = 0; i < availableDocumentFormats.length; i++) {
				if (availableDocumentFormats[i].equals(documentFormat)) {
					select(i);
					break;
				}
			}
		}
	}


	public void setFormat(String formatKey) {
		if (availableDocumentFormats != null) {
			for (int i = 0; i < availableDocumentFormats.length; i++) {
				if (availableDocumentFormats[i].getFormatKey().equals(formatKey)) {
					select(i);
					break;
				}
			}
		}
	}


	@Override
	protected void checkSubclass() {
		// Disable check for subclassing
	}

}
