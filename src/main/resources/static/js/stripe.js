const stripe = Stripe('pk_test_51S9IoeK78ZeXsneyWpzOzExhms18tDS3CpLub4OJNBCwgHtmh6rmG2b1yI0oRJ7Jh6gCHDUiDScTzo2pWRatpcGI00kbOBVs0L');
const paymentButton = document.querySelector('#paymentButton');

paymentButton.addEventListener('click', () => {
 stripe.redirectToCheckout({
   sessionId: sessionId
 })
});