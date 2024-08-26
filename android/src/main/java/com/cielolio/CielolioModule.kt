package com.cielolio

import android.util.Log
import cielo.orders.domain.CheckoutRequest
import cielo.orders.domain.Credentials
import cielo.orders.domain.Order
import cielo.sdk.order.OrderManager
import cielo.sdk.order.ServiceBindListener
import cielo.sdk.order.payment.Payment
import cielo.sdk.order.payment.PaymentError
import cielo.sdk.order.payment.PaymentListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.google.gson.Gson

class CielolioModule(private var reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {
  private val TAG = "CieloLioModule"

  private var clientId: String = ""
  private var accessToken: String = ""
  private var credentials: Credentials? = null
  private var orderManager: OrderManager? = null
  private var order: Order? = null

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "CieloLio"
  }

  override fun getConstants(): Map<String, Any> {
    val constants = mutableMapOf<String, Any>()
    return constants
  }

  @ReactMethod
  fun initialize(clientID: String, accessToken: String, promise: Promise) {
    this.clientId = clientID
    this.accessToken = accessToken
    this.order = null
    this.orderManager = null

    credentials = Credentials(this.clientId, this.accessToken)
    orderManager = OrderManager(credentials!!, this.reactContext)

    val serviceBindListener: ServiceBindListener = object : ServiceBindListener {
      override fun onServiceBound() {
        Log.d(TAG, "onServiceBound")

        promise.resolve("onServiceBound")
      }

      override fun onServiceBoundError(throwable: Throwable) {
        Log.d(TAG, "onServiceBoundError")

        promise.reject("onServiceBoundError", throwable);
      }

      override fun onServiceUnbound() {
        Log.d(TAG, "onServiceUnbound")
      }
    }

    orderManager!!.bind(currentActivity!!, serviceBindListener)
  }

  @ReactMethod
  fun createDraftOrder(orderId: String) {
    this.order = orderManager!!.createDraftOrder(orderId)
  }

  @ReactMethod
  fun addItem(sku: String, name: String, unitPrice: Int, quantity: Int, unitOfMeasure: String) {
    this.order?.addItem(
      sku,
      name,
      unitPrice.toLong(),
      quantity,
      unitOfMeasure
    )
  }

  @ReactMethod
  fun placeOrder() {
    order?.let { orderManager?.placeOrder(it) }
  }

  @ReactMethod
  fun requestPayment(amount: Int, orderId: String, promise: Promise) {
    val checkoutRequest: CheckoutRequest = CheckoutRequest.Builder()
      .orderId(order!!.id)
      .amount(amount.toLong())
      .build()

    val paymentListener: PaymentListener = object : PaymentListener {
      override fun onStart() {
      }

      override fun onPayment(order: Order) {
        order.markAsPaid()
        orderManager?.updateOrder(order)

        val paymentResult: List<Payment> = order.payments

        val gson = Gson()
        val json: String = gson.toJson(paymentResult)

        orderManager?.unbind()

        promise.resolve(json)
      }

      override fun onCancel() {
        orderManager?.unbind()

        promise.reject("error", "Pagamento cancelado")
      }

      override fun onError(error: PaymentError) {
        orderManager?.unbind()

        promise.reject("error", "Houve um erro no pagamento")
      }
    }

    this.orderManager?.checkoutOrder(checkoutRequest, paymentListener);
  }

  @ReactMethod
  fun initializeAndRequestPayment(
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
          }

          override fun onPayment(order: Order) {
            order.markAsPaid()
            orderManager?.updateOrder(order)

            val paymentResult: List<Payment> = order.payments

            val gson = Gson()
            val json: String = gson.toJson(paymentResult)

            promise.resolve(json)

            orderManager?.unbind()
          }

          override fun onCancel() {
            orderManager?.unbind()

            promise.reject("error", "Pagamento cancelado")
          }

          override fun onError(error: PaymentError) {
            orderManager?.unbind()

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

  @ReactMethod
  fun unbind() {
    orderManager!!.unbind()
  }
}
