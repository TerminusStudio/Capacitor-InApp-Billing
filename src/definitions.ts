import { PluginListenerHandle } from '@capacitor/core';

declare module "@capacitor/core" {
  interface PluginRegistry {
    InAppBilling: InAppBillingPlugin;
  }
}

export interface InAppBillingPlugin {
  consumePurchase(options: { token : string }) : Promise<void>;
  buy(options: { sku : string }): Promise<PurchaseDetail>;
  getSkuDetails(options: { skus: string[], skuType: SkuType}): Promise<{ data: SkuDetail[] }>;
  queryPurchases(options: { skuType: SkuType}): Promise<{ data: PurchaseDetail[] }>;
  initialize(): Promise<void>;
  addListener(eventName: 'onPurchasesUpdated', listenerFunc: (info: any) => void): PluginListenerHandle;
}

export enum SkuType {
  InApp = 'INAPP',
  Subscription = 'SUBS'
}

export interface PurchaseDetail {
  orderId: string;
  packageName: string;
  accountID: string;
  sku: string;
  purchaseState: string;
  purchaseTime: string;
  purchaseToken: string;
  signature: string;
  receipt: string;
  acknowledged : boolean;
}

export interface SkuDetail {
  sku: string;
  title: string;
  description: string;
  price: number;
  type: SkuType,
  currencyCode: string;
  subscriptionPeriod: string;
}