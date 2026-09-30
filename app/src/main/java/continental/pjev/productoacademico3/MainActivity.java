package continental.pjev.productoacademico3;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // =====================================================
    // 1. DECLARACIÓN DE LOS COMPONENTES DE LA INTERFAZ
    // =====================================================

    // Campos del formulario
    private EditText txtNombre, txtCorreo, txtMensaje;

    // Botones para guardar y mostrar los registros
    private Button btnGuardar, btnMostrar;

    // Lista donde se mostrarán los datos obtenidos de Firebase
    private ListView listaDatos;

    // Referencia a Firebase Realtime Database
    private DatabaseReference databaseReference;


    // =====================================================
    // 2. MÉTODO PRINCIPAL DE LA APLICACIÓN
    // =====================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Relaciona esta clase Java con el archivo activity_main.xml
        setContentView(R.layout.activity_main);


        // =================================================
        // 3. INICIALIZAR FIREBASE REALTIME DATABASE
        // =================================================

        // Se crea una referencia al nodo "Usuarios"
        databaseReference = FirebaseDatabase.getInstance()
                .getReference("Usuarios");


        // =================================================
        // 4. REFERENCIAS A LOS ELEMENTOS DE LA INTERFAZ
        // =================================================

        txtNombre = findViewById(R.id.txtNombre);
        txtCorreo = findViewById(R.id.txtCorreo);
        txtMensaje = findViewById(R.id.txtMensaje);

        btnGuardar = findViewById(R.id.btnGuardar);
        btnMostrar = findViewById(R.id.btnMostrar);

        listaDatos = findViewById(R.id.listaDatos);


        // =================================================
        // 5. BOTÓN PARA GUARDAR DATOS
        // =================================================

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Obtenemos la información ingresada en el formulario
                String nombre = txtNombre.getText().toString().trim();
                String correo = txtCorreo.getText().toString().trim();
                String mensaje = txtMensaje.getText().toString().trim();


                // Verificamos que todos los campos tengan información
                if (nombre.isEmpty() ||
                        correo.isEmpty() ||
                        mensaje.isEmpty()) {

                    Toast.makeText(
                            MainActivity.this,
                            "Por favor, complete todos los campos",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                // Verificamos que el correo tenga un formato válido
                if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {

                    Toast.makeText(
                            MainActivity.this,
                            "Ingrese un correo válido",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                // Generamos una clave única para cada registro
                String usuarioId = databaseReference.push().getKey();

                // Creamos un objeto Usuario con los datos ingresados
                Usuario usuario = new Usuario(
                        nombre,
                        correo,
                        mensaje
                );


                // Verificamos que Firebase haya generado la clave
                if (usuarioId != null) {

                    // Guardamos el objeto Usuario en Firebase
                    databaseReference.child(usuarioId)
                            .setValue(usuario)

                            // Se ejecuta cuando Firebase confirma el guardado
                            .addOnSuccessListener(aVoid -> {

                                Toast.makeText(
                                        MainActivity.this,
                                        "Datos guardados correctamente",
                                        Toast.LENGTH_SHORT
                                ).show();


                                // Limpiamos los campos después de guardar
                                txtNombre.setText("");
                                txtCorreo.setText("");
                                txtMensaje.setText("");
                            })

                            // Se ejecuta si ocurre un error al guardar
                            .addOnFailureListener(e -> {

                                Toast.makeText(
                                        MainActivity.this,
                                        "Error al guardar los datos",
                                        Toast.LENGTH_SHORT
                                ).show();
                            });
                }
            }
        });


        // =================================================
        // 6. BOTÓN PARA MOSTRAR LOS DATOS
        // =================================================

        btnMostrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Leemos una vez todos los datos del nodo "Usuarios"
                databaseReference.addListenerForSingleValueEvent(
                        new ValueEventListener() {

                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot dataSnapshot) {

                                // Lista temporal para almacenar los registros
                                ArrayList<String> usuariosList =
                                        new ArrayList<>();


                                // Recorremos cada registro guardado en Firebase
                                for (DataSnapshot usuarioSnapshot :
                                        dataSnapshot.getChildren()) {

                                    // Convertimos cada registro en un objeto Usuario
                                    Usuario usuario =
                                            usuarioSnapshot.getValue(
                                                    Usuario.class
                                            );


                                    // Verificamos que el usuario exista
                                    if (usuario != null) {

                                        // Agregamos sus datos al listado
                                        usuariosList.add(
                                                "Nombre: " +
                                                        usuario.getNombre() +
                                                        "\nCorreo: " +
                                                        usuario.getCorreo() +
                                                        "\nMensaje: " +
                                                        usuario.getMensaje()
                                        );
                                    }
                                }


                                // Si no existen registros mostramos un mensaje
                                if (usuariosList.isEmpty()) {

                                    Toast.makeText(
                                            MainActivity.this,
                                            "No se encontraron registros",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }


                                // Adaptador para mostrar los registros
                                // dentro del ListView
                                ArrayAdapter<String> adapter =
                                        new ArrayAdapter<>(
                                                MainActivity.this,
                                                android.R.layout
                                                        .simple_list_item_1,
                                                usuariosList
                                        );


                                // Mostramos los datos en el ListView
                                listaDatos.setAdapter(adapter);
                            }


                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {

                                // Mensaje en caso de error al leer Firebase
                                Toast.makeText(
                                        MainActivity.this,
                                        "Error al leer los datos",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
            }
        });
    }


    // =====================================================
    // 7. CLASE MODELO USUARIO
    // =====================================================

    /*
     * Esta clase representa la estructura de cada registro
     * que será almacenado en Firebase Realtime Database.
     */
    public static class Usuario {

        private String nombre;
        private String correo;
        private String mensaje;


        // Constructor vacío requerido por Firebase
        public Usuario() {
        }


        // Constructor que recibe los datos del formulario
        public Usuario(
                String nombre,
                String correo,
                String mensaje) {

            this.nombre = nombre;
            this.correo = correo;
            this.mensaje = mensaje;
        }


        // Métodos utilizados para obtener los datos
        public String getNombre() {
            return nombre;
        }

        public String getCorreo() {
            return correo;
        }

        public String getMensaje() {
            return mensaje;
        }
    }
}