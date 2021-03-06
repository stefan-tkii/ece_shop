package com.example.eceshop;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import maes.tech.intentanim.CustomIntent;

public class EditProductActivity extends AppCompatActivity implements PhotoSourceDialog.onCameraClicked, PhotoSourceDialog.onGalleryClicked
{

    private Toolbar toolbar;
    private ConstraintLayout mainContainer;
    private TextInputEditText nameText;
    private TextInputEditText shortDescText;
    private TextInputEditText longDescText;
    private AutoCompleteTextView categorySelector;
    private TextInputEditText priceText;
    private TextInputEditText stockText;
    private ImageView uploadedImageContainer;
    private AppCompatButton editProductBtn;

    private AlertDialog progressDialog;
    private ImageCompressor compressor;

    private CategorySelectorAdapter categoryAdapter;

    private static final String EDIT_KEY = "com.example.eceshop.PRODUCT_EDIT";
    private static final String CLICKED_KEY = "com.example.eceshop.CLICKED_PRODUCT";
    private static final String FROM_EDIT_KEY = "com.example.eceshop.FROM_EDIT";
    private static final String ADMIN_KEY = "com.example.eceshop.Admin";

    private Product model;
    private Drawable[] categoryIcons;
    private String[] options;

    private PhotoSourceDialog selectionDialog;
    private StorageReference storageReference;
    private DatabaseReference productsReference;

    private String photoImagePath;
    private String compressedImagePath;
    private Uri photoUri;

    private ActivityResultLauncher<String[]> permissionResult;
    private ActivityResultLauncher<String> readPermissionResult;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    private boolean flag;

    private boolean priceUpdated;
    private boolean stockUpdated;
    private Product product;
    private MessagingApiManager messagingApiManager;

    private String name;
    private String shortDesc;
    private String longDesc;
    private String priceInput;
    private String stockInput;
    private String selectedCategory;
    private String imageUrl;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        changeStatusBarColor();
        toolbar = findViewById(R.id.edit_product_toolbar);
        mainContainer = findViewById(R.id.edit_product_container);
        nameText = findViewById(R.id.edit_name_input);
        shortDescText = findViewById(R.id.edit_shortDesc_input);
        longDescText = findViewById(R.id.edit_longDesc_input);
        categorySelector = findViewById(R.id.edit_category_select);
        priceText = findViewById(R.id.edit_price_input);
        stockText = findViewById(R.id.edit_stock_input);
        uploadedImageContainer = findViewById(R.id.edit_uploaded_image);
        editProductBtn = findViewById(R.id.editProductButton);

        flag = true;
        photoUri = null;
        photoImagePath = "";
        compressedImagePath = "";

        priceUpdated = false;
        stockUpdated = false;

        messagingApiManager = new MessagingApiManager();

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_arrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        compressor = new ImageCompressor(this);

        progressDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setCancelable(false).setTheme(R.style.CustomProgressDialog)
                .build();

