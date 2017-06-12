package com.example.bogdi.bogdan;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class CallSoap {
    private final String SOAP_ACTION = "http://tempuri.org/Add";

    private final String OPERATION_NAME = "Add";

    private final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";

    private final String SOAP_ADDRESS = "http://192.168.43.158/SignCard.asmx";

    private final String SOAP_ACTION2 = "http://tempuri.org/Sign";

    private final String OPERATION_NAME2 = "Sign";

    public String CallValidate(String card) {
        SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME);
        PropertyInfo pi = new PropertyInfo();
        pi.setName("cardData");
        pi.setValue(card);
        pi.setType(String.class);
        request.addProperty(pi);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.implicitTypes = true;
        envelope.setAddAdornments(false);
        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(SOAP_ADDRESS);
        httpTransport.debug = true;
        httpTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");

        Object response = null;
        try {
            httpTransport.call(SOAP_ACTION, envelope);
            response = envelope.getResponse();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return response.toString();
    }

    public String CallSign(String card) {
        SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME2);
        PropertyInfo pi = new PropertyInfo();
        pi.setName("cardData");
        pi.setValue(card);
        pi.setType(String.class);
        request.addProperty(pi);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.implicitTypes = true;
        envelope.setAddAdornments(false);
        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(SOAP_ADDRESS);
        httpTransport.debug = true;
        httpTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");

        Object response = null;
        try {
            httpTransport.call(SOAP_ACTION2, envelope);
            response = envelope.getResponse();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return response.toString();
    }
}
