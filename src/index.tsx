import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-cielolio' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const CieloLio = NativeModules.CieloLio
  ? NativeModules.CieloLio
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

function initialize(clientID: string, accessToken: string): Promise<void> {
  return CieloLio.initialize(clientID, accessToken);
}

function createDraftOrder(orderId: string): void {
  return CieloLio.createDraftOrder(orderId);
}

function addItem(
  sku: string,
  name: string,
  unitPrice: number,
  quantity: number,
  unitOfMeasure: string
) {
  return CieloLio.addItem(sku, name, unitPrice, quantity, unitOfMeasure);
}

function placeOrder(): void {
  return CieloLio.placeOrder();
}

function requestPayment(amount: number, orderId: string): string {
  return CieloLio.requestPayment(amount, orderId);
}

function initializeAndRequestPayment(
  clientID: string,
  accessToken: string,
  amount: number,
  orderId: string,
  sku: string,
  name: string,
  quantity: number,
  unityOfMeasure: string
): Promise<string> {
  return CieloLio.initializeAndRequestPayment(
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

export {
  initialize,
  createDraftOrder,
  addItem,
  placeOrder,
  requestPayment,
  initializeAndRequestPayment,
};
