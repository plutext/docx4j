package org.docx4j.model.fields.merge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.fields.ComplexFieldLocator;
import org.docx4j.model.fields.FieldRef;
import org.docx4j.model.fields.FieldsPreprocessor;
import org.docx4j.model.fields.FldSimpleModel;
import org.docx4j.model.fields.FormattingSwitchHelper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Body;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailMergerWithNext extends MailMerger {
	
	private static Logger log = LoggerFactory.getLogger(MailMergerWithNext.class);		
	
    /**
     * Similar to performMerge method but better to merge labels template because of using the NEXT instruction to go to the next item.
     * 
     * The contents of the input pkg are replaced with the results of the merge.
     * 
     * @param input Document template
     * @param data List of multiple datamap
     * @throws Docx4JException
     */
    public static void performLabelMerge(WordprocessingMLPackage input, List<Map<DataFieldName, String>> data) throws Docx4JException {
        // Required where converting MERGEFIELD to FORMTEXT
        FormTextFieldNames formTextFieldNames = new FormTextFieldNames();

        FieldsPreprocessor.complexifyFields(input.getMainDocumentPart());
        List<Object> mdpResults = perform(input, input.getMainDocumentPart().getContent(), data, formTextFieldNames);
        input.getMainDocumentPart().getContent().clear();
        input.getMainDocumentPart().getContent().addAll(mdpResults);
    }	

    /**
     * Perform the merge. Recognize the NEXT instruction to go to the next data.
     * 
     * @param input
     * @param contentList
     * @param data All datamap into a list
     * @param formTextFieldNames
     * @return
     * @throws Docx4JException
     */
    private static List<Object> perform(WordprocessingMLPackage input, List<Object> contentList, List<Map<DataFieldName, String>> data,
            FormTextFieldNames formTextFieldNames) throws Docx4JException {

        // We need our fieldRefs point to the correct objects;
        // the easiest way to do this is to create them after cloning!

        // to facilitate cloning, wrap the list in a body
        Body shell = Context.getWmlObjectFactory().createBody();
        shell.getContent().addAll(contentList);
        Body shellClone = (Body) XmlUtils.deepCopy(shell);

        // find fields
        ComplexFieldLocator fl = new ComplexFieldLocator();
        new TraversalUtil(shellClone, fl);
        log.info("Found " + fl.getStarts().size() + " fields ");

        // canonicalise and setup fieldRefs
        List<FieldRef> fieldRefs = new ArrayList<FieldRef>();
		canonicaliseStarts(fl, fieldRefs);

        int datamapIndex = 0;
        Map<DataFieldName, String> datamap = data.get(datamapIndex);

        // Populate
        for (FieldRef fr : fieldRefs) {

            if (fr.getFldName().equals("MERGEFIELD")) {

                String instr = extractInstr(fr.getInstructions());
                String datafieldName = getDatafieldNameFromInstr(instr);
                String val = datamap.get(new DataFieldName(datafieldName));
                String gFormat = null; // required only for FORMTEXT conversion

                if (val == null || val.length() == 0) {
                    if (fieldFate.equals(OutputField.REMOVED)) {
                        // Remove the mergefield from the document
                        removeSimpleField(fr);

                        // Concatenate all content still present in the parent
                        String text = getTextInsideContent(fr.getParent());

                        // If the parent still contains data, don't delete it
                        if (StringUtils.isBlank(text)) {
                            // Don't need to remove the paragraph if we've already inserted all data
                            if (data.size() > datamapIndex) {
                                recursiveRemove(shellClone, fr.getParent());
                            }
                        }
                    }
                } else {

                    // Now format the result
                    FldSimpleModel fsm = new FldSimpleModel();
                    try {
                        fsm.build(instr);
                        val = FormattingSwitchHelper.applyFormattingSwitch(input, fsm, val);

                        gFormat = FormattingSwitchHelper.findFirstSwitchValue("\\*", fsm.getFldParameters(), true);
                        // Solely for potential use in OutputField.AS_FORMTEXT_REGULAR
                        // We are in fact applying all formatting switches above.

                    } catch (TransformerException e) {
                        log.warn("Can't format the field", e);
                    }

                    fr.setResult(val);
                }

                if (fieldFate.equals(OutputField.AS_FORMTEXT_REGULAR)) {

                    log.debug(gFormat);
                    // TODO if we're going to use gFormat, setup FSM irrespective of whether we can find key

                    // TODO: other format instructions
                    // if (gFormat!=null) {
                    // if (gFormat.equals("Upper")) {
                    // gFormat = "UPPERCASE";
                    // } else if (gFormat.equals("Lower")) {
                    // gFormat = "LOWERCASE";
                    // }
                    // }

                    // replace instrText
                    // eg MERGEFIELD CLIENT.ORGANIZATIONSTATE \* Upper \* MERGEFORMAT
                    // to FORMTEXT
                    // Do this first, so we can abort without affecting output
                    List<Object> instructions = fr.getInstructions();
                    if (instructions.size() != 1) {
                        log.error("TODO MERGEFIELD field contained complex instruction");
                        continue;
                    }
                    Object o = XmlUtils.unwrap(instructions.get(0));
                    if (o instanceof Text) {
                        ((Text) o).setValue("FORMTEXT");
                    } else {
                        if(log.isErrorEnabled()) {
                            log.error("TODO: set FORMTEXT in" + o.getClass().getName());
                            log.error(XmlUtils.marshaltoString(instructions.get(0), true, true));
                        }
                        continue;
                    }

                    String fieldName = formTextFieldNames.generateName(datafieldName);
                    log.debug("Field name normalisation: " + datafieldName + " -> " + fieldName);
                    setFormFieldProperties(fr, fieldName, null);

                    // remove <w:highlight w:val="lightGray"/>, if present
                    // (corresponds in Word to clicking Legacy Forms > Form Field Shading)
                    // so that the result is not printed in grey
                    R resultR = fr.getResultsSlot();
                    if (resultR.getRPr() != null && resultR.getRPr().getHighlight() != null) {
                        resultR.getRPr().setHighlight(null);
                    }

                } else if (!fieldFate.equals(OutputField.KEEP_MERGEFIELD)) {
                    // If doing an actual mail merge, the begin-separate run is removed, as is the end run
                    fr.getParent().getContent().remove(fr.getBeginRun());
                    fr.getParent().getContent().remove(fr.getEndRun());
                }

                // System.out.println("AFTER " +XmlUtils.marshaltoString(
                // fr.getParent(), true, true));

            } else if (fr.getFldName().equals("NEXT")) {
                // Remove the NEXT field from the document
                removeSimpleField(fr);

                datamapIndex++;
                if (datamapIndex >= data.size()) {
                    datamap = new HashMap<DataFieldName, String>();
                } else {
                    datamap = data.get(datamapIndex);
                }
            }
        }

        return shellClone.getContent();

    }	
}
