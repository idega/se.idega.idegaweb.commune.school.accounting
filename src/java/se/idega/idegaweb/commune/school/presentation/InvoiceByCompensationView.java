package se.idega.idegaweb.commune.school.presentation;

import com.idega.block.school.business.SchoolBusiness;
import com.idega.block.school.data.*;
import com.idega.business.IBOLookup;
import com.idega.presentation.*;
import com.idega.presentation.text.*;
import com.idega.presentation.ui.*;
import com.idega.user.data.User;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.ejb.FinderException;
import se.idega.idegaweb.commune.accounting.presentation.*;
import se.idega.idegaweb.commune.school.business.SchoolCommuneBusiness;

/**
 * InvoiceByCompensationView is an IdegaWeb block were the user can view and
 * edit the factoring by compensation field of school members in the current
 * season.
 * <p>
 * Last modified: $Date: 2003/10/24 11:24:53 $ by $Author: laddi $
 *
 * @author <a href="http://www.staffannoteberg.com">Staffan N�teberg</a>
 * @version $Revision: 1.13 $
 * @see com.idega.block.school.data.SchoolClassMember
 * @see se.idega.idegaweb.commune.school.businessSchoolCommuneBusiness
 * @see javax.ejb
 */
public class InvoiceByCompensationView extends AccountingBlock {
    private static final String PREFIX = "CompByInv_";
    
    private static final String BACK_DEFAULT = "Tillbaka";
    private static final String BACK_KEY = PREFIX + "back";
    private static final String CANCEL_DEFAULT = "Avbryt";
    private static final String CANCEL_KEY = PREFIX + "cancel";
    private static final String COMPENSATIONBYINVOICE_DEFAULT
        = "Ers�ttning mot faktura";
    private static final String COMPENSATIONBYINVOICE_KEY
        = PREFIX + "compensationByInvoice";
    private static final String INVOICEINTERVAL_DEFAULT = "Fakturaintervall";
    private static final String INVOICEINTERVAL_KEY
        = PREFIX + "invoiceInterval";
    private static final String ISUPDATED_DEFAULT = " �r nu �ndrad till ";
    private static final String ISUPDATED_KEY = PREFIX + "isUpdated";
    private static final String LATESTINVOICEDATE_DEFAULT
        = "Senaste fakturadatum";
    private static final String LATESTINVOICEDATE_KEY
        = PREFIX + "latestInvoiceDate";
    private static final String MAINACTIVITY_DEFAULT = "Huvudverksamhet";
    private static final String MAINACTIVITY_KEY = PREFIX + "mainActivity";
    private static final String MEMBERID_KEY = PREFIX + "memberId";
    private static final String NAME_DEFAULT = "Namn";
    private static final String NAME_KEY = PREFIX + "name";
    //private final static String OPERATIONAL_FIELD_KEY = PREFIX + "operational_field";
    private static final String PROVIDER_DEFAULT = "Anordnare";
    private static final String PROVIDER_KEY = PREFIX + "provider";
    private static final String SAVE_DEFAULT = "Spara";
    private static final String SAVE_KEY = PREFIX + "save";
    private static final String SCHOOL_KEY = PREFIX + "school";
    private static final String SSN_DEFAULT = "Personnummer";
    private static final String SSN_KEY = PREFIX + "ssn";
    private static final String WRONGDATEFORMAT_DEFAULT
        = "Felaktigt datumformat";
    private static final String WRONGDATEFORMAT_KEY
        = PREFIX + "wrongDateFormat";
    
    private static final String ACTION_KEY = PREFIX + "action";
    private static final String ACTION_SAVE_KEY = PREFIX + "actionSave";
    private static final String ACTION_SHOWUSER_KEY = PREFIX + "actionShowUser";
    
    
    private static final SimpleDateFormat dateFormatter
        = new SimpleDateFormat ("yyyy-MM-dd");
    private static final SimpleDateFormat shortDateFormatter
        = new SimpleDateFormat ("yyyyMMdd");
    
	/**
	 * Init is the event handler of InvoiceByCompensationForm.
	 *
	 * @param context session data like user info etc.
	 */
	public void init (final IWContext context) {
		setResourceBundle (getResourceBundle(context));
        
		try {
            if (context.isParameterSet (ACTION_KEY)
                && ACTION_SHOWUSER_KEY.equals (context.getParameter
                                               (ACTION_KEY))) {
                showStudent (context);
            } else if (context.isParameterSet (ACTION_KEY)
                       && ACTION_SAVE_KEY.equals (context.getParameter
                                                  (ACTION_KEY))) {
                updateStudent (context);
            } else {
                showInvoiceByCompensationList (context);
            }
        } catch (final Exception exception) {
            System.err.println ("Exception caught in " + getClass ().getName ()
                                + " " + (new Date ()).toString ());
            System.err.println ("Parameters:");
            final Enumeration enum = context.getParameterNames ();
            while (enum.hasMoreElements ()) {
                final String key = (String) enum.nextElement ();
                System.err.println ('\t' + key + "='"
                                    + context.getParameter (key) + "'");
            }
            exception.printStackTrace ();
            add ("Det intr�ffade ett fel. F�rs�k igen senare.");
		}
	}
    
