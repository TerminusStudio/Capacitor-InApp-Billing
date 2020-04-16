import { WebPlugin } from '@capacitor/core';
import { InAppBillingPlugin } from './definitions';

export class InAppBillingWeb extends WebPlugin implements InAppBillingPlugin {
  constructor() {
    super({
      name: 'InAppBilling',
      platforms: ['web']
    });
  }

  async echo(options: { value: string }): Promise<{value: string}> {
    console.log('ECHO', options);
    return options;
  }
}

const InAppBilling = new InAppBillingWeb();

export { InAppBilling };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(InAppBilling);
