/* NOTICE: This file has been changed by Plutext Pty Ltd for use with docx4j.
 * The package name has been changed; there may also be other changes.
 * 
 * This notice is included to meet the condition in clause 4(b) of the License. 
 */
 
 /* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

/* ====================================================================
   This product contains an ASLv2 licensed version of the OOXML signer
   package from the eID Applet project
   http://code.google.com/p/eid-applet/source/browse/trunk/README.txt  
   Copyright (C) 2008-2014 FedICT.
   ================================================================= */ 

package org.docx4j.dsig.crypt.facets;

import java.io.ByteArrayInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Manifest;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureProperties;
import javax.xml.crypto.dsig.SignatureProperty;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignatureException;

import org.docx4j.XmlUtils;
//import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.org.apache.xml.security.Init;
import org.docx4j.org.apache.xml.security.c14n.Canonicalizer;
import org.docx4j.relationships.Relationship;
import org.docx4j.vml.officedrawing.CTSignatureLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.docx4j.com.microsoft.schemas.office.x2006.digsig.CTSignatureInfoV1;
import org.docx4j.dsig.crypt.DSigJAXBContext;
import org.docx4j.dsig.crypt.SignatureDetail;
import org.docx4j.dsig.crypt.services.RelationshipTransformService;
import org.docx4j.dsig.crypt.services.RelationshipTransformService.RelationshipTransformParameterSpec;
import org.docx4j.dsig.DigitalSignatureException;
import org.docx4j.org.openxmlformats.schemas.xpackage.x2006.digitalSignature.CTSignatureTime;


/**
 * Office OpenXML Signature Facet implementation.
 * 
 * @author fcorneli
 * @see <a href="http://msdn.microsoft.com/en-us/library/cc313071.aspx">[MS-OFFCRYPTO]: Office Document Cryptography Structure</a>
 */
public class OOXMLSignatureFacet extends SignatureFacet {

	private static Logger LOG = LoggerFactory.getLogger(OOXMLSignatureFacet.class);	
	
