package edu.poly.chatapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class enviarimage extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText messageEditText;
    private ImageView selectedImageView;
    private Uri selectedImageUri;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(edu.poly.chatapp.R.layout.activity_chat); // Reemplaza R.layout.activity_chat por tu valor real de diseño

        selectedImageView = findViewById(edu.poly.chatapp.R.id.selectedImageView); // Reemplaza R.id.selectedImageView por tu valor real de identificador
        Button selectImageButton = findViewById(edu.poly.chatapp.R.id.selectImageButton); // Reemplaza R.id.selectImageButton por tu valor real de identificador
        Button sendButton = findViewById(edu.poly.chatapp.R.id.sendButton); // Reemplaza R.id.sendButton por tu valor real de identificador

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageWithImage();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                selectedImageView.setImageBitmap(bitmap);
                selectedImageView.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessageWithImage() {
        String message = messageEditText.getText().toString().trim();

        if (message.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "selecciona una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();

            uploadImage(message, imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uploadImage(String message, byte[] imageBytes) {
        OkHttpClient client = new OkHttpClient();
        MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("message", message)
                .addFormDataPart("image", "image.jpg",
                        RequestBody.create(MEDIA_TYPE_JPEG, imageBytes))
                .build();

        Request request = new Request.Builder()
                .url("http://your-server-url/send-message") // Cambia esto a tu URL del servidor
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(enviarimage.this, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(enviarimage.this, "Mensaje enviado con éxito", Toast.LENGTH_SHORT).show();
                            messageEditText.setText("");
                            selectedImageView.setImageBitmap(null);
                            selectedImageView.setVisibility(View.GONE);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(enviarimage.this, "Error en la respuesta del servidor", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}