	/**
	 * Displays table with all school members that have invoice interval is set
	 *
	 * @param context session data like user info etc.
     * @exception FinderException if the school class wasn't found
     * @exception RemoteException if something fails in business layer
	 */
    private void showInvoiceByCompensationList (final IWContext context)
        throws RemoteException, FinderException {
        
        // set up header row
        final String [][] columnNames =
                {{ SSN_KEY, SSN_DEFAULT }, { NAME_KEY, NAME_DEFAULT },
                 { PROVIDER_KEY, PROVIDER_DEFAULT },
                 { INVOICEINTERVAL_KEY, INVOICEINTERVAL_DEFAULT },
                 { LATESTINVOICEDATE_KEY, LATESTINVOICEDATE_DEFAULT }};
        final Table studentTable = new Table();
        studentTable.setCellpadding(getCellpadding());
        studentTable.setCellspacing(getCellspacing());
        studentTable.setWidth(Table.HUNDRED_PERCENT);
        studentTable.setColumns (columnNames.length);
        studentTable.setRowColor(1, getHeaderColor());
        for (int i = 0; i < columnNames.length; i++) {
            studentTable.add(getSmallHeader(localize (columnNames [i][0],
                                                      columnNames [i][1])),
                             i + 1, 1);
        }
        
        // get some business objects
        final SchoolCommuneBusiness communeBusiness = (SchoolCommuneBusiness)
                IBOLookup.getServiceInstance (context,
                                              SchoolCommuneBusiness.class);
        final SchoolBusiness schoolBusiness
                = communeBusiness.getSchoolBusiness ();
        final SchoolClassHome classHome = schoolBusiness.getSchoolClassHome ();
        final SchoolHome schoolHome = schoolBusiness.getSchoolHome ();
        
        // search the database for students to display
        final String operationalField = getSession ().getOperationalField ();
        final SchoolClassMember [] students = communeBusiness
                .getCurrentMembersWithInvoiceInterval (operationalField);
        
        // display each student
        for (int i = 0; i < students.length; i++) {
			final SchoolClassMember student = students [i];
            int row = i + 2;
			showStudentInTableRow (studentTable, classHome, schoolHome, student,
                                   row);
        }
        
        // add to output
        add (createMainTable (studentTable));
    }
    
    /**
     * Display information about one particular student on a row in a provided
     * table.
     *
     * @param studentTable table to put row in
     * @param classHome home object for school class entities
     * @param schoolHome home class for school entities
     * @param student student to display
     * @param row the table row to use
     * @param FinderException if one of the entities are missing
     */
	private void showStudentInTableRow
        (final Table studentTable, final SchoolClassHome classHome,
         final SchoolHome schoolHome, final SchoolClassMember student,
         int row) throws FinderException {
		int col = 1;
		studentTable.setRowColor (row, (row % 2 == 0) ? getZebraColor1 ()
                                  : getZebraColor2 ());
		final User user = student.getStudent ();
		final String ssn = user.getPersonalID ();
		final Link ssnLink = getSmallLink (ssn);
		final Object studentId = student.getPrimaryKey ();
		final String userName = user.getFirstName () + " "
		        + user.getLastName ();
		final String schoolName
		        = getSchoolNameFromStudent (student, classHome, schoolHome);
		final String intervalKey = student.getInvoiceInterval ();
		final String intervalString = localize (intervalKey, intervalKey);
		ssnLink.addParameter (ACTION_KEY, ACTION_SHOWUSER_KEY);
		ssnLink.addParameter (MEMBERID_KEY, studentId.toString ());
		ssnLink.addParameter (SSN_KEY, ssn);
		ssnLink.addParameter (NAME_KEY, userName);
		ssnLink.addParameter (SCHOOL_KEY, schoolName);
		studentTable.add (ssnLink, col++, row);
		studentTable.add (new Text(userName), col++, row);
		studentTable.add (new Text(schoolName), col++, row);
		studentTable.add (new Text(intervalString), col++, row);
		final Date latestInvoiceDate = student.getLatestInvoiceDate ();
		if (null != latestInvoiceDate) {
		    studentTable.add (new Text(dateFormatter.format
                                       (latestInvoiceDate)), col++, row);
		}
	}
    