	/* responsible for both idPackageObject AND idOfficeObject:
	 * 
		  <SignedInfo>

		    <Reference URI="#idPackageObject" Type="http://www.w3.org/2000/09/xmldsig#Object">
		      <DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
		      <DigestValue>eF9Oon7/a5sSQTLOAn8YJvQ8cVg=</DigestValue>
		    </Reference>
		    <Reference URI="#idOfficeObject" Type="http://www.w3.org/2000/09/xmldsig#Object">
		      <DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
		      <DigestValue>brcuqkizCZNXjffaALjau7M1I5M=</DigestValue>
		    </Reference>
		    
    
    
		  <Object Id="idPackageObject" xmlns:mdssi="http://schemas.openxmlformats.org/package/2006/digital-signature">
		    <Manifest>
		      <Reference URI="/word/styles.xml?ContentType=application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml">
		        <DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
		        <DigestValue>Z3yNQj0/Ivk+tD6ZAbGRRVzs9/M=</DigestValue>
		      </Reference>
		      :
		    </Manifest>
		    <SignatureProperties>
		      <SignatureProperty Id="idSignatureTime" Target="#idPackageSignature">
		        <mdssi:SignatureTime>
		          <mdssi:Format>YYYY-MM-DDThh:mm:ssTZD</mdssi:Format>
		          <mdssi:Value>2016-03-03T06:07:19Z</mdssi:Value>
		        </mdssi:SignatureTime>
		      </SignatureProperty>
		    </SignatureProperties>
		  </Object>

		  <Object Id="idOfficeObject">
		    <SignatureProperties>
		      <SignatureProperty Id="idOfficeV1Details" Target="#idPackageSignature">
		        <SignatureInfoV1 xmlns="http://schemas.microsoft.com/office/2006/digsig">
		          <SetupID>{32344BBF-7D11-45C5-AC43-F2DA145CBEFB}</SetupID>
		          <SignatureText/>
		          <SignatureImage>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9evYsmULXnvtNdq8eTMMw0C9XqdarYbOzk6qVqvIZDIol8sENK6W9Xqdjh49ip6eHv/i3/p2tJE84JW4SKfTpGkaz87OorOzk1RVZVcLhEZNA03TyHEcPnLkCCzLIsuy/Nc2RVG4WCySEIITiQSklJRIJFhKiUQiQYODgxyPx+G+XDf97WjDGvDMIZFI4OzZs6ua2ErfADAwMLDiuJcf1kIjCGbDtVAQTyA5Nak5wFCwjln2tOilkuDa4Li3NgBm3QCkbdvVWq22TByrSXMtYxsZN02zDqCyLgDMbBiG8fhzzz33vwC0tez5IFqglCYppZBSEjNPbt261X+EXtM/ewDApUuXBABlFTP6f2veHcNNhsTMkojq586dYwD4P47/ovUsIk5mAAAAJXRFWHRkYXRlOmNyZWF0ZQAyMDEwLTAyLTExVDEzOjI1OjU1LTA2OjAwUGlh0wAAACV0RVh0ZGF0ZTptb2RpZnkAMjAwNy0wNS0zMVQxNzoxNzoxNC0wNTowMCy6YH0AAAAASUVORK5CYIIIQAEIJAAAABgAAAACEMDbAQAAAAMAAAAAAAAAAAAAAAAAAAAbQAAAQAAAADQAAAABAAAAAgAAAAAAAL8AAAC/AABAQgAAQEIDAAAAAAAAswAAALP//z9CAAAAswAAALP//z9CIQAAAAgAAABiAAAADAAAAAEAAAAVAAAADAAAAAQAAAAVAAAADAAAAAQAAABGAAAAFAAAAAgAAABUTlBQBgEAAFEAAAAAAgAAAAAAAAAAAAAvAAAALwAAAAAAAAAAAAAAAAAAAAAAAAAwAAAAMAAAAFAAAAAwAAAAgAAAAIABAAAAAAAAhgDuADAAAAAwAAAAKAAAADAAAAAwAAAAAQABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD///8AAAAAAAAAAAAAKqqqqqgAAAAf////+ABDAD/////4bEIAH/////AcQgA/////+BBCAB/////4ZEIAP/////hsQgAf////eGRCAD/////4bEIAH/////hkQgA/////+GxCAB/////wZEIAP/////goQgAf////+CBCAD/////4KEIAH3////ggQgA/////+ChCAB/////4IEIAP/////goQgAf////8CBCAD/////4KEIAH/////ggQgA/////+ChCAB/////4IEIAP/////goQgAf////+CBCAD/////4KEIAH/////AgQgA/////+ChCAB/////4IEIKv/////goQhd/////+CBCP//////4KEIf//////wgQj//////+ChCH//////wIEI///////goQh//////8CBCP//////gKEIff////0AgQj//////gChCH/////8AIEI//////gAoQgAf///8ACBCAD////gAKEIAFVVVUAAgQgAIiIiAAChCUQAAAHgSAAAAAAAAAAAAAC8AAAAvAAAAAAAAAAAAAAAAAAAAAAAAADAAAAAwAAAAUAAAACgAAAB4AAAAABIAAAAAAADGAIgAMAAAADAAAAAoAAAAMAAAADAAAAABABAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA/3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3+1Vv9/c07/f5RS/39zTv9/lFL/f3NO/3+UUv9/c07/f5RS/39zTv9/lFL/f3NO/3+UUv9/c07/f5RS/39zTv9/c07/f3NO/3//f/9//3//f/9//3//f/9//3//f/9//3//f5xznHOcc5xznHOcc5xznHOcc5xznHOcc5xznHOcc3tvnHOcc5xze2+cc3tvnHN7b5xze2+cc3tvnHN7b5xze2+ccxhj/3//f/9//3//f/9//3//f/9//3//f/9//3/3Xr133nu9d957vXfee7133nu9d957vXe9d713vXe9d713vXe9d713vXecc713vXe9d5xzvXecc713nHO9d713vXecc1pr/3//f/9//3//f/9//3//f/9//3//f/9//3//f957vXe9d713vXe9d713vXe9d713vXe9d713vXe9d713vXecc713nHO9d5xzvXecc713nHO9d5xzvXecc713nHO9d/9//3//f/9//3//f/9//3//f/9//3//f/9//3/3Xr133nu9d5xzc06UUjFGlFKUUrVWtVa1VpRStVa1VjlnGGO1VpRStVa1VnNOc06UUnNOlFJzTtZaWmu9d5xzvXecc1pr/3//f/9//3//f/9//3//f/9//3//f/9//3//f957vXfee3tvMUZSSlJKMUYQQjFGMUYQQhBCEELWWjFGtVbOOVJKEEIQQu89MUbvPXNOMUYxRlJKUko5Z713nHO9dzln/3//f/9//3//f/9//3//f/9//3//f/9//3/3Xr133nu9d957nHO9d3tvnHOcc713nHO9d5xznHOcc5xznHO9d5xznHN7b5xze2+cc3tvnHOcc713e2+cc713vXecc1pr/3//f/9//3//f/9//3//f/9//3//f/9//3//f957vXfee3tvlFLWWpRS1lrWWtZa916UUlprvXe9d713vXe9d713vXe9d713vXe9d713nHO9d5xzvXf/f713nHO9dzln/3//f/9//3//f/9//3//f/9//3//f/9//3/3Xt573nvee5xzc05zTjFGUkoxRrVWMUZSSpRSMUaUUnNOtVaUUrVWUkqUUjFGMUZSSlJK916cc713nHO9d713vXe9d1pr/3//f/9//3//f/9//3//f/9//3//f/9//3//f957vXfee1prtVa1VrVWc06UUjFGc05SSnNOMUa1VlJKUkoxRvdeEEK1VjFGtVYxRvdec05SSlJKlFL3Xr13nHO9dzln/3//f/9//3//f/9//3//f/9//3//f/9//3/3Xr133nvee9573nvee9573nu9d957vXfee7133nu9d957vXfee7133nu9d957vXe9d7133nu9d713vXfee713vXe9d1pr/3//f/9//3//f/9//3//f/9//3//f/9//3//f957vXfee7133nu9d957vXfee7133nu9d957vXfee7133nu9d713vXe9d713vXe9d713vXe9d713vXe9d713vXe9d/9//3//f/9//3//f/9//3//f/9//3//f/9//3/3Xt573nvee9573nvee5xz3nvee9573nvee713nHPXWnVOM0JURpRO9145Z5xzvXfee713vXecc957vXe9d7133nu9d1pr/3//f/9//3//f/9//3//f/9//3//f/9//3//f9573nvee7133nu9d1prnHPee7133nu9dxljkS3KHUoebyb1KVcudjaVTtZaOWecc713e29aa713vXe9d713vXe9dzln/3//f/9//3//f/9//3//f/9//3//f/9//3/3Xt57/3/ee9573nt7b1prvXfee9573ns7Y1IlkCFqHmgaZxarHhUmWC54LthSWmu9d713nHM5Z3tvvXfee7133nu9d1pr/3//f/9//3//f/9//3//f/9//3//f/9//3//f9573nvee957vXf/f3tv3nvee713XGtSIVIh5hHDCcIFBAokCisWlB3VIfUpXGe9d957vXdaazlnvXe9d713vXe9dzln/3//f/9//3//f/9//3//f/9//3//f/9//3/3Xt57/3/ee957Wmtaa957/3/ee957tC3yGKsVogXiBSMKCBJyGTMZNBkzGVMd1S3ee7133nu9d1prWmvee7133nu9d1pr/3//f/9//3//f/9//3//f/9//3//f/9//3//f9573nvee3tvWmucc9573nveeztj0BCxEFAVIgplEu4ZdR0WGRYV9BQUFRIVMRn6Wt57vXfee3tvOWd7b957vXe9d1pr/3//f/9//3//f/9//3//f/9//3//f/9//3/3Xt57/3+9d1pre2//f957/3/ee3dKjgwRFXQZ1RnXGfkd2B3YIXcdFhn0FJAZqxWWRt573nvee957Wmtaa7133nu9d1pr/3//f/9//3//f/9//3//f/9//3//f/9//3//f/9/3nt7b1pr3nvee9573nvee/Q5rgyrEbcZNx56Hnoeex45Hvkhdx0WGYgmhB6uNt57vXfee713vXc5Z3tvvXfee/9//3//f/9//3//f/9//3//f/9//3//f/9//3/3Xv9//397b1prvXf/f957/3/eezY+iQ1zGTce3B4cIz0r/Ca8Jlom+SVXHStDJzcxQ9573nvee957nHNaa3tv3nu9d1pr/3//f/9//3//f/9//3//f/9//3//f/9//3//f/9/3nvee1pre2/ee/9/3nvee7hOyRErFs4iWyu/O747njccK7sqGSqYIc06SENVU957vXfee713Wmtaa957vXfee1pr/3//f/9//3//f/9//3//f/9//3//f/9//3/3Xv9//3//f713Wmu9d957/3/ee51zChYlGyYjsU/+V/9f30d+M9suWi7XJbE66zqbb9573nvee5xzOWe9d9573nu9d1pr/3//f/9//3//f/9//3//f/9//3//f/9//3//f/9/3nv/f957e29aa9573nv/f957tEJGH0crclP4b91j/09+M9wyWS42LlEyFjree9573nu9dzlne2+9d957vXfee1pr/3//f/9//3//f/9//3//f/9//3//f/9//3/3Xv9//3//f/9/3ntaa3tv/3//f/9/vnfxMicnbT+WV9hnvzt+N9wyejKyMm9HvXP/f9573nt7b1prvXfee9573nvee1pr/3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f957/3+cc1prvXf/f957/3++d1E/Ki9TP3VPfFM8S9syuTb3Qrxz3nvee957vXdaa5xz3nvee957vXfee1pr/3//f/9//3//f/9//3//f/9//3//f/9//3/3Xv9//3//f/9//3//f3tv/3//f/9//3//f957u2dYW1hXWV8bT9tGm2fee/9/3nv/f9573nt7b9573nv/f9573nvee3tv/3//f/9//3//f/9//3//f/9//3//f/9//3//f/9/3nv/f957/3/ee/9//3//f957/3/ee/9/3nv/f957/3vee/9/3nv/f9573nvee/9/3nvee9573nvee9573nvee/9//3//f/9//3//f/9//3//f/9//3//f/9//3/3Xv9//3//f/9//3//f/9//3//f/9//3//f/9//3/ee/9//3//f957/3/ee/9/3nv/f957/3/ee/9/3nv/f957/3/ee3tv/3//f/9//3//f/9//3//f/9//3//f/9//3//f/9/3nv/f957/3//f/9/3nv/f/9//3/ee/9/3nv/f957/3/ee/9/3nv/f957/3/ee/9/3nv/f957/3/ee/9/3nv/f1pr/3//f/9//3//f/9//38hBP9/IQT/fyEE/38xRhhjOWc5ZzlnOWc5ZzlnOWc5ZzlnOWc5ZzlnOWc5ZzlnOWc5ZzlnOWc5Zzlne2/ee957/3//f/9/3nv/f/9//3/ee3tv/3//f/9//3//f/9/5Bj/f+QY5BgEGf9/BB1GIYgpZymIKYgpiC2HKYgpiCmoLYcpqC2IKagtiCmoLagpqC2oKagtqC2oLagtUUacc/9/3nv/f/9//3/ee/9//3//f1pr/3//f/9//3//f8QYwRDhEOEQARXhEAEV4RQCFQEVIhUBFSIZIRUiGSEVQhlBGUIZQRliHUEZYh1BGWIdYhliHWEZYh1iGWIdyzWcc/9//3//f/9//3//f/9//3/ee1pr/3//f/9//3//f/9/4RDBEOEQ4RDhEAIVhykBEQEVRB2GJQEVxy0hFUMdhCFCGYUlZCFBGcYp6C3oMaUlYhlBGWIdQRliHWEZ6zV7b/9//3//f/9//3//f/9/3nvee1pr917/f/9//3//f8QYwRDhEOEQARXhECIZem/qMQEVeWsLOiIZ3XdCGW9GeWtBGRVfCjZiGVdn81aPRgo2QRliHWIZYh1iGWIdyzWcc/9//3//f/9//3//f9573nu9d1pr/3//f/9//3//f/9/4RDBEOEQ4RABFeEQqC16a05CFl8hFQEV3XsiGZtzeWvILfRaCjZBGVhnyC1CGUEZYh1BGWIdQRliHUEZ6zV7b/9//3//f/9//3/ee957vXecc/9//3//f/9//3//f8QYwRDhEOEQAhXhEAEV4RTTVv5/hSUBFSIZ3XcsPvVaKzoUWxVfCjZiHVdn6DFBGWIdQhliHWEZYh1iGWIdyzWcc/9/nHNaa3tve2+cc1prOWcYY9Za/3//f/9//3//f/9/4RDBEOEQ4RDhEOEQAhU3Y713yTEhFQEV3XtYZ8gtQhmab1hnCjZBGVhn6C1iGUEZYhlBGWIdQRliHWEZ7Dl7b/9/917WWtZa1lrWWtZaGGPWWv9//3//f/9//3//f8QYwRDhEOEQ4RThEAIVCzpYZ4cpm3MiGSIZ/395ayEZQhluRv9/CjZiGVdn6DFBGWIdQRliHWIZYh1hGWId6zWcc/9/GGP/f/9//3+9d3tv1lr/f/9//3//f/9//3//f/9/4RDBEOEQ4RDhEP9/WGeHKQEVkU5OQgEVvHMLOiEZIRVjIZpv6TFBGRVfxy1CGUEZYh1BGWIdQRliHUEZ6zV7b/9/917/f/9/vXf/f9Za/3//f/9//3//f/9//3//f8QYwRDhEOEQARXhEAEV4RACFQEVIhUBFSIZIRUiGSEVQhlBGUIZQRliHUIZYh1BGWIdYhliHWEZYh1iGWIdyzWcc957917ee713e2/WWv9//3//f/9//3//f/9//3//f/9/wRDBEOEQ4RDhEOEQARUBEQEVARUhFQEVIRkhFSIZIRVCGSEZQhlBGUIZQRlCGUEZYhlBGWIdQRliHWEZDDp7b9571lqcc3tv1lr/f/9//3//f/9//3//f/9//3//fycl5BjkGOQU5BjkGOQYJiGqMQ0+LT4NPi0+DT4tQi0+LUItPi1CLT4tQi0+TUItPk1CLT5OQi1CTUItPk5CF2Pee713916cc9Za/3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f9573nu9d5xzGGPWWv9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3/3Xv9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f957vXe9d1prtVb/f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/de/3/3Xv9/917/f/de/3/3Xv9/917/f/de/3/3Xv9/917/f/de/3/3Xv9/917/f9Za/3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9//3//f/9/917/f/9//3/3Xv9//3//f/de/3//f/9/917/f/9//3/3Xv9//3//f/de/3//f/9//3//f/9//3//f/9//3//f/9//3//f/9/RgAAABQAAAAIAAAAVE5QUAcBAABMAAAAZAAAAAAAAAAAAAAALwAAAC8AAAAAAAAAAAAAADAAAAAwAAAAKQCqAAAAAAAAAAAAAACAPwAAAAAAAAAAAACAPwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIgAAAAwAAAD/////RgAAABwAAAAQAAAARU1GKwJAAAAMAAAAAAAAAA4AAAAUAAAAAAAAABAAAAAUAAAA</SignatureImage>
		          <SignatureComments/>
		          <WindowsVersion>6.1</WindowsVersion>
		          <OfficeVersion>14.0</OfficeVersion>
		          <ApplicationVersion>14.0</ApplicationVersion>
		          <Monitors>2</Monitors>
		          <HorizontalResolution>1920</HorizontalResolution>
		          <VerticalResolution>1080</VerticalResolution>
		          <ColorDepth>32</ColorDepth>
		          <SignatureProviderId>{00000000-0000-0000-0000-000000000000}</SignatureProviderId>
		          <SignatureProviderUrl/>
		          <SignatureProviderDetails>9</SignatureProviderDetails>
		          <ManifestHashAlgorithm>http://www.w3.org/2000/09/xmldsig#sha1</ManifestHashAlgorithm>
		          <SignatureType>2</SignatureType>
		        </SignatureInfoV1>
		      </SignatureProperty>
		    </SignatureProperties>
		  </Object>    
    	 * 
    	 * 
	 */
	
