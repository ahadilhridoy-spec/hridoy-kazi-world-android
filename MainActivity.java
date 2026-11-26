package com.hridoykazi.world;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private String accessToken = "", userId = "";
    private LinearLayout root;
    private TextView status;
    private EditText emailInput, passwordInput;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        showAuth();
    }

    private void base(String title) {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 28, 24, 28); root.setBackgroundColor(0xFFF3F4F6);
        scroll.addView(root); setContentView(scroll);
        TextView heading = new TextView(this); heading.setText(title); heading.setTextSize(24);
        heading.setTextColor(0xFF111827); heading.setTypeface(null, Typeface.BOLD);
        root.addView(heading, new LinearLayout.LayoutParams(-1, -2));
        status = new TextView(this); status.setTextColor(0xFFB45309); status.setPadding(0, 12, 0, 12); root.addView(status);
    }
    private EditText field(String hint, int inputType) {
        EditText e = new EditText(this); e.setSingleLine(true); e.setHint(hint); e.setInputType(inputType);
        root.addView(e, new LinearLayout.LayoutParams(-1, -2)); return e;
    }
    private void button(String label, Runnable action) {
        Button b = new Button(this); b.setText(label); b.setOnClickListener(v -> action.run());
        root.addView(b, new LinearLayout.LayoutParams(-1, -2));
    }
    private void showAuth() {
        base("HRIDOY KAZI WORLD");
        TextView sub = new TextView(this); sub.setText("নিরাপদ ব্যবসা ব্যবস্থাপনা • বাংলাদেশি টাকা (৳)"); sub.setTextSize(14); root.addView(sub);
        emailInput = field("ইমেইল", 33);
        passwordInput = field("পাসওয়ার্ড", 129);
        button("লগইন", () -> authenticate(false));
        button("নতুন অ্যাকাউন্ট তৈরি", () -> authenticate(true));
        status.setText("Supabase-এ সংযোগের জন্য ইন্টারনেট প্রয়োজন।");
    }
    private void authenticate(boolean signup) {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        if (email.isEmpty() || password.length() < 6) { status.setText("সঠিক ইমেইল ও কমপক্ষে ৬ অক্ষরের পাসওয়ার্ড দিন।"); return; }
        status.setText("সংযোগ হচ্ছে...");
        new Thread(() -> {
            try {
                String path = signup ? "/auth/v1/signup" : "/auth/v1/token?grant_type=password";
                JSONObject body = new JSONObject(); body.put("email", email); body.put("password", password);
                JSONObject result = request("POST", path, body.toString(), null);
                String token = result.optString("access_token", "");
                JSONObject user = result.optJSONObject("user");
                if (user == null) user = result;
                String uid = user.optString("id", "");
                runOnUiThread(() -> {
                    if (token.isEmpty() || uid.isEmpty()) {
                        status.setText(signup ? "অ্যাকাউন্ট তৈরি হয়েছে কি না ইমেইল যাচাই করুন; নিশ্চিতকরণ প্রয়োজন হতে পারে।" : "লগইন হয়নি। ইমেইল/পাসওয়ার্ড যাচাই করুন।");
                    } else { accessToken = token; userId = uid; showDashboard(); }
                });
            } catch (Exception ex) { runOnUiThread(() -> status.setText("সংযোগ সমস্যা: " + ex.getMessage())); }
        }).start();
    }
    private void showDashboard() {
        base("ব্যবসার ড্যাশবোর্ড");
        text("অ্যাকাউন্ট: " + userId);
        text("স্টক, বিক্রয় ও দেনার তথ্য সার্ভার থেকে পড়া হবে।");
        button("ড্যাশবোর্ড রিফ্রেশ", this::loadDashboard);
        button("লগআউট", () -> { accessToken = ""; userId = ""; showAuth(); });
        loadDashboard();
    }
    private void text(String s) { TextView t = new TextView(this); t.setText(s); t.setTextSize(16); t.setTextColor(0xFF1F2937); t.setPadding(0, 8, 0, 8); root.addView(t); }
    private void loadDashboard() {
        status.setText("হিসাব লোড হচ্ছে...");
        new Thread(() -> {
            try {
                JSONArray products = getRows("products?select=id,name&owner_user_id=eq." + enc(userId));
                JSONArray purchases = getRows("purchase_items?select=quantity&owner_user_id=eq." + enc(userId));
                JSONArray sales = getRows("sale_items?select=quantity&owner_user_id=eq." + enc(userId));
                JSONArray payments = getRows("payments?select=amount,purpose,verification_status&owner_user_id=eq." + enc(userId) + "&purpose=eq.wholesaler_debt_settlement&verification_status=eq.verified");
                double bought = sum(purchases, "quantity"), sold = sum(sales, "quantity"), paid = sum(payments, "amount");
                String report = "পণ্য রেকর্ড: " + products.length() + "\nমোট ক্রয়কৃত একক: " + bought + "\nমোট বিক্রিত একক: " + sold + "\nস্টক (প্রাথমিক গণনা): " + (bought-sold) + "\nযাচাইকৃত পাইকারি দেনা পরিশোধ: ৳" + paid + "\n\nনোট: প্রাথমিক ড্যাশবোর্ড। রানা ভাইয়ের পূর্ণ দেনা, গ্রাহক বকেয়া ও নগদ ব্যালেন্সের জন্য পরবর্তী লেজার/সমন্বয় কাজ প্রয়োজন।";
                runOnUiThread(() -> { status.setText("সার্ভার থেকে তথ্য পাওয়া গেছে।"); text(report); });
            } catch (Exception ex) { runOnUiThread(() -> status.setText("তথ্য পড়া যায়নি: " + ex.getMessage())); }
        }).start();
    }
    private double sum(JSONArray a, String key) { double s=0; for(int i=0;i<a.length();i++) s += a.optJSONObject(i).optDouble(key,0); return s; }
    private JSONArray getRows(String path) throws Exception {
        JSONObject ignored = null;
        String response = requestRaw("GET", "/rest/v1/" + path, null, accessToken);
        return new JSONArray(response);
    }
    private JSONObject request(String method, String path, String body, String bearer) throws Exception { return new JSONObject(requestRaw(method,path,body,bearer)); }
    private String requestRaw(String method, String path, String body, String bearer) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(BuildConfig.SUPABASE_URL + path).openConnection();
        c.setRequestMethod(method); c.setConnectTimeout(15000); c.setReadTimeout(15000);
        c.setRequestProperty("apikey", BuildConfig.SUPABASE_KEY); c.setRequestProperty("Content-Type", "application/json");
        if (bearer != null && !bearer.isEmpty()) c.setRequestProperty("Authorization", "Bearer " + bearer);
        if (body != null) { c.setDoOutput(true); try(OutputStream os=c.getOutputStream()){ os.write(body.getBytes(StandardCharsets.UTF_8)); } }
        int code=c.getResponseCode(); InputStream is=(code>=200 && code<300)?c.getInputStream():c.getErrorStream();
        ByteArrayOutputStream out=new ByteArrayOutputStream(); byte[] buf=new byte[4096]; int n;
        while((n=is.read(buf))!=-1) out.write(buf,0,n); c.disconnect();
        String result=out.toString("UTF-8"); if(code<200 || code>=300) throw new IOException("HTTP " + code + ": " + result);
        return result;
    }
    private String enc(String s) throws Exception { return URLEncoder.encode(s,"UTF-8"); }
}
