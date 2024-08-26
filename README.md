# react-native-cielolio

React Native CieloLio

## Installation

1. Install a package

from npm

```bash
npm install react-native-lio --save
```

2. Download SDK Cielo

After download SDK clone in your mavenLocal repository .m2/repository:

- Windows: `C:\Users\<user>\.m2`
- Linux: `/home/<user>/.m2`
- Mac: `/Users/<user>/.m2`

3. Add at end of file /android/build.gradle

```groove
allprojects {
  repositories {
    mavenLocal()
  }
}
``````

4. Add dependency in build.gradle:

```groove
dependencies {
  implementation 'com.cielo.lio:order-manager:1.8.6'
}
```

5. Change or add in android/app/src/main/AndroidManifest.xml allowBackup to true

```groove
android:allowBackup="true"
```

## Usage

### - initialize(clientID: string, accessToken: string): Promise<void>

Load library with client ID, accessToken.

- Client-Id Access identification;
- Access-Token Access token identification, which stores the access rules allowed to the Client ID;

### createDraftOrder(orderId: string): void

Create a draft order.

### addItem(sku: string, name: string, unitPrice: number, quantity: number, unitOfMeasure: string): void

Add items to order.

### placeOrder(): void

Place order.

### requestPayment(amount: number, orderId: string): Promise<string>

Request Payment.

## Example

```js
import {
  initialize,
  createDraftOrder,
  addItem,
  placeOrder,
  requestPayment,
} from 'react-native-cielolio';

const total = 1000;

const sku = 'sku-123';

const clientID = 'your-client-id';
const accessToken = 'your-access-token';
const orderId = 'your-invoice-code';
const productName = 'Product Name';
const productQuantity = 1;
const unitOfMeasure = 'UN';

initialize(clientID, accessToken)
  .then(() => {
    createDraftOrder(orderId);

    addItem(
      sku,
      productName,
      total,
      productQuantity,
      unitOfMeasure,
    );

    placeOrder();

    requestPayment(totalFormattedCielo, orderId)
      .then(res => {
        const paymentData = transformCieloPaymentResponse(JSON.parse(res));

        resolve(paymentData);
      })
      .catch(e => {
        const error = handleResponseError(e);

        reject(error || 'PAYMENT_ERROR');
      });
  })
  .catch((e) => {
    reject(e || 'INITIALIZE_ERROR');
  });

```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