	/**
	 * Displays user info form, where latest invoice date can be modified
	 *
	 * @param context session data like user info etc.
     * @exception FinderException if the school class member wasn't found
     * @exception RemoteException if something fails in business layer
	 */
    private void showStudent (final IWContext context) throws RemoteException,
                                                              FinderException {
        // get some business objects
        final SchoolCommuneBusiness communeBusiness = (SchoolCommuneBusiness)
                IBOLookup.getServiceInstance (context,
                                              SchoolCommuneBusiness.class);
        final SchoolBusiness schoolBusiness
                = communeBusiness.getSchoolBusiness ();
        final SchoolClassMemberHome memberHome
                = schoolBusiness.getSchoolClassMemberHome ();
        
        // get student info
        final Integer studentId
                = new Integer (context.getParameter (MEMBERID_KEY));
        final SchoolClassMember student
                = memberHome.findByPrimaryKey (studentId);
        final String ssn = context.getParameter (SSN_KEY);
        final String studentName = context.getParameter (NAME_KEY);
        final String schoolName = context.getParameter (SCHOOL_KEY);
        final Date latestInvoiceDate = student.getLatestInvoiceDate ();
        final String intervalKey = student.getInvoiceInterval ();
        final String intervalString = localize (intervalKey, intervalKey);
        
        // display student info
        final Table studentTable = getStudentInfoTable
                (ssn,  studentName, schoolName, latestInvoiceDate,
                 intervalString);
        
        // display buttons
        final Table buttonTable = getSaveButtonTable ();
        
        // put output together and publish
        final Table mainTable = new Table ();
        mainTable.setCellpadding (getCellpadding ());
        mainTable.setCellspacing (getCellspacing ());
        mainTable.add (studentTable, 1, 1);
        mainTable.add (buttonTable, 1, 2);
        final Form form = new Form ();
        form.add (mainTable);
        form.maintainParameter (MEMBERID_KEY);
        final Table formTable = new Table ();
        formTable.add (form, 1, 1);
        add (createMainTable (formTable));
    }

    /**
     * Creates a table with save and cancel buttons
     *
     * @return table with save and cancel button
     */
	private Table getSaveButtonTable () {
        final Table buttonTable = new Table ();
		buttonTable.setCellpadding (getCellpadding ());
		buttonTable.setCellspacing (getCellspacing ());
		buttonTable.setColumns (2);
		buttonTable.add (getButton (new SubmitButton
		                            (localize (SAVE_KEY, SAVE_DEFAULT),
		                             ACTION_KEY, ACTION_SAVE_KEY)), 1, 1);
		buttonTable.add (getButton (new SubmitButton (CANCEL_KEY, localize
		                                              (CANCEL_KEY,
		                                               CANCEL_DEFAULT))), 2, 1);
        return buttonTable;
	}
    
    /**
     * Creates a table with information about this student
     *
     * @param ssn social security number (personal id) for this person
     * @param studentName display name for this person
     * @param schoolName name of the school that this person is member of
     * @param latestInvoiceDate last time this person was invoiced
     * @param intervalString key for term, year etc.
     */
	private Table getStudentInfoTable
        (final String ssn, final String studentName, final String schoolName,
         final Date latestInvoiceDate, final String intervalString) {
        final Table studentTable = new Table ();
		studentTable.setCellpadding (getCellpadding ());
		studentTable.setCellspacing (getCellspacing ());
		studentTable.setColumns (2);
		final String [][] cells =
		        {{ SSN_KEY, SSN_DEFAULT,  ssn },
		         { NAME_KEY, NAME_DEFAULT, studentName },
		         { PROVIDER_KEY, PROVIDER_DEFAULT, schoolName },
		         { INVOICEINTERVAL_KEY, INVOICEINTERVAL_DEFAULT,
		           intervalString }};
		final TextInput textInput = (TextInput) getStyledInterface
		        (new TextInput (LATESTINVOICEDATE_KEY));
		textInput.setLength (10);
		textInput.setContent (null == latestInvoiceDate
		                      ? shortDateFormatter.format (new Date ())
		                      : shortDateFormatter.format (latestInvoiceDate));
		int row = 1;
		for (int i = 0; i < cells.length; i++) {
		    studentTable.add (getSmallHeader (localize (cells [i][0],
		                                                cells [i][1]) + ":"), 1,
		                      row);
		    studentTable.add(new Text (cells [i][2]), 2, row++);
		}
		studentTable.add (getSmallHeader (localize (LATESTINVOICEDATE_KEY,
		                                            LATESTINVOICEDATE_DEFAULT)
		                                  + ":"), 1, row);
		studentTable.add (textInput, 2, row++);
        return studentTable;
	}
    
    
	/**
	 * Updates latest invoice date
	 *
	 * @param context session data like user info etc.
     * @exception FinderException if the school class member wasn't found
     * @exception RemoteException if something fails in business layer
	 */
    private void updateStudent (final IWContext context) throws RemoteException,
                                                             FinderException   {
        // get some business objects
        final SchoolCommuneBusiness communeBusiness = (SchoolCommuneBusiness)
                IBOLookup.getServiceInstance (context,
                                              SchoolCommuneBusiness.class);
        final SchoolBusiness schoolBusiness
                = communeBusiness.getSchoolBusiness ();
        final SchoolClassMemberHome memberHome
                = schoolBusiness.getSchoolClassMemberHome ();
        
        // get student info
        final Integer studentId
                = new Integer (context.getParameter (MEMBERID_KEY));
        final SchoolClassMember student
                = memberHome.findByPrimaryKey (studentId);
        final String latestInvoiceDate
                = context.getParameter (LATESTINVOICEDATE_KEY);
        final Date date = getDateFromString (latestInvoiceDate);
        
        if (null != date) {
            student.setLatestInvoiceDate (new java.sql.Timestamp
                                          (date.getTime ()));
            student.store ();
        }

        // display output
        final Table table = new Table ();
        table.setCellpadding (getCellpadding ());
        table.setCellspacing (getCellspacing ());
        
        if (null != date) {
            table.add (new Text (localize (LATESTINVOICEDATE_KEY,
                                           LATESTINVOICEDATE_DEFAULT) + 
                                 localize (ISUPDATED_KEY, ISUPDATED_DEFAULT)
                                 +  dateFormatter.format (date)), 1, 1);
        } else {
            final Text text = new Text (localize (WRONGDATEFORMAT_KEY,
                                                  WRONGDATEFORMAT_DEFAULT));
            text.setFontColor ("#ff0000");
            table.add (text, 1, 1);
        }
		table.setHeight (2, 12);
        table.add (getSmallLink (localize (BACK_KEY, BACK_DEFAULT)), 1, 3);
        add (createMainTable (table));
    }
    
