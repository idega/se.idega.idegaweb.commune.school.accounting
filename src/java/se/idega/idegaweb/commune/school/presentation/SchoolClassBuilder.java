package se.idega.idegaweb.commune.school.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import se.idega.idegaweb.commune.school.event.SchoolEventListener;

import com.idega.block.school.data.SchoolClass;
import com.idega.business.IBOLookup;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;

/**
 * @author Laddi
 * */
public class SchoolClassBuilder extends SchoolCommuneBlock {
	
	private final String PARAMETER_ACTION = "scb_action";
	private final String PARAMETER_CLASS_NAME = "scb_class_name";
	private final String PARAMETER_TEACHER_ID = "scb_teacher_id";

	private final int ACTION_NEW = 1;
	private final int ACTION_SAVE = 2;
	private final int ACTION_DELETE = 3;
	private final int ACTION_EDIT = 4;
	
	private int action = -1;

	public SchoolClassBuilder() {
	}
	
	public void init(IWContext iwc) throws RemoteException {
		parseAction(iwc);
		drawForm(iwc);
	}
	
	private void parseAction(IWContext iwc) throws RemoteException {
		if (iwc.isParameterSet(PARAMETER_ACTION))
			action = Integer.parseInt(iwc.getParameter(PARAMETER_ACTION));
			
		if (action == ACTION_SAVE) {
			String name = iwc.getParameter(PARAMETER_CLASS_NAME);
			getBusiness().getSchoolClassBusiness().storeSchoolClass(getSession().getSchoolClassID(), name, getSchoolID(), getSchoolSeasonID(), getSchoolYearID(), -1);
		}	
		else if (action == ACTION_DELETE) {
			if (getBusiness().getSchoolClassBusiness().getNumberOfStudentsInClass(getSession().getSchoolClassID()) == 0)
				getBusiness().getSchoolClassBusiness().removeSchoolClass(getSession().getSchoolClassID());
			else
				getParentPage().setAlertOnLoad(localize("school.class_not_empty","Class is not empty! Remove all students from class before deleting."));
		}	
	}
	
	private void drawForm(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.setEventListener(SchoolEventListener.class);
		
		Table table = new Table(1,3);
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setWidth(getWidth());
		table.setHeight(2, "6");
		form.add(table);
		
		table.add(getNavigationTable(),1,1);
		table.add(getClassTable(iwc),1,3);
		
		add(form);
	}
	
	private Table getNavigationTable() throws RemoteException {
		Table table = new Table(5,1);
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setWidth(3,"8");

		table.add(getSmallHeader(localize("school.season","Season")+":"+Text.NON_BREAKING_SPACE),1,1);
		table.add(getSchoolSeasons(),2,1);
		table.add(getSmallHeader(localize("school.year","Year")+":"+Text.NON_BREAKING_SPACE),4,1);
		table.add(getSchoolYears(),5,1);
		
		return table;
	}
	
	private Table getClassTable(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setColumns(4);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setWidth(1,"50%");
		table.setWidth(2,"50%");
		table.setWidth(3,"10");
		table.setWidth(4,"10");
		table.setWidth(1,"50%");
		table.setCellpadding(1);
		table.setCellspacing(0);

		int row = 1;
		table.add(getHeader(localize("school.class_name", "Class name")),1,row);
		table.add(getHeader(localize("school.teacher", "Teacher")),2,row);
		HiddenInput classID = new HiddenInput(getSession().getParameterSchoolClassID(),"-1");
		if (action == ACTION_EDIT)
			classID.setValue(getSchoolClassID());
		table.add(classID,3,row++);

		Collection schoolClasses = getBusiness().getSchoolClassBusiness().findSchoolClassesBySchoolAndSeasonAndYear(getSchoolID(), getSchoolSeasonID(), getSchoolYearID());
		if (!schoolClasses.isEmpty()) {
			Iterator iter = schoolClasses.iterator();
			while (iter.hasNext()) {
				SchoolClass element = (SchoolClass) iter.next();
				User teacher = null;
				if ( element.getTeacherId() != -1 )
					teacher = getUserBusiness(iwc).getUser(element.getTeacherId());
				
				SubmitButton edit = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.edit","Edit"),PARAMETER_ACTION,String.valueOf(ACTION_EDIT)));
				edit.setValueOnClick(getSession().getParameterSchoolClassID(), element.getPrimaryKey().toString());

				SubmitButton delete = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.delete","Delete"),PARAMETER_ACTION,String.valueOf(ACTION_DELETE)));
				delete.setValueOnClick(getSession().getParameterSchoolClassID(), element.getPrimaryKey().toString());
				
				if (action == ACTION_EDIT && getSchoolClassID() == ((Integer)element.getPrimaryKey()).intValue()) {
					TextInput nameInput = (TextInput) getStyledInterface(new TextInput(PARAMETER_CLASS_NAME));
					nameInput.setValue(element.getName());
					
					TextInput teacherInput = (TextInput) getStyledInterface(new TextInput(PARAMETER_TEACHER_ID));
					teacherInput.setValue(localize("school.disabled","disabled"));
					teacherInput.setDisabled(true);
					
					table.add(nameInput,1,row);
					table.add(teacherInput,2,row);
					table.add(new HiddenInput(getSession().getParameterSchoolClassID(),element.getPrimaryKey().toString()),3,row++);
				}
				else {
					table.add(getSmallText(element.getName()),1,row);
					if ( teacher != null )
						table.add(getSmallText(teacher.getName()),2,row);
					table.add(edit,3,row);
					table.add(delete,4,row++);
				}
			}
		}
		
		if (action == ACTION_NEW) {
			TextInput nameInput = (TextInput) getStyledInterface(new TextInput(PARAMETER_CLASS_NAME));
			
			TextInput teacherInput = (TextInput) getStyledInterface(new TextInput(PARAMETER_TEACHER_ID));
			teacherInput.setValue(localize("school.disabled","disabled"));
			teacherInput.setDisabled(true);
			
			table.add(nameInput,1,row);
			table.add(teacherInput,2,row++);
		}
		
		SubmitButton newButton = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.new","New"),PARAMETER_ACTION,String.valueOf(ACTION_NEW)));
		SubmitButton submit = (SubmitButton) getStyledInterface(new SubmitButton(localize("save","Save"),PARAMETER_ACTION,String.valueOf(ACTION_SAVE)));
		
		if (!(action == ACTION_EDIT || action == ACTION_NEW))
			table.add(newButton,1,row);
		if (action == ACTION_EDIT || action == ACTION_NEW)
			table.add(submit,1,row);
		table.mergeCells(1, row, table.getColumns(), row);
		table.setHorizontalZebraColored("#EFEFEF", "#FFFFFF");
		table.setRowColor(1, "#CCCCCC");
		table.setRowColor(row, "#FFFFFF");
		
		return table;
	}

	private UserBusiness getUserBusiness(IWContext iwc) throws RemoteException {
		return (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);	
	}	
}