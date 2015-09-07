package com.forex.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PaymentDoneReceiver extends BroadcastReceiver {
    public PaymentDoneReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        String action = intent.getAction();
        if (action.equals("com.clover.intent.action.PAYMENT_PROCESSED")) {
            String orderId = intent.getStringExtra("com.clover.intent.extra.ORDER_ID");
            String paymentId = intent.getStringExtra("com.clover.intent.extra.PAYMENT_ID");
            long amount = intent.getLongExtra("com.clover.intent.extra.AMOUNT", 0l);
            Intent start = new Intent(context, MainActivity.class);
            start.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            start.putExtra("amount", amount);
            context.startActivity(start);
            return;
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