    @Override
    public void preSign(Document document, List<Reference> references, List<XMLObject> objects) throws XMLSignatureException, DigitalSignatureException {
    	
        LOG.debug( "pre sign");
        addPackageObject(document, references, objects);
        addSignatureInfo(document, references, objects);
    }

	protected void addPackageObject(Document document,
			List<Reference> references, List<XMLObject> objects)
			throws XMLSignatureException, DigitalSignatureException {

        List<Reference> manifestReferences = new ArrayList<Reference>();
        addManifestReferences(manifestReferences);
        Manifest manifest =  getSignatureFactory().newManifest(manifestReferences);
        
        String objectId = "idPackageObject"; 
        List<XMLStructure> objectContent = new ArrayList<XMLStructure>();
        objectContent.add(manifest);

        addSignatureTime(document, objectContent);

        XMLObject xo = getSignatureFactory().newXMLObject(objectContent, objectId, null, null);
        objects.add(xo);
        
        String foo = CanonicalizationMethod.EXCLUSIVE;  // so this is easy to find
//        List<Transform> transforms = new ArrayList<Transform>();
//        transforms.add(newTransform(CanonicalizationMethod.EXCLUSIVE));        
//        Reference reference = newReference("#" + objectId, transforms, XML_DIGSIG_NS+"Object", null, null);

        Reference reference = newReference("#" + objectId, null, XML_DIGSIG_NS+"Object", null, null);
        references.add(reference);
    }
    
