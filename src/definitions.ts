declare module "@capacitor/core" {
  interface PluginRegistry {
    InAppBilling: InAppBillingPlugin;
  }
}

export interface InAppBillingPlugin {
  echo(options: { value: string }): Promise<{value: string}>;
}
