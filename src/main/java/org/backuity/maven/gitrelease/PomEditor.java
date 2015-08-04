package org.backuity.maven.gitrelease;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class PomEditor {

	private File file;
	private final Namespace pomNs = Namespace.getNamespace("http://maven.apache.org/POM/4.0.0");
	private Document doc;
	
	public PomEditor(File pomFile) throws IOException, JDOMException {
		this.file = pomFile;
		
		SAXBuilder builder = new SAXBuilder();		
		InputStream input = new FileInputStream(file);
		assert input != null;		
		doc = builder.build( input );		
	}
	
	public File getFile() {
		return file;
	}
	
	public Version getVersion() {
		return new Version( getVersionElement().getValue() );
	}

	private Element getVersionElement() {
		return doc.getRootElement().getChild("version", pomNs);
	}

	public void updateVersion(Version newVersion) throws IOException {
		XMLOutputter output = new XMLOutputter();
		FileOutputStream fos = new FileOutputStream(file);			
		
		getVersionElement().setText( newVersion.toString() );

		try {
			output.output(doc, fos );
		} finally {
			fos.close();
		}
	}
}
