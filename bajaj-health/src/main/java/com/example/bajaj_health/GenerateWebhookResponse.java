package com.example.bajaj_health;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenerateWebhookResponse {
    private String webhook;
    private String accessToken;
    private DataWrapper data;

    public String getWebhook() { return webhook; }
    public void setWebhook(String webhook) { this.webhook = webhook; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public DataWrapper getData() { return data; }
    public void setData(DataWrapper data) { this.data = data; }
}
