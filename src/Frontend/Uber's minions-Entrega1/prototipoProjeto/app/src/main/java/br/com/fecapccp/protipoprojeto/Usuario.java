package br.com.fecapccp.protipoprojeto;

import com.google.gson.annotations.SerializedName;

public class Usuario {
    @SerializedName("email")
    private String email;

    @SerializedName("senha")
    private String senha;

    public Usuario(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }
}
