package com.plaid.quickstart.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.InstitutionsGetByIdRequest;
import com.plaid.client.request.ItemPublicTokenExchangeRequest;
import com.plaid.client.request.ItemStripeTokenCreateRequest;
import com.plaid.client.response.Institution;
import com.plaid.client.response.InstitutionsGetByIdResponse;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import com.plaid.client.response.ItemStripeTokenCreateResponse;
import com.plaid.quickstart.QuickstartApplication;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.BankAccount;
import com.stripe.model.Customer;
import com.stripe.model.PaymentSource;
import com.stripe.param.BankAccountVerifyParams;
import com.stripe.param.CustomerRetrieveParams;
import com.stripe.param.PaymentSourceCollectionCreateParams;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Arrays;

//@Path("/create_stripe_token")
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class PlaidStripeResource {

    private static final Logger LOG = LoggerFactory.getLogger(PlaidStripeResource.class);

    private final String STRIPE_CUSTOMER_ID = "cus_IcG5OViY84aGru"; // cus_IaMSNboA7QCxWz - cus_IaNqHbsHTj9eql (french) - cus_IcG5OViY84aGru (trux)
    private final PlaidClient plaidClient;

    public PlaidStripeResource(PlaidClient plaidClient) {
        this.plaidClient = plaidClient;
    }

    public static class BankAccountToken {
        @JsonProperty
        private String bankAccountToken;

        public BankAccountToken(String bankAccountToken) {
            this.bankAccountToken = bankAccountToken;
        }
    }

    @POST
    public String createStripeToken(@FormParam("public_token") String publicToken) throws IOException {
//        Response<ItemPublicTokenExchangeResponse> itemResponse = plaidClient.service()
//                .itemPublicTokenExchange(new ItemPublicTokenExchangeRequest(publicToken))
//                .execute();
//
//        if (itemResponse.isSuccessful()) {
//
//        }

        String bankAccountToken = StringUtils.EMPTY;

        Response<ItemStripeTokenCreateResponse> stripeResponse = plaidClient.service()
                .itemStripeTokenCreate(new ItemStripeTokenCreateRequest(
                    QuickstartApplication.accessToken,
                    "4obKkA6e9VSo37g9ZMokCGARewGQ1MudQmZEd"
                )).execute();

        if (stripeResponse.isSuccessful()) {
            bankAccountToken = stripeResponse.body().getStripeBankAccountToken();
        }

        return bankAccountToken;
    }

    @Path("/create_stripe_token")
    @GET
    public BankAccountToken createStripeToken() throws IOException {

        String bankAccountToken = StringUtils.EMPTY;

        Response<ItemStripeTokenCreateResponse> stripeResponse = plaidClient.service()
                .itemStripeTokenCreate(new ItemStripeTokenCreateRequest(
                    QuickstartApplication.accessToken,
                    QuickstartApplication.accountId
                )).execute();

        if (stripeResponse.isSuccessful()) {
            bankAccountToken = stripeResponse.body().getStripeBankAccountToken();
            QuickstartApplication.stripeBankToken = bankAccountToken;
        } else {
            LOG.error(stripeResponse.errorBody().string());
        }

        return new BankAccountToken(bankAccountToken);
    }

    @Path("/create_stripe_account")
    @POST
    public InfoResource.InfoResponse attachSourceToStripeCustomer() throws StripeException {
//        Stripe.apiKey = "sk_test_51Hyqg2FSIwrP5rSmm6qSaPeeQlH048hr23TZgCORUasaj0eodYMQ57IoFQvMUA3F5RYZoYD8l2uz0LaJsU4ahvGh00KkQA0VKt"; // Mine
        Stripe.apiKey = "sk_test_RETQPygAtl9cZukoL6YnkbN8"; // Trux

        // Customer Retrieve Params
        CustomerRetrieveParams customerRetrieveParams = CustomerRetrieveParams.builder()
                .addExpand("sources")
                .build();

        Customer customer = Customer.retrieve(STRIPE_CUSTOMER_ID, customerRetrieveParams, null);

        // Assign ACH account to a customer
        PaymentSourceCollectionCreateParams sourceParams = PaymentSourceCollectionCreateParams.builder()
                .setSource(QuickstartApplication.stripeBankToken)
                .build();

        PaymentSource achPaymentSource = customer.getSources().create(sourceParams);
        // System.out.println(gson.toJson(achPaymentSource));

        // Verify ACH Account - This step is not required since PLAID based accounts are already verified, It will throw an exception
//        Customer updatedCustomer = Customer.retrieve(STRIPE_CUSTOMER_ID, customerRetrieveParams, null);
//        BankAccount customerAchAccount = (BankAccount) updatedCustomer.getSources().retrieve(achPaymentSource.getId());
//
//        BankAccountVerifyParams verifyParams = BankAccountVerifyParams.builder()
//                .addAmount(32L)
//                .addAmount(45L)
//                .build();
//
//        BankAccount verifiedAchAccount = customerAchAccount.verify(verifyParams);

        return new InfoResource.InfoResponse(Arrays.asList(), QuickstartApplication.accessToken,
                QuickstartApplication.itemID, QuickstartApplication.accountId, achPaymentSource.getId());
    }
}
