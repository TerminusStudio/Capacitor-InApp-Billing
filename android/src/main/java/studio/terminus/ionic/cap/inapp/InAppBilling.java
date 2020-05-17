package studio.terminus.ionic.cap.inapp;

import android.app.Activity;
import android.os.Debug;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import com.android.billingclient.api.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.Nullable;

@NativePlugin()
public class InAppBilling extends Plugin implements PurchasesUpdatedListener {
    private boolean isInit = false;
    private BillingClient billingClient;

    private List<SkuDetails> InAppSkuDetails;
    private List<SkuDetails> SubsSkuDetails;

    @PluginMethod()
    public void initialize(final PluginCall call) {
        billingClient = BillingClient.newBuilder(getBridge().getActivity()).setListener(this).enablePendingPurchases().build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() ==  BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    isInit = true;
                    call.resolve();
                }
                else
                    call.error("Error connecting to Google Play. " + billingResult.getResponseCode());
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                call.error("Error connecting to Google Play.");
            }
        });
    }

    @PluginMethod()
    public void getSkuDetails(final PluginCall call) {
        if(!isInit)
        {
            call.error("Billing client not initialized");
            return;
        }

        List<String> skuList = new ArrayList<>();

        try {
            JSArray skuArray = call.getArray("skus");
            String skuType = call.getString("skuType", "INAPP");

            switch(skuType){
                case "SUBS":
                    skuType = BillingClient.SkuType.SUBS;
                case "INAPP":
                default:
                    skuType = BillingClient.SkuType.INAPP;
            }

            for (int i=0; i < skuArray.length(); i++) {
                skuList.add(skuArray.getString(i));
            }
            //skuList.add(call.getData().getString("skus"));
            SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
            params.setSkusList(skuList).setType(skuType);

            final String finalSkuType = skuType;
            billingClient.querySkuDetailsAsync(params.build(),
                    new SkuDetailsResponseListener() {
                        @Override
                        public void onSkuDetailsResponse(BillingResult billingResult,
                                                         List<SkuDetails> skuDetailsList) {
                            if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                if (finalSkuType.equals(BillingClient.SkuType.INAPP))
                                    InAppSkuDetails = skuDetailsList;
                                else if (finalSkuType.equals(BillingClient.SkuType.SUBS))
                                    SubsSkuDetails = skuDetailsList;

                                JSONArray responseArray = new JSONArray();
                                for (int i = 0; i < skuDetailsList.size(); i++) {
                                    JSONObject response = new JSONObject();
                                    try {
                                        response.put("sku", skuDetailsList.get(i).getSku());
                                        response.put("title", skuDetailsList.get(i).getTitle());
                                        response.put("description", skuDetailsList.get(i).getDescription());
                                        response.put("price", skuDetailsList.get(i).getPrice());
                                        response.put("currencyCode", skuDetailsList.get(i).getPriceCurrencyCode());
                                        response.put("type", skuDetailsList.get(i).getType());
                                        response.put("subscriptionPeriod", skuDetailsList.get(i).getSubscriptionPeriod());
                                        responseArray.put(response);
                                    } catch (JSONException e) {
                                        call.error("Error - " + e.getMessage());
                                    }
                                }
                                JSObject response = new JSObject();
                                response.put("data", responseArray);
                                call.resolve(response);
                            } else
                                call.error("Error querying product " + billingResult.getDebugMessage());
                        }
                    });
        } catch (Exception e) {
            call.error("Error - " + e.getMessage());
        }
    }

    @PluginMethod()
    public void queryPurchases(PluginCall call) {
        if(!isInit)
            call.error("Billing client not initialized");

        String skuType = call.getString("skuType", "INAPP");

        switch(skuType){
            case "SUBS":
                skuType = BillingClient.SkuType.SUBS;
            case "INAPP":
            default:
                skuType = BillingClient.SkuType.INAPP;
        }
        JSONArray responseArray = new JSONArray();
        Purchase.PurchasesResult purchases = billingClient.queryPurchases(skuType);
        for(int i = 0; i < purchases.getPurchasesList().size(); i++) {
            Purchase purchase = purchases.getPurchasesList().get(i);

            JSObject response = new JSObject();
            response.put("orderId", purchase.getOrderId());
            response.put("packageName", purchase.getPackageName());
            response.put("accountID", purchase.getAccountIdentifiers());
            response.put("sku", purchase.getSku());
            response.put("purchaseState", purchase.getPurchaseState());
            response.put("purchaseTime", purchase.getPurchaseTime());
            response.put("purchaseToken", purchase.getPurchaseToken());
            response.put("signature", purchase.getSignature());
            response.put("receipt", purchase.getOriginalJson());
            response.put("acknowledged", purchase.isAcknowledged());

            responseArray.put(response);
        }
        JSObject response = new JSObject();
        response.put("data", responseArray);
        call.resolve(response);
    }

    @PluginMethod()
    public void buy(PluginCall call) {
        if(!isInit)
            call.error("Billing client not initialized");

        String sku = call.getString("sku");
        internalBuy(sku, call);
    }

    @PluginMethod()
    public void subscribe(PluginCall call) {
        if(!isInit)
            call.error("Billing client not initialized");

        call.reject("not implemented.");
    }

    @PluginMethod()
    public void consumePurchase(final PluginCall call) {
        if (!isInit)
            call.error("Billing client not initialized");
      handleConsume(call.getString("token"), call);
    }

    public void internalBuy(String sku, final PluginCall call)
    {
        SkuDetails skuDetails = null;

        for (int i = 0; i < InAppSkuDetails.size(); i++) {
            if(InAppSkuDetails.get(i).getSku().equals(sku))
                skuDetails = InAppSkuDetails.get(i);
        }

        if(skuDetails == null)
            call.error("Cannot find sku. Make sure to query sku first.");

        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();
        BillingResult billingResult = billingClient.launchBillingFlow(getBridge().getActivity(), flowParams);
        call.resolve();
    }

    public void handleConsume(String token, final PluginCall call)
    {
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(token)
                        .build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String outToken) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    call.success();
                } else {
                    call.error("error");
                }
            }
        };

        billingClient.consumeAsync(consumeParams, listener);
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> list) {
        if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list.size() > 0){
            for(int i = 0; i < list.size(); i++) {
                handleBuyPurchase(list.get(i));
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            notifyListeners("onPurchasesUpdated", new JSObject().put("success", false).put("error", "UserCanceled"));
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            notifyListeners("onPurchasesUpdated", new JSObject().put("success", false).put("error", "AlreadyOwned"));
        } else {
            notifyListeners("onPurchasesUpdated", new JSObject().put("success", false).put("error", billingResult.getDebugMessage()));
        }
    }

    void handleBuyPurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            // Grant entitlement to the user.
            JSObject response = new JSObject();
            response.put("orderId", purchase.getOrderId());
            response.put("packageName", purchase.getPackageName());
            response.put("accountID", purchase.getAccountIdentifiers() != null ? purchase.getAccountIdentifiers().getObfuscatedAccountId() : "");
            response.put("sku", purchase.getSku());
            response.put("purchaseState", purchase.getPurchaseState());
            response.put("purchaseTime", purchase.getPurchaseTime());
            response.put("purchaseToken", purchase.getPurchaseToken());
            response.put("signature", purchase.getSignature());
            response.put("receipt", purchase.getOriginalJson());
            notifyListeners("onPurchasesUpdated", new JSObject().put("success", true).put("data", response));
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            notifyListeners("onPurchasesUpdated", new JSObject().put("success", true).put("pending", true));
        }
    }
}
