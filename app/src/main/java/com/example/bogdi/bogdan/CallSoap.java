package com.example.bogdi.bogdan;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

public class CallSoap {
    public final String SOAP_ACTION = "http://tempuri.org/Add";

    public  final String OPERATION_NAME = "Add";

    public  final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";

    public  final String SOAP_ADDRESS = "http://192.168.43.158/SignCard.asmx";

    public String CallValidate(String card)
    {
        SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE,OPERATION_NAME);
        PropertyInfo pi=new PropertyInfo();
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
        httpTransport.debug=true;
        httpTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");

        Object  response=null;
        //String response=null;
        try
        {
            httpTransport.call(SOAP_ACTION, envelope);
            //response=httpTransport.responseDump;
            response =envelope.getResponse();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        return response.toString();
    }


    public final String SOAP_ACTION2 = "http://tempuri.org/Sign";

    public  final String OPERATION_NAME2 = "Sign";


    public String CallSign(String card)
    {
        SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE,OPERATION_NAME2);
        PropertyInfo pi=new PropertyInfo();
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
        httpTransport.debug=true;
        httpTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");

        Object  response=null;
        //String response=null;
        try
        {
            httpTransport.call(SOAP_ACTION2, envelope);
            //response=httpTransport.responseDump;
            response =envelope.getResponse();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        return response.toString();
    }


    public final String SOAP_ACTION1 = "http://tempuri.org/verifySignature";

    public  final String OPERATION_NAME1 = "verifySignature";

    public String Call(String clearData, String signedData)
    {
        SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE,OPERATION_NAME1);
        PropertyInfo pi=new PropertyInfo();
        pi.setName("clearData");
        pi.setValue(clearData);
        pi.setType(String.class);
        request.addProperty(pi);

        pi=new PropertyInfo();
        pi.setName("signedData");
        pi.setValue(signedData);
        pi.setType(String.class);
        request.addProperty(pi);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.implicitTypes = true;
        envelope.setAddAdornments(false);
        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(SOAP_ADDRESS);
        httpTransport.debug=true;
        httpTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");

        Object  response=null;
        //String response=null;
        try
        {
            httpTransport.call(SOAP_ACTION1, envelope);
            //response=httpTransport.responseDump;
            response =envelope.getResponse();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        return response.toString();
    }
}
