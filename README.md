# ECommPay Recurring Payments Java 
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

ECommPay recurring payments provide the possibility to pull funds from the accounts of the customers without re-entering payment card details.

**How it works**
--------------------------

1. The customer allows the store to collect automatically the funds from his bank account via the payment card

2. The customer pays by card as usual, and ECommPay remembers the card for subsequent payments

3. ECommPay, at the disposal of the store, debits the payment from the card account without customer&#39;s participation.

**How to implement it**
--------------------------

1. Send a [request to connect](https://ecommpay.com/ru/apply-now/) to ECommPay, if the business does not have a project ID (merchant accpunt) and a secret key to interact with ECommPay, and get two IDs 

    **project\_id**

    **secret\_key**

2. Generate signature

    An important feature is the field _signature_.

    There are two ways to get this field:

    a) using the form for testing

    [**https://developers.ecommpay.com/ru/ru\_Gate\_Authentication.html**](https://developers.ecommpay.com/ru/ru_Gate_Authentication.html)

    b) by running the _Signature Creator_ 

    The json from the following request should be used as signed data

    ```
    {
      "project_id": 42,
      "payment_currency": "EUR",
      "payment_amount": 1000,
      "customer_id": 123,
      "payment_id": "4438"
    }
    ```
   project_id - you’ve got it from ECommPay via request to connect
   payment_currency – it is ISO 4217 — general currency, currency sign
   payment_id - Unique ID of the payment in your project
   customer_id - it is your client id from your system or database
   recurring - parameter that indicates whether this payment should be registered as recurring

   https://developers.ecommpay.com/en/en_PP_Parameters.html


   <a href="https://github.com/arbmaha/ecommpay-recurring"><img alt="https://github.com/arbmaha/ecommpay-recurring" width="500px" src="https://github.com/arbmaha/ecommpay-recurring/blob/master/pic1.jpg"></a>

3. Create a request to get the link form to the payment form (page)
   GET

   Host: https://paymentpage.ecommpay.com
    ```
      https://paymentpage.ecommpay.com/payment?payment\_currency=EUR&amp;project\_id=47331&amp;payment\_amount=1000&amp;customer\_id=21123&amp;payment\_id=44sd38sd543sd2zaq&amp;signature=U5RB0V7ymNy8ouXdhhByNsZBJgf3cy0rqATPlNXCAQkQclisMgjUKqJKwwIkvH+23ToDNoIZwrKasHcUhjAePw==
    ```
4. Apply the Visa / MasterCard testing cards.

      `4000000000000077`

      `12/24 cvv 123`

      The list of cards from documentation is attached below:

      [https://developers.ecommpay.com/ru/ru\_PP\_TestCards.html](https://developers.ecommpay.com/ru/ru_PP_TestCards.html)

      The result of executing the request to open the Payment Page for registering a recurring payment is:recurring, register = true, type=U - for automatic payment.

5.Authorize yourself in the service

Open https://dashboard.ecommpay.com/projects
  
<a href="https://github.com/arbmaha/ecommpay-recurring"><img alt="https://github.com/arbmaha/ecommpay-recurring" width="500px" src="https://github.com/arbmaha/ecommpay-recurring/blob/master/pic2.jpg"></a>
  

Press Add new and add the link to your webhook, where the information about payments should be got.



**How we do it in three steps**
--------------------------

**1. Run test `testPaymentPage()`**

Add to application.properties projectId and secretKey

We get the link to Payment Page


**2. Open the link in the browser**

   <a href="https://github.com/arbmaha/ecommpay-recurring"><img alt="https://github.com/arbmaha/ecommpay-recurring" width="200px" src="https://github.com/arbmaha/ecommpay-recurring/blob/master/pic3.jpg"></a>

You'll get a notification to your webhook as result of completing the payment 

    
```
      {
       "customer":{
           "id":"123"
       },
       "recurring":{
           "id":1640233721,
           "currency":"USD",
           "valid_thru":"2022-12-31T23:59:59+0000"
       }
      }
```
<a href="https://github.com/arbmaha/ecommpay-recurring"><img alt="https://github.com/arbmaha/ecommpay-recurring" width="200px" src="https://github.com/arbmaha/ecommpay-recurring/blob/master/pic4.jpg"></a>

In this notification we get recurring id equal to 1640233721


**3. Add recurringId 1640233721 and run test `testRecurringPayment()`**


  
