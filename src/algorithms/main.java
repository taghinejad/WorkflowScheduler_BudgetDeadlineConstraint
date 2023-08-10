package algorithms;


import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import DAG.*;
public class main {

 
	public static  Adag unmarshall(String filename) throws JAXBException {
		JAXBContext context= JAXBContext.newInstance(Adag.class);
		Unmarshaller umar= context.createUnmarshaller();
		Adag unmarshalled= (Adag)umar.unmarshal(new File(filename));
	
		return unmarshalled;
	}
	
	public static void main(String[] args) throws JAXBException {
		// TODO Auto-generated method stub
		try {
			Adag returnedDuke=unmarshall("D:/JavaApps/WfDescFiles/Inspiral_30.xml");
			
			System.out.println("unmarshalled: " +returnedDuke +"child size:" +returnedDuke.childCount);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
		}
	
	}

}
