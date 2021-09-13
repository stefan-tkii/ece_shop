package com.example.eceshop;

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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class AddProductFragment extends Fragment implements PhotoSourceDialog.onCameraClicked, PhotoSourceDialog.onGalleryClicked
{

    private ConstraintLayout mainContainer;
    private TextInputEditText nameText;
    private TextInputEditText shortDescText;
    private TextInputEditText longDescText;
    private AutoCompleteTextView categorySelector;
    private TextInputEditText priceText;
    private TextInputEditText stockText;
    private ImageView uploadedImageContainer;
    private AppCompatButton addProductBtn;

    private AddProductFragmentTouchListener listener;
    private CategorySelectorAdapter categoryAdapter;
    private AlertDialog progressDialog;
    private ImageCompressor compressor;

    private ActivityResultLauncher<String[]> permissionResult;
    private ActivityResultLauncher<String> readPermissionResult;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    private String selectedCategory;
    private Drawable[] categoryIcons;
    private String[] options;
    private PhotoSourceDialog selectionDialog;
    private StorageReference storageReference;
    private DatabaseReference productsReference;

    private String photoImagePath;
    private String compressedImagePath;
    private Uri photoUri;

    private String name;
    private String shortDesc;
    private String longDesc;
    private String priceInput;
    private String stockInput;
    private String imageUrl;

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.add_product_layout, container, false);
        mainContainer = root.findViewById(R.id.addProductContainer);
        nameText = root.findViewById(R.id.name_input);
        shortDescText = root.findViewById(R.id.shortDesc_input);
        longDescText = root.findViewById(R.id.longDesc_input);
        categorySelector = root.findViewById(R.id.category_select);
        priceText = root.findViewById(R.id.price_input);
        stockText = root.findViewById(R.id.stock_input);
        uploadedImageContainer = root.findViewById(R.id.uploaded_image);
        addProductBtn = root.findViewById(R.id.addProductButton);

        photoImagePath = "";
        compressedImagePath = "";
        photoUri = null;

        name = "";
        shortDesc = "";
        longDesc = "";
        priceInput = "";
        stockInput = "";
        imageUrl = "";

        compressor = new ImageCompressor(getActivityNonNull());

        progressDialog = new SpotsDialog.Builder()
                .setContext(getActivityNonNull())
                .setCancelable(false).setTheme(R.style.CustomProgressDialog)
                .build();

        storageReference = FirebaseStorage.getInstance("gs://ece-shop.appspot.com/").getReference("product_images");
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://ece-shop-default-rtdb.europe-west1.firebasedatabase.app/");
        productsReference = db.getReference("Products");

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
                    Toast.makeText(getActivityNonNull(),"You need to provide a camera and external storage access permission to the application in order to use the camera as an image source.",
                            Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getActivityNonNull(),"You need to provide an external storage access permission for reading to the application in order to use the gallery as an image source.",
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
                            getActivityNonNull().getContentResolver().notifyChange(photoUri, null);
                            ContentResolver cr = getActivityNonNull().getContentResolver();
                            Bitmap bitmap;
                            try
                            {
                                bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, photoUri);
                                uploadedImageContainer.setImageBitmap(bitmap);
                            }
                            catch (Exception e)
                            {
                                Toast.makeText(getActivityNonNull(), "Failed to load the created photo.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            String errorText = "Error: " + result.getResultCode() + " | " + "Could not obtain the image data.";
                            Toast.makeText(getActivityNonNull(), errorText, Toast.LENGTH_SHORT).show();
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
                        InputStream iStream =  getActivityNonNull().getContentResolver().openInputStream(photoUri);
                        byte[] inputData = getBytes(iStream);
                        OutputStream out;
                        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        String imageFileName = timeStamp + ".jpg";
                        File storageDir = getActivityNonNull().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
                    Toast.makeText(getActivityNonNull(), errorText, Toast.LENGTH_SHORT).show();
                }
            }
        });

        selectionDialog = new PhotoSourceDialog(getActivityNonNull(), "Photo source", "Select from where do you wish to select a photo to upload.",
                this, this);

        mainContainer.setOnTouchListener(new View.OnTouchListener()
        {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                listener.onAddProductFragmentTouch();
                InputMethodManager imm = (InputMethodManager) getActivityNonNull().getSystemService(Context.INPUT_METHOD_SERVICE);
                View focusedView = getActivityNonNull().getCurrentFocus();
                if (focusedView != null)
                {
                    imm.hideSoftInputFromWindow(getActivityNonNull().getCurrentFocus().getWindowToken(), 0);
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
                    View focusedView = getActivityNonNull().getCurrentFocus();
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
                    InputMethodManager imm = (InputMethodManager) getActivityNonNull().getSystemService(Context.INPUT_METHOD_SERVICE);
                    View focusedView = getActivityNonNull().getCurrentFocus();
                    if (focusedView != null)
                    {
                        imm.hideSoftInputFromWindow(getActivityNonNull().getCurrentFocus().getWindowToken(), 0);
                        stockText.clearFocus();
                    }
                    return true;
                }
                return false;
            }
        });

        addProductBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                addProductToDb();
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

        selectedCategory = "Clothes";

        categoryIcons = loadCategoryIcons();
        options = getResources().getStringArray(R.array.productCategories);

        return root;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        categoryAdapter = new CategorySelectorAdapter(getActivityNonNull(), options, categoryIcons);
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
                    Log.e("FF", selectedCategory);
                }
            }
        });
    }

    private void addProductToDb()
    {
        name = nameText.getText().toString();
        shortDesc = shortDescText.getText().toString();
        longDesc = longDescText.getText().toString();
        priceInput = priceText.getText().toString();
        stockInput = stockText.getText().toString();

        if(TextUtils.isEmpty(name.trim()))
        {
            CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "Please provide a product name.", false);
            dialog.show();
        }
        else if(TextUtils.isEmpty(shortDesc))
        {
            CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "Please provide a short description for the product.", false);
            dialog.show();
        }
        else if(TextUtils.isEmpty(longDesc))
        {
            CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "Please a long description for the product.", false);
            dialog.show();
        }
        else if(TextUtils.isEmpty(priceInput))
        {
            CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "Please provide a price for the product.", false);
            dialog.show();
        }
        else if(TextUtils.isEmpty(stockInput))
        {
            CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "Please provide a stock value for the product.", false);
            dialog.show();
        }
        else if(photoUri == null)
        {
            CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Input error", "Please provide an image for the product.", false);
            dialog.show();
        }
        else
        {
            progressDialog.show();
            productsReference.orderByChild("name").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot)
                {
                    if(snapshot.exists())
                    {
                        long count = snapshot.getChildrenCount();
                        if(count != 0)
                        {
                            progressDialog.dismiss();
                            CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Product error", "A product already exists with this name.", false);
                            dialog.show();
                        }
                        else
                        {
                            uploadImage();
                        }
                    }
                    else
                    {
                        uploadImage();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error)
                {
                    progressDialog.dismiss();
                    CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Product error", error.getMessage(), false);
                    dialog.show();
                }
            });
        }
    }

    private void uploadImage()
    {
        String imgName = name.replace(" ", "").toLowerCase() + "." + getExtensionFromUri(photoUri);
        StorageReference ref = storageReference.child(imgName);
        String latestPath = compressor.compressImage(photoImagePath);
        File file = new File(latestPath);
        Uri compressedUri = FileProvider.getUriForFile(
                getActivityNonNull(),
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
                        addProductToDatabase();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                deleteTemporaryImages();
                uploadedImageContainer.setImageResource(R.drawable.upload_placeholder);
                progressDialog.dismiss();
                CustomDialog dialog = new CustomDialog(getActivityNonNull(), "File error", e.getLocalizedMessage(), false);
                dialog.show();
            }
        });
    }

    private void addProductToDatabase()
    {
        ProductDb p = new ProductDb(name, shortDesc, longDesc, imageUrl, Double.parseDouble(priceInput), 0,
                selectedCategory, Integer.parseInt(stockInput));
        productsReference.push().setValue(p).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    progressDialog.dismiss();
                    CustomDialog dialog = new CustomDialog(getActivityNonNull(), "Success", "This product is successfully added to the database.", true);
                    dialog.show();
                    nameText.setText("");
                    shortDescText.setText("");
                    longDescText.setText("");
                    priceText.setText("");
                    stockText.setText("");
                    uploadedImageContainer.setImageResource(R.drawable.upload_placeholder);
                }
                else
                {
                    FirebaseStorage storage = FirebaseStorage.getInstance("gs://ece-shop.appspot.com/");
                    StorageReference ref = storage.getReferenceFromUrl(imageUrl);
                    ref.delete().addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            progressDialog.dismiss();
                            CustomDialog dialog = new CustomDialog(getActivityNonNull(), "File error", "Could not create the product.", false);
                            dialog.show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if(context instanceof AddProductFragmentTouchListener)
        {
            listener = (AddProductFragmentTouchListener) context;
        }
        else
        {
            throw  new RuntimeException(context.toString() + " must implement the AddProductFragmentTouchListener.");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        deleteTemporaryImages();
        listener = null;
    }

    protected FragmentActivity getActivityNonNull()
    {
        if (super.getActivity() != null)
        {
            return super.getActivity();
        }
        else {
            throw new RuntimeException("null returned from getActivity()");
        }
    }

    private String getExtensionFromUri(Uri uri)
    {
        ContentResolver cr = getActivityNonNull().getContentResolver();
        MimeTypeMap typeMap = MimeTypeMap.getSingleton();
        return typeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    @Override
    public void onCameraOptionClicked()
    {
        selectionDialog.dismiss();
        if((ContextCompat.checkSelfPermission(getActivityNonNull(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(getActivityNonNull(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))
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
        File storageDir = getActivityNonNull().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        photoImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
        File file = new File(photoImagePath);
        photoUri = FileProvider.getUriForFile(
                getActivityNonNull(),
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
        if(ContextCompat.checkSelfPermission(getActivityNonNull(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
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

    public interface AddProductFragmentTouchListener
    {
        void onAddProductFragmentTouch();
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
                icons[i] = ContextCompat.getDrawable(getActivityNonNull(), id);
            }
        }
        ta.recycle();
        return icons;
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

}
