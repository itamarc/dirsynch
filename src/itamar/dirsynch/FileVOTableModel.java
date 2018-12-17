/*
 * FileVOTableModel.java
 *
 * Created on 4 de Agosto de 2006, 01:42
 */
package itamar.dirsynch;

import java.util.Iterator;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 * TableModel with a set of FilePair's and an attribute to define if the equal pairs should be hidden or not.
 * @author Itamar
 */
public class FileVOTableModel extends DefaultTableModel {
    /**
     * Should the equals files be hidden?
     */
    boolean hideEquals = false;
    /**
     * Constructor method.
     * @param data The Object[][] model data.
     * @param columnNames The String array with the column names.
     */
    public FileVOTableModel(Object[][] data, String[] columnNames) {
	super(data, columnNames);
    }
    /**
     * Vector of FilePair's.
     */
    Vector files;
    
    /**
     * Sets both the files data and the hideEquals boolean attribute.
     * @param files A Vector with FilePair's.
     * @param hideEquals Boolean meaning if the FilePair's with equals files should be hidden or not.
     */
    public void setFiles(Vector files, boolean hideEquals) {
	this.files = files;
	this.hideEquals = hideEquals;
	setRowCount(0);
	for (Iterator iter = files.iterator(); iter.hasNext();) {
	    FilePair file = (FilePair) iter.next();
	    if (hideEquals != true || file.getNewer() != FilePair.EQUALS) {
		addRow(getDataVector(file));
	    }
	}
    }
    /**
     * Returns the FilePair's Vector with all files or only the different ones depending on the hideEquals attribute.
     * @return Vector with FilePair's.
     */
    public Vector getFiles() {
	if (hideEquals) {
	    Vector clean = new Vector();
	    for (Iterator iter = files.iterator(); iter.hasNext();) {
		FilePair filePair = (FilePair) iter.next();
		if (!filePair.isEquals()) {
		    clean.add(filePair);
		}
	    }
	    return clean;
	} else {
	    return files;
	}
    }
    /**
     * Converts a FilePair object with an Vector representing one row.
     * @param file A FilePair object to convert.
     * @return The Vector representing one row of the TableModel.
     */
    private Vector getDataVector(FilePair file) {
	Vector v = new Vector();
	v.add(new Boolean(file.getNewer() != FilePair.EQUALS));
	v.add(file.getMainSymbol());
	v.add(file.getSecSymbol());
	v.add(file.getPath());
	return v;
    }
    /**
     * The types (Classes) of the columns.
     */
    Class[] types = new Class [] {
	java.lang.Boolean.class,
	java.lang.String.class,
	java.lang.String.class,
	java.lang.String.class
    };
    /**
     * Can each column be edited?
     */
    boolean[] canEdit = new boolean [] { true, false, false, false };
    
    /**
     * Return the types of the columns.
     * @param columnIndex The index of the column to return.
     * @return The column type (Class).
     */
    public Class getColumnClass(int columnIndex) {
	return types [columnIndex];
    }
    
    /**
     * Defines if the cell is editable based only on the column.
     * @param rowIndex Needs to be here, but it's just ignored.
     * @param columnIndex The value based on which the editable value is defined.
     * @return Boolean meaning if the cell is editable.
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
	return canEdit [columnIndex];
    }
}
