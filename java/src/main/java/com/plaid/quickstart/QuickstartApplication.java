package com.plaid.quickstart;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.plaid.client.PlaidClient;
import com.plaid.quickstart.resources.*;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.Arrays;
import java.util.List;

public class QuickstartApplication extends Application<QuickstartConfiguration> {
  // We store the accessToken in memory - in production, store it in a secure
  // persistent data store.
  public static String accessToken;
  public static String itemID;
  // The paymentId is only relevant for the UK Payment Initiation product.
  // We store the paymentId in memory - in production, store it in a secure
  // persistent data store.
  public static String paymentId;
  public static String accountId;
  public static String institutionId;
  public static String stripeBankToken;

  public static void main(final String[] args) throws Exception {
    new QuickstartApplication().run(args);
  }

  @Override
  public String getName() {
    return "Quickstart";
  }

  @Override
  public void initialize(final Bootstrap<QuickstartConfiguration> bootstrap) {
    bootstrap.getObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    bootstrap.setConfigurationSourceProvider(
      new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
        new EnvironmentVariableSubstitutor(false)
      )
    );
    bootstrap.addBundle(new AssetsBundle("/static/", "/static/"));
    bootstrap.addBundle(new AssetsBundle("/templates/index.html", "/index.html", null, "index"));
    bootstrap.addBundle(new AssetsBundle("/templates/index.html", "/", null, "index"));
    bootstrap.addBundle(
      new AssetsBundle("/templates/oauth-response.html", "/oauth-response.html", null,
        "oauth-response"));
  }

  @Override
  public void run(final QuickstartConfiguration configuration,
    final Environment environment) {
    // or equivalent, depending on which environment you're calling into
    PlaidClient.Builder builder = PlaidClient.newBuilder()
      .clientIdAndSecret(configuration.getPlaidClientID(), configuration.getPlaidSecret());
    switch (configuration.getPlaidEnv()) {
    case "sandbox":
      builder = builder.sandboxBaseUrl();
      break;
    case "development":
      builder = builder.developmentBaseUrl();
      break;
    case "production":
      builder = builder.productionBaseUrl();
      break;
    default:
      throw new IllegalArgumentException("unknown environment: " + configuration.getPlaidEnv());
    }
    PlaidClient plaidClient = builder.build();
    List<String> plaidProducts = Arrays.asList(configuration.getPlaidProducts().split(","));
    List<String> countryCodes = Arrays.asList(configuration.getPlaidCountryCodes().split(","));
    String redirectUri = null;
    if (configuration.getPlaidRedirectUri() != null && configuration.getPlaidRedirectUri().length() > 0) {
      redirectUri = configuration.getPlaidRedirectUri();
    }
    environment.jersey().register(new AccessTokenResource(plaidClient));
    environment.jersey().register(new AccountsResource(plaidClient));
    environment.jersey().register(new AuthResource(plaidClient));
    environment.jersey().register(new BalanceResource(plaidClient));
    environment.jersey().register(new HoldingsResource(plaidClient));
    environment.jersey().register(new IdentityResource(plaidClient));
    environment.jersey().register(new InfoResource(plaidProducts));
    environment.jersey().register(new InvestmentTransactionsResource(plaidClient));
    environment.jersey().register(new ItemResource(plaidClient));
    environment.jersey().register(new LinkTokenResource(plaidClient, plaidProducts, countryCodes, redirectUri));
    environment.jersey().register(new LinkTokenWithPaymentResource(plaidClient, plaidProducts, countryCodes, redirectUri));
    environment.jersey().register(new PaymentInitiationResource(plaidClient));
    environment.jersey().register(new PublicTokenResource(plaidClient));
    environment.jersey().register(new TransactionsResource(plaidClient));
    environment.jersey().register(new PlaidStripeResource(plaidClient));
  }
}