        mainContainer.setOnTouchListener(new View.OnTouchListener()
        {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                InputMethodManager imm = (InputMethodManager) EditProductActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                View focusedView = EditProductActivity.this.getCurrentFocus();
                if (focusedView != null)
                {
                    imm.hideSoftInputFromWindow(EditProductActivity.this.getCurrentFocus().getWindowToken(), 0);
                    if(nameText.isFocused())
                    {
                        nameText.clearFocus();
                    }
                    else if(shortDescText.isFocused())
                    {
                        shortDescText.clearFocus();
                    }
                    else if(longDescText.isFocused())
                    {
                        longDescText.clearFocus();
                    }
                    else if(categorySelector.isFocused())
                    {
                        categorySelector.clearFocus();
                    }
                    else if(priceText.isFocused())
                    {
                        priceText.clearFocus();
                    }
                    else if(stockText.isFocused())
                    {
                        stockText.clearFocus();
                    }
                    else if(uploadedImageContainer.isFocused())
                    {
                        uploadedImageContainer.clearFocus();
                    }
                    return true;
                }
                else
                {
                    return false;
                }
            }
        });

        priceText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_NEXT)
                {
                    View focusedView = EditProductActivity.this.getCurrentFocus();
                    if (focusedView != null)
                    {
                        priceText.clearFocus();
                        stockText.requestFocus();
                    }
                    return true;
                }
                return false;
            }
        });

        stockText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    InputMethodManager imm = (InputMethodManager) EditProductActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    View focusedView = EditProductActivity.this.getCurrentFocus();
                    if (focusedView != null)
                    {
                        imm.hideSoftInputFromWindow(EditProductActivity.this.getCurrentFocus().getWindowToken(), 0);
                        stockText.clearFocus();
                    }
                    return true;
                }
                return false;
            }
        });

        permissionResult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>()
        {
            @Override
            public void onActivityResult(Map<String, Boolean> result)
            {
                if(result.get(Manifest.permission.CAMERA) && result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    openCamera();
                }
                else
                {
                    Toast.makeText(EditProductActivity.this,"You need to provide a camera and external storage access permission to the application in order to use the camera as an image source.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>()
        {
            @Override
            public void onActivityResult(ActivityResult result)
            {
                if (result.getResultCode() == Activity.RESULT_OK)
                {
                    EditProductActivity.this.getContentResolver().notifyChange(photoUri, null);
                    ContentResolver cr = EditProductActivity.this.getContentResolver();
                    Bitmap bitmap;
                    try
                    {
                        bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, photoUri);
                        uploadedImageContainer.setImageBitmap(bitmap);
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(EditProductActivity.this, "Failed to load the created photo.", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    String errorText = "Error: " + result.getResultCode() + " | " + "Could not obtain the image data.";
                    Toast.makeText(EditProductActivity.this, errorText, Toast.LENGTH_SHORT).show();
                }
            }
        });

        readPermissionResult = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>()
        {
            @Override
            public void onActivityResult(Boolean result)
            {
                if(result)
                {
                    openGallery();
                }
                else
                {
                    Toast.makeText(EditProductActivity.this,"You need to provide an external storage access permission for reading to the application in order to use the gallery as an image source.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>()
        {
            @Override
            public void onActivityResult(ActivityResult result)
            {
                if(result.getResultCode() == Activity.RESULT_OK)
                {
                    photoUri = result.getData().getData();
                    try
                    {
                        InputStream iStream =  EditProductActivity.this.getContentResolver().openInputStream(photoUri);
                        byte[] inputData = getBytes(iStream);
                        OutputStream out;
                        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        String imageFileName = timeStamp + ".jpg";
                        File storageDir = EditProductActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                        photoImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
                        File file = new File(photoImagePath);
                        out = new FileOutputStream(file);
                        out.write(inputData);
                        out.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    uploadedImageContainer.setImageURI(photoUri);
                }
                else
                {
                    String errorText = "Error: " + result.getResultCode() + " | " + "Could not obtain the image data.";
                    Toast.makeText(EditProductActivity.this, errorText, Toast.LENGTH_SHORT).show();
                }
            }
        });

        uploadedImageContainer.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(nameText.isFocused())
                {
                    nameText.clearFocus();
                }
                else if(shortDescText.isFocused())
                {
                    shortDescText.clearFocus();
                }
                else if(longDescText.isFocused())
                {
                    longDescText.clearFocus();
                }
                else if(categorySelector.isFocused())
                {
                    categorySelector.clearFocus();
                }
                else if(priceText.isFocused())
                {
                    priceText.clearFocus();
                }
                else if(stockText.isFocused())
                {
                    stockText.clearFocus();
                }
                selectionDialog.show();
            }
        });

        editProductBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                doUpdates();
            }
        });

        storageReference = FirebaseStorage.getInstance("gs://ece-shop.appspot.com/").getReference("product_images");
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
        productsReference = db.getReference("Products");

        selectionDialog = new PhotoSourceDialog(this, "Photo source", "Select from where do you wish to select a photo to upload.",
                this, this);

        model = getIntent().getParcelableExtra(EDIT_KEY);
        if(model != null)
        {
            categoryIcons = loadCategoryIcons();
            options = getResources().getStringArray(R.array.productCategories);
            loadData();
        }
        else
        {
            CustomDialog dialog = new CustomDialog(EditProductActivity.this, "Data error", "Could not fetch the needed data.", false);
            dialog.show();
        }
    }

    private void loadData()
    {
        nameText.setText(model.getName());
        shortDescText.setText(model.getShortDesc());
        longDescText.setText(model.getLongDesc());
        priceText.setText(String.valueOf(model.getPrice()));
        stockText.setText(String.valueOf(model.getInStock()));
        Picasso.get().load(model.getImgUri()).placeholder(R.drawable.load_placeholder)
                .into(uploadedImageContainer);
    }

    @Override
    public void onCameraOptionClicked()
    {
        selectionDialog.dismiss();
        if((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
        {
            String[] permissionArray =
                    {
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    };
            permissionResult.launch(permissionArray);
        }
        else
        {
            openCamera();
        }
    }

    private void openCamera()
    {
        deleteTemporaryImages();
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        photoImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
        File file = new File(photoImagePath);
        photoUri = FileProvider.getUriForFile(
                this,
                "com.example.eceshop.fileprovider",
                file);
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camera.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        cameraLauncher.launch(camera);
    }

    @Override
    public void onGalleryOptionClicked()
    {
        selectionDialog.dismiss();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            readPermissionResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        else
        {
            openGallery();
        }
    }

    private void openGallery()
    {
        deleteTemporaryImages();
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(gallery);
    }

    private void doUpdates()
    {
        name = nameText.getText().toString();
        shortDesc = shortDescText.getText().toString();
        longDesc = longDescText.getText().toString();
        priceInput = priceText.getText().toString();
        stockInput = stockText.getText().toString();

        if(TextUtils.isEmpty(name.trim()))
        {
            CustomDialog dialog = new CustomDialog(EditProductActivity.this, "Input error", "Please provide a product name.", false);
            dialog.show();
        }
        else if(TextUtils.isEmpty(shortDesc))
        {
            CustomDialog dialog = new CustomDialog(EditProductActivity.this, "Input error", "Please provide a short description for the product.", false);
            dialog.show();
        }
        else if(TextUtils.isEmpty(longDesc))
        {
            CustomDialog dialog = new CustomDialog(EditProductActivity.this, "Input error", "Please a long description for the product.", false);
            dialog.show();
        }
        else if(TextUtils.isEmpty(priceInput))
        {
            CustomDialog dialog = new CustomDialog(EditProductActivity.this, "Input error", "Please provide a price for the product.", false);
            dialog.show();
        }
        else if(TextUtils.isEmpty(stockInput))
        {
            CustomDialog dialog = new CustomDialog(EditProductActivity.this, "Input error", "Please provide a stock value for the product.", false);
            dialog.show();
        }
        else
        {
            boolean check = checkForChanges();
            if(check)
            {
                progressDialog.show();
                if(photoUri != null)
                {
                    deleteOldImage();
                }
                else
                {
                    updateProductWithOldImage();
                }
            }
            else
            {
                CustomDialog dialog = new CustomDialog(EditProductActivity.this, "Input error", "Cannot update, no changes have been made.", false);
                dialog.show();
            }
        }
    }

    private void updateProductWithOldImage()
    {
        double price = Double.parseDouble(priceInput);
        int stock = Integer.parseInt(stockInput);
        final HashMap<String, Object> updatesMap = new HashMap<>();
        ProductDb updatedProduct = new ProductDb(name, shortDesc, longDesc, model.getImgUri(), price, model.getOrders(), selectedCategory,
                stock);

        if((model.getPrice() != price) && (model.getInStock() != stock))
        {
            priceUpdated = true;
            stockUpdated = true;
        }
        else if(model.getPrice() != price)
        {
            priceUpdated = true;
            stockUpdated = false;
        }
        else if(model.getInStock() != stock)
        {
            priceUpdated = false;
            stockUpdated = true;
        }
        else
        {
            priceUpdated = false;
            stockUpdated = false;
        }
        product = new Product(model.getProductId(), updatedProduct.getName(), updatedProduct.getShortDesc(), updatedProduct.getLongDesc(),
                updatedProduct.getImgUri(), updatedProduct.getPrice(), updatedProduct.getOrders(), updatedProduct.getCategoryId(), updatedProduct.getInStock());

        updatesMap.put(model.getProductId(), updatedProduct);
        productsReference.updateChildren(updatesMap).addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void aVoid)
            {
                progressDialog.dismiss();
                model.setName(name);
                model.setShortDesc(shortDesc);
                model.setLongDesc(longDesc);
                model.setPrice(price);
                model.setInStock(stock);
                model.setCategoryId(selectedCategory);

                if(priceUpdated && stockUpdated)
                {
                    messagingApiManager.sendUpdatedProductInfo(product, 1);
                }
                else if(priceUpdated)
                {
                    messagingApiManager.sendUpdatedProductInfo(product, 2);
                }
                else if(stockUpdated)
                {
                    messagingApiManager.sendUpdatedProductInfo(product, 3);
                }

                Intent intent = new Intent(EditProductActivity.this, ProductDetailsActivity.class);
                intent.putExtra(CLICKED_KEY, model);
                intent.putExtra(ADMIN_KEY, true);
                intent.putExtra(FROM_EDIT_KEY, true);
                startActivity(intent);
                CustomIntent.customType(EditProductActivity.this, "right-to-left");
                finish();
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                progressDialog.dismiss();
                CustomDialog dialog = new CustomDialog(EditProductActivity.this, "Database error", e.getMessage(), false);
                dialog.show();
            }
        });
    }

    private boolean checkForChanges()
    {
        double p = Double.parseDouble(priceInput);
        int s = Integer.parseInt(stockInput);
        if(name.equals(model.getName()) && longDesc.equals(model.getLongDesc()) && shortDesc.equals(model.getShortDesc())
        && (p == model.getPrice()) && (s == model.getInStock()) && (photoUri == null) && selectedCategory.equals(model.getCategoryId()))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    private void deleteOldImage()
    {
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://ece-shop.appspot.com/");
        StorageReference ref = storage.getReferenceFromUrl(model.getImgUri());
        ref.delete().addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void aVoid)
            {
                uploadNewImage();
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                progressDialog.dismiss();
                CustomDialog dialog = new CustomDialog(EditProductActivity.this, "Database error", e.getMessage(), false);
                dialog.show();
            }
        });
    }

    private void uploadNewImage()
    {
        String imgName = name.replace(" ", "").toLowerCase() + "." + getExtensionFromUri(photoUri);
        StorageReference ref = storageReference.child(imgName);
        String latestPath = compressor.compressImage(photoImagePath);
        File file = new File(latestPath);
        Uri compressedUri = FileProvider.getUriForFile(
                EditProductActivity.this,
                "com.example.eceshop.fileprovider",
                file);

        ref.putFile(compressedUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                deleteTemporaryImages();
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                {
                    @Override
                    public void onSuccess(Uri uri)
                    {
                        imageUrl = uri.toString();
                        updateProductWithNewImage();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                deleteTemporaryImages();
                Picasso.get().load(model.getImgUri()).placeholder(R.drawable.load_placeholder)
                        .into(uploadedImageContainer);
                progressDialog.dismiss();
                CustomDialog dialog = new CustomDialog(EditProductActivity.this, "File error", e.getLocalizedMessage(), false);
                dialog.show();
            }
        });
    }

    private void updateProductWithNewImage()
    {
        double price = Double.parseDouble(priceInput);
        int stock = Integer.parseInt(stockInput);
        final HashMap<String, Object> updatesMap = new HashMap<>();
        ProductDb updatedProduct = new ProductDb(name, shortDesc, longDesc, imageUrl, price, model.getOrders(), selectedCategory,
                stock);

        if((model.getPrice() != price) && (model.getInStock() != stock))
        {
            priceUpdated = true;
            stockUpdated = true;
        }
        else if(model.getPrice() != price)
        {
            priceUpdated = true;
            stockUpdated = false;
        }
        else if(model.getInStock() != stock)
        {
            priceUpdated = false;
            stockUpdated = true;
        }
        else
        {
            priceUpdated = false;
            stockUpdated = false;
        }
        product = new Product(model.getProductId(), updatedProduct.getName(), updatedProduct.getShortDesc(), updatedProduct.getLongDesc(),
                updatedProduct.getImgUri(), updatedProduct.getPrice(), updatedProduct.getOrders(), updatedProduct.getCategoryId(), updatedProduct.getInStock());

        updatesMap.put(model.getProductId(), updatedProduct);
        productsReference.updateChildren(updatesMap).addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void aVoid)
            {
                progressDialog.dismiss();
                model.setName(name);
                model.setShortDesc(shortDesc);
                model.setLongDesc(longDesc);
                model.setImgUri(imageUrl);
                model.setPrice(price);
                model.setInStock(stock);
                model.setCategoryId(selectedCategory);

                if(priceUpdated && stockUpdated)
                {
                    messagingApiManager.sendUpdatedProductInfo(product, 1);
                }
                else if(priceUpdated)
                {
                    messagingApiManager.sendUpdatedProductInfo(product, 2);
                }
                else if(stockUpdated)
                {
                    messagingApiManager.sendUpdatedProductInfo(product, 3);
                }

                Intent intent = new Intent(EditProductActivity.this, ProductDetailsActivity.class);
                intent.putExtra(CLICKED_KEY, model);
                intent.putExtra(ADMIN_KEY, true);
                intent.putExtra(FROM_EDIT_KEY, true);
                startActivity(intent);
                CustomIntent.customType(EditProductActivity.this, "right-to-left");
                finish();
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                progressDialog.dismiss();
                CustomDialog dialog = new CustomDialog(EditProductActivity.this, "Database error", e.getMessage(), false);
                dialog.show();
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        categoryAdapter = new CategorySelectorAdapter(this, options, categoryIcons);
        categorySelector.setAdapter(categoryAdapter);

        categorySelector.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String item = categoryAdapter.getItem(position);
                categorySelector.clearFocus();
                if(!selectedCategory.equals(item))
                {
                    selectedCategory = item;
                }
            }
        });
        if(flag)
        {
            flag = false;
            selectedCategory = model.getCategoryId();
            categorySelector.setText(selectedCategory, false);
        }
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(EditProductActivity.this, ProductDetailsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        CustomIntent.customType(EditProductActivity.this, "right-to-left");
        finish();
    }

    private String getExtensionFromUri(Uri uri)
    {
        ContentResolver cr = EditProductActivity.this.getContentResolver();
        MimeTypeMap typeMap = MimeTypeMap.getSingleton();
        return typeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    private byte[] getBytes(InputStream inputStream) throws IOException
    {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1)
        {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void deleteTemporaryImages()
    {
        if(!photoImagePath.equals(""))
        {
            File f1 = new File(photoImagePath);
            boolean result = f1.delete();
            if(result)
            {
                photoImagePath = "";
            }
        }
        if(!compressedImagePath.equals(""))
        {
            File f2 = new File(compressedImagePath);
            boolean result = f2.delete();
            if(result)
            {
                compressedImagePath = "";
            }
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        deleteTemporaryImages();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        deleteTemporaryImages();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                Intent intent = new Intent(EditProductActivity.this, ProductDetailsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                CustomIntent.customType(EditProductActivity.this, "right-to-left");
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeStatusBarColor()
    {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    private Drawable[] loadCategoryIcons()
    {
        TypedArray ta = getResources().obtainTypedArray(R.array.category_drawables);
        Drawable[] icons = new Drawable[ta.length()];
        for(int i=0;i<ta.length();i++)
        {
            int id = ta.getResourceId(i, 0);
            if(id != 0)
            {
                icons[i] = ContextCompat.getDrawable(this, id);
            }
        }
        ta.recycle();
        return icons;
    }

}