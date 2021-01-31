package com.plaid.quickstart.resources;

import java.io.IOException;

import com.plaid.client.PlaidClient;
import com.plaid.client.request.ItemPublicTokenExchangeRequest;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import com.plaid.quickstart.QuickstartApplication;

import java.util.Arrays;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import retrofit2.Response;

@Path("/set_access_token")
@Produces(MediaType.APPLICATION_JSON)
public class AccessTokenResource {
  private static final Logger LOG = LoggerFactory.getLogger(AccessTokenResource.class);
  private final PlaidClient plaidClient;

  public AccessTokenResource(PlaidClient plaidClient) {
    this.plaidClient = plaidClient;
  }

  @POST
  public InfoResource.InfoResponse getAccessToken(@FormParam("public_token") String publicToken,
                                                  @FormParam("account_id") String accountId,
                                                  @FormParam("institution_id") String institutionId)
    throws IOException {
    Response<ItemPublicTokenExchangeResponse> itemResponse = plaidClient.service()
      .itemPublicTokenExchange(new ItemPublicTokenExchangeRequest(publicToken))
      .execute();

    // Ideally, we would store this somewhere more persistent
    QuickstartApplication.accessToken = itemResponse.body().getAccessToken();
    QuickstartApplication.itemID = itemResponse.body().getItemId();
    QuickstartApplication.accountId = accountId;
    QuickstartApplication.institutionId = institutionId;
    
    LOG.info("public token: " + publicToken);
    LOG.info("access token: " + QuickstartApplication.accessToken);
    LOG.info("item ID: " + itemResponse.body().getItemId());
    LOG.info("Account ID: " + accountId);

    return new InfoResource.InfoResponse(Arrays.asList(), QuickstartApplication.accessToken,
      QuickstartApplication.itemID, QuickstartApplication.accountId, StringUtils.EMPTY);
  }
}