    protected void addManifestReferences(List<Reference> manifestReferences) throws XMLSignatureException, DigitalSignatureException {

        OpcPackage pkg = signatureConfig.getOpcPackage();
        
        digestedPartNames = new HashSet<String>();
        
        // We're different to POI here, since our parts collections doesn't contain rels parts.
        // So we take a recursive approach
		addManifestReferences( pkg.getRelationshipsPart(), manifestReferences);		
    }
    

    private Set<String> digestedPartNames = null;
    
    private void addManifestReferences(RelationshipsPart rp, List<Reference> manifestReferences)
    throws XMLSignatureException, DigitalSignatureException {
    	
    	
    	// Handle each target part
        RelationshipTransformParameterSpec parameterSpec = new RelationshipTransformParameterSpec();
        for (Relationship relationship : rp.getJaxbElement().getRelationship()) {
            String relationshipType = relationship.getType();
            
            if (!isSignedRelationship(relationshipType)) continue;

            // Office signs every relationship it doesn't exclude, a hyperlink's included
            parameterSpec.addRelationshipReference(relationship.getId());
            
            /*
             * ECMA-376 Part 2 - 3rd edition
             * 13.2.4.16 Manifest Element
             * "The producer shall not create a Manifest element that references any data outside of the package."
             */
            if (relationship.getTargetMode() !=null
            		&& relationship.getTargetMode().equals("External")) {
            	
                continue;
            }
            
            Part p = rp.getPart(relationship);
            if (p==null) {
            	// Better to fail than to leave it out, which Office would report as a partial signature 
            	throw new DigitalSignatureException("Can't sign: " + rp.getPartName().getName() + " relationship " + relationship.getId() 
            			+ " (" + relationshipType + ") has target " + relationship.getTarget() + ", but the package has no such part");
            }

            String partName = p.getPartName().getName();
            
            String contentType = p.getContentType();
            
            // (POI signs a customXml part only if its content type is text/xml, 
            //  which leaves a signed relationship pointing at an unsigned part.
            //  Word calls that signature invalid.  So no exception for customXml.)
            
            if (this.signatureConfig.getPartnameBlackList()!=null
            		&& this.signatureConfig.getPartnameBlackList().contains(partName)) {
            	
               /* you can skip images (relationshipType.equals(Namespaces.IMAGE)) 
                * in which case you can alter their content without invalidating the signatures (!),
                * but Word will warn its a "valid partial signature"  
                */            	
                LOG.debug( "skipping part: " + partName);
                continue;            	
            } 
            
            
            if (!digestedPartNames.contains(partName)) {
                // We only digest a part once.
                String uri = partName + "?ContentType=" + contentType;
                
//                List<Transform> transforms = new ArrayList<Transform>();
//                transforms.add(newTransform(CanonicalizationMethod.EXCLUSIVE));                
//                Reference reference = newReference(uri, transforms, null, null, null);
                
                Reference reference = newReference(uri, null, null, null, null);
                
                manifestReferences.add(reference);
                digestedPartNames.add(partName);
                
                LOG.debug(uri + " added to manifestReferences and digestedPartNames");
                
                // Recurse
                if (p.getRelationshipsPart()!=null) {
            		addManifestReferences( p.getRelationshipsPart(), manifestReferences);            	
                }
                
            }
            
        }
        
        // Handle the rels part itself
        if (parameterSpec.hasSourceIds()) {
            List<Transform> transforms = new ArrayList<Transform>();
            transforms.add(newTransform(RelationshipTransformService.TRANSFORM_URI, parameterSpec));
            transforms.add(newTransform(CanonicalizationMethod.INCLUSIVE));
//            transforms.add(newTransform(CanonicalizationMethod.EXCLUSIVE));
            String uri = rp.getPartName().getName()
                + "?ContentType=application/vnd.openxmlformats-package.relationships+xml";
            Reference reference = newReference(uri, transforms, null, null, null);
            manifestReferences.add(reference);
            LOG.debug(uri + " added to manifestReferences only");
        }
    	
    }
    



