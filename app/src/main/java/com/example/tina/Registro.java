package com.example.tina;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class Registro extends AppCompatActivity {

    private EditText edt_nome;
    private EditText edt_telefone;
    private EditText edt_data_nascimento;
    private EditText edt_CPF;
    private EditText edt_email_registro;
    private EditText edt_senha_registro;
    private EditText edt_confirmar_senha;
    private FirebaseAuth mAuth;

    public class MaskUtils {

        public static TextWatcher insertMask(final EditText editText, final String mask) {
            return new TextWatcher() {
                private boolean isUpdating = false;
                private String current = "";

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isUpdating) {
                        isUpdating = false;
                        return;
                    }

                    String text = s.toString();
                    String cleanText = text.replaceAll("[^0-9]*", "");

                    isUpdating = true;

                    String formatted = applyMask(cleanText, mask);
                    current = formatted;
                    editText.setText(formatted);
                    editText.setSelection(formatted.length());
                }

                private String applyMask(String value, String mask) {
                    if (value.length() > mask.length()) {
                        value = value.substring(0, mask.length());
                    }

                    int valueIndex = 0;
                    StringBuilder maskedValue = new StringBuilder();

                    for (int i = 0; i < mask.length(); i++) {
                        char maskChar = mask.charAt(i);
                        if (maskChar == '#') {
                            if (valueIndex < value.length()) {
                                maskedValue.append(value.charAt(valueIndex));
                                valueIndex++;
                            } else {
                                break;
                            }
                        } else {
                            maskedValue.append(maskChar);
                        }
                    }

                    return maskedValue.toString();
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            };
        }
    }

    private boolean isValidCPF(String cpf) {
        // Remove caracteres não numéricos do CPF
        cpf = cpf.replaceAll("[^0-9]", "");

        // Verifica se o CPF tem 11 dígitos
        if (cpf.length() != 11) {
            return false;
        }

        // Verifica se todos os dígitos são iguais (CPF inválido)
        boolean allDigitsEqual = true;
        for (int i = 1; i < cpf.length(); i++) {
            if (cpf.charAt(i) != cpf.charAt(0)) {
                allDigitsEqual = false;
                break;
            }
        }
        if (allDigitsEqual) {
            return false;
        }

        // Calcula o primeiro dígito verificador
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (cpf.charAt(i) - '0') * (10 - i);
        }
        int remainder = 11 - (sum % 11);

        if (remainder == 10 || remainder == 11) {
            remainder = 0;
        }

        if (remainder != (cpf.charAt(9) - '0')) {
            return false;
        }

        // Calcula o segundo dígito verificador
        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (cpf.charAt(i) - '0') * (11 - i);
        }
        remainder = 11 - (sum % 11);

        if (remainder == 10 || remainder == 11) {
            remainder = 0;
        }

        if (remainder != (cpf.charAt(10) - '0')) {
            return false;
        }

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth = FirebaseAuth.getInstance();

        edt_nome = findViewById(R.id.edt_nome);
        edt_email_registro = findViewById(R.id.edt_email_registro);
        edt_senha_registro = findViewById(R.id.edt_senha_registro);
        edt_confirmar_senha = findViewById(R.id.edt_confirmar_senha);
        CheckBox ckb_mostrar_senha_registro = findViewById(R.id.ckb_mostrar_senha_registro);
        Button btn_entrar_registro = findViewById(R.id.btn_entrar_registro);
        Button btn_voltar = findViewById(R.id.btn_voltar);

        EditText edt_telefone = findViewById(R.id.edt_telefone);
        edt_telefone.addTextChangedListener(MaskUtils.insertMask(edt_telefone, "(##) #####-####"));

        EditText edt_data_nascimento = findViewById(R.id.edt_data_nascimento);
        edt_data_nascimento.addTextChangedListener(MaskUtils.insertMask(edt_data_nascimento, "##/##/####"));

        EditText edt_CPF = findViewById(R.id.edt_CPF);
        edt_CPF.addTextChangedListener(MaskUtils.insertMask(edt_CPF, "###.###.###-##"));

        ckb_mostrar_senha_registro.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                edt_senha_registro.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                edt_confirmar_senha.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                edt_senha_registro.setTransformationMethod(PasswordTransformationMethod.getInstance());
                edt_confirmar_senha.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        btn_entrar_registro.setOnClickListener(view -> {
            String RegisterEmail = edt_email_registro.getText().toString();
            String Senha = edt_senha_registro.getText().toString();
            String ConfirmarSenha = edt_confirmar_senha.getText().toString();

            String nome = edt_nome.getText().toString();
            String telefone = edt_telefone.getText().toString();
            String dataNascimento = edt_data_nascimento.getText().toString();
            String cpf = edt_CPF.getText().toString();
            String email = edt_email_registro.getText().toString();

            if (!TextUtils.isEmpty(RegisterEmail) || !TextUtils.isEmpty(ConfirmarSenha) || !TextUtils.isEmpty(Senha)) {
                if (Senha.equals(ConfirmarSenha)) {
                    if (isValidCPF(cpf)) {
                        mAuth.createUserWithEmailAndPassword(RegisterEmail, Senha).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                uploadData(nome, telefone, dataNascimento, cpf, email);
                                abrirTelaPrincipal();
                            } else {
                                String error = Objects.requireNonNull(task.getException()).getMessage();
                                Toast.makeText(Registro.this, "" + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(Registro.this, "CPF inválido. Verifique o número do CPF.", Toast
                                .LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Registro.this, "A senha deve ser a mesma em ambos os campos!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_voltar.setOnClickListener(view -> abrirTelaPrincipal());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void uploadData(String nome, String telefone, String dataNascimento, String cpf, String email) {
        int indiceArroba = email.indexOf('@');

        if (indiceArroba != -1) {
            String novoEmail = email.substring(0, indiceArroba).replace(".", "-");

            nome = Codex.encode(nome);
            telefone = Codex.encode(telefone);
            dataNascimento = Codex.encode(dataNascimento);
            cpf = Codex.encode(cpf);
            email = Codex.encode(email);

            DataClass dataClass = new DataClass(nome, telefone, dataNascimento, cpf, email, null);

            FirebaseDatabase.getInstance().getReference("Usuário").child(novoEmail).setValue(dataClass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(Registro.this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> Toast.makeText(Registro.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            System.out.println("O email não contém um '@'.");
        }
    }

    private void abrirTelaPrincipal() {
        Intent intent = new Intent(Registro.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
