package com.example.bogdi.bogdan;

public class CreditCard {
    private int cardNumber;
    private String cardName;
    private String expirationDate;
    private int CVC;

    public CreditCard() {
    }

    public CreditCard(int cardNumber, String cardName, String expirationDate, int CVC){
        this.cardNumber=cardNumber;
        this.cardName=cardName;
        this.expirationDate=expirationDate;
        this.CVC=CVC;
    }


    public int getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(int cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public int getCVC() {
        return CVC;
    }

    public void setCVC(int CVC) {
        this.CVC = CVC;
    }
}
