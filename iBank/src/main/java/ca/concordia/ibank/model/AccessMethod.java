package ca.concordia.ibank.model;

/** Ways a client can present credentials at the ABM (simulated in this version). */
public enum AccessMethod {
  CHIP_AND_PIN("access.chip"),
  CONTACTLESS("access.tap"),
  MOBILE_QR("access.qr");

  private final String messageKey;

  AccessMethod(String messageKey) {
    this.messageKey = messageKey;
  }

  public String getMessageKey() {
    return messageKey;
  }
}
