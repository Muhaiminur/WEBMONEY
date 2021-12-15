package com.misfit.webmoney;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.misfit.webmoney.data.NRCMODEL;
import com.misfit.webmoney.databinding.ActivityMatchPageBinding;
import com.misfit.webmoney.http.ApiService;
import com.misfit.webmoney.http.Controller;
import com.misfit.webmoney.utility.FileUtil;
import com.misfit.webmoney.utility.KeyWord;
import com.misfit.webmoney.utility.Utility;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchPage extends AppCompatActivity {
    ActivityMatchPageBinding binding;
    String token = "";

    Context context;
    Utility utility;
    ApiService apiInterface = Controller.getBaseClient().create(ApiService.class);
    Gson gson = new Gson();

    File selfiimage = null;
    File nrcfrontimage = null;
    File nrcbackimage = null;
    Uri selfiimage_uri = null;
    Uri nrcfrontimage_uri = null;
    Uri nrcbackimage_uri = null;

    String like = "";
    String liveliness = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMatchPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        try {
            if (getIntent() != null) {
                nrcfrontimage = (File) getIntent().getSerializableExtra("NRC_FRONT");
                nrcfrontimage_uri = Uri.parse(getIntent().getExtras().getString("NRC_FRONT_URI"));
                nrcbackimage = (File) getIntent().getSerializableExtra("NRC_BACK");
                nrcbackimage_uri = Uri.parse(getIntent().getExtras().getString("NRC_BACK_URI"));
                context = this;
                utility = new Utility(context);
                binding.selfiOne.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ImagePicker.with(MatchPage.this).cameraOnly()
                                .start(203);
                    }
                });
                binding.selfiSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (selfiimage != null && selfiimage_uri != null && !TextUtils.isEmpty(selfiimage_uri.toString()) && !TextUtils.isEmpty(liveliness)) {
                            get_matchness();
                        } else {
                            utility.showToast("Submit Selfie");
                        }
                    }
                });
                gen_token();
            } else {
                utility.showDialog(context.getResources().getString(R.string.something_went_wrong));
            }

        } catch (Exception e) {
            Log.d("Error Line Number", Log.getStackTraceString(e));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);  // You MUST have this line to be here
        // so ImagePicker can work with fragment mode

        if (resultCode == Activity.RESULT_OK && requestCode == 203) {
            try {
                selfiimage_uri = data.getData();
                //final InputStream imageStream = getContentResolver().openInputStream(selfiimage_uri);
                //final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                //String encodedImage = encodeImage(selectedImage);
                //utility.logger("image base64" + encodedImage);
                //send_nrcfirst(encodedImage);
                selfiimage = new File(FileUtil.getPath(selfiimage_uri, this));
                //Glide.with(context).load(selfiimage).circleCrop().into(binding.selfiOne);
                //Glide.with(context).load(selfiimage).circleCrop().into(binding.selfiCom1);
                //Glide.with(context).load(nrcfrontimage).circleCrop().into(binding.selfiCom2);
                get_liveness(selfiimage);
            } catch (Exception e) {
                Log.d("Error Line Number", Log.getStackTraceString(e));
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void gen_token() {
        try {
            Controller.changeApiBaseUrl("https://ekycapi.bitanon.xyz/api/");
            apiInterface = Controller.getBaseClient().create(ApiService.class);
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("password", "misfit");
            hashMap.put("username", "misfit");
            utility.showProgress(false, context.getResources().getString(R.string.wait_string));
            Call<JsonElement> call = apiInterface.gen_token(hashMap);
            call.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    utility.hideProgress();
                    try {
                        utility.logger(response.toString());
                        if (response.isSuccessful() && response.code() == 200 && response != null) {
                            utility.logger("gen token " + response.body().toString());
                            JSONObject jObj = new JSONObject(response.body().toString());
                            JSONObject tokobj = new JSONObject(jObj.getString("data"));
                            token = tokobj.getString("token");
                            utility.logger("token " + token);
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

    private void get_matchness() {
        try {
            utility.logger("abir " + selfiimage.toString());
            RequestBody requestFile1 = RequestBody.create(selfiimage, /*MediaType.parse("image/*")*//*MediaType.parse(getContentResolver().getType(selfiimage_uri))*/MediaType.parse("multipart/form-data"));
            // MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part image_body1 = MultipartBody.Part.createFormData("face1", selfiimage.getName(), requestFile1);


            RequestBody requestFile2 = RequestBody.create(nrcfrontimage, MediaType.parse(getContentResolver().getType(nrcfrontimage_uri)));
            // MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part image_body2 = MultipartBody.Part.createFormData("face2", nrcfrontimage.getName(), requestFile2);


            Controller.changeApiBaseUrl("https://ekycapi.bitanon.xyz/api/");
            apiInterface = Controller.getBaseClient().create(ApiService.class);


            // create RequestBody instance from file
            /*RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), f);
            MultipartBody.Part body =
                    MultipartBody.Part.createFormData("picture", f.getName(), requestFile);*/
            utility.showProgress(false, context.getResources().getString(R.string.wait_string));
            Call<JsonElement> call = apiInterface.get_macthness(token, image_body1, image_body2);
            call.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    utility.hideProgress();
                    try {
                        utility.logger(response.toString());
                        if (response.isSuccessful() && response.code() == 200 && response != null) {
                            utility.logger("gen liveness " + response.body().toString());
                            JSONObject jObj = new JSONObject(response.body().toString());
                            JSONObject erobj = new JSONObject(jObj.getString("data"));
                            like = erobj.getString("confidence");
                            utility.logger("likeness value" + like);
                            //binding.submitSecondImage.setBackground(context.getResources().getDrawable(R.drawable.ic_uploaded));
                            //int percentage = (int) Math.ceil((1 - Float.parseFloat(liveliness)) * 100);
                            int percentage = (int) Math.ceil(Float.parseFloat(like) * 100);
                            showDialog("Your matchness " + percentage, 1, percentage + "", liveliness);
                            //showDialog("Thank you. Once we have verified the information, we will upgrade your account.Your matchness " + percentage);
                        } else {
                            //JSONObject jObj = new JSONObject(response.errorBody().string());
                            //JSONObject erobj = new JSONObject(jObj.getString("error"));
                            //utility.logger("result" + gson.toJson(response.body().toString()));

                            JSONObject jObj = new JSONObject(response.errorBody().string());
                            String d = jObj.getString("message");
                            //utility.showDialog(d);
                            showDialog(d, 2, "20", "2");
                            utility.logger("gen likeness " + response.errorBody().string());
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

    private void send_nrcfirst(String m, String live, String like) {
        try {
            Controller.changeApiBaseUrl("http://ocr-stage.misfit.tech/api/v1/ocr/");
            apiInterface = Controller.getBaseClient().create(ApiService.class);
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("image", m);
            hashMap.put("source", "MisfitTech");
            utility.showProgress(false, context.getResources().getString(R.string.wait_string));
            Call<JsonElement> call = apiInterface.Nrc_first("Token eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjo0LCJleHAiOjE1OTQxODQxMTl9.anFFYrdAtyzAkGDmzZVYsjVpFNH-g2baV4koFgzsEB0", hashMap);
            call.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    utility.hideProgress();
                    try {
                        utility.logger(response.toString());
                        if (response.isSuccessful() && response.code() == 200 && response != null) {
                            utility.logger("nrc first " + response.body().toString());
                            NRCMODEL model = gson.fromJson(response.body().toString(), NRCMODEL.class);
                            if (model != null) {
                                startActivity(new Intent(context, SubmitPage.class).putExtra("nrcmodel", gson.toJson(model)).putExtra("live", live).putExtra("like", like).putExtra("NRC_FRONT", nrcfrontimage).putExtra("NRC_FRONT_URI", nrcfrontimage_uri.toString()).putExtra("NRC_BACK", nrcbackimage).putExtra("NRC_BACK_URI", nrcbackimage_uri.toString()).putExtra("SELFI", selfiimage).putExtra("SELFI_URI", selfiimage_uri.toString()));
                                finish();
                            }
                        } else {
                            JSONObject jObj = new JSONObject(response.errorBody().string());
                            JSONObject erobj = new JSONObject(jObj.getString("error"));
                            //utility.logger("result" + gson.toJson(response.body().toString()));
                            utility.showDialog(erobj.getString("message"));
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

    public void showDialog(String message, int i, String like, String live) {
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
                if (i == 1) {
                    try {
                        final InputStream imageStream = getContentResolver().openInputStream(nrcfrontimage_uri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        String encodedImage = encodeImage(selectedImage);
                        utility.logger("image base64" + encodedImage);
                        send_nrcfirst(encodedImage, live, like);

                    } catch (Exception e) {
                        Log.d("Error Line Number", Log.getStackTraceString(e));
                    }
                }
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private String encodeImage(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encImage;
    }

    private void get_liveness(File f) {
        try {

            RequestBody requestFile = RequestBody.create(/*MediaType.parse(getContentResolver().getType(fileUri))*/ f, MediaType.parse("multipart/form-data"));
            // MultipartBody.Part is used to send also the actual file name
            MultipartBody.Part image_body = MultipartBody.Part.createFormData("face", f.getName(), requestFile);
            Controller.changeApiBaseUrl("https://ekycapi.bitanon.xyz/api/");
            apiInterface = Controller.getBaseClient().create(ApiService.class);
            utility.showProgress(false, context.getResources().getString(R.string.wait_string));
            Call<JsonElement> call = apiInterface.get_liveness(token, image_body);
            call.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    utility.hideProgress();
                    try {
                        utility.logger(response.toString());
                        if (response.isSuccessful() && response.code() == 200 && response != null) {
                            utility.logger("gen liveness " + response.body().toString());
                            JSONObject jObj = new JSONObject(response.body().toString());
                            JSONObject erobj = new JSONObject(jObj.getString("data"));
                            liveliness = erobj.getString("confidence");
                            utility.logger("liveness value" + liveliness);
                            int percentage = (int) Math.ceil((1 - Float.parseFloat(liveliness)) * 100);
                            if (percentage == 1) {
                                liveliness = 100 + "";
                                //showmatch("Your Liveliness value " + 100);
                            } else {
                                //showmatch("Your Liveliness value " + percentage);
                                liveliness = percentage + "";
                            }
                            Glide.with(context).load(f).circleCrop().into(binding.selfiOne);
                            Glide.with(context).load(selfiimage).circleCrop().into(binding.selfiCom1);
                        } else if (response.code() == 500) {
                            utility.showToast("Take A Selfi");
                        } else {
                            //JSONObject jObj = new JSONObject(response.errorBody().string());
                            //JSONObject erobj = new JSONObject(jObj.getString("error"));
                            //utility.logger("result" + gson.toJson(response.body().toString()));

                            JSONObject jObj = new JSONObject(response.errorBody().string());
                            String d = jObj.getString("message");
                            utility.showDialog(d);
                            utility.logger("gen liveness " + response.errorBody().string());
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
}