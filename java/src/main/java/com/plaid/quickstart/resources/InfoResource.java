package com.plaid.quickstart.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.plaid.quickstart.QuickstartApplication;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/info")
@Produces(MediaType.APPLICATION_JSON)
public class InfoResource {
  private final List<String> plaidProducts;

  public InfoResource(List<String> plaidProducts) {
    this.plaidProducts = plaidProducts;
  }

  public static class InfoResponse {
    @JsonProperty
    private final String itemId;
    @JsonProperty
    private final String accessToken;
    @JsonProperty
    private final String plaidAccountId;
    @JsonProperty
    private final String stripeAccountId;
    @JsonProperty
    private final List<String> products;

    public InfoResponse(List<String> plaidProducts, String accessToken, String itemID, String plaidAccountId, String stripeAccountId) {
      this.products = plaidProducts;
      this.accessToken = accessToken;
      this.itemId = itemID;
      this.plaidAccountId = plaidAccountId;
      this.stripeAccountId = stripeAccountId;
    }
  }

  @POST
  public InfoResponse getInfo() {
    return new InfoResponse(plaidProducts, QuickstartApplication.accessToken,
      QuickstartApplication.itemID, StringUtils.EMPTY, StringUtils.EMPTY);
  }
}
