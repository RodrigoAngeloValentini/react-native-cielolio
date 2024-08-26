package com.cielolio

import android.util.Log
import cielo.orders.domain.CheckoutRequest
import cielo.orders.domain.Credentials
import cielo.orders.domain.Order
import cielo.sdk.info.InfoManager
import cielo.sdk.order.OrderManager
import cielo.sdk.order.ServiceBindListener
import cielo.sdk.order.payment.PaymentError
import cielo.sdk.order.payment.PaymentListener
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule

class CielolioModule(private var reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private val TAG = "RNLio"

  private var clientID: String = ""
  private var accessToken: String = ""
  private lateinit var credentials: Credentials
  private lateinit var orderManager: OrderManager
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
  fun startPayment(clientID: String, accessToken: String, orderId: String, sku: String, amount: Int) {
    this.clientID = clientID
    this.accessToken = accessToken
    this.paymentType = ""
    this.order = null

    this.credentials = Credentials(this.clientID, this.accessToken)
    this.orderManager = OrderManager(credentials, this.reactContext)

    val serviceBindListener = object : ServiceBindListener {
      override fun onServiceBound() {
        Log.d(TAG, "onServiceBound")

        createDraftOrder(orderId);
        addOrderItem(sku, amount, "PRODUCT", 1, "UN")
        requestPayment(amount, orderId)
      }

      override fun onServiceBoundError(throwable: Throwable) {
        Log.d(TAG, "onServiceBoundError")
      }

      override fun onServiceUnbound() {
        Log.d(TAG, "onServiceUnbound")
      }
    }

    orderManager.bind(this.reactContext, serviceBindListener)
  }

  private fun createPaymentListener(): PaymentListener {
    val paymentListener: PaymentListener = object : PaymentListener {
      override fun onStart() {
        Log.d(TAG, "O pagamento começou.")
      }

      override fun onPayment(order: Order) {
        Log.d(TAG, "Um pagamento foi realizado")
        order.markAsPaid()
        orderManager.updateOrder(order)
        val infoManager = InfoManager()

        var amount = 0.0
        var installments = 1
        val product = paymentType
        var brand = ""
        var nsu = ""
        var authorizationCode = ""
        var authorizationDate = ""

        val payment = order.payments[0]

        amount = order.paidAmount.toDouble()
        installments = Math.toIntExact(payment.installments)
        brand = payment.brand
        nsu = payment.cieloCode
        authorizationCode = payment.authCode
        authorizationDate = payment.requestDate

        val stateService = Arguments.createMap()

        stateService.putInt("paymentState", 1)
        stateService.putInt("amount", amount.toInt())
        stateService.putInt("installments", installments)
        stateService.putString("product", product)
        stateService.putString("brand", brand)
        stateService.putString("nsu", nsu)
        stateService.putString("authorizationCode", authorizationCode)
        stateService.putString("authorizationDate", authorizationDate)

        orderManager.unbind()
      }

      override fun onCancel() {
        Log.d(TAG, "A operação foi cancelada")
      }

      override fun onError(error: PaymentError) {
        Log.d(TAG, "Houve um erro no pagamento.")
      }
    }

    return paymentListener
  }

  fun requestPayment(amount: Int, orderId: String?) {
    orderManager.placeOrder(order!!)
    val checkoutRequest: CheckoutRequest = CheckoutRequest.Builder()
      .orderId(order!!.id)
      .amount(amount.toLong())
      .build()

    orderManager.checkoutOrder(checkoutRequest, createPaymentListener())
  }

  fun createDraftOrder(orderId: String?) {
    order = orderManager.createDraftOrder(orderId!!)
  }

  fun addOrderItem(sku: String, amount: Int, name: String, quantity: Int, unityOfMeasure: String) {
    order!!.addItem(sku, name, amount.toLong(), quantity, unityOfMeasure)
  }

  private fun sendEvent(eventName: String, params: WritableMap?) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }
}
