import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-cielolio' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const Cielolio = NativeModules.Cielolio
  ? NativeModules.Cielolio
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function startPayment(
  clientID: string,
  accessToken: string,
  orderId: string,
  sku: string,
  amount: number
): Promise<number> {
  return Cielolio.startPayment(clientID, accessToken, orderId, sku, amount);
}
