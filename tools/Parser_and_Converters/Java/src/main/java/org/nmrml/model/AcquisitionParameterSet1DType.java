//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.01.24 at 10:31:51 PM GMT 
//


package org.nmrml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AcquisitionParameterSet1DType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AcquisitionParameterSet1DType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://nmrml.org/schema}AcquisitionParameterSetType">
 *       &lt;sequence>
 *         &lt;element name="DirectDimensionParameterSet" type="{http://nmrml.org/schema}AcquisitionDimensionParameterSetType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AcquisitionParameterSet1DType", propOrder = {
    "directDimensionParameterSet"
})
public class AcquisitionParameterSet1DType
    extends AcquisitionParameterSetType
{

    @XmlElement(name = "DirectDimensionParameterSet", required = true)
    protected AcquisitionDimensionParameterSetType directDimensionParameterSet;

    /**
     * Gets the value of the directDimensionParameterSet property.
     * 
     * @return
     *     possible object is
     *     {@link AcquisitionDimensionParameterSetType }
     *     
     */
    public AcquisitionDimensionParameterSetType getDirectDimensionParameterSet() {
        return directDimensionParameterSet;
    }

    /**
     * Sets the value of the directDimensionParameterSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link AcquisitionDimensionParameterSetType }
     *     
     */
    public void setDirectDimensionParameterSet(AcquisitionDimensionParameterSetType value) {
        this.directDimensionParameterSet = value;
    }

}
