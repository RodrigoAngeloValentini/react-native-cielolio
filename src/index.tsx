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
  amount: number,
  orderId: string,
  sku: string,
  name: string,
  quantity: number,
  unityOfMeasure: string
): Promise<any> {
  return Cielolio.startPayment(
    clientID,
    accessToken,
    amount,
    orderId,
    sku,
    name,
    quantity,
    unityOfMeasure
  );
}
