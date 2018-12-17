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
 *
 * @author Itamar
 */
public class FileVOTableModel extends DefaultTableModel {
    boolean hideEquals = false;
    public FileVOTableModel(Object[][] data, String[] columnNames) {
	super(data, columnNames);
    }
    Vector files;
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
    private Vector getDataVector(FilePair file) {
	Vector v = new Vector();
	v.add(new Boolean(file.getNewer() != FilePair.EQUALS));
	v.add(file.getMainSymbol());
	v.add(file.getSecSymbol());
	v.add(file.getPath());
	return v;
    }
    Class[] types = new Class [] {
	java.lang.Boolean.class,
	java.lang.String.class,
	java.lang.String.class,
	java.lang.String.class
    };
    boolean[] canEdit = new boolean [] { true, false, false, false };
    
    public Class getColumnClass(int columnIndex) {
	return types [columnIndex];
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
	return canEdit [columnIndex];
    }
}
