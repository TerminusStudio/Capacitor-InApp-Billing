import { WebPlugin } from '@capacitor/core';
import { InAppBillingPlugin } from './definitions';

export class InAppBillingWeb extends WebPlugin implements InAppBillingPlugin {
  constructor() {
    super({
      name: 'InAppBilling',
      platforms: ['web']
    });
  } 
  queryPurchases(options: { skuType: import("./definitions").SkuType; }): Promise<{ data: import("./definitions").PurchaseDetail[]; }> {
    console.log('ECHO', options);
    throw new Error("Method not implemented.");
  }
  async consumePurchase(options: { token: String; }): Promise<void> {
    console.log('ECHO', options);
    throw new Error("Method not implemented.");
  }
  async buy(options: { sku: String; }): Promise<import("./definitions").PurchaseDetail> {
    console.log('ECHO', options);
    throw new Error("Method not implemented.");
  }
  async getSkuDetails(options: { skus: string[]; skuType: import("./definitions").SkuType; }): Promise<{ data: import("./definitions").SkuDetail[]; }> {
    console.log('ECHO', options);
    throw new Error("Method not implemented.");
  }
  
  async initialize(): Promise<void> {
    throw new Error("Method not implemented.");
  }
}

const InAppBilling = new InAppBillingWeb();

export { InAppBilling };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(InAppBilling);
