package com.example.and103_ph44245_lab6.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.and103_ph44245_lab6.R;
import com.example.and103_ph44245_lab6.adapter.FruitAdapter;
import com.example.and103_ph44245_lab6.databinding.ActivityHomeBinding;
import com.example.and103_ph44245_lab6.databinding.DialogEditBinding;
import com.example.and103_ph44245_lab6.model.Distributor;
import com.example.and103_ph44245_lab6.model.Fruit;
import com.example.and103_ph44245_lab6.model.Response;
import com.example.and103_ph44245_lab6.services.HttpRequest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class HomeActivity extends AppCompatActivity implements FruitAdapter.FruitClick {
    ActivityHomeBinding binding;

    DialogEditBinding binding1;
    private HttpRequest httpRequest;
    private SharedPreferences sharedPreferences;
    private String token;
    private FruitAdapter adapter;
    private static final String TAG = "HomeActivity";
    private ArrayList<File> ds_image;
    private String id_Distributor;
    private ArrayList<Distributor> distributorArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        httpRequest = new HttpRequest();
        sharedPreferences = getSharedPreferences("INFO",MODE_PRIVATE);
        ds_image = new ArrayList<>();

        token = sharedPreferences.getString("token","");
        httpRequest.callAPI().getListFruit("Bearer " + token).enqueue(getListFruitAPI);
        userListener();
    }
    private void userListener () {
        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this , AddFruitActivity.class));
            }
        });
    }



    Callback<Response<ArrayList<Fruit>>> getListFruitAPI = new Callback<Response<ArrayList<Fruit>>>() {
        @Override
        public void onResponse(Call<Response<ArrayList<Fruit>>> call, retrofit2.Response<Response<ArrayList<Fruit>>> response) {
            if (response.isSuccessful()) {
                if (response.body().getStatus() ==200) {
                    ArrayList<Fruit> ds = new ArrayList<>();
                    ds = response.body().getData();
                    getData(ds);
                    Log.d(TAG, "onResponse: "+ ds.size());
                }
            }
        }

        @Override
        public void onFailure(Call<Response<ArrayList<Fruit>>> call, Throwable t) {

        }
    };
    Callback<Response<Fruit>> responseFruitAPI  = new Callback<Response<Fruit>>() {
        @Override
        public void onResponse(Call<Response<Fruit>> call, retrofit2.Response<Response<Fruit>> response) {
            if (response.isSuccessful()) {
                if (response.body().getStatus() == 200) {
                    httpRequest.callAPI()
                            .getListFruit()
                            .enqueue(getListFruitAPI);
                    Toast.makeText(HomeActivity.this, response.body().getMessenger(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onFailure(Call<Response<Fruit>> call, Throwable t) {
            Log.e(TAG, "onFailure: "+t.getMessage() );
        }
    };
    private void getData (ArrayList<Fruit> ds) {
        adapter = new FruitAdapter(this, ds,this );
        binding.rcvFruit.setAdapter(adapter);
    }

    @Override
    public void delete(Fruit fruit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm delete");
        builder.setMessage("Are you sure you want to delete?");
        builder.setPositiveButton("yes", (dialog, which) -> {
            httpRequest.callAPI()
                    .deleteFruit(fruit.get_id())
                    .enqueue(responseFruitAPI);
            httpRequest.callAPI().getListFruit("Bearer " + token).enqueue(getListFruitAPI);
        });
        builder.setNegativeButton("no", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();


    }

    @Override
    public void edit(Fruit fruit) {
        showDialogEdit(fruit);
    }
    private void showDialogEdit(Fruit fruit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        binding1 = DialogEditBinding.inflate(LayoutInflater.from(this));
        builder.setView(binding1.getRoot());
        AlertDialog alertDialog = builder.create();

        ds_image = new ArrayList<>();
        configSpinner();

        binding1.edName.setText(fruit.getName());
        binding1.edQuantity.setText(fruit.getQuantity());
        binding1.edPrice.setText(fruit.getPrice());
        binding1.edStatus.setText(fruit.getStatus());
        binding1.edDescription.setText(fruit.getQuantity());

        String url  = fruit.getImage().get(0);
        String newUrl = url.replace("localhost", "192.168.1.65");
        Glide.with(this)
                .load(newUrl)
                .thumbnail(Glide.with(this).load(R.drawable.baseline_broken_image_24))
                .into(binding1.avatar);

        binding1.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        binding1.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String , RequestBody> mapRequestBody = new HashMap<>();
                String _name = binding1.edName.getText().toString().trim();
                String _quantity = binding1.edQuantity.getText().toString().trim();
                String _price = binding1.edPrice.getText().toString().trim();
                String _status = binding1.edStatus.getText().toString().trim();
                String _description = binding1.edDescription.getText().toString().trim();

                if (_name.isEmpty()||_quantity.isEmpty()||_price.isEmpty()||_status.isEmpty()||_description.isEmpty()) {
                    Toast.makeText(HomeActivity.this, "Ban can nhap du thong tin", Toast.LENGTH_SHORT).show();
                }   else {
                    mapRequestBody.put("name", getRequestBody(_name));
                    mapRequestBody.put("quantity", getRequestBody(_quantity));
                    mapRequestBody.put("price", getRequestBody(_price));
                    mapRequestBody.put("status", getRequestBody(_status));
                    mapRequestBody.put("description", getRequestBody(_description));
                    mapRequestBody.put("id_distributor", getRequestBody(id_Distributor));

                    ArrayList<MultipartBody.Part> _ds_image = new ArrayList<>();
                    ds_image.forEach(file1 -> {
                        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"),file1);
                        MultipartBody.Part multipartBodyPart = MultipartBody.Part.createFormData("image", file1.getName(),requestFile);
                        _ds_image.add(multipartBodyPart);
                    });
                    httpRequest.callAPI().updateFruit(fruit.get_id(), mapRequestBody, _ds_image).enqueue(responseFruit);
                    alertDialog.dismiss();
                }
            }
        });
        alertDialog.show();
    }
    Callback<Response<Fruit>> responseFruit = new Callback<Response<Fruit>>() {
        @Override
        public void onResponse(Call<Response<Fruit>> call, retrofit2.Response<Response<Fruit>> response) {
            if (response.isSuccessful()) {
                Log.d("123123", "onResponse: " + response.body().getStatus());
                if (response.body().getStatus()==200) {
                    httpRequest.callAPI().getListFruit("Bearer " + token).enqueue(getListFruitAPI);
                    Toast.makeText(HomeActivity.this, "Sua mới thành công", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }

        @Override
        public void onFailure(Call<Response<Fruit>> call, Throwable t) {
            httpRequest.callAPI().getListFruit("Bearer " + token).enqueue(getListFruitAPI);
            Toast.makeText(HomeActivity.this, "Sua thành công", Toast.LENGTH_SHORT).show();
            Log.e("zzzzzzzzzz", "onFailure: "+t.getMessage());
        }
    };
    private RequestBody getRequestBody(String value) {
        return RequestBody.create(MediaType.parse("multipart/form-data"),value);
    }
    private void configSpinner() {
        httpRequest.callAPI().getListDistributor().enqueue(getDistributorAPI);
        binding1.spDistributor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                id_Distributor = distributorArrayList.get(position).getId();
                Log.d("123123", "onItemSelected: " + id_Distributor);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding1.spDistributor.setSelection(0);
    }

    Callback<Response<ArrayList<Distributor>>> getDistributorAPI = new Callback<Response<ArrayList<Distributor>>>() {
        @Override
        public void onResponse(Call<Response<ArrayList<Distributor>>> call, retrofit2.Response<Response<ArrayList<Distributor>>> response) {
            if (response.isSuccessful()) {
                if (response.body().getStatus() == 200) {
                    distributorArrayList = response.body().getData();
                    String[] items = new String[distributorArrayList.size()];

                    for (int i = 0; i< distributorArrayList.size(); i++) {
                        items[i] = distributorArrayList.get(i).getName();
                    }
                    ArrayAdapter<String> adapterSpin = new ArrayAdapter<>(HomeActivity.this, android.R.layout.simple_spinner_item, items);
                    adapterSpin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    binding1.spDistributor.setAdapter(adapterSpin);
                }
            }
        }

        @Override
        public void onFailure(Call<Response<ArrayList<Distributor>>> call, Throwable t) {
            t.getMessage();
        }

    };

    private void chooseImage() {
//        if (ContextCompat.checkSelfPermission(RegisterActivity.this,
//                android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
        Log.d("123123", "chooseAvatar: " +123123);
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        getImage.launch(intent);
//        }else {
//            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
//
//        }
    }
    ActivityResultLauncher<Intent> getImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if (o.getResultCode() == Activity.RESULT_OK) {

                        Uri tempUri = null;

                        ds_image.clear();
                        Intent data = o.getData();
                        if (data.getClipData() != null) {
                            int count = data.getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                                tempUri = imageUri;

                                File file = createFileFormUri(imageUri, "image" + i);
                                ds_image.add(file);
                            }


                        } else if (data.getData() != null) {
                            // Trường hợp chỉ chọn một hình ảnh
                            Uri imageUri = data.getData();

                            tempUri = imageUri;
                            // Thực hiện các xử lý với imageUri
                            File file = createFileFormUri(imageUri, "image" );
                            ds_image.add(file);

                        }

                        if (tempUri != null) {
                            Glide.with(HomeActivity.this)
                                    .load(tempUri)
                                    .thumbnail(Glide.with(HomeActivity.this).load(R.drawable.baseline_broken_image_24))
                                    .centerCrop()
                                    .circleCrop()
                                    .skipMemoryCache(true)
//                                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                                .skipMemoryCache(true)
                                    .into(binding1.avatar);
                        }

                    }
                }
            });

    private File createFileFormUri (Uri path, String name) {
        File _file = new File(HomeActivity.this.getCacheDir(), name + ".png");
        try {
            InputStream in = HomeActivity.this.getContentResolver().openInputStream(path);
            OutputStream out = new FileOutputStream(_file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) >0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
            Log.d("123123", "createFileFormUri: " +_file);
            return _file;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    protected void onResume() {
        super.onResume();
        httpRequest.callAPI().getListFruit("Bearer "+token).enqueue(getListFruitAPI);
    }

}