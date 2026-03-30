package org.webswing.toolkit.util;

public class EvaluationProperties {

  private boolean enabled;
  private String mainText;
  private String linkText;
  private String linkUrl;
  private long timeout;
  private String dismissText;
  private int height;

  public EvaluationProperties(boolean enabled, String mainText, String linkText, String linkUrl,
      long timeout, String dismissText, int height) {
    super();
    this.enabled = enabled;
    this.mainText = mainText;
    this.linkText = linkText;
    this.linkUrl = linkUrl;
    this.timeout = timeout;
    this.dismissText = dismissText;
    this.height = height;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public String getMainText() {
    return mainText;
  }

  public String getLinkText() {
    return linkText;
  }

  public String getLinkUrl() {
    return linkUrl;
  }

  public long getTimeout() {
    return timeout;
  }

  public String getDismissText() {
    return dismissText;
  }

  public int getHeight() {
    return height;
  }

}