    protected void addSignatureTime(Document document, List<XMLStructure> objectContent) {
        /*
         * SignatureTime
         */
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        String nowStr = fmt.format(signatureConfig.getExecutionTime());
        LOG.debug( "now: " + nowStr);

        //SignatureTimeDocument sigTime = SignatureTimeDocument.Factory.newInstance();
        //CTSignatureTime ctTime = sigTime.addNewSignatureTime();
        CTSignatureTime ctTime = new CTSignatureTime(); 
        ctTime.setFormat("YYYY-MM-DDThh:mm:ssTZD");
        ctTime.setValue(nowStr);

        //Element n = (Element)document.importNode(ctTime.getDomNode(),true);
        
        //Node child = XmlUtils.marshaltoW3CDomDocument(ctTime, Context.jcXmlDSig).getDocumentElement();
        
        // Get rid of the superfluous namespaces
        Node child = null;
        try {
			Document signatureTimeDoc = XmlUtils.marshaltoW3CDomDocument(ctTime, DSigJAXBContext.jcXmlDSig);
			LOG.debug("Input to Canonicalizer: " + XmlUtils.w3CDomNodeToString(signatureTimeDoc));
			
			Init.init();
			Canonicalizer c = Canonicalizer.getInstance(CanonicalizationMethod.EXCLUSIVE);
			byte[] bytes = c.canonicalizeSubtree(signatureTimeDoc);
			Document myDoc = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(bytes));
	//		System.out.println("\n\n!! " + XmlUtils.w3CDomNodeToString(myDoc));
			child = myDoc.getDocumentElement();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        
        Element n = (Element)document.importNode(child, true);
        
        
        List<XMLStructure> signatureTimeContent = new ArrayList<XMLStructure>();
        signatureTimeContent.add(new DOMStructure(n));
        SignatureProperty signatureTimeSignatureProperty = getSignatureFactory()
            .newSignatureProperty(signatureTimeContent, "#" + signatureConfig.getPackageSignatureId(),
            "idSignatureTime");
        List<SignatureProperty> signaturePropertyContent = new ArrayList<SignatureProperty>();
        signaturePropertyContent.add(signatureTimeSignatureProperty);
        SignatureProperties signatureProperties = getSignatureFactory()
            .newSignatureProperties(signaturePropertyContent,
            "id-signature-time-" + signatureConfig.getExecutionTime());
        objectContent.add(signatureProperties);
    }

