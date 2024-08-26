package com.cielolio

import android.util.Log
import cielo.orders.domain.CheckoutRequest
import cielo.orders.domain.Credentials
import cielo.orders.domain.Order
import cielo.sdk.order.OrderManager
import cielo.sdk.order.ServiceBindListener
import cielo.sdk.order.payment.PaymentError
import cielo.sdk.order.payment.PaymentListener
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.google.gson.Gson

class CielolioModule(private var reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {
  private val TAG = "CiloLioModule"

  private var clientId: String = ""
  private var accessToken: String = ""
  private var credentials: Credentials? = null
  private var orderManager: OrderManager? = null
  private var order: Order? = null
  private var paymentType: String = ""

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "Cielolio"
  }

  override fun getConstants(): Map<String, Any> {
    val constants = mutableMapOf<String, Any>()
    return constants
  }

  @ReactMethod
  fun startPayment(
    clientId: String,
    accessToken: String,
    amount: Int,
    orderId: String,
    sku: String,
    name: String,
    quantity: Int,
    unityOfMeasure: String,
    promise: Promise
  ) {
    this.clientId = clientId
    this.accessToken = accessToken
    this.paymentType = ""
    this.order = null
    this.orderManager = null;

    this.credentials = Credentials(this.clientId, this.accessToken)
    this.orderManager = OrderManager(credentials!!, this.reactContext)

    val serviceBindListener = object : ServiceBindListener {
      override fun onServiceBound() {
        Log.d(TAG, "onServiceBound")

        order = orderManager?.createDraftOrder(orderId)

        order!!.addItem(sku, name, amount.toLong(), quantity, unityOfMeasure)

        orderManager?.placeOrder(order!!)
        val checkoutRequest: CheckoutRequest = CheckoutRequest.Builder()
          .orderId(order!!.id)
          .amount(amount.toLong())
          .build()

        val paymentListener: PaymentListener = object : PaymentListener {
          override fun onStart() {
            Log.d(TAG, "O pagamento começou.")
          }

          override fun onPayment(order: Order) {
            Log.d(TAG, "Um pagamento foi realizado")
            order.markAsPaid()
            orderManager?.updateOrder(order)

            val payment = order.payments[0]

            val gson: Gson = Gson()
            val json: String = gson.toJson(payment)

            orderManager?.unbind()

            promise.resolve(json)
          }

          override fun onCancel() {
            Log.d(TAG, "A operação foi cancelada")

            promise.reject("error", "Pagamento cancelado")
          }

          override fun onError(error: PaymentError) {
            Log.d(TAG, "Houve um erro no pagamento.")

            promise.reject("error", "Houve um erro no pagamento")
          }
        }

        orderManager?.checkoutOrder(checkoutRequest, paymentListener)
      }

      override fun onServiceBoundError(throwable: Throwable) {
        Log.d(TAG, "onServiceBoundError")

        promise.reject("onServiceBoundError", throwable);
      }

      override fun onServiceUnbound() {
        Log.d(TAG, "onServiceUnbound")
      }
    }

    orderManager!!.bind(this.reactContext, serviceBindListener)
  }

  private fun sendEvent(eventName: String, params: WritableMap?) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }
}
