package com.misfit.webmoney;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.misfit.webmoney.data.NRCMODEL;
import com.misfit.webmoney.databinding.ActivitySubmitPageBinding;
import com.misfit.webmoney.http.ApiService;
import com.misfit.webmoney.http.Controller;
import com.misfit.webmoney.utility.KeyWord;
import com.misfit.webmoney.utility.Utility;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubmitPage extends AppCompatActivity {
    ActivitySubmitPageBinding binding;
    Context context;
    Utility utility;
    ApiService apiInterface = Controller.getBaseClient().create(ApiService.class);
    Gson gson = new Gson();
    NRCMODEL model;
    String like = "";
    String live = "";
    File selfiimage = null;
    File nrcfrontimage = null;
    File nrcbackimage = null;
    Uri selfiimage_uri = null;
    Uri nrcfrontimage_uri = null;
    Uri nrcbackimage_uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySubmitPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        utility = new Utility(context);
        try {
            if (getIntent() != null) {
                model = gson.fromJson(getIntent().getStringExtra("nrcmodel"), NRCMODEL.class);
                binding.regNum.setText(model.getData().getNrcId().getEn().toString());
                binding.regNumBm.setText(model.getData().getNrcId().getMm().toString());
                binding.regFullname.setText(model.getData().getName().getEn().toString());
                binding.regFullnameBm.setText(model.getData().getName().getMm().toString());
                binding.regFathername.setText(model.getData().getFathersName().getEn().toString());
                binding.regFathernameBm.setText(model.getData().getFathersName().getMm().toString());
                binding.regDob.setText(model.getData().getBirthDate().getEn().toString());
                binding.regReligion.setText(model.getData().getReligion().getEn().toString());
                like = getIntent().getStringExtra("like");
                live = getIntent().getStringExtra("live");
                binding.regLikeness.setText(like);
                binding.regLiveness.setText(live);
                nrcfrontimage = (File) getIntent().getSerializableExtra("NRC_FRONT");
                nrcfrontimage_uri = Uri.parse(getIntent().getExtras().getString("NRC_FRONT_URI"));
                nrcbackimage = (File) getIntent().getSerializableExtra("NRC_BACK");
                nrcbackimage_uri = Uri.parse(getIntent().getExtras().getString("NRC_BACK_URI"));
                selfiimage = (File) getIntent().getSerializableExtra("SELFI");
                selfiimage_uri = Uri.parse(getIntent().getExtras().getString("SELFI_URI"));

                binding.regStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!TextUtils.isEmpty(binding.regPhone.getEditableText().toString())) {
                            send_registration();
                        } else {
                            utility.showToast("Enter Phone Number");
                        }
                    }
                });
            } else {
                utility.showDialog(context.getResources().getString(R.string.something_went_wrong));
            }

        } catch (Exception e) {
            Log.d("Error Line Number", Log.getStackTraceString(e));
        }
    }


    private void send_registration() {
        try {
            Controller.changeApiBaseUrl("http://ekyc.misfit-test.com/api/v1/");
            apiInterface = Controller.getBaseClient().create(ApiService.class);

            RequestBody requestFile1 = RequestBody.create(nrcfrontimage, MediaType.parse(getContentResolver().getType(nrcfrontimage_uri)));
            // MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part nrcfront = MultipartBody.Part.createFormData("front_nrc", nrcfrontimage.getName(), requestFile1);

            RequestBody requestFile2 = RequestBody.create(nrcbackimage, MediaType.parse(getContentResolver().getType(nrcbackimage_uri)));
            // MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part nrcback = MultipartBody.Part.createFormData("back_nrc", nrcbackimage.getName(), requestFile2);


            RequestBody requestFile3 = RequestBody.create(selfiimage, /*MediaType.parse(getContentResolver().getType(selfiimage_uri))*/MediaType.parse("multipart/form-data"));
            // MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part selfi = MultipartBody.Part.createFormData("selfie", selfiimage.getName(), requestFile3);


            HashMap<String, RequestBody> hashMap = new HashMap<>();
            hashMap.put("name_en", createPartFromString(model.getData().name.en.toString()));
            hashMap.put("name_mm", createPartFromString(model.getData().name.mm.toString()));
            hashMap.put("father_name_en", createPartFromString(model.getData().fathersName.en.toString()));
            hashMap.put("father_name_mm", createPartFromString(model.getData().fathersName.mm.toString()));
            hashMap.put("nrc_id_en", createPartFromString(model.getData().nrcId.en.toString()));
            hashMap.put("nrc_id_mm", createPartFromString(model.getData().nrcId.mm.toString()));
            hashMap.put("dob_en", createPartFromString(model.getData().birthDate.en.toString()));
            hashMap.put("dob_mm", createPartFromString(model.getData().birthDate.mm.toString()));
            hashMap.put("address_en", createPartFromString(model.getData().nrcId.en.toString()));
            hashMap.put("address_mm", createPartFromString(model.getData().religion.mm.toString()));
            hashMap.put("phone_num", createPartFromString(binding.regPhone.getEditableText().toString()));
            hashMap.put("pic_match_percentage", createPartFromString(like));
            utility.showProgress(false, context.getResources().getString(R.string.wait_string));
            Call<JsonElement> call = apiInterface.send_registration2(hashMap, nrcfront, nrcback, selfi);
            call.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    utility.hideProgress();
                    try {
                        utility.logger(response.toString());
                        if (response != null && response.isSuccessful() && response.code() == 201) {
                            utility.logger("send registartion " + response.body().toString());
                            JSONObject jObj = new JSONObject(response.body().toString());
                            showDialog(jObj.getString("message"));
                        } else {
                            //JSONObject jObj = new JSONObject(response.errorBody().string());
                            //JSONObject erobj = new JSONObject(jObj.getString("error"));
                            //utility.logger("result" + gson.toJson(response.body().toString()));
                            utility.showDialog("Server Not Found");
                        }
                    } catch (Exception e) {
                        utility.hideProgress();
                        Log.d("Failed to hit api", Log.getStackTraceString(e));
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    Log.d("On Failure to hit api", t.toString());
                    utility.hideProgress();
                }
            });
        } catch (Exception e) {
            utility.hideProgress();
            Log.d("Error Line Number", Log.getStackTraceString(e));
        }
    }

    public RequestBody createPartFromString(String string) {
        return RequestBody.create(MultipartBody.FORM, string);
    }

    public void showDialog(String message) {
        HashMap<String, Integer> screen = utility.getScreenRes();
        int width = screen.get(KeyWord.SCREEN_WIDTH);
        int height = screen.get(KeyWord.SCREEN_HEIGHT);
        int mywidth = (width / 10) * 7;
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.dialog_toast);
        TextView tvMessage = dialog.findViewById(R.id.tv_message);
        Button btnOk = dialog.findViewById(R.id.btn_ok);
        tvMessage.setText(message);
        LinearLayout ll = dialog.findViewById(R.id.dialog_layout_size);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ll.getLayoutParams();
        params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        params.width = mywidth;
        ll.setLayoutParams(params);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }
}