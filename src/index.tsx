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

export function init(clientID: string, accessToken: string): Promise<number> {
  return Cielolio.startPayment(clientID, accessToken);
}

export function requestPayment(
  amount: number,
  orderId: string,
  name: string,
  quantity: number,
  unityOfMeasure: string
): Promise<number> {
  return Cielolio.startPayment(amount, orderId, name, quantity, unityOfMeasure);
}
