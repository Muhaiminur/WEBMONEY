package com.misfit.webmoney;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.misfit.webmoney.databinding.ActivityNrcpageBinding;
import com.misfit.webmoney.utility.FileUtil;
import com.misfit.webmoney.utility.Utility;

import java.io.File;
import java.io.InputStream;

public class NRCPage extends AppCompatActivity {
    ActivityNrcpageBinding binding;
    Utility utility;
    Context context;


    File nrcfrontimage = null;
    Uri nrcfrontimageuri = null;
    File nrcbackimage = null;
    Uri nrcbackimageuri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNrcpageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        try {
            context = NRCPage.this;
            utility = new Utility(context);
            binding.nrcBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(NRCPage.this, OnboaredPage.class));
                    finish();
                }
            });
            binding.nrcBrowse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ImagePicker.with(NRCPage.this).galleryOnly()
                            .start(201);
                }
            });
            binding.nrcCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ImagePicker.with(NRCPage.this).cameraOnly()
                            .start(201);
                }
            });
            binding.nrcRetakefront.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    binding.nrcRetakefront.setVisibility(View.GONE);
                    binding.nrcView11.setVisibility(View.VISIBLE);
                }
            });
            binding.nrcNext1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    binding.nrcView1.setVisibility(View.GONE);
                    binding.nrcView2.setVisibility(View.VISIBLE);
                }
            });

            binding.nrcRetakefront2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    binding.nrcView2.setVisibility(View.GONE);
                    binding.nrcView1.setVisibility(View.VISIBLE);
                }
            });


            binding.nrcBrowse2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ImagePicker.with(NRCPage.this).galleryOnly()
                            .start(202);
                }
            });
            binding.nrcCamera2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ImagePicker.with(NRCPage.this).cameraOnly()
                            .start(202);
                }
            });
            binding.nrcRetakeback.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    binding.nrcRetakeback.setVisibility(View.GONE);
                    binding.nrcView21.setVisibility(View.VISIBLE);
                }
            });

            /*binding.nrcNext2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });*/
            binding.nrcNext2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (nrcfrontimage != null && nrcfrontimageuri != null && !TextUtils.isEmpty(nrcfrontimageuri.toString())) {
                        startActivity(new Intent(context, MatchPage.class).putExtra("NRC_FRONT", nrcfrontimage).putExtra("NRC_FRONT_URI", nrcfrontimageuri.toString()).putExtra("NRC_BACK", nrcbackimage).putExtra("NRC_BACK_URI", nrcbackimageuri.toString()));
                        finish();
                    } else {
                        utility.showToast("Submit All Data");
                    }
                }
            });

        } catch (Exception e) {
            Log.d("Error Line Number", Log.getStackTraceString(e));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);  // You MUST have this line to be here
        // so ImagePicker can work with fragment mode

        if (resultCode == Activity.RESULT_OK && requestCode == 201) {
            try {
                nrcfrontimageuri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(nrcfrontimageuri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                //String encodedImage = encodeImage(selectedImage);
                //utility.logger("image base64" + encodedImage);
                //send_nrcfirst(encodedImage);
                nrcfrontimage = new File(FileUtil.getPath(nrcfrontimageuri, this));
                Glide.with(context).load(nrcfrontimage).into(binding.nrcFrontimage);
                Glide.with(context).load(nrcfrontimage).into(binding.nrcFrontimage2);
                binding.nrcFronttitle.setVisibility(View.VISIBLE);
                binding.nrcRetakefront.setVisibility(View.VISIBLE);
                binding.nrcView11.setVisibility(View.GONE);
            } catch (Exception e) {
                Log.d("Error Line Number", Log.getStackTraceString(e));
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == 202) {
            try {
                nrcbackimageuri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(nrcbackimageuri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                //String encodedImage = encodeImage(selectedImage);
                //utility.logger("image base64" + encodedImage);
                //send_nrcsecond(encodedImage);
                nrcbackimage = new File(FileUtil.getPath(nrcbackimageuri, this));
                Glide.with(context).load(nrcbackimage).into(binding.nrcBackimage);
                binding.nrcRetakeback.setVisibility(View.VISIBLE);
                binding.nrcView21.setVisibility(View.GONE);
            } catch (Exception e) {
                Log.d("Error Line Number", Log.getStackTraceString(e));
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }
}