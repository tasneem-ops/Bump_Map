package com.example.mapsdemo.authentication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mapsdemo.databinding.FragmentRegisterBinding
import com.example.mapsdemo.main_screen.MainActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterFragment : Fragment() {
    private lateinit var binding : FragmentRegisterBinding
    private lateinit var mAuth : FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        val mainActivityIntent = Intent(requireContext(), MainActivity::class.java)

        binding.registerBtn.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val validData = checkEmailPasswordNotEmpty(email, password)
            if (validData){
                registerUser(email, password)

            }
            else{
                return@setOnClickListener
            }
        }

        binding.loginNow.setOnClickListener {
            this.findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment())
        }
    }

    private fun registerUser(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity(),
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "createUserWithEmail:success")
                        val user = mAuth.currentUser
                        val name: String = binding.nameEditText.text.toString()
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name).build()
                        user!!.updateProfile(profileUpdates)
                        this.findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToLoginFragment())
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            requireContext(), "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
    }

    private fun checkEmailPasswordNotEmpty(email: String, password: String): Boolean {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(requireContext(), "Please enter Email", Toast.LENGTH_SHORT).show()
            return false
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(requireContext(), "Please enter Password", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

}