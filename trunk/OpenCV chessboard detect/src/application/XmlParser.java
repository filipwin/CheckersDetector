package application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlParser {
	private File xmlFile;
	private DocumentBuilderFactory dbFactory;
	private DocumentBuilder dBuilder;
	Document doc;
	private TransformerFactory tFactory;
	Transformer transformer;
	DOMSource source;

	private void initDocuments(boolean readMode) {
		try {
			dbFactory = DocumentBuilderFactory.newInstance();
			dBuilder = dbFactory.newDocumentBuilder();
			if(readMode){
				doc = dBuilder.parse(xmlFile);
				doc.getDocumentElement().normalize();
			}
			else {
				doc = dBuilder.newDocument();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initTransformers(){
		try {
			this.initDocuments(false);
			tFactory = TransformerFactory.newInstance();
			transformer = tFactory.newTransformer();
			doc = dBuilder.newDocument();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public List<Integer> readXml(String filename, String nodeName) {
		String loadPath = "profiles/" + filename + ".xml";
		this.xmlFile = new File(loadPath);
		this.initDocuments(true);
		List<Integer> hsvValues = new ArrayList<Integer>();// elements at
															// indexes: 0 -
															// hueStart, 1 -
															// hueStop
															// 2 -
															// saturationStart,
															// 3 -
															// saturationStop,
															// 4 - valueStart, 5
															// - valueStop
		NodeList nList = doc.getElementsByTagName(nodeName);
		for (int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				String current = eElement.getElementsByTagName("hueStart").item(0).getTextContent();
				hsvValues.add(Integer.parseInt(current));
				current = eElement.getElementsByTagName("hueStop").item(0).getTextContent();
				hsvValues.add(Integer.parseInt(current));
				current = eElement.getElementsByTagName("satStart").item(0).getTextContent();
				hsvValues.add(Integer.parseInt(current));
				current = eElement.getElementsByTagName("satStop").item(0).getTextContent();
				hsvValues.add(Integer.parseInt(current));
				current = eElement.getElementsByTagName("valStart").item(0).getTextContent();
				hsvValues.add(Integer.parseInt(current));
				current = eElement.getElementsByTagName("valStop").item(0).getTextContent();
				hsvValues.add(Integer.parseInt(current));
			}
		}
		return hsvValues;
	}

	public void createXml(String filename, List<Integer> board, List<Integer> p1, List<Integer> p2) {
		Path savePath = Paths.get("profiles/" + filename + ".xml");
		this.initTransformers();
		Element hsvElement = doc.createElement("hsv");
		doc.appendChild(hsvElement);
			Element boardElement = doc.createElement("board");
			hsvElement.appendChild(boardElement);
				Element hueStart1 = doc.createElement("hueStart");
				boardElement.appendChild(hueStart1);
				hueStart1.appendChild(doc.createTextNode(String.valueOf(board.get(0))));
				Element hueStop1 = doc.createElement("hueStop");
				boardElement.appendChild(hueStop1);
				hueStop1.appendChild(doc.createTextNode(String.valueOf(board.get(1))));
				Element satStart1 = doc.createElement("satStart");
				boardElement.appendChild(satStart1);
				satStart1.appendChild(doc.createTextNode(String.valueOf(board.get(2))));
				Element satStop1 = doc.createElement("satStop");
				boardElement.appendChild(satStop1);
				satStop1.appendChild(doc.createTextNode(String.valueOf(board.get(3))));
				Element valStart1 = doc.createElement("valStart");
				boardElement.appendChild(valStart1);
				valStart1.appendChild(doc.createTextNode(String.valueOf(board.get(4))));
				Element valStop1 = doc.createElement("valStop");
				boardElement.appendChild(valStop1);
				valStop1.appendChild(doc.createTextNode(String.valueOf(board.get(5))));
			Element p1Element = doc.createElement("p1");
			hsvElement.appendChild(p1Element);
				Element hueStart2 = doc.createElement("hueStart");
				p1Element.appendChild(hueStart2);
				hueStart2.appendChild(doc.createTextNode(String.valueOf(p1.get(0))));
				Element hueStop2 = doc.createElement("hueStop");
				p1Element.appendChild(hueStop2);
				hueStop2.appendChild(doc.createTextNode(String.valueOf(p1.get(1))));
				Element satStart2 = doc.createElement("satStart");
				p1Element.appendChild(satStart2);
				satStart2.appendChild(doc.createTextNode(String.valueOf(p1.get(2))));
				Element satStop2 = doc.createElement("satStop");
				p1Element.appendChild(satStop2);
				satStop2.appendChild(doc.createTextNode(String.valueOf(p1.get(3))));
				Element valStart2 = doc.createElement("valStart");
				p1Element.appendChild(valStart2);
				valStart2.appendChild(doc.createTextNode(String.valueOf(p1.get(4))));
				Element valStop2 = doc.createElement("valStop");
				p1Element.appendChild(valStop2);
				valStop2.appendChild(doc.createTextNode(String.valueOf(p1.get(5))));
			Element p2Element = doc.createElement("p2");
			hsvElement.appendChild(p2Element);
				Element hueStart3 = doc.createElement("hueStart");
				p2Element.appendChild(hueStart3);
				hueStart3.appendChild(doc.createTextNode(String.valueOf(p2.get(0))));
				Element hueStop3 = doc.createElement("hueStop");
				p2Element.appendChild(hueStop3);
				hueStop3.appendChild(doc.createTextNode(String.valueOf(p2.get(1))));
				Element satStart3 = doc.createElement("satStart");
				p2Element.appendChild(satStart3);
				satStart3.appendChild(doc.createTextNode(String.valueOf(p2.get(2))));
				Element satStop3 = doc.createElement("satStop");
				p2Element.appendChild(satStop3);
				satStop3.appendChild(doc.createTextNode(String.valueOf(p2.get(3))));
				Element valStart3 = doc.createElement("valStart");
				p2Element.appendChild(valStart3);
				valStart3.appendChild(doc.createTextNode(String.valueOf(p2.get(4))));
				Element valStop3 = doc.createElement("valStop");
				p2Element.appendChild(valStop3);
				valStop3.appendChild(doc.createTextNode(String.valueOf(p2.get(5))));
				
		source = new DOMSource(doc);
		if(Files.exists(savePath)){
			try {
				Files.delete(savePath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		StreamResult result = new StreamResult(new File(savePath.toString()));
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
}