	/**
	 * Returns a styled table with content placed properly
	 *
	 * @param content the page unique content
     * @return Table to add to output
	 */
    private Table createMainTable (final PresentationObject content)
        throws RemoteException {
        final Table mainTable = new Table();
        mainTable.setCellpadding (getCellpadding ());
        mainTable.setCellspacing (getCellspacing ());
        mainTable.setWidth (Table.HUNDRED_PERCENT);
        mainTable.setColumns (1);
        mainTable.setRowColor (1, getHeaderColor ());
        mainTable.setRowAlignment(1, Table.HORIZONTAL_ALIGN_CENTER) ;
        
        mainTable.add (getSmallHeader
                       (localize (COMPENSATIONBYINVOICE_KEY,
                                  COMPENSATIONBYINVOICE_DEFAULT)),
                       1, 1);
        final Table innerTable = new Table ();
        innerTable.setColumns (2);
        innerTable.add (getSmallHeader (localize (MAINACTIVITY_KEY,
                                                  MAINACTIVITY_DEFAULT) + ":"),
                        1, 1);
        String operationalField = getSession ().getOperationalField();
        operationalField = operationalField == null ? "" : operationalField;
        innerTable.add (new OperationalFieldsMenu (), 2, 1);
        mainTable.add (innerTable, 1, 2);
        mainTable.add (content, 1, 3);
        return mainTable;
    }
    
	/**
	 * Returns name of the school that the school class member is member of
	 *
	 * @param student The person that is member of a school class
	 * @param classHome home object of school class beans
	 * @param schoolHome home object of school beans
     * @return String with school name
     * @exception FinderException if the school class wasn't found
	 */
    static private String getSchoolNameFromStudent
        (final SchoolClassMember student, final SchoolClassHome classHome,
         final SchoolHome schoolHome) throws FinderException {
        final Integer classId = new Integer (student.getSchoolClassId ());
        final SchoolClass schoolClass = classHome.findByPrimaryKey(classId);
        final Integer schoolId = new Integer (schoolClass.getSchoolId ());
        final School school = schoolHome.findByPrimaryKey (schoolId);
        return school.getName ();
    }
    
    private static Date getDateFromString (final String rawInput) {
        final StringBuffer digitOnlyInput = new StringBuffer();
        for (int i = 0; i < rawInput.length(); i++) {
            if (Character.isDigit(rawInput.charAt(i))) {
                digitOnlyInput.append(rawInput.charAt(i));
            }
        }
        if (digitOnlyInput.length() == 6) {
            digitOnlyInput.insert(0, 20);
        }
        
        if (digitOnlyInput.length() != 8) {
            return null;
        }
        final int year = new Integer(digitOnlyInput.substring(0, 4)).intValue();
        final int month
                = new Integer(digitOnlyInput.substring(4, 6)).intValue();
        final int day = new Integer(digitOnlyInput.substring(6, 8)).intValue();
        if (year < 2003 || month < 1 || month > 12 || day < 1 || day > 31) {
            return null;
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.set (year, month - 1, day);
        return calendar.getTime ();
    }
}