    protected void addSignatureInfo(Document document,
        List<Reference> references,
        List<XMLObject> objects)
    throws XMLSignatureException, DigitalSignatureException {
        List<XMLStructure> objectContent = new ArrayList<XMLStructure>();

        //SignatureInfoV1Document sigV1 = SignatureInfoV1Document.Factory.newInstance();
        
        //CTSignatureInfoV1 ctSigV1 = sigV1.addNewSignatureInfoV1();
        CTSignatureInfoV1 ctSigV1 = new CTSignatureInfoV1(); 
        ctSigV1.setManifestHashAlgorithm(signatureConfig.getDigestMethodUri());
        
        SignatureDetail signatureDetail = signatureConfig.getCurrentSignatureDetail();
        CTSignatureLine signatureLine = signatureDetail.getSignatureLine();
        if (signatureLine==null) {
        	
        	ctSigV1.setSignatureType(1);
        	
        } else {

        	ctSigV1.setSignatureType(2);
        	
        	ctSigV1.setSetupID(signatureLine.getId());
        	
        	ctSigV1.setSignatureProviderId(signatureLine.getProvid());
        	ctSigV1.setSignatureProviderUrl(signatureLine.getSigprovurl());
        	
        	ctSigV1.setSignatureProviderDetails(9);
        }
        ctSigV1.setSignatureComments(signatureDetail.getSignatureComments());
        ctSigV1.setSignatureText(signatureDetail.getSignatureText());
        ctSigV1.setSignatureImage(signatureDetail.getSignatureImage());
        
        //Node child = XmlUtils.marshaltoW3CDomDocument(ctSigV1, Context.jcXmlDSig).getDocumentElement();
        
        // Get rid of the superfluous namespaces; done already in XAdESSignatureFacet
        Node child = null;
        try {
			Document sigv1TmpDoc = XmlUtils.marshaltoW3CDomDocument(ctSigV1, DSigJAXBContext.jcXmlDSig);
			LOG.debug("Input to Canonicalizer: " + XmlUtils.w3CDomNodeToString(sigv1TmpDoc));
			
			Init.init();
			Canonicalizer c = Canonicalizer.getInstance(CanonicalizationMethod.EXCLUSIVE);
			byte[] bytes = c.canonicalizeSubtree(sigv1TmpDoc);
			Document myDoc = XmlUtils.getNewDocumentBuilder().parse(new ByteArrayInputStream(bytes));
	//		System.out.println("\n\n!! " + XmlUtils.w3CDomNodeToString(myDoc));
			child = myDoc.getDocumentElement();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        
        Element n = (Element)document.importNode(child, true);        
        //n.setAttributeNS(XML_NS, XMLConstants.XMLNS_ATTRIBUTE, MS_DIGSIG_NS);
        
        List<XMLStructure> signatureInfoContent = new ArrayList<XMLStructure>();
        signatureInfoContent.add(new DOMStructure(n));
        SignatureProperty signatureInfoSignatureProperty = getSignatureFactory()
            .newSignatureProperty(signatureInfoContent, "#" + signatureConfig.getPackageSignatureId(),
            "idOfficeV1Details");

        List<SignatureProperty> signaturePropertyContent = new ArrayList<SignatureProperty>();
        signaturePropertyContent.add(signatureInfoSignatureProperty);
        SignatureProperties signatureProperties = getSignatureFactory()
            .newSignatureProperties(signaturePropertyContent, null);
        objectContent.add(signatureProperties);

        String objectId = "idOfficeObject";
        objects.add(getSignatureFactory().newXMLObject(objectContent, objectId, null, null));
        
//        List<Transform> transforms = new ArrayList<Transform>();
//        transforms.add(newTransform(CanonicalizationMethod.EXCLUSIVE));        
//        Reference reference = newReference("#" + objectId, transforms, XML_DIGSIG_NS+"Object", null, null);

        Reference reference = newReference("#" + objectId, null, XML_DIGSIG_NS+"Object", null, null);
        references.add(reference);
    }

    protected static String getRelationshipReferenceURI(String zipEntryName) {
        return "/"
            + zipEntryName
            + "?ContentType=application/vnd.openxmlformats-package.relationships+xml";
    }

    protected static String getResourceReferenceURI(String resourceName, String contentType) {
        return "/" + resourceName + "?ContentType=" + contentType;
    }

    /**
     * Office signs every part, and every relationship, except the document properties, 
     * the thumbnail, and the signatures themselves.  (Until 2026 this was a list of the 
     * types to sign, taken from POI; it had fallen behind Office, which then reported 
     * a signature as partial or invalid.  See CR-002 section 6.)
     */
    public static boolean isSignedRelationship(String relationshipType) {
        LOG.debug("relationship type: " + relationshipType);
        for (String unsignedTypeExtension : unsigned) {
            if (relationshipType.endsWith(unsignedTypeExtension)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * The relationship types (their endings, so that the transitional and strict 
     * forms both match) which Office leaves out of a signature.
     */
    public static final String[] unsigned = {
        "/metadata/core-properties", //
        "/extended-properties", //
        "/extendedProperties", // strict
        "/custom-properties", //
        "/customProperties", // strict
        "/metadata/thumbnail", //
        "/digital-signature/origin", //
        "/digital-signature/signature", //
        "/digital-signature/certificate" //
    };

    public static final String[] contentTypes = {
        /*
         * Word
         */
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.fontTable+xml",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.settings+xml",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml",
        "application/vnd.openxmlformats-officedocument.theme+xml",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.webSettings+xml",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml",

        /*
         * Word 2010
         */
        "application/vnd.ms-word.stylesWithEffects+xml",

        /*
         * Excel
         */
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml",

        /*
         * Powerpoint
         */
        "application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml",
        "application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml",
        "application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml",
        "application/vnd.openxmlformats-officedocument.presentationml.slide+xml",
        "application/vnd.openxmlformats-officedocument.presentationml.tableStyles+xml",

        /*
         * Powerpoint 2010
         */
        "application/vnd.openxmlformats-officedocument.presentationml.viewProps+xml",
        "application/vnd.openxmlformats-officedocument.presentationml.presProps+xml"
    };

}