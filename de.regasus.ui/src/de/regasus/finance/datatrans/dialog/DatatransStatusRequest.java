package de.regasus.finance.datatrans.dialog;

import java.io.StringReader;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;

import de.regasus.core.error.RegasusErrorHandler;
import de.regasus.ui.Activator;

public class DatatransStatusRequest {

	private Document document;
	
	private String merchantID;
	private String status;
	private String responseCode;
	private String responseMessage;
	private String refno;
	private String amount;
	private String currency;
	private String authorizationCode;
	private String pmethod;
	private String uppTransactionId;
	private String maskedCC;
	private String aliasCC;
	private String expm;
	private String expy;
	
/*
<?xml version='1.0' encoding='UTF-8'?>
<statusService version='1'>
  <body merchantId='1100001523' testOnly='yes' status='accepted'>
    <transaction trxStatus='response'>
      <request>
        <refno>11</refno>
        <reqtype>STX</reqtype>
      </request>
      <response>
        <responseCode>21</responseCode>
        <responseMessage>Trx already settled</responseMessage>
        <refno>11</refno>
        <amount>1000</amount>
        <currency>CHF</currency>
        <authorizationCode>112373031</authorizationCode>
        <pmethod>VIS</pmethod>
        <uppTransactionId>100903133047263020</uppTransactionId>
        <maskedCC>424242xxxxxx4242</maskedCC>
        <aliasCC>70119122433810042</aliasCC>
        <expm>12</expm>
        <expy>10</expy>
      </response>
    </transaction>
  </body>
</statusService>
*/

	public DatatransStatusRequest(String xmlSource) {
        SAXBuilder builder = new SAXBuilder(XMLReaders.NONVALIDATING);
        StringReader stringReader = new StringReader(xmlSource);
        try {
            document = builder.build(stringReader);
            
            Element rootElement = document.getRootElement();
            Element bodyElement = rootElement.getChild("body");
            Element transactionElement = bodyElement.getChild("transaction");
            Element responseElement = transactionElement.getChild("response");
            
        	merchantID = bodyElement.getAttributeValue("merchantId");
        	status = bodyElement.getAttributeValue("status");
        	responseCode = responseElement.getChildTextNormalize("responseCode");
        	responseMessage = responseElement.getChildTextNormalize("responseMessage");
        	refno = responseElement.getChildTextNormalize("refno");
        	amount = responseElement.getChildTextNormalize("amount");
        	currency = responseElement.getChildTextNormalize("currency");
        	authorizationCode = responseElement.getChildTextNormalize("authorizationCode");
        	pmethod = responseElement.getChildTextNormalize("pmethod");
        	uppTransactionId = responseElement.getChildTextNormalize("uppTransactionId");
        	maskedCC = responseElement.getChildTextNormalize("maskedCC");
        	aliasCC = responseElement.getChildTextNormalize("aliasCC");
        	expm = responseElement.getChildTextNormalize("expm");
        	expy = responseElement.getChildTextNormalize("expy");
        }
        catch (Exception e) {
            RegasusErrorHandler.handleError(Activator.PLUGIN_ID, getClass().getName(), e);
        }
    }
	

	public String getMerchantID() {
		return merchantID;
	}

	public String getStatus() {
		return status;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public String getRefno() {
		return refno;
	}

	public String getAmount() {
		return amount;
	}

	public String getCurrency() {
		return currency;
	}

	public String getAuthorizationCode() {
		return authorizationCode;
	}

	public String getPmethod() {
		return pmethod;
	}

	public String getUppTransactionId() {
		return uppTransactionId;
	}

	public String getMaskedCC() {
		return maskedCC;
	}

	public String getAliasCC() {
		return aliasCC;
	}

	public String getExpm() {
		return expm;
	}

	public String getExpy() {
		return expy;
	}

}
