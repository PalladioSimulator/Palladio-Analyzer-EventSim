package edu.kit.ipd.sdq.eventsim.instrumentation.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "restriction")
public class AdaptedInstrumentableRestriction {

	private Class<?> type;
	private List<Element> elements;

	public AdaptedInstrumentableRestriction(Class<?> type, List<Element> elements) {
		this.type = type;
		this.elements = elements;
	}

	public AdaptedInstrumentableRestriction() {
		this.elements = new ArrayList<>();
	}

	@XmlAttribute
	@XmlJavaTypeAdapter(RestrictionTypeXmlAdapter.class)
	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	@XmlElement(name = "element")
	public List<Element> getElements() {
		return elements;
	}

	public void setElements(List<Element> elements) {
		this.elements = elements;
	}

	public void addElement(Element element) {
		this.elements.add(element);
	}

	public void addElement(String name, Class<?> type, String content) {
		addElement(new Element(name, type, content));
	}

	public boolean hasValue(String name, Class<?> type) {
		for (Element e : elements) {
			if (e.name.equals(name) && e.type.equals(type)) {
				return true;
			}
		}

		return false;
	}

	public String getValue(String name, Class<?> type) {
		for (Element e : elements) {
			if (e.name.equals(name) && e.type.equals(type)) {
				return e.content;
			}
		}

		throw new NoSuchElementException("No element with name \"" + name + "\" and type \"" + type + "\" found.");
	}

	public static class Element {

		private String name;
		private Class<?> type;
		private String content;

		public Element(String name, Class<?> type, String content) {
			this.name = name;
			this.type = type;
			this.content = content;
		}

		public Element() {
		}

		@XmlAttribute
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@XmlAttribute
		public Class<?> getType() {
			return type;
		}

		public void setType(Class<?> type) {
			this.type = type;
		}

		@XmlValue
		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}
	}
}
